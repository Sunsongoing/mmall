package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;

/**
 * @author Sunsongoing
 */

public interface ProductService {

    ServerResponse saveProduct(Product product);

    ServerResponse setSaleStatus(Integer productId, Integer status);
    ServerResponse getDetail(Integer productId);

    ServerResponse getList(int pageNum,int pageSize);
    ServerResponse searchProduct(String productName,Integer productId,int pageNum, int pageSize);
}
