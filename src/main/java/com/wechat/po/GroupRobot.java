package com.wechat.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("group_robot")
public class GroupRobot implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String groupId;

    private Long robotId;

}