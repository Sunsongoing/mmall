package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.ProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author Sunsongoing
 */
@Controller
@RequestMapping("/product")
public class ProductController {
    @Resource
    private ProductService productService;


    @RequestMapping("/detail")
    @ResponseBody
    public ServerResponse<ProductDetailVo> getDetail(Integer productId) {
        return productService.getProductDetail(productId);
    }

    @RequestMapping("/list")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "keywords", required = false) String keywords,
                                         @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                         @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                         @RequestParam(value = "orderBy", defaultValue = "") String orderBy) {

        return productService.getProductByKeywordsCategory(keywords, categoryId, pageNum, pageSize, orderBy);
    }


}
