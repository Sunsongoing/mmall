package com.mmall.common;

/**
 * @author Sunsongoing
 *
 * 响应扩展类
 */

public enum ResponseCode {
    //成功
    SUCCESS(0, "SUCCESS"),
    //失败
    ERROR(1, "ERROR"),
    //需要登录
    NEED_LOGIN(10, "NEED_LOGIN"),
    //参数错误
    ILLEGA_ARGUMENT(2, "ILLEGA_ARGUMENT");
    /**
     * 响应码
     */
    private final int code;
    /**
     * 描述
     */
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
