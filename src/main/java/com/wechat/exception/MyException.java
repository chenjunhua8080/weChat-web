package com.wechat.exception;

import com.wechat.enums.ResultEnum;
import lombok.Data;

@Data
public class MyException extends RuntimeException {

    private int code;
    private String msg;

    public MyException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public MyException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public MyException(ResultEnum resultEnum) {
        super(resultEnum.getName());
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getName();
    }
}
