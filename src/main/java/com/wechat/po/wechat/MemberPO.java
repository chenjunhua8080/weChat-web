package com.wechat.po.wechat;

import lombok.Data;

@Data
public class MemberPO {

    private int uid;
    private String userName;
    private String nickName;
    private int attrStatus;
    private String PYInitial;
    private String PYQuanPin;
    private String remarkPYInitial;
    private String remarkPYQuanPin;
    private int memberStatus;
    private String displayName;
    private String keyWord;

}
