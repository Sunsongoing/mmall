package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author Sunsongoing
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        if (StringUtils.isNoneBlank(username, password)) {
            ServerResponse<User> response = userService.login(username, password);
            if (response.isSuccess()) {
                session.setAttribute(Const.CURRENT_USER, response.getData());
            }
            return response;
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        if (StringUtils.isNoneBlank(user.getUsername(), user.getPassword(), user.getEmail())) {
            return userService.register(user);
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    /**
     * 实时校验接口
     */
    @RequestMapping(value = "/check_valid", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return userService.checkValid(str, type);
    }

    @RequestMapping(value = "/get_user_info", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null != user) {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户信息获取失败");
    }

    @RequestMapping(value = "/forget_get_question", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        if (StringUtils.isNotBlank(username)) {
            return userService.selectQuestion(username);
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    @RequestMapping(value = "/forget_check_answer")
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        if (StringUtils.isNoneBlank(username, question, answer)) {
            return userService.checkAnswer(username, question, answer);
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    @RequestMapping(value = "/forget_reset_password", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        return userService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    @RequestMapping(value = "/reset_password", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return userService.resetPassword(user, passwordOld, passwordNew);
    }

    @RequestMapping(value = "/update_information", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateUserInfo(User user, HttpSession session) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == currentUser) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = userService.updateInformation(user);
        if (response.isSuccess()) {
            //将更新后的用户信息存入session中
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    @RequestMapping(value = "/get_information", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == currentUser) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGA_ARGUMENT.getCode()
                    , "未登录，需要强制登录status=10");
        }
        return userService.getInformation(currentUser.getId());
    }
}
