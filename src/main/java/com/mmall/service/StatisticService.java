package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Statistic;
import com.mmall.pojo.User;

/**
 * @author Zjl
 */

public interface StatisticService {

    ServerResponse<Statistic> baseCount(User user);
}
