package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;

/**
 * @author Sunsongoing
 */

public interface ProductService {

    ServerResponse saveProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse<PageInfo>  getList(int pageNum, int pageSize);

    ServerResponse<PageInfo>  searchProduct(String productName, Integer productId, int pageNum, int pageSize);

    ServerResponse<PageInfo<ProductListVo>> getProductByKeywordsCategory(String keywords, Integer categoryId, int pageNum,
                                                                         int pageSize, String orderBy);
}
