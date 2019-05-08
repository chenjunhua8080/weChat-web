package com.weChat.po.wechat;

import java.util.List;
import lombok.Data;

@Data
public class SyncKeyPO {

    private int count;
    private List<SyncKeyItemPO> list;

}
