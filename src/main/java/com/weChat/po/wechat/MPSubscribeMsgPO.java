package com.weChat.po.wechat;

import java.util.List;
import lombok.Data;

@Data
public class MPSubscribeMsgPO {

    private String userName;
    private int MPArticleCount;
    private List<MPArticlePO> MPArticleList;
    private long time;
    private String nickName;
}
