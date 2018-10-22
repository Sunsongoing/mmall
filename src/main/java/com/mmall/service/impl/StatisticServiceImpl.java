package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.OrderMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.Statistic;
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
    public ServerResponse<Statistic> baseCount() {
        int userCount = userMapper.selectCount(),
                productCount = productMapper.selectCount(),
                orderCount = orderMapper.selectCount();
        Statistic statistic = new Statistic();
        statistic.setUserCount(userCount);
        statistic.setProductCount(productCount);
        statistic.setOrderCount(orderCount);

        return ServerResponse.createBySuccess(statistic);
    }
}
