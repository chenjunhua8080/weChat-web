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
import java.util.List;
import lombok.AllArgsConstructor;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class WeChatService {

    RedisService redisService;
    CloudService cloudService;

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
        String msgText;
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
        } else if (content.contains("#学车")) {
            QuestionBankPO questionBank = ApiUtil.getQuestionBank();
            if (questionBank == null) {
                msgText = "获取试题失败";
            } else {
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
                new Thread(
                    () -> {
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //撤回倒计时
                        revokeMsg(msgResponse.getLocalID(), msgResponse.getMsgID(), toUser);
                        //答案
                        String msg = questionBank.getAnswer() + "\n" + questionBank.getExplains();
                        //回复
                        sendMsg1(msg, toUser, loginPage);
                    }
                ).start();
                return;
            }
        } else {
            return;
        }
//        msgText += "\n                                              -- 小尾巴";

        //回复
        sendMsg1(msgText, toUser, loginPage);
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
