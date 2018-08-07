package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.FileService;
import com.mmall.service.ProductService;
import com.mmall.service.UserService;
import com.mmall.util.PropertiesUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Map;

/**
 * @author Sunsongoing
 */

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Resource
    private ProductService productService;
    @Resource
    private UserService userService;
    @Resource
    private FileService fileService;

    @RequestMapping("/product_save")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        if (userService.checkAdminRole(user).isSuccess()) {
            return productService.saveProduct(product);
        } else {
            return ServerResponse.createByErrorMessage("需要管理员权限");
        }
    }

    @RequestMapping("/set_sale_status")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        if (userService.checkAdminRole(user).isSuccess()) {
            return productService.setSaleStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMessage("需要管理员权限");
        }
    }

    @RequestMapping("/detail")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        if (userService.checkAdminRole(user).isSuccess()) {
            return productService.getDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("需要管理员权限");
        }
    }

    @RequestMapping("/list")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        if (userService.checkAdminRole(user).isSuccess()) {
            return productService.getList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("需要管理员权限");
        }
    }

    @RequestMapping("/search")
    @ResponseBody
    public ServerResponse search(HttpSession session, String producName, Integer productId,
                                 @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                 @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == user) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        if (userService.checkAdminRole(user).isSuccess()) {
            return productService.searchProduct(producName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("需要管理员权限");
        }
    }

    @RequestMapping("/upload")
    @ResponseBody
    public ServerResponse upload(MultipartFile file, HttpServletRequest request) {
        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetFileName = fileService.upload(file, path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+ File.separator+targetFileName;
        Map<String,String> fileMap = Maps.newHashMap();
        fileMap.put("uri",targetFileName);
        fileMap.put("url",url);
        return ServerResponse.createBySuccess(fileMap);
    }

    //TODO springmvc 关于上传文件的配置

}
