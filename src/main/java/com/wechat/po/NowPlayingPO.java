package com.wechat.po;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class NowPlayingPO {

    private String id;
    private String name;
    private String img;
    private String actors;
    private double score;
    private String url;
    private String desc;
    private List<String> comments;

}
