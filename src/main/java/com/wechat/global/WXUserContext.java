package com.wechat.global;

import com.wechat.po.wechat.UserPO;

public class WXUserContext {

    private static ThreadLocal<UserPO> userThreadLocal = new ThreadLocal<>();

    public static UserPO getUser() {
        return userThreadLocal.get();
    }

    public static String getUserName() {
        return getUser().getNickName();
    }

    public static String getUserId() {
        return getUser().getUserName();
    }

    public static void setUser(UserPO user) {
        userThreadLocal.set(user);
    }

    public static void clearUser() {
        userThreadLocal.remove();
    }

}
