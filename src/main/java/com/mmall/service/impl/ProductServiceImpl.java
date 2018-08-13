package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.CategoryService;
import com.mmall.service.ProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    @Resource
    private CategoryService categoryService;

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
            int checkCount = productMapper.checkProductByPrimaryKey(product.getId(),null);
            if (checkCount > 0) {
                //id存在则更新，否则添加
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
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
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
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (null == productId) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (null == product) {
            return ServerResponse.createByErrorMessage("产品不存在");
        }
        ProductDetailVo productDetailVo = this.assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 前台获取商品详情接口,增加了商品是否下架的判断
     *
     * @param productId 商品id
     * @return
     */
    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (null == productId) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (null == product) {
            return ServerResponse.createByErrorMessage("产品不存在");
        }
        //判断商品状态
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("产品已下架或删除");
        }
        ProductDetailVo productDetailVo = this.assembleProductDetailVo(product);
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
            ProductListVo productListItem = this.assembleProductListVo(proItem);
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
            ProductListVo productListVo = this.assembleProductListVo(pItem);
            productListVos.add(productListVo);
        }
        PageInfo<ProductListVo> pageResult = new PageInfo<>(productListVos);
        pageResult.setList(productListVos);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 根据商品关键字和分类id集合搜索商品
     * @param keywords
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getProductByKeywordsCategory(String keywords, Integer categoryId,
                                                                 int pageNum, int pageSize, String orderBy) {
        if (StringUtils.isBlank(keywords) && null == categoryId) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<>();
        if (null != categoryId) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (null == category && StringUtils.isBlank(keywords)) {
                //没有该分类，并且没有关键字，返回一个空的结果集
                PageHelper.startPage(pageNum, pageSize);
                List<ProductDetailVo> productList = Lists.newArrayList();
                PageInfo<ProductDetailVo> pageInfo = new PageInfo<>(productList);
                pageInfo.setList(productList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            //拿到该分类下的所有子分类
            categoryIdList = categoryService.selectCategoryAndChildById(categoryId).getData();
        }
        if (StringUtils.isNotBlank(keywords)) {
            keywords = new StringBuffer().append("%").append(keywords).append("%").toString();
        }
        PageHelper.startPage(pageNum, pageSize);
        //排序处理
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByParam = orderBy.split("_");
                PageHelper.orderBy(orderByParam[0] + " " + orderByParam[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keywords) ? null : keywords,
                categoryIdList.size() == 0 ? null : categoryIdList);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVo productListVo = this.assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo<ProductListVo> pageInfo = new PageInfo<>(productListVoList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    /**
     * 组装productDetailVo对象
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
     * 组装productListVo对象
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
