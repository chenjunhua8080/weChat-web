package com.wechat.service;

import com.wechat.util.HttpsUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ElemeService {

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
            Map<String, Object> map = new HashMap<>();
            map.put("phone", phone);
            return HttpsUtil.get(getCodeUrl, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
