package com.wechat.service;

import com.wechat.enums.ContentTypeEnum;
import com.wechat.enums.FileTypeEnum;
import com.wechat.enums.MediaTypeEnum;
import com.wechat.po.NowPlayingPO;
import com.wechat.po.QuestionBankPO;
import com.wechat.po.response.SendMsgResponse;
import com.wechat.po.wechat.AddMsgListPO;
import com.wechat.po.wechat.LoginPagePO;
import com.wechat.po.wechat.UserPO;
import com.wechat.po.wechat.WebWxSyncPO;
import com.wechat.request.SendMsgRequest;
import com.wechat.util.ApiUtil;
import com.wechat.util.HttpsUtil;
import com.wechat.util.WeChatUtil;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WeChatService {

    private static Map<String, List<QuestionBankPO>> userQuestionList = new HashMap<>();
    private static Map<String, SendMsgResponse> userQuestionRollBackId = new HashMap<>();
    private static Map<String, Integer> userQuestionIndex = new HashMap<>();
    private static Map<String, Integer> userQuestionScore = new HashMap<>();
    private static Map<String, ScheduledExecutorService> userQuestionThread = new HashMap<>();
    @Autowired
    private RedisService redisService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private CloudService cloudService;

    /**
     * 处理消息
     */
    public void handleMsg(WebWxSyncPO webWxSyncPO) throws Exception {
        int msgCount = webWxSyncPO.getAddMsgCount();
        if (msgCount == 0) {
            return;
        }
        String userName = getUserName();

        List<AddMsgListPO> msgList = webWxSyncPO.getAddMsgList();
        for (AddMsgListPO addMsgListPO : msgList) {
            String content = addMsgListPO.getContent();

            //已经加上识别符号#，可以直接处理消息
            if ("filehelper".equals(addMsgListPO.getToUserName()) ||
                userName.equals(addMsgListPO.getFromUserName())) {
                //处理文本消息
                handleTextMsg(content, addMsgListPO.getToUserName());
            } else {
                //处理文本消息
                handleTextMsg(content, addMsgListPO.getFromUserName());
            }
        }
    }

    /**
     * 处理基本文本指令
     */
    private void handleTextMsg(String content, String toUser) throws Exception {
        LoginPagePO loginPage = getLoginPage();
        String msgText = null;
        if (content.contains("#天气")) {
            msgText = ApiUtil.getSimpleWeadther();
        } else if (content.contains("#笑话")) {
            msgText = ApiUtil.getRandJoke();
        } else if (content.contains("#今天")) {
            msgText = ApiUtil.getTodayHistory();
        } else if (content.contains("#help")) {
            msgText = "支持指令：#笑话、#天气、#历史上的今天";
        } else if (content.equals("#会员号")) {
            //回复
            sendMsg1("正在获取手机号，请稍后", toUser, loginPage);
            msgText = cloudService.getVip();
            if (msgText != null) {
                msgText += "\n点击获取验证码后，给我发送手机号！";
            }
        } else if (content.matches("^#\\d{11}$")) {
            //回复
            sendMsg1("正在获取验证码，请稍后", toUser, loginPage);
            msgText = cloudService.getCode(content.substring(1));
        } else if (content.contains("#头像")) {
            //回复
            sendMsg1("正在获取图片，请稍后", toUser, loginPage);
            File file = new File("C:\\robot(9).jpg");
            String mediaId = WeChatUtil.upload(loginPage, file,
                ContentTypeEnum.CONTENT_TYPE_JPEG.getName(),
                MediaTypeEnum.MEDIA_TYPE_PIC.getName());
            sendMsg3(mediaId, toUser, loginPage);
            return;
        } else if (content.contains("#电影推荐")) {
            //回复
            sendMsg1("正在查找，请稍后", toUser, loginPage);
            NowPlayingPO nowPlaying = cloudService.getNowPlaying();
            //发送文字
            String movieInfo = nowPlaying.getId() + "\n"
                + nowPlaying.getName() + "/" + nowPlaying.getScore() + "/" + nowPlaying.getActors();
            sendMsg1(movieInfo, toUser, loginPage);
            String movieDesc = cloudService.getMovieDesc(nowPlaying.getId());
            sendMsg1(movieDesc, toUser, loginPage);
            //发送图片
            String imgSrc = nowPlaying.getImg();
            File file = HttpsUtil.downFile(imgSrc, FileTypeEnum.FILE_TYPE_JPEG.getName());
            String mediaId = WeChatUtil.upload(loginPage, file,
                ContentTypeEnum.CONTENT_TYPE_JPEG.getName(),
                MediaTypeEnum.MEDIA_TYPE_PIC.getName());
            sendMsg3(mediaId, toUser, loginPage);
            return;
        } else if (content.contains("#影评#")) {
            String id = content.substring(content.lastIndexOf("#") + 1);
            List<String> comments = cloudService.getComments(id, 0, 10);
            StringBuilder msgTextBuilder = new StringBuilder();
            for (String item : comments) {
                msgTextBuilder.append(item).append("\n");
            }
            msgText = msgTextBuilder.toString();
        } else if (content.contains("#星座运势#")) {
            String name = content.substring(content.lastIndexOf("#") + 1);
            msgText = ApiUtil.getConstellation(name);
        } else if (content.equals("刷题模式")) {
            List<QuestionBankPO> questionBankList = ApiUtil.getQuestionBankList();
            if (questionBankList == null) {
                msgText = "获取试题失败";
            } else {
                if (userQuestionThread.get(toUser) != null) {
                    sendMsg1("重新开始", toUser, loginPage);
                }
                sendMsg1("开始刷题，回复[退出刷题模式]即可退出！", toUser, loginPage);
                //把用户刷题信息添加到map保存
                ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
                userQuestionThread.put(toUser, scheduledExecutorService);
                userQuestionList.put(toUser, questionBankList);

                //第一题
                userQuestionIndex.put(toUser, 0);
                userQuestionScore.put(toUser, 0);
                redisCache.setCacheObject("car1:" + toUser, 999);
                //立即执行，随后60秒执行一次
                scheduledExecutorService.scheduleAtFixedRate(() -> {
                    //发送问题
                    SendQuestion(toUser, loginPage, questionBankList);
                }, 0, 1, TimeUnit.SECONDS);

                return;
            }
            //回复
            sendMsg1(msgText, toUser, loginPage);
            return;
        } else if (content.equals("退出刷题模式")) {
            ScheduledExecutorService scheduledExecutorService = userQuestionThread.get(toUser);
            scheduledExecutorService.shutdownNow();
            userQuestionThread.remove(toUser);
            userQuestionList.remove(toUser);
            userQuestionIndex.remove(toUser);
            redisCache.deleteObject("car1:" + toUser);
            SendMsgResponse msgResponse = userQuestionRollBackId.get(toUser);
            userQuestionRollBackId.remove(toUser);
            revokeMsg(msgResponse.getLocalID(), msgResponse.getMsgID(), toUser);
            Integer score = userQuestionScore.get(toUser);
            sendMsg1("已退出，得分：" + score, toUser, loginPage);
            return;
        } else if ("ABCD".contains(content.toUpperCase())) {
            content = content.toUpperCase();
            List<QuestionBankPO> list = userQuestionList.get(toUser);
            if (list == null) {
                return;
            }
            Integer i = userQuestionIndex.get(toUser);
            if (i == null) {
                return;
            }
            QuestionBankPO questionBank = list.get(i);
            boolean bingo = content.equals(questionBank.getAnswer());
            //积分
            if (bingo) {
                Integer score = userQuestionScore.get(toUser);
                userQuestionScore.put(toUser, score + 1);
            }
            //回复答案
            String msg =
                "回答" + (bingo ? "正确！" : "错误！")
                    + "\n"
                    + questionBank.getAnswer()
                    + "\n"
                    + questionBank.getExplains();
            sendMsg1(msg, toUser, loginPage);

            //100道题，刷题结束
            if (i == 100) {
                ScheduledExecutorService scheduledExecutorService = userQuestionThread.get(toUser);
                scheduledExecutorService.shutdownNow();
                userQuestionThread.remove(toUser);
                userQuestionList.remove(toUser);
                userQuestionIndex.remove(toUser);
                redisCache.deleteObject("car1:" + toUser);
                Integer score = userQuestionScore.get(toUser);
                sendMsg1("刷题结束~ 得分：" + score, toUser, loginPage);
            } else {
                //撤回计时
                SendMsgResponse msgResponse = userQuestionRollBackId.get(toUser);
                userQuestionRollBackId.remove(toUser);
                revokeMsg(msgResponse.getLocalID(), msgResponse.getMsgID(), toUser);
                //下一题
                int next = i + 1;
                userQuestionIndex.put(toUser, next);
            }
            return;
        } else {
            return;
        }
//        msgText += "\n                                              -- 小尾巴";

        //回复
        sendMsg1(msgText, toUser, loginPage);
    }

    /**
     * 学车发送问题
     *
     * @param toUser           发给谁
     * @param loginPage        登录信息
     * @param questionBankList 题库
     */
    @SneakyThrows
    private void SendQuestion(String toUser, LoginPagePO loginPage, List<QuestionBankPO> questionBankList) {
        System.out.println("尝试发送试题...");

        Integer i = userQuestionIndex.get(toUser);
        if (i == null) {
            i = 0;
        }
        if (i.equals(redisCache.getCacheObject("car1:" + toUser))) {
            return;
        }
        //暂存当前题目, 下次进来相同就不发
        redisCache.setCacheObject("car1:" + toUser, i);

        String msgText;
        QuestionBankPO questionBank = questionBankList.get(i);
        String url = questionBank.getUrl();
        if (url != null) {
            File file = HttpsUtil.downFile(url, FileTypeEnum.FILE_TYPE_JPEG.getName());
            String mediaId = WeChatUtil.upload(loginPage, file,
                ContentTypeEnum.CONTENT_TYPE_JPEG.getName(),
                MediaTypeEnum.MEDIA_TYPE_PIC.getName());
            sendMsg3(mediaId, toUser, loginPage);
        }
        msgText = questionBank.getQuestion() + "\n";
        msgText += "A：" + questionBank.getItem1() + "\n";
        msgText += "B：" + questionBank.getItem2() + "\n";
        if (questionBank.getItem3() != null && !questionBank.getItem3().equals("")) {
            msgText += "C：" + questionBank.getItem3() + "\n";
        }
        if (questionBank.getItem4() != null && !questionBank.getItem4().equals("")) {
            msgText += "D：" + questionBank.getItem4();
        }
        //发送试题
        sendMsg1(msgText, toUser, loginPage);
        //发送倒计时
        String imgSrc = "https://img-blog.csdnimg.cn/20190912113835841.gif";
        File file = HttpsUtil.downFile(imgSrc, FileTypeEnum.FILE_TYPE_GIF.getName());
        String mediaId = WeChatUtil.upload(loginPage, file,
            ContentTypeEnum.CONTENT_TYPE_GIF.getName(),
            MediaTypeEnum.MEDIA_TYPE_DOC.getName());
        SendMsgResponse msgResponse = sendMsg47(mediaId, toUser, loginPage);
        userQuestionRollBackId.put(toUser, msgResponse);

        ScheduledExecutorService scheduledExecutorService = userQuestionThread.get(toUser);
        Integer finalI = i;
        scheduledExecutorService.schedule(
            () -> {
                //清理redis
                Integer cacheQuestionId = redisCache.getCacheObject("car1:" + toUser);
                if (!finalI.equals(cacheQuestionId)) {
                    return;
                }

                //撤回倒计时
                revokeMsg(msgResponse.getLocalID(), msgResponse.getMsgID(), toUser);
                //答案
                String msg = "作答超时，公布答案~" + "\n" + questionBank.getAnswer() + "\n" + questionBank.getExplains();
                //回复
                sendMsg1(msg, toUser, loginPage);

                if (finalI == 100) {
                    //100道题，刷题结束
                    scheduledExecutorService.shutdownNow();
                    userQuestionThread.remove(toUser);
                    userQuestionList.remove(toUser);
                    userQuestionIndex.remove(toUser);
                    redisCache.deleteObject("car1:" + toUser);

                    sendMsg1("刷题结束~", toUser, loginPage);
                } else {
                    //下一题
                    int next = finalI + 1;
                    userQuestionIndex.put(toUser, next);
                }
            }, 60, TimeUnit.SECONDS
        );
    }

    /**
     * 发送文本消息
     */
    private SendMsgResponse sendMsg1(String msg, String toUser, LoginPagePO loginPage) {
        String userName = getUserName();
        SendMsgRequest sendMsgRequest = WeChatUtil.getSendMsgRequest(1, msg, null, userName, toUser);
        return WeChatUtil.sendTextMsg(loginPage, sendMsgRequest);
    }

    /**
     * 发送图片消息
     */
    private void sendMsg3(String mediaId, String toUser, LoginPagePO loginPage) {
        String userName = getUserName();
        SendMsgRequest sendMsgRequest = WeChatUtil.getSendMsgRequest(3, "", mediaId, userName, toUser);
        WeChatUtil.sendImgMsg(loginPage, sendMsgRequest);
    }

    /**
     * 发送表情消息
     */
    private SendMsgResponse sendMsg47(String mediaId, String toUser, LoginPagePO loginPage) {
        String userName = getUserName();
        SendMsgRequest sendMsgRequest = WeChatUtil.getSendMsgRequest(47, "", mediaId, userName, toUser);
        return WeChatUtil.sendEmoji(loginPage, sendMsgRequest);
    }

    /**
     * 获取登录uuid
     */
    private String getUserName() {
        String jsonUser = redisService.get("WECHATUSER", String.class);
        UserPO user = (UserPO) JSONObject.toBean(JSONObject.fromObject(jsonUser), UserPO.class);
        return user.getUserName();
    }

    /**
     * 获取登录LoginPage
     */
    private LoginPagePO getLoginPage() {
        String loginPageJson = redisService.get(WeChatUtil.LOGINPAGE, String.class);
        return (LoginPagePO) JSONObject.toBean(JSONObject.fromObject(loginPageJson), LoginPagePO.class);
    }

    /**
     * 撤回
     */
    private String revokeMsg(String locationMsgId, String serviceMsgId, String toUser) {
        WeChatUtil.revokeMsg(locationMsgId, serviceMsgId, toUser, getLoginPage());
        String jsonUser = redisService.get("WECHATUSER", String.class);
        UserPO user = (UserPO) JSONObject.toBean(JSONObject.fromObject(jsonUser), UserPO.class);
        return user.getUserName();
    }

}
