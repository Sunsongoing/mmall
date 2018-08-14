package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

public interface ShippingMapper {
    /**
     * 根据主键id删除
     *
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 添加收货地址
     *
     * @param record
     * @return
     */
    int insert(Shipping record);

    /**
     * 有选择的添加收货地址字段
     *
     * @param record
     * @return
     */
    int insertSelective(Shipping record);

    /**
     * 根据主键id查找收货地址
     *
     * @param id
     * @return
     */
    Shipping selectByPrimaryKey(Integer id);

    /**
     * 根据主键id有选择的更新字段
     *
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(Shipping record);

    /**
     * 根据主键id更新收货地址
     *
     * @param record
     * @return
     */
    int updateByPrimaryKey(Shipping record);

    /**
     * 根据收货地址id和用户id删除
     * @param shippingId
     * @param userId
     * @return
     */
    int deleteByShippingIdUserId(@Param("shippingId") Integer shippingId, @Param("userId") Integer userId);
}