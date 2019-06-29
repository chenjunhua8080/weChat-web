package com.wechat.po.wechat;

import lombok.Data;

@Data
public class AddMsgListPO {

    private String appInfo;//{AppID,Type}
    private int appMsgType;
    private String content;
    private long createTime;
    private String encryFileName;
    private String fileName;
    private String fileSize;
    private int forwardFlag;
    private String fromUserName;
    private int hasProductId;
    private int imgHeight;
    private int imgStatus;
    private int imgWidth;
    private String mediaId;
    private String msgId;
    private int msgType;
    private long newMsgId;
    private String oriContent;
    private int playLength;
    private String recommendInfo;//object
    private int status;
    private int statusNotifyCode;
    private String statusNotifyUserName;
    private int subMsgType;
    private String ticket;
    private String toUserName;
    private String url;
    private int voiceLength;

}
