package com.wechat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnswerEnum {

    Answer_1(1, "A或正确"),
    Answer_2(2, "B或错误"),
    Answer_3(3, "C"),
    Answer_4(4, "D"),
    Answer_7(7, "AB"),
    Answer_8(8, "AC"),
    Answer_9(9, "AD"),
    Answer_10(10, "BC"),
    Answer_11(11, "BD"),
    Answer_12(12, "CD"),
    Answer_13(13, "ABC"),
    Answer_14(14, "ABD"),
    Answer_15(15, "ACD"),
    Answer_16(16, "BCD"),
    Answer_17(17, "ABCD");

    private int code;
    private String name;

    public static AnswerEnum from(Integer code) {
        if (code == null) {
            return null;
        }
        for (AnswerEnum e : AnswerEnum
            .values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        AnswerEnum e = from(code);
        return e == null ? "" : e.getName();
    }

}
