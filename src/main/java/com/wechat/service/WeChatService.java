package com.wechat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wechat.dao.GroupRobotDao;
import com.wechat.dao.RobotDao;
import com.wechat.po.GroupRobot;
import com.wechat.po.Robot;
import com.wechat.po.wechat.AddMsgListPO;
import com.wechat.po.wechat.BatchContactPO;
import com.wechat.po.wechat.LoginPagePO;
import com.wechat.po.wechat.MemberPO;
import com.wechat.po.wechat.UserPO;
import com.wechat.po.wechat.WebWxSyncPO;
import com.wechat.request.SendMsgRequest;
import com.wechat.util.ApiUtil;
import com.wechat.util.WeChatUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class WeChatService {

    RobotDao robotDao;
    GroupRobotDao groupRobotDao;
    RedisService redisService;

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

        String sendMsg;
        String jsonUser = redisService.get("WECHATUSER", String.class);
        UserPO user = (UserPO) JSONObject.toBean(JSONObject.fromObject(jsonUser), UserPO.class);
        String userName = user.getUserName();
        String loginPageJson = redisService.get(WeChatUtil.LOGINPAGE, String.class);
        LoginPagePO loginPagePO = (LoginPagePO) JSONObject
            .toBean(JSONObject.fromObject(loginPageJson), LoginPagePO.class);

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

                            if (content.contains("天气") && groupRobotIds.contains(1L)) {
                                sendMsg = ApiUtil.getSimpleWeadther();
                            } else if (content.contains("笑话") && groupRobotIds.contains(2L)) {
                                sendMsg = ApiUtil.getRandJoke();
                            } else if (content.contains("历史上的今天") && groupRobotIds.contains(3L)) {
                                sendMsg = ApiUtil.getTodayHistory();
                            } else if (content.contains("help")) {
                                sendMsg = "支持指令：笑话、天气、历史上的今天";
                            } else {
                                sendMsg = "试试@我发送help获取指令吧~";
                            }
                            //发送
                            SendMsgRequest sendMsgRequest = WeChatUtil
                                .getSendMsgRequest(sendMsg, userName, addMsgListPO.getFromUserName());
                            WeChatUtil.setSendMsg(loginPagePO, sendMsgRequest);
                        }
                    }
                }
            } else if (addMsgListPO.getFromUserName().contains("@")){
                if (content.contains("天气")) {
                    sendMsg = ApiUtil.getSimpleWeadther();
                } else if (content.contains("笑话")) {
                    sendMsg = ApiUtil.getRandJoke();
                } else if (content.contains("历史上的今天")) {
                    sendMsg = ApiUtil.getTodayHistory();
                } else if (content.contains("help")) {
                    sendMsg = "支持指令：笑话、天气、历史上的今天";
                } else {
                    sendMsg = "";
                }
                //发送
                SendMsgRequest sendMsgRequest = WeChatUtil
                    .getSendMsgRequest(sendMsg, userName, addMsgListPO.getFromUserName());
                WeChatUtil.setSendMsg(loginPagePO, sendMsgRequest);
            }
        }
    }

}
