package com.weChat.po.wechat;

import java.util.List;
import lombok.Data;

@Data
public class WebWxSyncPO {

    private int addMsgCount;
    private List<AddMsgListPO> addMsgList;//list
    private BaseResponsePO baseResponse;
    private String continueFlag;
    private int delContactCount;
    private String delContactList;//list
    private int modChatRoomMemberCount;
    private String modChatRoomMemberList;//list
    private int modContactCount;
    private String modContactList;//list
    private String profile;
    private String sKey;
    private SyncKeyPO syncCheckKey;
    private SyncKeyPO syncKey;

}
