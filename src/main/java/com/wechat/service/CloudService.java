package com.wechat.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wechat.po.NowPlayingPO;
import com.wechat.util.HttpsUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CloudService {

    private static final String getVipUrl = "http://127.0.0.1:8055/wx/getVip";
    private static final String getCodeUrl = "http://127.0.0.1:8055/wx/getCode?phone=PHONE";
    private final String host = "http://douban/";

    /**
     * 获取会员号
     */
    public String getVip() {
        try {
            return HttpsUtil.get(getVipUrl, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取验证码
     */
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

    /**
     * 获取热映电影
     */
    public NowPlayingPO getNowPlaying() {
        String url = host + "getNowPlaying";
        try {
            String resp = restTemplate.getForObject(url, String.class);
            JSONArray jsonArray = JSONArray.parseArray(resp);
            assert jsonArray != null;
            JSONObject jsonObject = jsonArray.getJSONObject((int) (Math.random() * jsonArray.size()));
            return jsonObject.toJavaObject(NowPlayingPO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取电影简述
     */
    public String getMovieDesc(String id) {
        String url = host + "getMovieDesc?id=" + id;
        return restTemplate.getForObject(url, String.class);
    }

    /**
     * 获取电影评论
     */
    public List<String> getComments(String id, int pageNum, int pageSize) {
        String url = host + "getComments?id=" + id + "&pageNum=" + pageNum + "&pageSize=" + pageSize;
        return restTemplate.getForObject(url, List.class);
    }
}
