package com.wechat.po.wechat;

import java.util.List;
import lombok.Data;

@Data
public class InitPO {

    /*
     * BaseResponse: {Ret: 0, ErrMsg: ""}
     * ChatSet: "filehelper,@@1381ee5f36f7d66f012eb8234d1436f66f67808c18ad821edf4d9987341df854,@d407a503d84fdd9118f1bf291eb11765,@5d955c120c989650af9367747e648fcabbb68a9e852182f868f7876728410129,weixin,filehelper,@3bc67fd76d0485bda7bbe211a2949591,@@468d5e7ec42a5a6b8cf504be959f8a761f970fd42aed3447820f63bbb69887fe,@@d381e5b4a8429798c24dd2abf4fa567e53646bcfff4fb214ecd8fead9c21ad43,@d9252832c94aebd2ca40e21db467fe88,@@b5d0d1a17bc2d93f0b33693c0cf7a1b8deb19312e860a65e446f6dd8fd703c60,@@1623c80424561b674d2fdd7d238097871856af08ef91ce991036fc78f84fa38e,@@e5b0924d8093361a2a7ee29875e71a15162f7f0cb84432e88025d699e33c1bb5,@52b5c3b9e05d1044f3969f320c7085d1,@dcd76f8d0444ab9e187e07744b1a0338,@7c426167cb95f8851b90d08bb1e44de7,@b6b1363e73e12d39b45ae59948735bcf,@8c072439020c1496da5cb86cb9ddfa3b,@@f065cc65634474261c409574898801ef53e93ea1c6809cadac836dd3599cf57c,fmessage,newsapp,@@636dc67487ec9f531f7fbb3a4d0f905813acf0e5a33449c62fb42104fd560681,@6390c6a17e6ed443388601341df916f0285f14ebe19e4c3d397e818c26568d5e,@0d5d370088593da99f81721e6b8bbecad05e5f78764bdde3bcb100ced5eba0c0,@@01c40efbdbec8d0220701a6711ad82760542c576407af76ed6b0cf34ab1af06e,@66cefb36cdc57f3ea4783d226a0a56c94b2309f14cce2f2e07cc4851a61949a2,"
     * ClickReportInterval: 600000
     * ClientVersion: 654312506
     * ContactList: [{Uin: 0, UserName: "filehelper", NickName: "æ–‡ä»¶ä¼ è¾“åŠ©æ‰‹",…},…]
     * Count: 11
     * GrayScale: 1
     * InviteStartCount: 40
     * MPSubscribeMsgCount: 7
     * MPSubscribeMsgList: [{UserName: "@d407a503d84fdd9118f1bf291eb11765", MPArticleCount: 2,…},…]
     * SKey: "@crypt_253d2949_b195b14efa911d623d9eae272cebd068"
     * SyncKey: {Count: 4, List: [{Key: 1, Val: 661091461}, {Key: 2, Val: 661091587}, {Key: 3, Val: 661091588},…]}
     * SystemTime: 1557286175
     * User: {Uin: 3162028971, UserName: "@dbc955536b0b80b8e9154560f9cc8871fcdf150c087335212ef9219b7cd28764",…}
     */

    private BaseResponsePO baseResponse;
    private String chatSet;
    private long clickReportInterval;
    private long clientVersion;
    private List<ContactPO> contactList;
    private int count;
    private int grayScale;
    private int inviteStartCount;
    private int MPSubscribeMsgCount;
    private List<MPSubscribeMsgPO> MPSubscribeMsgList;
    private String sKey;
    private SyncKeyPO syncKey;
    private long systemTime;
    private UserPO user;

}
