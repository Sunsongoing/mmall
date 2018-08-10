package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

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
     * 商品排序规则
     * 下划线之前表示按什么排序，之后是排序规则(升序或降序)
     * price_asc  按价格升序排序
     * price_desc 按价格降序排序
     */
    public interface ProductListOrderBy {
        /**
         * set的contains()的时间复杂度为O(1) list的contains()复杂度为O(n)
         * 所以这里使用set
         */
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");
    }

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

    public enum ProductStatusEnum {

        /**
         * 商品状态.1-在售 2-下架 3-删除
         */
        ON_SALE("在线状态", 1),
        UNDER("下架状态", 2),
        DELETE("删除状态", 3);

        private String value;
        private int code;

        ProductStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
}
