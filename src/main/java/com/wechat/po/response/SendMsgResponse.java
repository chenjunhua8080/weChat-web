package com.wechat.po.response;

import com.wechat.po.wechat.BaseResponsePO;
import lombok.Data;

@Data
public class SendMsgResponse {

    private BaseResponsePO baseResponse;

    private String msgID;

    private String localID;

}
