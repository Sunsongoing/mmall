package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Statistic;

/**
 * @author Zjl
 */

public interface StatisticService {

    ServerResponse<Statistic> baseCount();
}
