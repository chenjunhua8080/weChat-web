package com.wechat.request;

import java.util.List;
import lombok.Data;

@Data
public class addRobotToGroupRequest {

    private List<Long> robotIds;
    private String groupId;

}
