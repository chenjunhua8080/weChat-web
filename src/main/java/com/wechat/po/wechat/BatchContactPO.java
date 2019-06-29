package com.wechat.po.wechat;

import java.util.List;
import lombok.Data;

@Data
public class BatchContactPO {

    private BaseResponsePO baseResponse;
    private List<ContactPO> contactList;
    private int count;

}
