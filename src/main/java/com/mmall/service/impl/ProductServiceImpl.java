package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Sunsongoing
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;
    @Resource
    private CategoryMapper categoryMapper;

    /**
     * 根据是否存在商品id来判断是添加商品还是更新商品
     *
     * @return
     */
    @Override
    public ServerResponse saveProduct(Product product) {
        if (null != product) {
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                //取出主图
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }
            if (null != product.getId()) {
                //id不等于null表示是已经存在的记录,存在则更新，否则添加
                int resultCount = productMapper.updateByPrimaryKeySelective(product);
                if (resultCount > 0) {
                    return ServerResponse.createBySuccessMessage("更新商品成功");
                }
                return ServerResponse.createByErrorMessage("更新商品失败");
            } else {
                int resultCount = productMapper.insert(product);
                if (resultCount > 0) {
                    return ServerResponse.createBySuccessMessage("添加商品成功");
                }
                return ServerResponse.createByErrorMessage("添加商品失败");
            }
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    /**
     * 修改商品销售状态
     *
     * @param productId
     * @param status
     * @return
     */
    @Override
    public ServerResponse setSaleStatus(Integer productId, Integer status) {
        if (null == productId || null == status) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGA_ARGUMENT.getCode(), ResponseCode.ILLEGA_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int resultCount = productMapper.updateByPrimaryKeySelective(product);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMessage("产品销售状态修改成功");
        }
        return ServerResponse.createByErrorMessage("产品销售状态修改失败");
    }

    /**
     * 获取商品详情
     *
     * @param productId 商品id
     * @return 对应商品详情
     */
    @Override
    public ServerResponse<ProductDetailVo> getDetail(Integer productId) {
        if (null == productId) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGA_ARGUMENT.getCode(), ResponseCode.ILLEGA_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (null == product) {
            return ServerResponse.createByErrorMessage("产品不存在");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 分页获取商品列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize) {
        //使用pageHelper
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVo = Lists.newArrayList();

        for (Product proItem : productList) {
            ProductListVo productListItem = assembleProductListVo(proItem);
            productListVo.add(productListItem);
        }

        PageInfo<ProductListVo> pageResult = new PageInfo<>(productListVo);
        pageResult.setList(productListVo);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 根据商品名称或商品id搜索商品
     *
     * @param productName 商品名称
     * @param productId   商品id
     * @param pageNum     分页-当前页数
     * @param pageSize    分页-页面大小
     * @return
     */
    @Override
    public ServerResponse searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuffer().append("%").append(productName).append("%").toString();
        }

        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        List<ProductListVo> productListVos = Lists.newArrayList();

        for (Product pItem : productList) {
            ProductListVo productListVo = assembleProductListVo(pItem);
            productListVos.add(productListVo);
        }
        PageInfo<ProductListVo> pageResult = new PageInfo<>(productListVos);
        pageResult.setList(productListVos);
        return ServerResponse.createBySuccess(pageResult);
    }


    /**
     * 将product对象转换成productDetailVo对象
     *
     * @param product
     * @return
     */
    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setName(product.getName());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImage(product.getSubImages());
        productDetailVo.setSubTiltle(product.getSubtitle());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());
        //imageHost
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        //parentCategoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (null == category) {
            //默认根节点
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setParentCategoryId(category.getId());
        }
        //createTime
        productDetailVo.setCreateTime(DateTimeUtil.date2Str(product.getCreateTime()));
        //updateTime
        productDetailVo.setUpdateTime(DateTimeUtil.date2Str(product.getUpdateTime()));

        return productDetailVo;
    }


    /**
     * 将product对象转换成productListVo对象
     *
     * @param product
     * @return
     */
    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        //imageHost
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }


}
