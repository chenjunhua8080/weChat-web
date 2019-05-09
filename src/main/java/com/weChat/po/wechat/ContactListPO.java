package com.weChat.po.wechat;

import java.util.List;
import lombok.Data;

@Data
public class ContactListPO {

    private BaseResponsePO baseResponse;
    private int memberCount;
    private List<ContactPO> memberList;
    private int seq;

}
