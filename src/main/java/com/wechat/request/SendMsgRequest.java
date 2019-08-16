package com.wechat.request;

import lombok.Data;

@Data
public class SendMsgRequest {

    private int type;
    private String mediaId;
    private String content;
    private String fromUserName;
    private String toUserName;

}
