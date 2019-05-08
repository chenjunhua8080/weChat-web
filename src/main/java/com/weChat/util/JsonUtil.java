package com.weChat.util;

import java.util.Map;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JavaIdentifierTransformer;
import net.sf.json.util.PropertySetStrategy;

public class JsonUtil {

    public static Object toBean(JSONObject jsonObject,Class rootClass, Map<String,Class> childClass){
        JsonConfig config = new JsonConfig();
        config.setRootClass(rootClass);
        config.setJavaIdentifierTransformer(new JavaIdentifierTransformer() {
            @Override
            public String transformToJavaIdentifier(String str) {
                if (str.contains("MP")) {
                    return str;
                }
                char[] chars = str.toCharArray();
                chars[0] = Character.toLowerCase(chars[0]);
                return new String(chars);
            }
        });
        config.setPropertySetStrategy(new PropertySetStrategy() {
            @Override
            public void setProperty(Object o, String s, Object o1) throws JSONException {
                try {
                    PropertySetStrategy.DEFAULT.setProperty(o, s, o1);
                } catch (JSONException e) {
                }
            }
        });
        return JSONObject.toBean(jsonObject,config);
    }

}
