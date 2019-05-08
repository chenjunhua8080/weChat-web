package com.weChat.po.wechat;

import lombok.Data;

@Data
public class ContactPO {

    private BaseResponsePO baseResponse;
    private int memberCount;
    private MemberPO memberList;
    private int seq;

}
