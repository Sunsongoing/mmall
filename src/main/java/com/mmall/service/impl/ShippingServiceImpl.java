package com.mmall.service.impl;

import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.ShippingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Sunsongoing
 */
@Service
public class ShippingServiceImpl implements ShippingService {

    @Resource
    private ShippingMapper shippingMapper;

    /**
     * 添加收货地址
     *
     * @param userId
     * @param shipping springMVC会根据传递的字段自动填充
     * @return
     */
    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int resultCount = shippingMapper.insert(shipping);
        if (resultCount > 0) {
            Map<String, Object> resultMap = Maps.newHashMap();
            resultMap.put("shippingId", shipping.getId());

            return ServerResponse.createBySuccess("新建地址成功", resultMap);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    /**
     * 删除地址
     *
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse<String> delete(Integer userId, Integer shippingId) {
        int resultCount = shippingMapper.deleteByShippingIdUserId(shippingId, userId);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }
}
