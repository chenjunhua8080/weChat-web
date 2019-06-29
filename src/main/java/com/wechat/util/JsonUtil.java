package com.wechat.util;

import java.util.Map;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JavaIdentifierTransformer;
import net.sf.json.util.PropertySetStrategy;

public class JsonUtil {

    public static Object toBean(JSONObject jsonObject, Class rootClass,
        String[] ignoreLowercase, Map<String, Class> childClass) {
        JsonConfig config = new JsonConfig();

        //转换对象
        config.setRootClass(rootClass);

        //首字母小写
        config.setJavaIdentifierTransformer(new JavaIdentifierTransformer() {
            @Override
            public String transformToJavaIdentifier(String str) {
                if (ignoreLowercase.length > 0) {
                    for (String item : ignoreLowercase) {
                        if (str.indexOf(item) == 0) {
                            return str;
                        }
                    }
                }
                char[] chars = str.toCharArray();
                chars[0] = Character.toLowerCase(chars[0]);
                return new String(chars);
            }
        });

        //对象里没有的属性忽略
        config.setPropertySetStrategy(new PropertySetStrategy() {
            @Override
            public void setProperty(Object o, String s, Object o1) throws JSONException {
                try {
                    PropertySetStrategy.DEFAULT.setProperty(o, s, o1);
                } catch (JSONException e) {
                }
            }
        });

        //自定义的list声明
        if (childClass != null) {
            config.setClassMap(childClass);
        }

        return JSONObject.toBean(jsonObject, config);
    }

}
