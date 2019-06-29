package com.wechat.global;

import com.wechat.po.User;

public class UserContext {

    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public static User getUser() {
        return userThreadLocal.get();
    }

    public static String getUserName() {
        return getUser().getName();
    }

    public static Integer getUserId() {
        return getUser().getId();
    }

    public static void setUser(User user) {
        userThreadLocal.set(user);
    }

    public static void clearUser() {
        userThreadLocal.remove();
    }

}
