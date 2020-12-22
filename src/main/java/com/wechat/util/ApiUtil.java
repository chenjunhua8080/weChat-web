package com.wechat.util;

import com.wechat.enums.AnswerEnum;
import com.wechat.po.QuestionBankPO;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ApiUtil {

    public static final String today_history = "http://v.juhe.cn/todayOnhistory/queryEvent.php?date=DATE&key=e90db0341047acc15edc10ee8458f880";

    public static final String rand_joke = "http://v.juhe.cn/joke/randJoke.php?key=938da9ec9cc43d7aebf5d28bfe91e68f";

    public static final String simple_weadther = "http://apis.juhe.cn/simpleWeather/query?city=%E5%B9%BF%E5%B7%9E&key=cb36aed6af10cfac7a20f9208c6ba14d";

    public static final String constellation = "http://web.juhe.cn:8080/constellation/getAll?consName=NAME&type=week&key=eddce9423c555147abc43a740f299431";

    public static final String question_bank = "http://v.juhe.cn/jztk/query?subject=1&model=c1&testType=&=&key=01b8d30914c2e0c78205b4775ab125dc";

    public static final String question_answers = "http://v.juhe.cn/jztk/answers?key=01b8d30914c2e0c78205b4775ab125dc";

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

    public static String getConstellation(String name) throws Exception {
        String url = constellation.replace("NAME", URLEncoder.encode(name, "utf-8"));
        JSONObject resp = JSONObject.fromObject(HttpsUtil.get(url, null));
        int errorCode = resp.getInt("error_code");
        String result = "";
        if (errorCode == 0) {
            String love = resp.getString("love");
            String money = resp.getString("money");
            String work = resp.getString("work");
            result = name + "\n\n" + love + "\n\n" + money + "\n\n" + work;
        } else {
            result = resp.getString("reason");
        }
        return result;
    }

    public static List<QuestionBankPO> getQuestionBankList() throws Exception {
        String url = question_bank;
        JSONObject resp = JSONObject.fromObject(HttpsUtil.get(url, null));
        int errorCode = resp.getInt("error_code");
        if (errorCode == 0) {
            JSONArray jsonArray = resp.getJSONArray("result");
            for (Object item : jsonArray) {
                JSONObject jsonObject = (JSONObject) item;
                String item1 = jsonObject.getString("item1");
                String answer = jsonObject.getString("answer");
                if (item1 == null || item1.equals("")) {
                    //判断题
                    answer = AnswerEnum.getNameByCode(Integer.parseInt(answer));
                    answer = answer.substring(answer.length() - 2);
                } else {
                    answer = AnswerEnum.getNameByCode(Integer.parseInt(answer));
                    answer = answer.substring(0, 1);
                }
                //设置答案选项
                jsonObject.put("answer", answer);
            }
            return com.alibaba.fastjson.JSONObject.parseArray(jsonArray.toString(), QuestionBankPO.class);
        }
        return null;
    }

    public static QuestionBankPO getQuestionBank() throws Exception {
        List<QuestionBankPO> questionBankList = getQuestionBankList();
        if (questionBankList != null) {
            return questionBankList.get(0);
        }
        return null;
    }

    public static JSONArray getQuestionAnswers() throws Exception {
        String url = question_answers;
        JSONObject resp = JSONObject.fromObject(HttpsUtil.get(url, null));
        int errorCode = resp.getInt("error_code");
        if (errorCode == 0) {
            return resp.getJSONArray("result");
        }
        return null;
    }

}
