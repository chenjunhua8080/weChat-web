package com.weChat.request;

import lombok.Data;

@Data
public class SendMsgRequest {

    private int type;
    private String content;
    private String fromUserName;
    private String toUserName;

}
