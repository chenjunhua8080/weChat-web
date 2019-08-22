package com.wechat.po;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class NowPlayingPO {

    private String name;
    private String img;
    private String actors;
    private double score;
    private String url;

}
