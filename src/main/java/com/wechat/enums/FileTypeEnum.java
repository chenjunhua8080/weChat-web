package com.wechat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileTypeEnum {

    FILE_TYPE_JPEG(1, "jpeg"),
    FILE_TYPE_GIF(2, "gif");


    private int code;
    private String name;

    public static FileTypeEnum from(Integer code) {
        if (code == null) {
            return null;
        }
        for (FileTypeEnum e : FileTypeEnum
            .values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        FileTypeEnum e = from(code);
        return e == null ? "" : e.getName();
    }

}
