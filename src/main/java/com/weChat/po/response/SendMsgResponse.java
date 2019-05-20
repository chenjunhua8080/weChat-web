package com.weChat.po.response;

import com.weChat.po.wechat.BaseResponsePO;
import lombok.Data;

@Data
public class SendMsgResponse {

    private BaseResponsePO baseResponse;

    private String msgID;

    private String localID;

}
