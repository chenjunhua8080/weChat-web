package com.wechat.service;

import com.wechat.util.HttpsUtil;
import org.springframework.stereotype.Component;

@Component
public class CloudService {

    private static final String getVipUrl = "http://127.0.0.1:8055/wx/getVip";
    private static final String getCodeUrl = "http://127.0.0.1:8055/wx/getCode?phone=PHONE";

    public String getVip() {
        try {
            return HttpsUtil.get(getVipUrl, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCode(String phone) {
        try {
            return HttpsUtil.get(getCodeUrl.replace("PHONE", phone), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
