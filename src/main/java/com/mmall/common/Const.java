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
     * 购物车常量
     */
    public interface Cart {
        /**
         * 购物车为选中状态
         */
        int CHECKED = 1;
        /**
         * 购物车未选中状态
         */
        int UN_CHECKED = 0;

        /**
         * 库存限制失败
         * 表示库存不足
         */
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        /**
         * 库存限制成功
         * 表示库存足够
         */
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
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

    public enum OrderStatusEnum {

        /**
         * 订单状态.0-取消 10-未支付 20-已付款 30-已发货 40-订单完成 50-订单关闭
         */
        CANCELED("已取消", 0),
        NO_PAY("未支付", 10),
        PAID("已付款", 20),
        SHIPPED("已发货", 30),
        ORDER_SUCCESS("订单完成", 40),
        ORDER_CLOSED("订单关闭", 50);

        private String value;
        private int code;

        OrderStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        /**
         * 根据code返回对应枚举
         * @param code
         * @return
         */
        public static OrderStatusEnum codeOf(int code) {
            for (OrderStatusEnum ord : values()) {
                if (ord.getCode() == code) {
                    return ord;
                }
            }
            throw new RuntimeException("没有找到对应枚举");
        }

    }

    /**
     * 支付宝交易状态
     */
    public interface AlipayCallback {
        /**
         * 交易创建，等待买家付款
         */
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        /**
         * 未付款交易超时关闭，或支付完成后全额退款
         */
        String TRADE_STATUS_TRADE_CLOSED = "TRADE_CLOSED";
        /**
         * 交易支付成功
         */
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
        /**
         * 交易结束，不可退款
         */
        String TRADE_STATUS_TRADE_FINISHED = "TRADE_FINISHED";

        /**
         * 回调成功的返回值
         */
        String RESPONSE_SUCCESS = "success";

        /**
         * 回调失败的返回值
         */
        String RESPONSE_FAILED = "failure";
    }

    /**
     * 支付平台
     */
    public enum PayPlatFormEnum {
        /**
         * 支付宝
         */
        ALIPAY("支付宝", 1);

        private String value;
        private int code;

        PayPlatFormEnum(String value, int code) {
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

    /**
     * 支付方式
     */
    public enum PaymentTypeEnum {
        /**
         * 在线支付
         */
        ONLINE_PAY("在线支付", 1);

        private String value;
        private int code;

        PaymentTypeEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        /**
         * 根据枚举的code返回对应枚举
         *
         * @param code
         * @return
         */
        public static PaymentTypeEnum codeOf(int code) {
            for (PaymentTypeEnum p : values()) {
                if (p.getCode() == code) {
                    return p;
                }
            }
            throw new RuntimeException("没有找到对应枚举");
        }
    }
}
