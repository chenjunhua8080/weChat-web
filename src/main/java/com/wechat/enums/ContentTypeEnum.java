package com.wechat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentTypeEnum {

    CONTENT_TYPE_JPEG(1, "image/jpeg"),
    CONTENT_TYPE_GIF(2, "image/gif");

    private int code;
    private String name;

    public static ContentTypeEnum from(Integer code) {
        if (code == null) {
            return null;
        }
        for (ContentTypeEnum e : ContentTypeEnum
            .values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        ContentTypeEnum e = from(code);
        return e == null ? "" : e.getName();
    }

}
