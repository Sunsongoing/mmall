package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.OrderMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.Statistic;
import com.mmall.pojo.User;
import com.mmall.service.StatisticService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Zjl
 */
@Service
public class StatisticServiceImpl implements StatisticService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private OrderMapper orderMapper;

    @Override
    public ServerResponse<Statistic> baseCount(User user) {

        if (user.getRole() == Const.Role.ROLE_ADMIN) {
            int userCount = userMapper.selectCount(),
                    productCount = productMapper.selectCount(),
                    orderCount = orderMapper.selectCount();
            Statistic statistic = new Statistic();
            statistic.setUserCount(userCount);
            statistic.setProductCount(productCount);
            statistic.setOrderCount(orderCount);
            return ServerResponse.createBySuccess(statistic);
        }
        return ServerResponse.createByErrorMessage("需要管理员权限");
    }
}
