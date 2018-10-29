package com.mmall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayResponse;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.OrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.jedis.JedisClientPool;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Sunsongoing
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private PayInfoMapper payInfoMapper;
    @Resource
    private CartMapper cartMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private ShippingMapper shippingMapper;
    @Resource
    private JedisClientPool jedisClientPool;

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    /**
     * 支付宝当面付2.0服务
     */
    private static AlipayTradeService tradeService;

    static {
        /*
         *一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");
        // 初始化支付宝当面付2.0服务
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

    }


    /**
     * 创建订单
     *
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {

        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);
        if (null == cartList || cartList.isEmpty()) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //计算订单总价
        ServerResponse<List<OrderItem>> serverResponse = this.getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = serverResponse.getData();
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);
        //校验用户收货地址
        int checkUserShipping = shippingMapper.checkShippingIdUserId(userId, shippingId);
        if (checkUserShipping <= 0) {
            return ServerResponse.createByErrorMessage("用户不存在该收货地址");
        }
        //生成订单
        Order order = this.assembleOrder(userId, shippingId, payment);
        if (null == order) {
            return ServerResponse.createByErrorMessage("订单生成失败");
        }
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        //使用mybatis批量插入
        orderItemMapper.batchInsert(orderItemList);

        //订单生成成功，减少产品库存
        this.reduceProductStock(orderItemList);

        //清空购物车
        this.cleanCart(cartList);
        //返回给前端的数据
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 取消订单
     *
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<String> cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (null == order) {
            return ServerResponse.createByErrorMessage("该用户不存在此订单");
        }
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.createByErrorMessage("已付款,无法取消");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int resultCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    /**
     * 获取购物车中勾选的商品
     *
     * @param userId
     * @return
     */
    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);
        ServerResponse<List<OrderItem>> serverResponse = this.getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        //总价
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(this.assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
    }

    /**
     * 获取订单详情
     *
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (null == order) {
            return ServerResponse.createBySuccess("没有找到该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoUserId(userId, orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 获取用户订单列表
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectListByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 管理员-获取订单列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> allOrderList = orderMapper.selectAll();
        List<OrderVo> orderVoList = this.assembleOrderVoList(allOrderList, null);
        PageInfo pageInfo = new PageInfo(allOrderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 管理员-获取订单详情
     *
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<OrderVo> manageDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (null == order) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 管理员-搜索订单
     *
     * @param orderNo 关键字
     * @return
     */
    @Override
    public ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (null == order) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        List<OrderVo> orderVoList = Lists.newArrayList(orderVo);
        PageInfo pageInfo = new PageInfo(orderItemList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 管理员-发货
     *
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<String> manageSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (null == order) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
        } else if (order.getStatus() == Const.OrderStatusEnum.SHIPPED.getCode()) {
            return ServerResponse.createByErrorMessage("订单已发货");
        } else if (order.getStatus() == Const.OrderStatusEnum.CANCELED.getCode()) {
            return ServerResponse.createByErrorMessage("订单已取消");
        }

        int resultCount = orderMapper.updateByPrimaryKeySelective(order);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("发货成功");
        }
        return ServerResponse.createByErrorMessage("发货失败");
    }

    /**
     * 组装List<OrderVo>
     *
     * @param orderList
     * @param userId
     * @return
     */
    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        List<OrderItem> orderItemList = Lists.newArrayList();
        for (Order order : orderList) {
            if (userId == null) {
                //管理员查询不需要userId
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            } else {
                orderItemList = orderItemMapper.selectByOrderNoUserId(userId, order.getOrderNo());

            }
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    /**
     * 组装orderVo对象
     *
     * @param order
     * @param orderItemList
     * @return
     */
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (null != shipping) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.date2Str(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.date2Str(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.date2Str(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.date2Str(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.date2Str(order.getCloseTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    /**
     * 组装orderItemVo
     *
     * @param orderItem
     * @return
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.date2Str(orderItem.getCreateTime()));
        return orderItemVo;
    }

    /**
     * 组装shippingVo
     *
     * @param shipping
     * @return
     */
    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }


    /**
     * 清空购物车
     *
     * @param cartList
     */
    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /**
     * 减少产品库存
     *
     * @param orderItemList
     */
    private void reduceProductStock(List<OrderItem> orderItemList) {
        //更新购物车中每个商品的库存
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     * @param userId     用户id
     * @param shippingId 收货地址id
     * @param payment    订单总价
     * @return 如果订单生成失败返回null，成功返回订单对象
     */
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        order.setOrderNo(this.generateOrderNo());
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        //运费
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        //todo 发货时间，付款时间...
        int resultCount = orderMapper.insert(order);
        if (resultCount > 0) {
            return order;
        }
        return null;
    }

    /**
     * 生成订单号
     * 订单号生成策略(分布式可用)
     * 前缀根据数据环境/地区的不同来设置（通过配置文件）
     * 从redis中获取订单号,然后通过incr自增
     * 最后拼接前缀+自增值
     *
     * @return
     */
    private long generateOrderNo() {
        //设置订单号前缀（可以根据数据环境/地区的不同来设置不同订单号前缀）
        //从配置文件获取订单号前缀
        String orderPrefix = PropertiesUtil.getProperty("order.prefix");
        if (StringUtils.isBlank(orderPrefix)) {
            orderPrefix = "";
        }
        //从redis中获取订单号
        String redisOrderKey = PropertiesUtil.getProperty("order.redis.key");
        long orderId = jedisClientPool.incr(redisOrderKey);
        //判断订单号是否小于订单号初始值
        long orderIdInitValue = Long.parseLong(PropertiesUtil.getProperty("order.num.start"));
        if (orderId < orderIdInitValue) {
            //设置redis中orderNo的初始值
            jedisClientPool.set(redisOrderKey, String.valueOf(orderId));
            orderId = jedisClientPool.incr(redisOrderKey);
        }
        //拼接订单号（订单前缀+redis中的自增长值），并返回
        return Long.valueOf(new StringBuffer().append(orderPrefix).append(String.valueOf(orderId)).toString());
    }

    /**
     * 计算订单总价
     *
     * @param orderItemList 购物车订单列表
     * @return 订单总价
     */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal totalPrice = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }

        return totalPrice;
    }


    /**
     * 组装orderItemList
     *
     * @param userId   用户id
     * @param cartList 购物车列表
     * @return 订单item列表
     */
    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        //校验购物车的数据，包括产品的状态和数量
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (product.getStatus() > Const.ProductStatusEnum.ON_SALE.getCode()) {
                return ServerResponse.createByErrorMessage("产品 " + product.getName() + " 不是在线售卖状态");
            }
            //校验库存
            if (cart.getQuantity() > product.getStock()) {
                return ServerResponse.createByErrorMessage("库存不足");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            //当前单价
            orderItem.setCurrentUnitPrice(product.getPrice());
            //商品总数
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cart.getQuantity().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     * 支付
     *
     * @param userId  用户id
     * @param orderNo 订单号
     * @param path    二维码的上传路径
     * @return
     */
    @Override
    public ServerResponse pay(Integer userId, Long orderNo, String path) {
        Map<String, String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (null == order) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuffer().append("扫码支付,订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuffer().append("订单").append(outTradeNo)
                .append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<>();

        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoUserId(userId, orderNo);
        //将用户订单中的商品信息添加到商品明细列表
        for (OrderItem o : orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goodsDetail = GoodsDetail.newInstance(o.getProductId().toString(), o.getProductName(),
                    BigDecimalUtil.mul(o.getCurrentUnitPrice().doubleValue(), 100d).longValue(), o.getQuantity());
            goodsDetailList.add(goodsDetail);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                //支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback"))
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                //二维码保存路径
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                //二维码名称
                String qrName = String.format("qr-%s.png", response.getOutTradeNo());
                //生成二维码图片
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                //创建需要上传的文件对象
                File targetFile = new File(path, qrName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile), "images");
                } catch (IOException e) {
                    log.error("二维码上传失败", e);
                }
                log.info("qrPath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", qrUrl);
                targetFile.delete();
                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    /**
     * 支付宝回调验证
     *
     * @param request
     * @return
     */
    @Override
    public ServerResponse alipayCallback(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        HashMap<String, String> params = Maps.newHashMap();

        //获得alipay回调参数
        for (String s : parameterMap.keySet()) {
            String[] values = parameterMap.get(s);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                //最后一个下标直接拼接参数，其它则拼接逗号+参数
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(s, valueStr);
        }
        log.info("支付宝回调，sign:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());
        //验证回调的正确性
        //根据支付宝的文档需要移除sign，sign_type 两个字段，但是支付宝的源码中已经移除了sign字段
        //所以这里只移除 sign_type 字段
        params.remove("sign_type");
        try {
            boolean rsaCheckV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!rsaCheckV2) {
                log.info("验签失败");
                return ServerResponse.createByErrorMessage("非法请求，验证失败");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签异常", e);
        }

        return this.checkCallbackParams(params);
    }

    /**
     * 查询订单支付状态
     *
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (null == order) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


    /**
     * 验证out_trade_no是否为商户系统中创建的订单号，并判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额）
     * 同时校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
     * seller_id 如果未设置则默认为商户的pid
     *
     * @param params
     */
    private ServerResponse checkCallbackParams(Map<String, String> params) {
        Long outTradeNo = Long.parseLong(params.get("out_trade_no"));
        String totalAmount = params.get("total_amount");
        String sellerId = params.get("seller_id");
        String appId = params.get("app_id");
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");
        log.info("checkParams:{},{},{},{},{}", appId, outTradeNo, totalAmount, sellerId, tradeStatus);
        Order order = orderMapper.selectByOrderNo(outTradeNo);
        if (null == order) {
            return ServerResponse.createByErrorMessage("非本应用订单，回调忽略");
        }
        if (!StringUtils.equals(totalAmount, order.getPayment().toString()) ||
                !StringUtils.equals(appId, Configs.getAppid()) ||
                !StringUtils.equals(sellerId, Configs.getPid())) {

            return ServerResponse.createByErrorMessage("异常通知，回调忽略");
        }

        //判断订单状态,如果是支付状态，则返回success
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            //避免支付宝重复调用，返回success
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        //判断回调的交易状态,如果等于TRADE_SUCCESS,表示支付成功
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            //将订单状态置为已付款
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            order.setPaymentTime(DateTimeUtil.str2Date(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(outTradeNo);
        payInfo.setPayPlatform(Const.PayPlatFormEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }


    /**
     * 简单打印响应
     *
     * @param response
     */
    private void dumpResponse(AlipayResponse response) {
        if (null != response) {
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            log.info("body:" + response.getBody());
        }
    }
}
