package com.weChat.request;

import lombok.Data;

@Data
public class InitRequest {

    private String ticket;
    private String uuid;
    private String scan;

}
