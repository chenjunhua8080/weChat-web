package com.wechat.util;

import java.io.InputStream;
import lombok.Data;
import net.sf.json.JSONArray;

@Data
public class StreamAndHeaders {

    private InputStream body;

    private JSONArray headers;

}
