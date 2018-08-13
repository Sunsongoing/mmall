package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    /**
     * 根据主键id删除
     *
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 添加商品
     *
     * @param record
     * @return
     */
    int insert(Product record);

    /**
     * 根据字段是否为空有选择的插入字段
     *
     * @param record
     * @return
     */
    int insertSelective(Product record);

    /**
     * 根据主键id查找
     *
     * @param id
     * @return
     */
    Product selectByPrimaryKey(Integer id);

    /**
     * 根据主键id有选择的更新
     *
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(Product record);

    /**
     * 根据主键id更新所有字段
     *
     * @param record
     * @return
     */
    int updateByPrimaryKey(Product record);

    /**
     * 根据主键id判断商品是否存在
     *
     * @param productId
     * @return
     */
    int checkProductByPrimaryKey(@Param("productId") Integer productId, @Param("status") Integer status);

    /**
     * 查询商品列表
     *
     * @return
     */
    List<Product> selectList();

    /**
     * 根据商品关键字或商品id搜索商品
     *
     * @param productName
     * @param productId
     * @return
     */
    List<Product> selectByNameAndProductId(@Param("productName") String productName, @Param("productId") Integer productId);

    /**
     * 根据商品关键字和分类id集合搜索商品
     *
     * @param productName    商品关键字
     * @param categoryIdList 分类id集合
     * @return
     */
    List<Product> selectByNameAndCategoryIds(@Param("productName") String productName, @Param("categoryIdList") List<Integer> categoryIdList);
}