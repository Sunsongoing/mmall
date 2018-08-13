package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.CartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Sunsongoing
 */
@Service
public class CartServiceImpl implements CartService {
    @Resource
    private CartMapper cartMapper;
    @Resource
    private ProductMapper productMapper;
    Logger l = LoggerFactory.getLogger(CartServiceImpl.class);


    /**
     * 添加商品到购物车
     *
     * @param userId
     * @param count
     * @param productId
     * @return
     */
    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer count, Integer productId) {
        if (null == productId || null == count) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        int resultCount = productMapper.checkProductByPrimaryKey(productId, 1);
        if (resultCount <= 0) {
            return ServerResponse.createByErrorMessage("商品不存在或已下架");
        }
        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        //库存数量足够
        if (null == cart) {
            //这个商品不在购物车中,新增一条购物车记录
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            //默认是选中状态
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        } else {
            //这个商品已存在于购物车，数量相加
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    /**
     * 更新购物车
     *
     * @param userId
     * @param count
     * @param productId
     * @return
     */
    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer count, Integer productId) {
        if (null == productId || null == count) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if (null == cart) {
            return ServerResponse.createByErrorMessage("购物车中不存在此商品");
        }
        //更新购物车中产品的数量
        cart.setQuantity(count);
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.list(userId);
    }

    /**
     * 购物车中删除商品
     *
     * @param userId
     * @param productIds
     * @return
     */
    @Override
    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds) {
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productList)) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId, productList);
        return this.list(userId);
    }

    /**
     * 返回购物车列表
     *
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        if (null == userId) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 对购物车中的商品进行勾选或反选
     * 如果productId = null 表示全选或者全反选
     *
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked) {
        cartMapper.checkedOrUncheckedProduct(userId, productId, checked);
        return this.list(userId);
    }

    /**
     * 获得当前用户的购物车中的商品总数
     *
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        int resultCount = cartMapper.selectCartProductCountByUserId(userId);
        return ServerResponse.createBySuccess(resultCount);
    }


    /**
     * 购物车的计算
     *
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal totalPrice = new BigDecimal("0");

        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (null != product) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存
                    int productStock = product.getStock();
                    int cartItemQuantity = cartItem.getQuantity();
                    if (productStock >= cartItemQuantity) {
                        //库存足够
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                        cartProductVo.setQuantity(cartItemQuantity);
                    } else {
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(productStock);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                        cartProductVo.setQuantity(productStock);
                    }
                    //计算单个订单总价
                    l.info(BigDecimalUtil.mul(cartProductVo.getQuantity().doubleValue(),
                            cartProductVo.getProductPrice().doubleValue()) + "");
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(cartProductVo.getQuantity().doubleValue(),
                            cartProductVo.getProductPrice().doubleValue()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                if (cartItem.getChecked() == Const.Cart.CHECKED) {
                    //计算购物车所有勾选的订单的总价
                    l.info(cartProductVo.getProductTotalPrice() + "");
                    totalPrice = BigDecimalUtil.add(cartProductVo.getProductTotalPrice().doubleValue(), totalPrice.doubleValue());
                }
                cartProductVoList.add(cartProductVo);

            }
        }
        //组装CartVo
        cartVo.setCartTotalPrice(totalPrice);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckStatus(userId));
        return cartVo;
    }


    /**
     * 判断购物车中的订单是否全部勾选
     *
     * @param userId
     * @return
     */
    private boolean getAllCheckStatus(Integer userId) {
        if (null == userId) {
            return false;
        }
        return cartMapper.selectCartProductCheckedByUserId(userId) == 0;
    }
}
