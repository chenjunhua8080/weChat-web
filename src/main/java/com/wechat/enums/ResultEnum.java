package com.wechat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultEnum {

    OK(200, "OK"),
    NOT_FOUND(404, "404"),
    UNAUTHORIZED(412, "未授权"),
    INTERNAL_SERVER_ERROR(500, "其他错误");

    private int code;
    private String name;

}
