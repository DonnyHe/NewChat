# NewChat
采用socket.io实现的实时通讯app(界面仿微信)，服务端为node.js  
主要实现的功能有发送消息，发送语音，发送图片。  
因为Socket.io的实时性非常好，为了增加趣味性，和朋友想出了“射箭”的玩儿法  
当对方的滚动滑轮被射中时，会自动发送其在文本框输入的文字。  
服务端暂时放在朋友的服务器上。  

## 使用方法：
1. 启动页输入自己的用户名
2. 列表页标题栏输入对方用户名搜索
3. 点击item进入聊天页

## ps:服务器只简单实现转发，并未实现消息存储。故聊天双方都需在线才可交互

## 发送语音（请无视头像，随便找了几张）
![image](https://github.com/DonnyHe/NewChat/raw/master/screenshot/voice.png)
## 发送图片（发送前对图片进行压缩以节省流量）
![image](https://github.com/DonnyHe/NewChat/raw/master/screenshot/picture.png)
## 射箭(根据起点和终点确定方向，滑动速度换算成箭的速度)
![image](https://github.com/DonnyHe/NewChat/raw/master/screenshot/shoot.gif)
## 被对方射中（被对方的箭射中滚动的圆圈时，会自动发送文本框中的信息，射中的操作根据属性动画的值和自身尺寸虚拟为矩形，进行矩形相交检测）
![image](https://github.com/DonnyHe/NewChat/raw/master/screenshot/receive_shoot.gif)


socket.io地址：https://github.com/socketio/socket.io
