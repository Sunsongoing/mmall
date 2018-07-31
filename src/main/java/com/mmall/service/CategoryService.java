package com.mmall.service;

import com.mmall.common.ServerResponse;

/**
 * @author Sunsongoing
 */

public interface CategoryService {

    ServerResponse<String> addCategory(String categoryName, Integer parentId);

    ServerResponse<String> setCategoryName(Integer categoryId,String categoryName);
}
