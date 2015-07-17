#特性：  
1. 如果同事不在你本地通讯录，那么他给你打电话的时候，会自动显示其头像、名字、职位  
2. 支持一键将同事的头像、公司、部门、职位、家庭电话、公司电话添加到本地通讯录  
3. 使用不耗费任何流量  
4. 使用非常简单，短按拨打号码、长按查看详情、下拉刷新数据  
5. 远程版如何使用（推荐）[参考项目主页地址](http://www.cnblogs.com/kimmy/p/4636313.html)，本地版请切换到[Local](http://git.oschina.net/yso/SmartCall/tree/local/)分支，体验apk请[直接点我下载本地版](http://files.cnblogs.com/files/kimmy/%E4%BC%81%E4%B8%9A%E9%80%9A%E8%AE%AF%E5%BD%95%E6%9C%AC%E5%9C%B0%E7%89%88.apk)  
6. 充分利用json+sqlite，创建了用户手机号、用户头像索引，减少IO请求次数  
7. 引入LruCache内存缓存，在首字母频繁滚动时，还能保持流畅的使用体验。  
![输入图片说明](http://git.oschina.net/uploads/images/2015/0710/173317_b78509da_331643.png "来电识别")
![输入图片说明](http://git.oschina.net/uploads/images/2015/0710/173342_0a70e15e_331643.png "用户详情")
![输入图片说明](http://git.oschina.net/uploads/images/2015/0710/173601_6edc0049_331643.png "插入到本地通讯录了")
![输入图片说明](http://images0.cnblogs.com/blog2015/339868/201507/101710481119429.gif "软件详情")