package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Statistic;
import com.mmall.pojo.User;
import com.mmall.service.StatisticService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author Zjl
 */

@Controller
@RequestMapping("")
public class StatisticManageController {
    @Resource
    private StatisticService statisticService;

    @RequestMapping("/base_count")
    public ServerResponse<Statistic> baseCount(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null != user) {
            return statisticService.baseCount();
        }
        // 用户未登录提示登录
        return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(),
                ResponseCode.NEED_LOGIN.getDesc());
    }
}
