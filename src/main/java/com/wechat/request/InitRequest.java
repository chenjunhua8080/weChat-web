package com.wechat.request;

import lombok.Data;

@Data
public class InitRequest {

    private String ticket;
    private String uuid;
    private String scan;

}
