package com.wechat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wechat.dao.GroupRobotDao;
import com.wechat.dao.RobotDao;
import com.wechat.po.GroupRobot;
import com.wechat.po.Robot;
import com.wechat.po.wechat.AddMsgListPO;
import com.wechat.po.wechat.LoginPagePO;
import com.wechat.po.wechat.UserPO;
import com.wechat.po.wechat.WebWxSyncPO;
import com.wechat.request.SendMsgRequest;
import com.wechat.util.ApiUtil;
import com.wechat.util.WeChatUtil;
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
        List<AddMsgListPO> msgList = webWxSyncPO.getAddMsgList();
        for (AddMsgListPO addMsgListPO : msgList) {
            String content = addMsgListPO.getContent();
            List<Long> groupRobotIds = getGroupRobotIds(addMsgListPO.getFromUserName());
            if (content.contains(userName)) {
                if (content.contains("天气") && groupRobotIds.contains(1)) {
                    sendMsg = ApiUtil.getSimpleWeadther();
                } else if (content.contains("笑话") && groupRobotIds.contains(2)) {
                    sendMsg = ApiUtil.getRandJoke();
                } else if (content.contains("历史上的今天") && groupRobotIds.contains(3)) {
                    sendMsg = ApiUtil.getTodayHistory();
                } else {
                    sendMsg = "我不太懂你说什么~";
                }
                //发送
                LoginPagePO loginPagePO = redisService.get("loginPagePO", LoginPagePO.class);
                SendMsgRequest sendMsgRequest = WeChatUtil
                    .getSendMsgRequest(sendMsg, userName, addMsgListPO.getFromUserName());
                WeChatUtil.setSendMsg(loginPagePO, sendMsgRequest);
            }
        }
    }

}
