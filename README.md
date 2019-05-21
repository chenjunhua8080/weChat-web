# 基于网页版微信的机器人

#### 简单聊，写这个项目原因几个：

+ 需要有一个微信自动群发的工具
+ github上的不会用(当时也不懂找吧)
+ 不舍得买
+ 觉得自己可以写一个(天真啊！当时研究了一段时间，自己写的时候到请求需要cookie时走不下去，不会！)

### 优点
+ java！
+ idea！
+ wx2.qq.com(新版)

### 规划功能
功能 | 实现情况| 更新日期 | 备注
:---------------------------|:-----------|:------------------------------|:----------
监听文字、图像、动图、语音、视频、文件、系统提示等消息|
发送文字、图像、动图、文件等消息|
撤回发送的消息|
同意好友申请|
修改好友备注|
置顶/取消置顶联系人|
设置群名称|
微信创建群、添加群成员、移除群成员

### 开发版本

日期 | 版本号 | 更新内容 | 备注
---------------------------|-----------|:------------------------------|:----------
2018.01.20     | 1.0       | 简单的java控制台交互。<br/>能下载登录的二维码到电脑，扫码获取状态... | 其实啥功能也没实现，记录下来这份初心吧！
2019.05.21     | 2.0       | 通过web页面展示数据交互。<br/>web微信通过扫描客户端二维码进行登陆。<br/>获取最近会话列表。<br/>获取微信通讯录的联系人。<br/>获取公众号推文。<br/>监听微信新消息。<br/>选择会话列表联系人或群可发送消息。 | 许多异常也没处理，页面也是很low的。<br/>后续会慢慢更新其他功能！

### 帮助理解的流程图
Web Weixin Pipeline（偷来的）
       +--------------+     +---------------+   +---------------+
       |              |     |               |   |               |
       |   Get UUID   |     |  Get Contact  |   | Status Notify |
       |              |     |               |   |               |
       +-------+------+     +-------^-------+   +-------^-------+
               |                    |                   |
               |                    +-------+  +--------+
               |                            |  |
       +-------v------+               +-----+--+------+      +--------------+
       |              |               |               |      |              |
       |  Get QRCode  |               |  Weixin Init  +------>  Sync Check  <----+
       |              |               |               |      |              |    |
       +-------+------+               +-------^-------+      +-------+------+    |
               |                              |                      |           |
               |                              |                      +-----------+
               |                              |                      |
       +-------v------+               +-------+--------+     +-------v-------+
       |              | Confirm Login |                |     |               |
+------>    Login     +---------------> New Login Page |     |  Weixin Sync  |
|      |              |               |                |     |               |
|      +------+-------+               +----------------+     +---------------+
|             |
|QRCode Scaned|
+-------------+

### 

---
#### 参考文章，排名不分先后
+ https://github.com/moontide/WeChatBotEngine
+ https://github.com/Urinx/WeixinBot
+ https://github.com/Zhyblx/squirrelAI
