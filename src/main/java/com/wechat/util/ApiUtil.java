package com.wechat.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ApiUtil {

    public static final String today_history = "http://v.juhe.cn/todayOnhistory/queryEvent.php?date=DATE&key=e90db0341047acc15edc10ee8458f880";

    public static final String rand_joke = "http://v.juhe.cn/joke/randJoke.php?key=938da9ec9cc43d7aebf5d28bfe91e68f";

    public static final String simple_weadther = "http://apis.juhe.cn/simpleWeather/query?city=%E5%B9%BF%E5%B7%9E&key=cb36aed6af10cfac7a20f9208c6ba14d";

    public static String getTodayHistory() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d");
        String url = today_history.replace("DATE", dateFormat.format(new Date()));
        JSONObject resp = JSONObject.fromObject(HttpsUtil.get(url, null));
        int errorCode = resp.getInt("error_code");
        String result = "";
        if (errorCode == 0) {
            JSONArray data = resp.getJSONArray("result");
            for (Object item : data) {
                JSONObject day = (JSONObject) item;
                result += day.getString("date") + "：" + day.getString("title") + "\n";
            }
        } else {
            result = resp.getString("reason");
        }
        return result;
    }

    public static String getRandJoke() throws Exception {
        String url = rand_joke;
        JSONObject resp = JSONObject.fromObject(HttpsUtil.get(url, null));
        int errorCode = resp.getInt("error_code");
        String result = "";
        if (errorCode == 0) {
            JSONArray data = resp.getJSONArray("result");
            JSONObject joke = (JSONObject) data.get(0);
            result = joke.getString("content");
        } else {
            result = resp.getString("reason");
        }
        return result;
    }

    public static String getSimpleWeadther() throws Exception {
        String url = simple_weadther;
        JSONObject resp = JSONObject.fromObject(HttpsUtil.get(url, null));
        int errorCode = resp.getInt("error_code");
        String result = "";
        if (errorCode == 0) {
            JSONObject data = resp.getJSONObject("result");
            JSONObject realtime = data.getJSONObject("realtime");
            String info = realtime.getString("info");
            String temperature = realtime.getString("temperature");
            result = info + "，" + temperature + "°C";
        } else {
            result = resp.getString("reason");
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getSimpleWeadther());
    }

}
