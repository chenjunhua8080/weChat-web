package com.wechat.po.wechat;

import lombok.Data;

@Data
public class UserPO {

    private long uid;
    private String userName;
    private String nickName;
    private String HeadImgUrl;
    private String remarkName;
    private String PYInitial;
    private String PYQuanPin;
    private String remarkPYInitial;
    private String remarkPYQuanPin;
    private int hideInputBarFlag;
    private int starFriend;
    private int sex;
    private String signature;
    private int appAccountFlag;
    private int verifyFlag;
    private int contactFlag;
    private int webWxPluginSwitch;
    private int headImgFlag;
    private int snsFlag;

}

