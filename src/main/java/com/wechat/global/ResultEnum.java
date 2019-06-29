package com.wechat.global;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultEnum {

    OK(200, "OK"),
    PROCESSING(102, "处理中"),
    ALREADY_REPORTED(208, "已重复/已存在"),
    PERMISSION_DENIED(400, "权限不足"),
    UNAUTHORIZED(401, "授权失败/失效"),
    PAYMENT_REQUIRED(402, "缺少必须参数"),
    FORBIDDEN(403, "禁用（包含非法字符）"),
    NOT_FOUND(404, "资源不存在"),
    NOT_LOGIN(405, "未登录"),
    REQUEST_TIMEOUT(408, "请求过时/超时"),
    PRECONDITION_FAILED(412, "参数错误"),
    REQUEST_PARAM_TOO_LONG(413, "请求参数过长"),
    EXPECTATION_FAILED(417, "期望错误/逻辑错误"),
    SIGN_FAILURE(420, "签名错误/验签失败"),
    INTERNAL_SERVER_ERROR(500, "其他错误");

    private int code;
    private String name;

}
