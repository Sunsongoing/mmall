package com.mmall.dao;

import com.mmall.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    /**
     * 根据主键id删除
     *
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 插入
     *
     * @param record
     * @return
     */
    int insert(Order record);

    /**
     * 有选择的插入
     *
     * @param record
     * @return
     */
    int insertSelective(Order record);

    /**
     * 根据订单id查找订单
     *
     * @param id
     * @return
     */
    Order selectByPrimaryKey(Integer id);

    /**
     * 根据主键id有选择的更新
     *
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(Order record);

    /**
     * 根据主键id更新订单
     *
     * @param record
     * @return
     */
    int updateByPrimaryKey(Order record);

    /**
     * 根据用户id和订单号查询订单
     *
     * @param userId
     * @param orderNo 订单号
     * @return
     */
    Order selectByUserIdAndOrderNo(@Param("userId") Integer userId, @Param("orderNo") Long orderNo);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo
     * @return
     */
    Order selectByOrderNo(Long orderNo);

    /**
     * 查找用户id对应的订单列表
     * @param userId
     * @return
     */
    List<Order> selectListByUserId(Integer userId);

    /**
     * 管理员-查询所有订单
     * @return
     */
    List<Order> selectAll();
}