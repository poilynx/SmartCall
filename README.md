#特性：  
0. [如何快速搭建服务端指导](http://www.cnblogs.com/kimmy/p/4654683.html)
1. 如果同事不在你本地通讯录，那么他给你打电话的时候，会自动显示其头像、名字、职位  
2. 支持一键将同事的头像、公司、部门、职位、家庭电话、公司电话添加到本地通讯录  
3. 使用不耗费任何流量，打开即用，点击拨号。仅用两步，就联系上了一名陌生的同事。
4. 使用非常简单，短按拨打号码、长按查看详情、下拉刷新数据  
5. 本地版请切换到[Local](http://git.oschina.net/yso/SmartCall/tree/local/)分支，体验apk请[直接点我下载本地版](http://files.cnblogs.com/files/kimmy/%E4%BC%81%E4%B8%9A%E9%80%9A%E8%AE%AF%E5%BD%95%E6%9C%AC%E5%9C%B0%E7%89%88.apk)  
6. wifi下默认更新通讯录（最低触发间隔一天）、自动管理废弃图片、设置多重索引加速app运行 
7. 引入LruCache内存缓存，精心设计的缓存机制，确保app大部分情况下都是满60fps运行，丝滑顺手。具体看下图，绝大多时候，fps都是在绿线以下的（绿线代表流畅）。  
 ![输入图片说明](http://git.oschina.net/uploads/images/2015/0717/144802_bbbcba34_331643.png "在这里输入图片标题")
![输入图片说明](http://git.oschina.net/uploads/images/2015/0710/173317_b78509da_331643.png "来电识别")
![输入图片说明](http://git.oschina.net/uploads/images/2015/0710/173342_0a70e15e_331643.png "用户详情")
![输入图片说明](http://git.oschina.net/uploads/images/2015/0710/173601_6edc0049_331643.png "插入到本地通讯录了")
![输入图片说明](http://images0.cnblogs.com/blog2015/339868/201507/101710481119429.gif "软件详情")