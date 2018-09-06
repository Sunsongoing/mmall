package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {

    /**
     * 根据主键id删除购物车
     *
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 插入购物车
     *
     * @param record
     * @return
     */
    int insert(Cart record);

    /**
     * 有选择的插入购物车字段
     *
     * @param record
     * @return
     */
    int insertSelective(Cart record);

    /**
     * 按照主键id查找购物车
     *
     * @param id
     * @return
     */
    Cart selectByPrimaryKey(Integer id);

    /**
     * 按照主键id有选择的更新购物车字段
     *
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(Cart record);

    /**
     * 按照主键id更新购物车
     *
     * @param record
     * @return
     */
    int updateByPrimaryKey(Cart record);

    /**
     * 根据用户id和商品id查找购物车
     *
     * @param userId    用户id
     * @param productId 商品id
     * @return 购物车实体对象
     */
    Cart selectByUserIdProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    /**
     * 根据用户id查找购物车
     *
     * @param userId
     * @return
     */
    List<Cart> selectByUserId(Integer userId);

    /**
     * 根据用户id来查找对应用户的购物车，判断购物车是否是勾选状态
     *
     * @param userId
     * @return
     */
    int selectCartProductCheckedByUserId(Integer userId);

    /**
     * 根据用户id和商品列表来删除购物车
     *
     * @param userId
     * @param productList
     * @return
     */
    int deleteByUserIdProductIds(@Param("userId") Integer userId, @Param("productList") List<String> productList);

    /**
     * 将购物车的商品勾选或反选
     * 如果productId = null 表示全选或全反选
     *
     * @param userId
     * @param productId
     * @param checked
     * @return
     */
    int checkedOrUncheckedProduct(@Param("userId") Integer userId, @Param("productId") Integer productId,
                                  @Param("checked") Integer checked);

    /**
     * 查找当前用户购物车中的商品总数
     *
     * @param userId
     * @return
     */
    int selectCartProductCountByUserId(Integer userId);

    /**
     * 获取用户购物车中已经勾选的商品
     *
     * @param userId
     * @return
     */
    List<Cart> selectCheckedByUserId(Integer userId);
}