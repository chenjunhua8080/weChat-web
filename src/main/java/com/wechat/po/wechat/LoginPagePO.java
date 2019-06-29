package com.wechat.po.wechat;

import lombok.Data;

@Data
public class LoginPagePO {

    /*
     * <error>
     * <ret>0</ret>
     * <message></message>
     * <skey>@crypt_253d2949_b195b14efa911d623d9eae272cebd068</skey>
     * <wxsid>MqHSJVdEym+yvsiP</wxsid>
     * <wxuin>3162028971</wxuin>
     * <pass_ticket>W6hDdkay6sqO8qdGja5/8xPzGEJuC4lvSwCQ1z1+buigRfdinyjQJxfbGInoAI4c</pass_ticket>
     * <isgrayscale>1</isgrayscale>
     * </error>
     */
    private int ret;
    private String message;
    private String sKey;
    private String wxSid;
    private String wxUin;
    private String passTicket;
    private int isGrayscale;

    //cookie字段
    private String webwx_data_ticket;
    private String webwx_auth_ticket;
    private long wxloadtime;
    private String webwxuvid;

}
