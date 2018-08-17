package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     *
     * @param shippingId
     * @param userId
     * @return
     */
    int deleteByShippingIdUserId(@Param("shippingId") Integer shippingId, @Param("userId") Integer userId);

    /**
     * 更新收货地址
     * @param record
     * @return
     */
    int updateByShipping(Shipping record);

    /**
     * 根据用户id和收货地址id查询收货地址
     * @param userId
     * @param shippingId
     * @return
     */
    Shipping selectByPrimaryKeyUserId(@Param("userId") Integer userId,@Param("shippingId") Integer shippingId);

    /**
     * 查找用户id对应用户的所有收货地址
     * @param userId
     * @return
     */
    List<Shipping> selectByUserId(Integer userId);
}