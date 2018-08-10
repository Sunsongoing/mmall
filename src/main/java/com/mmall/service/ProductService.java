package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

/**
 * @author Sunsongoing
 */

public interface ProductService {

    ServerResponse saveProduct(Product product);

    ServerResponse setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse getList(int pageNum, int pageSize);

    ServerResponse searchProduct(String productName, Integer productId, int pageNum, int pageSize);

    ServerResponse<PageInfo> getProductByKeywordsCategory(String keywords, Integer categoryId, int pageNum,
                                                          int pageSize, String orderBy);
}
