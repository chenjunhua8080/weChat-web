package com.wechat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaTypeEnum {

    MEDIA_TYPE_PIC(1, "pic"),
    MEDIA_TYPE_DOC(2, "doc");


    private int code;
    private String name;

    public static MediaTypeEnum from(Integer code) {
        if (code == null) {
            return null;
        }
        for (MediaTypeEnum e : MediaTypeEnum
            .values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        MediaTypeEnum e = from(code);
        return e == null ? "" : e.getName();
    }

}
