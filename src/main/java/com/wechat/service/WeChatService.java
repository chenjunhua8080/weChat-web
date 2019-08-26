package com.wechat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wechat.dao.GroupRobotDao;
import com.wechat.dao.RobotDao;
import com.wechat.po.GroupRobot;
import com.wechat.po.NowPlayingPO;
import com.wechat.po.QuestionBankPO;
import com.wechat.po.Robot;
import com.wechat.po.wechat.AddMsgListPO;
import com.wechat.po.wechat.LoginPagePO;
import com.wechat.po.wechat.UserPO;
import com.wechat.po.wechat.WebWxSyncPO;
import com.wechat.request.SendMsgRequest;
import com.wechat.util.ApiUtil;
import com.wechat.util.HttpsUtil;
import com.wechat.util.WeChatUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class WeChatService {

    RobotDao robotDao;
    GroupRobotDao groupRobotDao;
    RedisService redisService;
    CloudService cloudService;

    public void saveGroupRobot(List<Long> robotIds, String groupId) {
        UpdateWrapper<GroupRobot> update = Wrappers.update(new GroupRobot());
        update.eq("group_id", groupId);
        groupRobotDao.delete(update);

        for (Long robotId : robotIds) {
            GroupRobot groupRobot = new GroupRobot();
            groupRobot.setRobotId(robotId);
            groupRobot.setGroupId(groupId);
            groupRobotDao.insert(groupRobot);
        }
    }

    public void delGroupRobot(List<Long> robotIds, String groupId) {
        UpdateWrapper<GroupRobot> update = Wrappers.update(new GroupRobot());
        update.eq("group_id", groupId);
        update.in("robot_id", robotIds);
        groupRobotDao.delete(update);
    }

    public List<GroupRobot> getGroupRobot(String groupId) {
        QueryWrapper<GroupRobot> query = Wrappers.query(new GroupRobot());
        query.eq("group_id", groupId);
        return groupRobotDao.selectList(query);
    }

    public List<Long> getGroupRobotIds(String groupId) {
        QueryWrapper<GroupRobot> query = Wrappers.query(new GroupRobot());
        query.eq("group_id", groupId);
        List<GroupRobot> groupRobots = groupRobotDao.selectList(query);
        List<Long> ids = new ArrayList<>();
        for (GroupRobot groupRobot : groupRobots) {
            ids.add(groupRobot.getRobotId());
        }
        return ids;
    }

    public List<Robot> getRobotList() {
        QueryWrapper<Robot> query = Wrappers.query(new Robot());
        return robotDao.selectList(query);
    }

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
        if (content.equals("#天气")) {
            msgText = ApiUtil.getSimpleWeadther();
        } else if (content.equals("#笑话")) {
            msgText = ApiUtil.getRandJoke();
        } else if (content.equals("#今天")) {
            msgText = ApiUtil.getTodayHistory();
        } else if (content.equals("#help")) {
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
        } else if (content.equals("#头像")) {
            //回复
            sendMsg1("正在获取图片，请稍后", toUser, loginPage);
            File file = new File("C:\\robot(9).jpg");
            String mediaId = WeChatUtil.upload(loginPage, file);
            sendMsg3(mediaId, toUser, loginPage);
            return;
        } else if (content.equals("#电影推荐")) {
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
            File file = HttpsUtil.downFile(imgSrc);
            String mediaId = WeChatUtil.upload(loginPage, file);
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
        } else if (content.contains("#驾考试题")) {
            QuestionBankPO questionBank = ApiUtil.getQuestionBank();
            if (questionBank == null) {
                msgText = "获取试题失败";
            } else {
                String url = questionBank.getUrl();
                if (url != null) {
                    File file = HttpsUtil.downFile(url);
                    String mediaId = WeChatUtil.upload(loginPage, file);
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
            }
        } else {
            return;
        }
        msgText += "\n                                              -- 小尾巴";

        //回复
        sendMsg1(msgText, toUser, loginPage);
    }

    /**
     * 发送文本消息
     */
    private void sendMsg1(String msg, String toUser, LoginPagePO loginPage) {
        String userName = getUserName();
        SendMsgRequest sendMsgRequest = WeChatUtil.getSendMsgRequest(1, msg, null, userName, toUser);
        WeChatUtil.sendTextMsg(loginPage, sendMsgRequest);
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

}
