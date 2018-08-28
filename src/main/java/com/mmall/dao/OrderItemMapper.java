package com.mmall.dao;

import com.mmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
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
    int insert(OrderItem record);

    /**
     * 有选择的插入
     *
     * @param record
     * @return
     */
    int insertSelective(OrderItem record);

    /**
     * 根据订单id查找
     *
     * @param id
     * @return
     */
    OrderItem selectByPrimaryKey(Integer id);

    /**
     * 根据主键id有选择的更新
     *
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(OrderItem record);

    /**
     * 根据主键id更新
     *
     * @param record
     * @return
     */
    int updateByPrimaryKey(OrderItem record);

    /**
     * 根据用户id和订单号查询
     * @param userId
     * @param orderNo
     * @return
     */
    List<OrderItem> selectByOrderNoUserId(@Param("userId") Integer userId, @Param("orderNo") Long orderNo);

    /**
     * 批量插入orderItem
     * @param orderItemList
     * @return
     */
    int batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);

    /**
     * 根据订单号查询
     * @param orderNo
     * @return
     */
    List<OrderItem> selectByOrderNo(Long orderNo);
}