package com.mmall.common;

/**
 * @author Sunsongoing
 */

public class Const {

    /**
     * 当前用户的session-key
     */
    public static final String CURRENT_USER = "currentUser";

    /**
     * 本地缓存的token前缀
     */
    public static final String TOKEN_PREFIX = "token_";
    /**
     * checkValid type 参数
     */
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    /**
     * 角色类型常量
     * 接口中的属性默认是静态的常量
     */
    public interface Role {

        /**
         * 普通用户
         */
        int ROLE_CUSTOMER = 0;

        /**
         * 管理员
         */
        int ROLE_ADMIN = 1;
    }
}
