#MyEffectTool

此项目是一个工具型app，具有下面五大功能：
>* 微信自动抢红包
>* 微信自动回复
>* 程序锁  --- *对MIUI 8系统新出的系统功能进行实现*
>* 进程管理  --- *针对android5.0以下系统* 
>* 软件管理  --  *可以对应用进行下载，运行，查看应用详细内容*


----------
##项目详细描述：

###自动抢红包功能


> **微信的红包功能可谓当下最受欢迎的功能之一，本应用针对这种迫切的需求，实现了后台自动抢红包功能，即使不在手机旁，照样能够秒抢红包，一个不放过**

原理：主要通过AccessibilityService对微信的通知进行监听，执行动作！


项目展示：

1.前台抢红包

![](http://i.imgur.com/z6um0qQ.gif)

2.屏幕关闭状态抢红包（抢完会自动锁屏，关掉屏幕）

![](http://i.imgur.com/SJNrdS8.gif)

因为手机同步在电脑上时，延迟得很厉害，所以勉强能看，具体可以把apk装在自己手机上运行，观察效果

###注意事项
在启动微信自动抢红包功能，需要把手机设置中-> 无障碍 -> AutoRobLuckMoney服务开启
![](http://i.imgur.com/Gh8UbjK.gif)


----------
###微信自动回复

> 微信没有自动回复的功能，但是有时候有事在身边，确实不能及时回复给别人，想拥有自动回复的功能，加上自己定制的回复语


1.后台自动回复

![](http://i.imgur.com/MiCTe2i.gif)

2.屏幕关闭时自动回复

![](http://i.imgur.com/00evowS.gif)


###注意：
自动回复功能和自动抢红包功能，都需要在无障碍中开启服务，
自动回复功能对应的服务是Auto wechat reply


----------

###程序锁

>小米系统MIUI8 最近新推出一个保护用户隐私的功能 -- 程序锁，凡是锁上的应用，输入需要绘制密码图案，有效防止手机离身时，被他人偷看隐私
，本应用对此做出了实现

效果图：

![](http://i.imgur.com/2xvEVlo.gif)

>程序锁自定义了九宫格控件LockView，作为密码输入界面 ，通过记录绘制的点，动态绘制线，我们还可以设置不同状态下九个点的图片(建议64dp大小)，还有不同状态下线条的颜色


----------
###进程管理

>进程管理通过设置RecyclerView的Item动画和自定义Item布局动画，实现了流畅的进程管理体验


###软件管理
>对软件进行管理，可以实现卸载，查看应用详细设置，启动的功能！并且用装饰者模式为ReyclerView添加HeadView，以用来区分开系统应用和第三方应用

![](http://i.imgur.com/Ls3JITU.gif)


###XUtils下载更新包，并且在通知栏显示进度条,点击安装

>用MVP架构，和Intentservice实现下载服务

>效果图：

![](http://i.imgur.com/2eId5E8.gif)
