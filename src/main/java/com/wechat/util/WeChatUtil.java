package com.wechat.util;

import com.wechat.global.GlobalConfig;
import com.wechat.global.WXUserContext;
import com.wechat.po.response.SendMsgResponse;
import com.wechat.po.wechat.AddMsgListPO;
import com.wechat.po.wechat.BatchContactPO;
import com.wechat.po.wechat.ContactListPO;
import com.wechat.po.wechat.ContactPO;
import com.wechat.po.wechat.InitPO;
import com.wechat.po.wechat.LoginPagePO;
import com.wechat.po.wechat.MPArticlePO;
import com.wechat.po.wechat.MPSubscribeMsgPO;
import com.wechat.po.wechat.MemberPO;
import com.wechat.po.wechat.SyncKeyItemPO;
import com.wechat.po.wechat.SyncKeyPO;
import com.wechat.po.wechat.WebWxSyncPO;
import com.wechat.request.SendMsgRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ArrayUtils;

/**
 * web微信用到的API<br/>
 *
 * <b>有个问题，结果返回到页面时候首字母大写的会自动转成小写</b>
 *
 * @author 陈俊华
 * @date 2018年1月19日
 */
@Slf4j
public final class WeChatUtil {

    public static String uin = "3162028971";
    public final static String LOGINPAGE = "loginPage";
    public final static String USERAVATAR = "userAvatar";

    /**
     * 退登
     */
    private final static String logout = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxlogout";

    /**
     * 免扫码
     */
    private final static String notScanLogin = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxpushloginurl";

    /**
     * 1.获取二维码uuid
     */
    private final static String jsLogin = "https://login.wx2.qq.com/jslogin";

    /**
     * 2.根据uuid生成二维码
     */
    public final static String qrCode = "https://login.weixin.qq.com/qrcode/UUID";

    /**
     * 3. 等待登录（参考方法 waitForLogin）这里是微信确认登录 method GET params tip : 1:未扫描 0:已扫描 uuid : 获取到的uuid _ : 时间戳
     * 返回数据(String):window.code=xxx;408,201,200
     */
    private final static String waitForLogin = "https://login.wx2.qq.com/cgi-bin/mmwebwx-bin/login";

    /**
     * 4. 登录获取Cookie（参考方法 login） method GET params ticket:xxx uuid:xxx lang:xxx scan:扫码成功后返回的时间戳（s） fun:new version:v2
     * 返回数据(XML)：解析可以得到：skey、sid、uin、pass_ticket的值。
     */
    private final static String loginPage = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage";

    /**
     * 5. 微信初始化（参考方法 wxInit） method POST params BaseRequest: { DeviceID:”xxx”, Sid: “xxx”, Skey: “xxx”, Uin: “xxx”, }
     * PS:DeviceID值的由来：e+15位随机数，JS中的实现如下： https://wx2.qq.com 报错1100
     */
    private final static String init = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxinit";

    /**
     * 6. 开启微信状态通知（参考方法 wxStatusNotify） method POST data JSON params BaseRequest: { ClientMsgId:时间戳（ms） Code:3
     * FromUserName:”自己的ID” ToUserName:”自己的ID” }
     */
    private final static String statusNotify = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxstatusnotify";

    /**
     * 7. 获取联系人列表（参考方法 getContact） method post params lang=zh_CN pass_ticket=xxx r=xxx seq=0 skey=xxx
     */
    private final static String getContact = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxgetcontact";

    /**
     * 8. 分批获取联系人method POST params BaseRequest: { DeviceID:”xxx” Sid:”xxx” Skey:”xxx” Uin:xxx } Count:4 List: [
     * 0:{UserName: “xxx”, EncryChatRoomId: “”} 1:{UserName: “xxx”, ChatRoomId: “”} … ]
     */
    private final static String batchGetContact = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxbatchgetcontact";

    /**
     * 9. 同步刷新 r: 时间戳 sid: xxx uin: xxx skey: xxx deviceid: xxx synckey: xxx _: 时间戳 返回：retcode: 0 正常 1100 失败/登出微信
     * selector: 0 正常 2 新的消息 7 进入/离开聊天界面
     */
    private final static String syncCheck = "https://webpush.wx2.qq.com/cgi-bin/mmwebwx-bin/synccheck";

    /**
     * 10. web同步刷新
     */
    private final static String webWxSync = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxsync";

    /**
     * 10. 发送消息（参考方法 webwxsendmsg） method POST params {"BaseRequest":{"Uin":3162028971,"Sid":"4y0mXxNmD8iFhOk+","Skey":"@crypt_253d2949_dbcea1e6456c79d8368d229ccbcf8d0a","DeviceID":"e624924664349023"},
     * "Msg":{"Type":1,"Content":"发送了，就不会有自己的？","FromUserName":"@217c1bd243e7ba360ba8f0e741fe0d0237a2a7b64905ea3e93f13695500b9262","ToUserName":"@@408297956eda7f2617f0ccb2e11a75a939054d45f259c5bd75a4b87d3d0afdd3","LocalID":"15583358878040410","ClientMsgId":"15583358878040410"},
     * "Scene":0}
     *
     * 17位时间戳
     */
    private final static String sendTextMsg = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxsendmsg";
    /**
     * ?fun=async&f=json&pass_ticket=Xe1ag%252Fg7%252FjeLNkQxF5S4x%252FJ0XwmgBWsTObblz5zC1l6yi3VqGPWZxKKmG03oxKRm
     */
    private final static String sendImgMsg = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxsendmsgimg";


    /**
     * 拉群 {"AddMemberList":"@a32d253baca68aa9b51f7b656fde81d73057f3ac453e0c9e4477fa8ab13b48f2",
     * "ChatRoomName":"@@408297956eda7f2617f0ccb2e11a75a939054d45f259c5bd75a4b87d3d0afdd3",
     * "BaseRequest":{"Uin":3162028971,"Sid":"4y0mXxNmD8iFhOk+","Skey":"@crypt_253d2949_dbcea1e6456c79d8368d229ccbcf8d0a","DeviceID":"e414552180563547"}}
     */
    private final static String chatRoom = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxupdatechatroom?fun=addmember&lang=zh_CN";

    /**
     * 上传文件POST 参数:f=json type: text/plain ---- result:{ "BaseResponse": { "Ret": 0, "ErrMsg": "" } ,
     * "MediaId":"@crypt_133f8e05_5723c9ea7868f4d236f818cd7f0fa27efdc9ca018fbc36930caf99069cad9680bd38ab9c636e1ad5d048ba6a003c82f193c66460f97f0bb922d09134e1a7b5bbf40352ae0b80a34b72d8c9fbc31c3d753f7babe16d20cea53dac271bfaae1f40771d0e129b1e9271446baeeaadae8705823f1334aa4168d442ed62b80df12c95d0ec083cd3beb8f3d3ce9e39aa26e586ee57afc7eb63aedcd2a7dc5919a70d8cbcb7549534795f9bad20c7988d945041207c3d6002b14fc4f5c28cdcc18aa48ea06efe4600f37cea743dcc57201641b4b71f65d5a25a26faddce7da4a419fa99e7f09e961955062f186946b8b5bfc695e9487906fe2fbd15f8daabfe586999b41f2986bc05c824cf083ef2c80389447c1017a209ca9b39f5aafc5ba76fdb2971f34a44ca3fe9bd3dfdafaa6962695baef80adcba88def86cc1cff34daa1d336d",
     * "StartPos": 7346, "CDNThumbImgHeight": 100, "CDNThumbImgWidth": 100, "EncryFileName":
     * "20180213181717%5FXHkGm%2Ejpeg" }
     */
    private final static String webwxuploadmedia = "https://file.wx2.qq.com/cgi-bin/mmwebwx-bin/webwxuploadmedia";

    /**
     * 发送图片 参数：?fun=async&f=json&pass_ticket=EQb1FkLM55MlS0jeE6ctuh7fwopT9qizsAKij%252BLz%252FtEZMtiEbtl5ZMvdIZ3Dh5h1
     * body:{"BaseRequest":{"Uin":3162028971,"Sid":"Ylc2ujBUMsyWNCJ9","Skey":"@crypt_253d2949_290d49c153b27e9f79972d1e3d24daa7","DeviceID":"e155869910696750"},
     * "Msg":{"Type":3,"MediaId":"@22222","Content":"","FromUserName":"@aa3f7272db30d27fabcb924ec8eafc3e7e6aea992fa57c69701e7e4c30356db7","ToUserName":"filehelper","LocalID":"15658606819560821","ClientMsgId":"15658606819560821"},"Scene":0}
     * result:{ "BaseResponse": { "Ret": 0, "ErrMsg": "" } , "MsgID": "209192978277204989",
     * "LocalID":"15658606819560821" }
     */
    private final static String webwxsendmsgimg = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxsendmsgimg";

    /**
     * 发送表情 参数：?fun=async&f=json&pass_ticket=EQb1FkLM55MlS0jeE6ctuh7fwopT9qizsAKij%252BLz%252FtEZMtiEbtl5ZMvdIZ3Dh5h1 {
     * "BaseRequest": { "Uin": 3162028971, "Sid": "Zh75lVx/qTuFBJFX", "Skey": "@crypt_253d2949_eaf1cc8eaffb76df419517b649ac19ac",
     * "DeviceID": "e940837190721168" }, "Msg": { "Type": 47, "EmojiFlag": 2, "MediaId":
     * "@crypt_f511bdc8_454e4c4a5b893e02fd0811b0d895a5a263187690bb1830100c2a5962f63ea969b30c14aef0d9652ef03c521a7d00d8d6cd1637a42baca53772495102dbcc92aeb3cf5d68af35a2843c053651e83a517059e181a60497809533bcc20a4dc96d5c70db8a06d0437c291f73242bb804f3a82bf388de82485d1646c8cbe8cf27221c684a9526c08ee669cfb658ab26fe99a678885bc96bb4b6ecf2e83700d5046b4b5efccc72c2250046a526e4356a20801ad14d2d56951f37e41f6ff3b001326cba99d361cfca996e3dce28a57ef706218b08b63d689cff9edca1da393659e93e02f8e1b9d3f1a8af925dbd1d5d7e78fc2588a7eb0c44c33aa850c2734cb6b092dd0eeb5b3d92c0014a0160580b905e47951d8d63131c3eda5e7d1cccb5d7a9a146d6ad0a10e69a1dda7d9dec35a27c48e3",
     * "FromUserName": "@66dc48f47ea0a6a6d34c83915d8f4a9384eb6a5e62e82e34cc581c0bb62fe89b", "ToUserName": "filehelper",
     * "LocalID": "15682670956170127", "ClientMsgId": "15682670956170127" }, "Scene": 0 }
     */
    private final static String webwxsendemoticon = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxsendemoticon";

    /**
     * 微信字段转JsonBean时，处理的特殊大小写字段
     */
    public static String[] ignoreLowercase = {"MP", "PY"};
    /**
     * 联系人为订阅号公众号等特殊标识
     */
    public static String[] mpKeyWord = {"gh_", "mcd"};

    /**
     * 1. 获取UUID（参考方法 getUUID） param : appid: wx782c26e4c19acffb redirect_uri: https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage
     * fun: new lang: zh_CN _: 1557284851564
     *
     * @return window.QRLogin.code = 200; window.QRLogin.uuid = "ActjS9HwSw==";
     */
    public static String jsLogin() throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("appid", "wx782c26e4c19acffb");
        query.put("fun", "new");
        query.put("lang", GlobalConfig.wechat_lang);
        query.put("redirect_uri", "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage");
        query.put("_", System.currentTimeMillis());
        JSONObject resp = JSONObject.fromObject("{" + HttpsUtil.get(jsLogin, query) + "}");
        return resp.getString("window.QRLogin.uuid");
    }

    /**
     * 2. 显示二维码，下载二维码到本地
     *
     * @return 图片名称
     */
    public static String getQrCode(String uuid) throws Exception {
        String resp = HttpsUtil.downFileToPath(qrCode.replace("UUID", uuid), "c://");
        return resp;
    }

    /**
     * 3. 等待登录，获取扫码结果
     *
     * @param uuid loginicon: true uuid: ActjS9HwSw== tip: 0 r: 1788016140 _: 1557284851575
     * @return window.code=400;
     *
     * window.code=408;
     *
     * window.code = 201; window.userAvatar = 'data:img/jpg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAoHBwgHBgoICAgLCgoLDhgQDg0NDh0VFhEYIx8lJCIfIiEmKzcvJik0KSEiMEExNDk7Pj4+JS5ESUM8SDc9Pjv/2wBDAQoLCw4NDhwQEBw7KCIoOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozv/wAARCACEAIQDASIAAhEBAxEB/8QAGwABAQACAwEAAAAAAAAAAAAAAAUDBgECBwT/xAAyEAACAgECAwYEBAcAAAAAAAAAAQIDEQQFEiExBhMiQVFhFBUjcSSRsfAlMoGhwdHx/8QAGQEBAAMBAQAAAAAAAAAAAAAAAAEDBAIF/8QAHhEBAQEAAgIDAQAAAAAAAAAAAAECAxESITFBYSL/2gAMAwEAAhEDEQA/APQgAY2U8gAAAAAAAPIAAAAA8gAAAADyAADyAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABw2km28JeRF1/anR6Pw1xlqLJfyRj1n9vb3G/6rNlegTxGUXZb7xXJR/q/wBDWtuitU7t0lV9STapT8opY/0ca12zcvLZfHKlHtTqFqrbrtIpQpxGUePlW355x+Zc2/fNLuFroxKm9JPu545r1TXJo1LYod9pLtTb4p6ib48rk8ftmHRUTnVZTTZjV6Kzhrtz5Zyl9uo8/dl+lOOfc6t99vRQT9k3JbptsL2sWLwWL0kigdt0ss7gAAkAAAAAAAAAAAAx3OyNUnSoOzouOWIr3b9ANZ39Te7amEW1KzSwUG+i5zX6o+Da41/JaY58LrfFj+5U3tzq1dUNTH8ZR4LODnCcJYeU/br+ZI29x0TellbXw2Tboinl464ZTyz5jDyTrbjs+38qjl5ipy4ftn/p12t/xPcorCXeJ4XrzONknGnbb+NcLqtlxrPmYNJbbpdtnbGK+J1trdMEst5/bIs7uv1RL6w2DsU269w5eD4huOOnn0NnJuwba9s2qqia+o1mfs/QpF70eLNziSgACwAAAAAAAAAAAAAdFVBW97w5s4eHifN49SHvm26WLhdwRjGU8y9n1yl69S+YNRo6dVGMbU2ovOE8EWdxzrPlOkWXZem2Li7a1CclKTjDnZ58+f8Akdn47ZfrNVLTwssv00lDvbfTp4V0iuTL0a4RgoJcksHTT6TTaRSWnorqUnmXBFLKEknwnOOPMv8APv6/GYAEpAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAf/Z';
     *
     * window.code=200; window.redirect_uri="https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=A3ytELDXEHJ9WmEzyBjawS_x@qrticket_0&uuid=AeioKQv69A==&lang=zh_CN&scan=1557286172";
     */
    public static Map<String, Object> waitForLogin(int tip, String uuid) throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("loginicon", true);
        query.put("uuid", uuid);
        query.put("tip", tip);
        query.put("_", System.currentTimeMillis());
        JSONObject resp = JSONObject.fromObject("{" + HttpsUtil.get(waitForLogin, query) + "}");
        int respCode = resp.getInt("window.code");
        Map<String, Object> result = new HashMap<>();
        result.put("code", resp.getInt("window.code"));
        switch (respCode) {
            case 200:
                log.info("登录成功");
                String redirect = resp.getString("window.redirect_uri");
                result.put("uri", redirect);
                String[] params = redirect.substring(redirect.indexOf("?") + 1).split("&");
                String[] param;
                for (String item : params) {
                    param = item.split("=");
                    //uuid有两个==，暂时不特殊处理直接后面加==
                    if ("uuid".equals(param[0])) {
                        result.put(param[0], param[1] + "==");
                    } else {
                        result.put(param[0], param[1]);
                    }
                }
                break;
            case 201:
                log.info("扫描成功，请确认登录");
                result.put("userAvatar", resp.getString("window.userAvatar"));
                break;
            case 408:
                log.info("登陆超时: {}", resp.toString());
                break;
            default:
                log.info("发现错误：{}", resp.toString());
                break;
        }
        return result;
    }

    /**
     * 4.初始化登录页面，直接用从waitForLogin返回的uri请求
     *
     * ticket: A3ytELDXEHJ9WmEzyBjawS_x@qrticket_0 uuid: AeioKQv69A== lang: zh_CN scan: 1557286172 fun: new version: v2
     * lang: zh_CN
     *
     * @return 重要的cookie： mm_lang	zh_CN webwx_auth_ticket CIsBEOTzr3wagAEVQ56NbJ0AViYB9E0nqg0NSD24/60LWp+GNBCyVzrjgfait2R8abbe9V/JU7Qh
     * webwx_data_ticket gSfvfQbvNCJnYqprUqJzhjdt wxloadtime 1557373727 wxsid gJeuoFQiQY5LBzlD wxuin 3162028971
     *
     * 主体：
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
    public static LoginPagePO loginPage(String ticket, String uuid, String scan) throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("ticket", ticket);
        query.put("uuid", uuid);
        query.put("scan", scan);
        query.put("lang", GlobalConfig.wechat_lang);
        query.put("fun", "new");
        query.put("version", 2);

        StreamAndHeaders respObject = HttpsUtil.getReturnHeadAndBody(loginPage, query);
        Map<String, Object> map = XmlUtil.parseXml(respObject.getBody());
        if (Integer.parseInt(map.get("ret").toString()) != 0) {
            log.info("登录失败：{}", map.get("message"));
        }
        //主体字段
        LoginPagePO loginPagePO = new LoginPagePO();
        loginPagePO.setRet(Integer.parseInt(map.get("ret").toString()));
        loginPagePO.setMessage(map.get("message").toString());
        loginPagePO.setSKey(map.get("skey").toString());
        loginPagePO.setWxSid(map.get("wxsid").toString());
        loginPagePO.setWxUin(map.get("wxuin").toString());
        loginPagePO.setPassTicket(map.get("pass_ticket").toString());
        loginPagePO.setIsGrayscale(Integer.parseInt(map.get("isgrayscale").toString()));
        //cookie字段
        JSONArray headers = respObject.getHeaders();
        for (int i = 0; i < headers.size(); i++) {
            JSONObject head = headers.getJSONObject(i);
            String headString = head.getString("value");
            log.info("head:{}", headString);
            if (headString.contains(";")) {
                String sub = headString.substring(0, headString.indexOf(";"));
                //不能用=分割可能含有=
                if (sub.contains("=")) {
                    String key = sub.substring(0, sub.indexOf("="));
                    String value = sub.substring(sub.indexOf("=") + 1);
                    switch (key) {
                        case "webwx_data_ticket":
                            loginPagePO.setWebwx_data_ticket(value);
                            break;
                        case "webwx_auth_ticket":
                            loginPagePO.setWebwx_auth_ticket(value);
                            break;
                        case "wxloadtime":
                            loginPagePO.setWxloadtime(Long.parseLong(value));
                            break;
                        case "webwxuvid":
                            loginPagePO.setWebwxuvid(value);
                            break;
                    }
                }
            }
        }
        return loginPagePO;
    }

    /**
     * 5.初始化数据
     *
     * @param passTicket r: 1788276847 lang: zh_CN pass_ticket: W6hDdkay6sqO8qdGja5%2F8xPzGEJuC4lvSwCQ1z1%2BbuigRfdinyjQJxfbGInoAI4c
     *
     * head: BaseRequest: {Uin: "3162028971", Sid: "MqHSJVdEym+yvsiP", Skey: "@crypt_253d2949_b195b14efa911d623d9eae272cebd068",…}
     * @return BaseResponse: {Ret: 0, ErrMsg: ""} ChatSet: "filehelper,@@1381ee5f36f7d66f012eb8234d1436f66f67808c18ad821edf4d9987341df854,@d407a503d84fdd9118f1bf291eb11765,@5d955c120c989650af9367747e648fcabbb68a9e852182f868f7876728410129,weixin,filehelper,@3bc67fd76d0485bda7bbe211a2949591,@@468d5e7ec42a5a6b8cf504be959f8a761f970fd42aed3447820f63bbb69887fe,@@d381e5b4a8429798c24dd2abf4fa567e53646bcfff4fb214ecd8fead9c21ad43,@d9252832c94aebd2ca40e21db467fe88,@@b5d0d1a17bc2d93f0b33693c0cf7a1b8deb19312e860a65e446f6dd8fd703c60,@@1623c80424561b674d2fdd7d238097871856af08ef91ce991036fc78f84fa38e,@@e5b0924d8093361a2a7ee29875e71a15162f7f0cb84432e88025d699e33c1bb5,@52b5c3b9e05d1044f3969f320c7085d1,@dcd76f8d0444ab9e187e07744b1a0338,@7c426167cb95f8851b90d08bb1e44de7,@b6b1363e73e12d39b45ae59948735bcf,@8c072439020c1496da5cb86cb9ddfa3b,@@f065cc65634474261c409574898801ef53e93ea1c6809cadac836dd3599cf57c,fmessage,newsapp,@@636dc67487ec9f531f7fbb3a4d0f905813acf0e5a33449c62fb42104fd560681,@6390c6a17e6ed443388601341df916f0285f14ebe19e4c3d397e818c26568d5e,@0d5d370088593da99f81721e6b8bbecad05e5f78764bdde3bcb100ced5eba0c0,@@01c40efbdbec8d0220701a6711ad82760542c576407af76ed6b0cf34ab1af06e,@66cefb36cdc57f3ea4783d226a0a56c94b2309f14cce2f2e07cc4851a61949a2,"
     * ClickReportInterval: 600000 ClientVersion: 654312506 ContactList: [{Uin: 0, UserName: "filehelper", NickName:
     * "æ–‡ä»¶ä¼ è¾“åŠ©æ‰‹",…},…] Count: 11 GrayScale: 1 InviteStartCount: 40 MPSubscribeMsgCount: 7 MPSubscribeMsgList:
     * [{UserName: "@d407a503d84fdd9118f1bf291eb11765", MPArticleCount: 2,…},…] SKey:
     * "@crypt_253d2949_b195b14efa911d623d9eae272cebd068" SyncKey: {Count: 4, List: [{Key: 1, Val: 661091461}, {Key: 2,
     * Val: 661091587}, {Key: 3, Val: 661091588},…]} SystemTime: 1557286175 User: {Uin: 3162028971, UserName:
     * "@dbc955536b0b80b8e9154560f9cc8871fcdf150c087335212ef9219b7cd28764",…}
     */
    public static InitPO init(String sid, String sKey, String uin, String passTicket) throws Exception {
        Map<String, Object> baseRequest = new HashMap<>();
        baseRequest.put("DeviceID", getDeviceId());
        baseRequest.put("Sid", sid);
        baseRequest.put("Skey", sKey);
        baseRequest.put("Uin", uin);
        Map<String, Object> body = new HashMap<>();
        body.put("BaseRequest", baseRequest);
        Map<String, Object> query = new HashMap<>();
        query.put("r", System.currentTimeMillis());
        query.put("lang", GlobalConfig.wechat_lang);
        query.put("pass_ticket", passTicket);
        JSONObject resp = JSONObject.fromObject(HttpsUtil.post(init, query, body));

        Map<String, Class> childClass = new HashMap<>();
        childClass.put("contactList", ContactPO.class);
        childClass.put("list", SyncKeyItemPO.class);
        childClass.put("MPSubscribeMsgList", MPSubscribeMsgPO.class);
        childClass.put("MPArticleList", MPArticlePO.class);
        childClass.put("memberList", MemberPO.class);

        InitPO initPO = (InitPO) JsonUtil.toBean(resp, InitPO.class, ignoreLowercase, childClass);

        //保存微信登录用户
        WXUserContext.setUser(initPO.getUser());
        return initPO;
    }

    /**
     * 6.状态通知？
     *
     * @param args lang: zh_CN pass_ticket: W6hDdkay6sqO8qdGja5%2F8xPzGEJuC4lvSwCQ1z1%2BbuigRfdinyjQJxfbGInoAI4c
     *
     * head: BaseRequest: {Uin: 3162028971, Sid: "MqHSJVdEym+yvsiP", Skey: "@crypt_253d2949_b195b14efa911d623d9eae272cebd068",…}
     * ClientMsgId: 1557286176329 Code: 3 FromUserName: "@dbc955536b0b80b8e9154560f9cc8871fcdf150c087335212ef9219b7cd28764"
     * ToUserName: "@dbc955536b0b80b8e9154560f9cc8871fcdf150c087335212ef9219b7cd28764"
     * @return BaseResponse: {Ret: 0, ErrMsg: ""} MsgID: "7992382417837797668"
     */
    public static JSONObject statusNotify(Map<String, Object> args) throws Exception {
        Map<String, Object> baseRequest = new HashMap<>();
        baseRequest.put("DeviceID", getDeviceId());
        baseRequest.put("Sid", args.get("wxsid"));
        baseRequest.put("Skey", args.get("skey"));
        baseRequest.put("Uin", args.get("wxuin"));
        Map<String, Object> body = new HashMap<>();
        body.put("BaseRequest", baseRequest);

        Map<String, Object> query = new HashMap<>();
        query.put("ClientMsgId", System.currentTimeMillis());
        query.put("Code", 3);
        query.put("FromUserName", args.get("userName"));
        query.put("ToUserName", args.get("userName"));

        JSONObject resp = JSONObject.fromObject(HttpsUtil.post(getContact, query, body));
        return resp;
    }

    /**
     * 7.获取联系人
     *
     * @param sKey lang: zh_CN pass_ticket: W6hDdkay6sqO8qdGja5%2F8xPzGEJuC4lvSwCQ1z1%2BbuigRfdinyjQJxfbGInoAI4c r:
     * 1557286176616 seq: 0 skey: @crypt_253d2949_b195b14efa911d623d9eae272cebd068
     * @return BaseResponse: {Ret: 0, ErrMsg: ""} MemberCount: 436 MemberList: [{Uin: 0, UserName: "weixin", NickName:
     * "å¾®ä¿¡å›¢é˜Ÿ",…},…] Seq: 0
     */
    public static ContactListPO getContact(String passTicket, String sKey, String sid, String uin) throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("lang", GlobalConfig.wechat_lang);
        query.put("pass_ticket", passTicket);
        query.put("r", System.currentTimeMillis());
        query.put("seq", 0);
        query.put("skey", sKey);

        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", "wxsid=" + sid + ";wxuin=" + uin + ";");

        JSONObject resp = JSONObject.fromObject(HttpsUtil.get(getContact, query, headers));

        Map<String, Class> childClass = new HashMap<>();
        childClass.put("memberList", ContactPO.class);
        ContactListPO contactListPO = (ContactListPO) JsonUtil
            .toBean(resp, ContactListPO.class, ignoreLowercase, childClass);

        return contactListPO;
    }

    /**
     * 8.分批获取指定联系人
     *
     * 获取chatSet的userName 的详细信息，有些在init和getContact 都可能获取不到(比如最近没新信息，也没保存到通讯录的群组。也可能根据手机里微信的聊天列表)
     *
     * @param args type: ex r: 1557286176637 lang: zh_CN pass_ticket: W6hDdkay6sqO8qdGja5%2F8xPzGEJuC4lvSwCQ1z1%2BbuigRfdinyjQJxfbGInoAI4c
     *
     * head: BaseRequest: {Uin: 3162028971, Sid: "MqHSJVdEym+yvsiP", Skey: "@crypt_253d2949_b195b14efa911d623d9eae272cebd068",…}
     * Count: 17 List: [1: {UserName: "@52b5c3b9e05d1044f3969f320c7085d1", EncryChatRoomId: ""} 2: {UserName:
     * "@dcd76f8d0444ab9e187e07744b1a0338", EncryChatRoomId: ""},…]
     *
     * 备注：这里的list 取值于init.chatSet除去特殊值 和 对应init.ContactList里不是"KeyWord": "gh_"(公众号)的数据
     * @return BaseResponse: {Ret: 0, ErrMsg: ""} ContactList: [{Uin: 0, UserName: "@@01c40efbdbec8d0220701a6711ad82760542c576407af76ed6b0cf34ab1af06e",…},…]
     * Count: 17
     */
    public static BatchContactPO batchGetContact(Map<String, Object> args) throws Exception {
        Map<String, Object> baseRequest = new HashMap<>();
        baseRequest.put("DeviceID", getDeviceId());
        baseRequest.put("Sid", args.get("wxsid"));
        baseRequest.put("Skey", args.get("skey"));
        baseRequest.put("Uin", args.get("wxuin"));
        Map<String, Object> body = new HashMap<>();
        body.put("BaseRequest", baseRequest);
        body.put("Count", args.get("count"));
        body.put("List", args.get("list"));

        Map<String, Object> query = new HashMap<>();
        query.put("type", "ex");
        query.put("r", System.currentTimeMillis());
        query.put("lang", GlobalConfig.wechat_lang);
        query.put("pass_ticket", args.get("pass_ticket"));
        JSONObject resp = JSONObject.fromObject(HttpsUtil.post(batchGetContact, query, body));

        Map<String, Class> childClass = new HashMap<>();
        childClass.put("contactList", ContactPO.class);
        childClass.put("memberList", MemberPO.class);
        BatchContactPO batchContactPO = (BatchContactPO) JsonUtil
            .toBean(resp, BatchContactPO.class, ignoreLowercase, childClass);
        return batchContactPO;
    }

    /**
     * 获取chatSet的userName 的详细信息，有些在init和getContact 都可能获取不到(比如最近没新信息，也没保存到通讯录的群组。也可能根据手机里微信的聊天列表) 这里的list
     * 取值于init.chatSet除去特殊值 和 对应init.ContactList里不是"KeyWord": "gh_"(公众号)的数据
     */
    public static InitPO batchGetContact(InitPO initPO, LoginPagePO loginPagePO) throws Exception {
        String chatSet = initPO.getChatSet();
        log.info("chatSet:{}", chatSet);

        List<ContactPO> contactList = initPO.getContactList();
        //除去contactList本身包含的公众号
        Iterator<ContactPO> iterator = contactList.iterator();
        while (iterator.hasNext()) {
            ContactPO contactPO = iterator.next();
            if (ArrayUtils.contains(mpKeyWord, contactPO.getKeyWord())) {
                iterator.remove();
                log.info("remove gh_ {} in initPO.getContactList()", contactPO.getNickName());
            }
        }
        //将chatSet中的联系人添加到contactList
        String[] split = chatSet.split(",");
        List<Map<String, String>> list = new ArrayList<>();
        for (String item : split) {
            if (item.contains("@")) {
                iterator = contactList.iterator();
                while (iterator.hasNext()) {
                    ContactPO contactPO = iterator.next();
                    if (item.equals(contactPO.getUserName())) {
                        //群聊的话，群成员在init没有信息，所以移除init里，在batchGetContact重新获取
                        iterator.remove();
//                        needAdd=false;
//                        break;
                    }
                }
                Map<String, String> map = new HashMap<>();
                map.put("UserName", item);
                map.put("EncryChatRoomId", "");
                list.add(map);
            }
        }
        Map<String, Object> param = new HashMap<>();
        param.put("pass_ticket", loginPagePO.getPassTicket());
        param.put("wxsid", loginPagePO.getWxSid());
        param.put("skey", loginPagePO.getSKey());
        param.put("wxuin", loginPagePO.getWxUin());
        param.put("count", list.size());
        param.put("list", list);
        BatchContactPO batchContactPO = batchGetContact(param);

        if (batchContactPO.getBaseResponse().getRet() == 0) {
            List<ContactPO> batchContactList = batchContactPO.getContactList();
            //除去contactList本身包含的公众号
            Iterator<ContactPO> iterator2 = batchContactList.iterator();
            while (iterator2.hasNext()) {
                ContactPO contactPO = iterator2.next();
                if (ArrayUtils.contains(mpKeyWord, contactPO.getKeyWord())) {
                    iterator2.remove();
                    log.info("remove gh_ {} in batchContactList", contactPO.getNickName());
                }
            }
            contactList.addAll(batchContactList);
            initPO.setCount(contactList.size());
        } else {
            log.info("batchGetContact错误：{}", batchContactPO);
        }

        return initPO;
    }

    /**
     * 9.同步刷新
     *
     * @param loginPagePO r: 1557286176625 skey: @crypt_253d2949_b195b14efa911d623d9eae272cebd068 sid: MqHSJVdEym+yvsiP
     * uin:3162028971 deviceid: e624739355264879 synckey: 1_661091461|2_661091587|3_661091588|1000_1557272238
     * synckey第一次从init的返回拿，后面从webwxsync拿 _: 1557284851581
     * @return window.synccheck={retcode:"0",selector:"2"}
     */
    public static JSONObject syncCheck(LoginPagePO loginPagePO, SyncKeyPO syncKeyPO) throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("r", System.currentTimeMillis());
        query.put("skey", loginPagePO.getSKey());
        query.put("sid", loginPagePO.getWxSid());
        query.put("uin", loginPagePO.getWxUin());
        query.put("deviceid", getDeviceId());

        List<SyncKeyItemPO> syncList = syncKeyPO.getList();//{Key,Val}
        StringBuilder sb = new StringBuilder();
        for (SyncKeyItemPO item : syncList) {
            sb.append(item.getKey());
            sb.append("_");
            sb.append(item.getVal());
            sb.append("|");
        }
        sb.delete(sb.lastIndexOf("|"), sb.length());
        query.put("synckey", sb.toString());
        query.put("_", System.currentTimeMillis());

        Map<String, String> headers = new HashMap<>();
        headers.put("cookie",
            "webwx_data_ticket=" + loginPagePO.getWebwx_data_ticket() + ";wxuin=" + loginPagePO.getWxUin() + ";");

        String respStr = HttpsUtil.get(syncCheck, query, headers);
        JSONObject resp = JSONObject.fromObject(respStr.substring(respStr.indexOf("{")));
        return resp;
    }

    /**
     * 10.web同步刷新
     *
     * @param loginPagePO sid: MqHSJVdEym yvsiP skey: @crypt_253d2949_b195b14efa911d623d9eae272cebd068 lang: zh_CN
     * pass_ticket: W6hDdkay6sqO8qdGja5%2F8xPzGEJuC4lvSwCQ1z1%2BbuigRfdinyjQJxfbGInoAI4c
     *
     * <b>head:</b><br/>
     * BaseRequest: {Uin: 3162028971, Sid: "MqHSJVdEym+yvsiP", Skey: "@crypt_253d2949_b195b14efa911d623d9eae272cebd068",…}
     * SyncKey: {Count: 4, List: [{Key: 1, Val: 661091461}, {Key: 2, Val: 661091587}, {Key: 3, Val: 661091588},…]} rr:
     * 1786951690
     * @return {BaseResponse: {Ret: 0, ErrMsg: ""}, AddMsgCount: 0, AddMsgListPO: [], ModContactCount: 0,…} AddMsgCount:
     * 0 AddMsgListPO: [] BaseResponse: {Ret: 0, ErrMsg: ""} ContinueFlag: 0 DelContactCount: 0 DelContactList: []
     * ModChatRoomMemberCount: 0 ModChatRoomMemberList: [] ModContactCount: 0 ModContactList: [] Profile: {BitFlag: 0,
     * UserName: {Buff: ""}, NickName: {Buff: ""}, BindUin: 0, BindEmail: {Buff: ""},…} SKey: "" SyncCheckKey: {Count:
     * 6, List: [{Key: 1, Val: 661091461}, {Key: 2, Val: 661091589}, {Key: 3, Val: 661091588},…]} SyncKey: {Count: 6,
     * List: [{Key: 1, Val: 661091461}, {Key: 2, Val: 661091589}, {Key: 3, Val: 661091588},…]}
     */
    public static WebWxSyncPO webWxSync(LoginPagePO loginPagePO, SyncKeyPO syncKeyPO) throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("sid", loginPagePO.getWxSid());
        query.put("skey", loginPagePO.getSKey());
        query.put("lang", GlobalConfig.wechat_lang);
        query.put("pass_ticket", loginPagePO.getPassTicket());

        Map<String, Object> baseRequest = new HashMap<>();
        baseRequest.put("DeviceID", getDeviceId());
        baseRequest.put("Sid", loginPagePO.getWxSid());
        baseRequest.put("Skey", loginPagePO.getSKey());
        baseRequest.put("Uin", loginPagePO.getWxUin());
        Map<String, Object> body = new HashMap<>();

        body.put("BaseRequest", baseRequest);
        body.put("rr", System.currentTimeMillis());

        Map<String, Object> syncKeyMap = new HashMap<>();
        syncKeyMap.put("Count", syncKeyPO.getCount());
        //不转会有类名SyncKeyItemPO, {SyncKeyItemPO(key=3, val=661093531)}
        List<Map<String, Object>> list = new ArrayList<>();
        for (SyncKeyItemPO item : syncKeyPO.getList()) {
            Map<String, Object> syncKeyItemMap = new HashMap<>();
            syncKeyItemMap.put("Key", item.getKey());
            syncKeyItemMap.put("Val", item.getVal());
            list.add(syncKeyItemMap);
        }
        syncKeyMap.put("List", list);
        body.put("SyncKey", syncKeyMap);

        JSONObject resp = JSONObject.fromObject(HttpsUtil.post(webWxSync, query, body));

        Map<String, Class> childClass = new HashMap<>();
        childClass.put("addMsgList", AddMsgListPO.class);
        childClass.put("list", SyncKeyItemPO.class);
        WebWxSyncPO webWxSyncPO = (WebWxSyncPO) JsonUtil.toBean(resp, WebWxSyncPO.class, ignoreLowercase, childClass);

        return webWxSyncPO;
    }

    public static SendMsgRequest getSendMsgRequest(int type, String content, String mediaId, String from, String to) {
        SendMsgRequest sendMsgRequest = new SendMsgRequest();
        sendMsgRequest.setType(type);
        sendMsgRequest.setContent(content);
        sendMsgRequest.setMediaId(mediaId);
        sendMsgRequest.setFromUserName(from);
        sendMsgRequest.setToUserName(to);
        return sendMsgRequest;
    }

    /**
     * 发送文本消息
     */
    public static SendMsgResponse sendTextMsg(LoginPagePO loginPagePO, SendMsgRequest msgRequest) {
        Map<String, Object> query = new HashMap<>();
        query.put("lang", GlobalConfig.wechat_lang);

        Map<String, Object> body = new HashMap<>();

        Map<String, Object> baseRequest = new HashMap<>();
        baseRequest.put("DeviceID", getDeviceId());
        baseRequest.put("Sid", loginPagePO.getWxSid());
        baseRequest.put("Skey", loginPagePO.getSKey());
        baseRequest.put("Uin", loginPagePO.getWxUin());

        Map<String, Object> msgMap = new HashMap<>();
        if (msgRequest.getType() == 3) {
            msgMap.put("MediaId", msgRequest.getMediaId());
        }
        msgMap.put("Type", msgRequest.getType());
        msgMap.put("Content", msgRequest.getContent());
        msgMap.put("FromUserName", msgRequest.getFromUserName());
        msgMap.put("ToUserName", msgRequest.getToUserName());
        long currentTimeMillis = System.currentTimeMillis();
        msgMap.put("LocalID", currentTimeMillis);
        msgMap.put("ClientMsgId", currentTimeMillis);

        body.put("BaseRequest", baseRequest);
        body.put("Scene", 0);
        body.put("Msg", msgMap);

        Map<String, String> headers = new HashMap<>();
        headers.put("cookie",
            "webwx_data_ticket=" + loginPagePO.getWebwx_data_ticket()
                + ";wxuin=" + loginPagePO.getWxUin() + ";"
                + ";wxsid=" + loginPagePO.getWxSid() + ";");

        JSONObject resp = null;
        try {
            resp = JSONObject.fromObject(HttpsUtil.post(sendTextMsg, query, body, headers));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送消息，请求失败");
        }

        SendMsgResponse sendMsgResponse = (SendMsgResponse) JsonUtil
            .toBean(resp, SendMsgResponse.class, ignoreLowercase, null);

        return sendMsgResponse;
    }

    /**
     * 发送图片消息
     */
    public static SendMsgResponse sendImgMsg(LoginPagePO loginPagePO, SendMsgRequest msgRequest) {
        Map<String, Object> query = new HashMap<>();
        query.put("fun", "async");
        query.put("f", "json");
        query.put("pass_ticket", loginPagePO.getPassTicket());

        Map<String, Object> body = new HashMap<>();

        Map<String, Object> baseRequest = new HashMap<>();
        baseRequest.put("DeviceID", getDeviceId());
        baseRequest.put("Sid", loginPagePO.getWxSid());
        baseRequest.put("Skey", loginPagePO.getSKey());
        baseRequest.put("Uin", loginPagePO.getWxUin());

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("MediaId", msgRequest.getMediaId());
        msgMap.put("Type", msgRequest.getType());
        msgMap.put("Content", msgRequest.getContent());
        msgMap.put("FromUserName", msgRequest.getFromUserName());
        msgMap.put("ToUserName", msgRequest.getToUserName());
        long currentTimeMillis = System.currentTimeMillis();
        msgMap.put("LocalID", currentTimeMillis);
        msgMap.put("ClientMsgId", currentTimeMillis);

        body.put("BaseRequest", baseRequest);
        body.put("Scene", 0);
        body.put("Msg", msgMap);

        Map<String, String> headers = new HashMap<>();
        headers.put("cookie",
            "webwx_data_ticket=" + loginPagePO.getWebwx_data_ticket()
                + ";wxuin=" + loginPagePO.getWxUin() + ";"
                + ";wxsid=" + loginPagePO.getWxSid() + ";");

        JSONObject resp = null;
        try {
            resp = JSONObject.fromObject(HttpsUtil.post(sendImgMsg, query, body, headers));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送消息，请求失败");
        }

        SendMsgResponse sendMsgResponse = (SendMsgResponse) JsonUtil
            .toBean(resp, SendMsgResponse.class, ignoreLowercase, null);

        return sendMsgResponse;
    }

    /**
     * 发送表情消息
     */
    public static SendMsgResponse sendEmoji(LoginPagePO loginPagePO, SendMsgRequest msgRequest) {
        Map<String, Object> query = new HashMap<>();
        query.put("fun", "sys");
        query.put("pass_ticket", loginPagePO.getPassTicket());

        Map<String, Object> body = new HashMap<>();

        Map<String, Object> baseRequest = new HashMap<>();
        baseRequest.put("DeviceID", getDeviceId());
        baseRequest.put("Sid", loginPagePO.getWxSid());
        baseRequest.put("Skey", loginPagePO.getSKey());
        baseRequest.put("Uin", loginPagePO.getWxUin());

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("Type", msgRequest.getType());
        msgMap.put("EmojiFlag", 2);
        msgMap.put("MediaId", msgRequest.getMediaId());
        msgMap.put("FromUserName", msgRequest.getFromUserName());
        msgMap.put("ToUserName", msgRequest.getToUserName());
        long currentTimeMillis = System.currentTimeMillis();
        msgMap.put("LocalID", currentTimeMillis);
        msgMap.put("ClientMsgId", currentTimeMillis);

        body.put("BaseRequest", baseRequest);
        body.put("Scene", 0);
        body.put("Msg", msgMap);

        Map<String, String> headers = new HashMap<>();
        headers.put("cookie",
            "webwx_data_ticket=" + loginPagePO.getWebwx_data_ticket()
                + ";wxuin=" + loginPagePO.getWxUin() + ";"
                + ";wxsid=" + loginPagePO.getWxSid() + ";");

        JSONObject resp = null;
        try {
            resp = JSONObject.fromObject(HttpsUtil.post(webwxsendemoticon, query, body, headers));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送消息，请求失败");
        }

        SendMsgResponse sendMsgResponse = (SendMsgResponse) JsonUtil
            .toBean(resp, SendMsgResponse.class, ignoreLowercase, null);

        return sendMsgResponse;
    }

    private static String getDeviceId() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 15; i++) {
            sb.append(random.nextInt(10));
        }
        return "e" + sb.toString();
    }

    /**
     * 免扫码登录
     */
    public static String notScanLogin(LoginPagePO loginPagePO) throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("uin", uin);
        Map<String, String> header = new HashMap<>();
        header.put("cookie",
            "webwx_auth_ticket=" + loginPagePO.getWebwx_auth_ticket()
                + ";webwxuvid=" + loginPagePO.getWebwxuvid() + ";"
                + ";wxuin=" + loginPagePO.getWxUin() + ";");
        JSONObject resp = JSONObject.fromObject(HttpsUtil.get(notScanLogin, query, header));
        if (resp.getInt("ret") == 0) {
            return resp.getString("uuid");
        }
        return null;
    }

    /**
     * 退出登录
     */
    public static void logout(LoginPagePO loginPagePO) throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("redirect", "1");
        query.put("type", "1");
        query.put("skey", loginPagePO.getSKey());

        Map<String, Object> body = new HashMap<>();
        body.put("sid", loginPagePO.getWxSid());
        body.put("uin", loginPagePO.getWxUin());

        Map<String, String> headers = new HashMap<>();
        headers.put("cookie",
            "webwx_data_ticket=" + loginPagePO.getWebwx_data_ticket()
                + ";wxuin=" + loginPagePO.getWxUin() + ";"
                + ";wxsid=" + loginPagePO.getWxSid() + ";");

        try {
            HttpsUtil.post(logout, query, body, headers);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("退出登录异常");
        }
    }

    /**
     * 上传图片
     */
    public static String upload(LoginPagePO loginPagePO, File file, String contentType, String mediaType) {
        Map<String, Object> query = new HashMap<>();
        query.put("f", "json");

        Map<String, Object> body = new HashMap<>();

        Map<String, Object> baseRequest = new HashMap<>();
        baseRequest.put("DeviceID", getDeviceId());
        baseRequest.put("Sid", loginPagePO.getWxSid());
        baseRequest.put("Skey", loginPagePO.getSKey());
        baseRequest.put("Uin", loginPagePO.getWxUin());

        Map<String, Object> uploadmediarequest = new HashMap<>();
        uploadmediarequest.put("BaseRequest", baseRequest);
        uploadmediarequest.put("UploadType", 2);
        uploadmediarequest.put("ClientMediaId", System.currentTimeMillis());
        uploadmediarequest.put("TotalLen", file.length());
        uploadmediarequest.put("DataLen", file.length());
        uploadmediarequest.put("StartPos", 0);
        uploadmediarequest.put("MediaType", 4);
        uploadmediarequest.put("FromUserName", null);
        uploadmediarequest.put("ToUserName", "filehelper");
        uploadmediarequest.put("FileMd5", UUID.randomUUID().toString().replaceAll("-", ""));

        body.put("id", "WU_FILE_" + file.length());
        body.put("name", file.getName());
        body.put("type", contentType);
        body.put("lastModifiedDate", new Date());
        body.put("size", file.length());
        body.put("mediatype", mediaType);
        body.put("uploadmediarequest", JSONObject.fromObject(uploadmediarequest));
        body.put("webwx_data_ticket", loginPagePO.getWebwx_data_ticket());
        body.put("pass_ticket", loginPagePO.getPassTicket());

        Map<String, String> headers = new HashMap<>();
        headers.put("cookie",
            "webwx_data_ticket=" + loginPagePO.getWebwx_data_ticket()
                + ";wxuin=" + loginPagePO.getWxUin() + ";"
                + ";wxsid=" + loginPagePO.getWxSid() + ";");

        try {
            String result = HttpsUtil.upload(webwxuploadmedia, null, query, body, file);
            JSONObject jsonObject = JSONObject.fromObject(result);
            return jsonObject.getString("MediaId");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传图片异常");
        }
        return null;
    }

}
