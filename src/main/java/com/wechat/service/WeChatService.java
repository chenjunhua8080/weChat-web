package com.wechat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wechat.dao.GroupRobotDao;
import com.wechat.dao.RobotDao;
import com.wechat.po.GroupRobot;
import com.wechat.po.NowPlayingPO;
import com.wechat.po.Robot;
import com.wechat.po.wechat.AddMsgListPO;
import com.wechat.po.wechat.BatchContactPO;
import com.wechat.po.wechat.LoginPagePO;
import com.wechat.po.wechat.MemberPO;
import com.wechat.po.wechat.UserPO;
import com.wechat.po.wechat.WebWxSyncPO;
import com.wechat.request.SendMsgRequest;
import com.wechat.util.ApiUtil;
import com.wechat.util.HttpsUtil;
import com.wechat.util.WeChatUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
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
        LoginPagePO loginPagePO = getLoginPage();

        List<AddMsgListPO> msgList = webWxSyncPO.getAddMsgList();
        for (AddMsgListPO addMsgListPO : msgList) {
            String content = addMsgListPO.getContent();

            //如果消息来自于群聊：判断是否@我，是@我再进行处理
            //个人消息直接处理
            if (addMsgListPO.getFromUserName().contains("@@")) {

                List<Map<String, String>> list = new ArrayList<>();
                Map<String, String> map = new HashMap<>();
                map.put("UserName", addMsgListPO.getFromUserName());
                map.put("EncryChatRoomId", "");
                list.add(map);

                Map<String, Object> param = new HashMap<>();
                param.put("pass_ticket", loginPagePO.getPassTicket());
                param.put("wxsid", loginPagePO.getWxSid());
                param.put("skey", loginPagePO.getSKey());
                param.put("wxuin", loginPagePO.getWxUin());
                param.put("count", list.size());
                param.put("list", list);
                //获取群聊详情
                BatchContactPO batchContactPO = WeChatUtil.batchGetContact(param);

                //获取群聊设置的机器
                List<Long> groupRobotIds = getGroupRobotIds(addMsgListPO.getFromUserName());

                List<MemberPO> memberList = batchContactPO.getContactList().get(0).getMemberList();
                for (MemberPO memberPO : memberList) {
                    if (memberPO.getUserName().equals(userName)) {
                        if ((!"".equals(memberPO.getNickName()) && content.contains("@" + memberPO.getNickName()))
                            || (!"".equals(memberPO.getDisplayName()) && content
                            .contains("@" + memberPO.getDisplayName()))) {
                            //处理文本消息
                            handleTextMsg(content, addMsgListPO.getFromUserName());
                        }
                    }
                }
            } else if (addMsgListPO.getToUserName().equals("filehelper")) {
                //处理文本消息
                handleTextMsg(content, addMsgListPO.getToUserName());
            } else if (userName.equals(addMsgListPO.getFromUserName())) {
                //处理文本消息
                handleTextMsg(content, addMsgListPO.getToUserName());
            } else if (addMsgListPO.getFromUserName().contains("@")) {
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
            String movie = nowPlaying.getName() + "/" + nowPlaying.getScore() + "/" + nowPlaying.getActors();
            sendMsg1(movie, toUser, loginPage);
            //发送图片
            String imgSrc = nowPlaying.getImg();
            File file = HttpsUtil.downFile(imgSrc);
            String mediaId = WeChatUtil.upload(loginPage, file);
            sendMsg3(mediaId, toUser, loginPage);
            return;
        } else {
            return;
        }
        msgText += "\n                                                  -- 小俊";

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

    public static void main(String[] args) throws IOException {
        Part[] parts = new Part[10];
        List<Part> list = new ArrayList<>();
        list.add(new StringPart("a", "1"));
        list.add(new StringPart("b", "2"));
        list.add(new StringPart("c", "3"));
        list.toArray(parts);
        for (Part part : parts) {
            System.out.println(part.toString());
        }
    }

}
