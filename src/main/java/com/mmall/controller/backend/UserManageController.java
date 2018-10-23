package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author Sunsongoing
 */
@Controller
@RequestMapping("/manage/user")
public class UserManageController {
    @Resource
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        ServerResponse<User> response = userService.login(username, password);
        if (response.isSuccess()) {
            //登录成功
            User user = response.getData();
            //判断权限
            if (user.getRole() == Const.Role.ROLE_ADMIN) {
                session.setAttribute(Const.CURRENT_USER, response.getData());
                return response;
            } else {
                return ServerResponse.createByErrorMessage("不是管理员，无法登录");
            }
        }
        return response;
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getUserList(HttpSession session,
                                      @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null != user) {
            if (user.getRole() == Const.Role.ROLE_ADMIN) {
                return userService.selectUserList(pageNum,pageSize);
            } else {
                return ServerResponse.createByErrorMessage("需要管理员权限");
            }
        }

        return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }
}
