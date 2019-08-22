package com.wechat.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wechat.po.NowPlayingPO;
import com.wechat.util.HttpsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private RestTemplate restTemplate;

    public NowPlayingPO getNowPlaying() {
        try {
            String resp = restTemplate.getForObject("http://douban/getNowPlaying", String.class);
            JSONArray jsonArray = JSONArray.parseArray(resp);
            assert jsonArray != null;
            JSONObject jsonObject = jsonArray.getJSONObject((int) (Math.random() * jsonArray.size()));
            return jsonObject.toJavaObject(NowPlayingPO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
