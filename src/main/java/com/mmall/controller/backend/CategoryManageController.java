package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.CategoryService;
import com.mmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author Sunsongoing
 */

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Resource
    private UserService userService;
    @Resource
    private CategoryService categoryService;

    @RequestMapping(value = "/add_category", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName,
                                      @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        User u = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == u) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录");
        }
        //校验是否是管理员
        if (userService.checkAdminRole(u).isSuccess()) {
            //添加category
            return categoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.createByErrorMessage("没有权限操作,需要管理员权限");
        }
    }

    @RequestMapping(value = "/set_category_name", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        User u = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == u) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,请登录");
        }
        //校验是否是管理员
        if (userService.checkAdminRole(u).isSuccess()) {
            //更新categoryName
            return categoryService.setCategoryName(categoryId, categoryName);
        } else {
            return ServerResponse.createByErrorMessage("没有权限操作,需要管理员权限");
        }
    }

    @RequestMapping(value = "/get_category", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,
                                                      @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User u = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == u) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if (userService.checkAdminRole(u).isSuccess()) {
            //查询子节点的category信息，并且不递归
            return categoryService.getChildrenParallelCategory(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("没有权限操作,需要管理员权限");
        }
    }

    @RequestMapping(value = "/get_deep_category", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenParallel(HttpSession session,
                                                             @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User u = (User) session.getAttribute(Const.CURRENT_USER);
        if (null == u) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if (userService.checkAdminRole(u).isSuccess()) {
            //查询当前节点的id和递归子节点的id
            return categoryService.selectCategoryAndChildById(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("没有权限操作,需要管理员权限");
        }
    }

}
