package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.OrderService;
import com.mmall.service.UserService;
import com.mmall.vo.OrderVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author Sunsongoing
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {
    @Resource
    private OrderService orderService;
    @Resource
    private UserService userService;

    @RequestMapping("/list")
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        //校验用户权限
        if (userService.checkAdminRole(user).isSuccess()) {
            //管理员可以获取所有的订单，所以不传userId
            return orderService.manageList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("需要管理员权限");
        }
    }

    @RequestMapping("/detail")
    @ResponseBody
    public ServerResponse<OrderVo> detail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        //校验用户权限
        if (userService.checkAdminRole(user).isSuccess()) {
            return orderService.manageDetail(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("需要管理员权限");
        }
    }

    @RequestMapping("/search")
    @ResponseBody
    public ServerResponse<PageInfo> search(HttpSession session, Long orderNo,
                                           @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        //校验用户权限
        if (userService.checkAdminRole(user).isSuccess()) {
            return orderService.manageSearch(orderNo, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("需要管理员权限");
        }
    }

    @RequestMapping("/send_goods")
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        //校验用户权限
        if (userService.checkAdminRole(user).isSuccess()) {
            return orderService.manageSendGoods(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("需要管理员权限");
        }
    }
}
