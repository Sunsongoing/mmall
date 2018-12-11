package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Sunsongoing
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;


    @RequestMapping("/create")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.createOrder(user.getId(), shippingId);
    }

    @RequestMapping("/cancel")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.cancel(user.getId(), orderNo);
    }

    @RequestMapping("/get_order_cart_product")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getOrderCartProduct(user.getId());
    }

    @RequestMapping("/detail")
    @ResponseBody
    public ServerResponse getOrderDetail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getOrderDetail(user.getId(), orderNo);
    }

    @RequestMapping("/list")
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.list(user.getId(), pageNum, pageSize);
    }

    @RequestMapping("/pay")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }

        //通过request域获取upload路径，将生成的二维码上传到ftp服务器
        String path = request.getSession().getServletContext().getRealPath("upload");
        return orderService.pay(user.getId(), orderNo, path);
    }

    @RequestMapping("/alipay_callback")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {

        ServerResponse serverResponse = orderService.alipayCallback(request);

        //按照支付宝的要求返回响应的值
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        } else {
            return Const.AlipayCallback.RESPONSE_FAILED;
        }
    }

    @RequestMapping("/query_order_pay_status")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse serverResponse = orderService.queryOrderPayStatus(user.getId(), orderNo);
        //返回订单支付状态
        if (serverResponse.isSuccess()) {
            return ServerResponse.createBySuccess(serverResponse.getMsg(),true);
        }
        return ServerResponse.createBySuccess(serverResponse.getMsg(),false);
    }
}
