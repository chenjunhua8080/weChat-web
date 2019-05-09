package com.weChat.po.wechat;

import java.util.List;
import lombok.Data;

@Data
public class ContactPO {

    private long uin;
    private String userName;
    private String nickName;
    private String headImgUrl;
    private int contactFlag;
    private int memberCount;
    private List<MemberPO> memberList;
    private String remarkName;
    private int hideInputBarFlag;
    private int sex;
    private String signature;
    private int verifyFlag;
    private int ownerUin;
    private String PYInitial;
    private String PYQuanPin;
    private String remarkPYInitial;
    private String remarkPYQuanPin;
    private int starFriend;
    private int appAccountFlag;
    private int statues;
    private int attrStatus;
    private String province;
    private String city;
    private String alias;
    private int snsFlag;
    private int uniFriend;
    private String displayName;
    private int chatRoomId;
    private String keyWord;
    private String encryChatRoomId;
    private int isOwner;

}
