
![Image text](https://raw.githubusercontent.com/yhluk/Chitung-public/main/repository-open-graph.png)

# Public Version of Chitung 
## - the Mirai-based Tencent QQ bot.

This project aims to provide users with a lite and open version of Chitung. Although Chitung itself has
already been open sourced, it contains numerous highly customized and personalized functions, which makes
it almost impossible to be used by others than the original developers.

Public Chitung allows users to run, customize and manage their own Chitung. 

# 七筒：开放版
## - 基于Mirai框架的QQ机器人

本项目由七筒官方开发者维护，旨在为用户提供开放版本的七筒插件。七筒本身虽然已经开源，但由于其包含了许多高度定制化和个性化的功能，使得其他用户若想使用七筒成本大为增加。

七筒开放版允许用户运行、定制和管理他们自己的**七筒/八条/九万**机器人。同时出于服务简体中文 Furry 社群和维持互联网开放的目的，七筒官方开发者希望即使七筒本身的账号出于各方面的限制停止运行，其他用户也能使用该插件继续延续七筒的服务和生命。

## 新功能
与2022年3月之前的七筒相比，七筒开放版新增了如下功能：
### 群管理 Group Config
允许群主和管理员自定义是否开启相应的功能。共有六个开关：global / responder / game / casino / fish / lottery。使用方法为 /open 或者 /close 加响应的模块名称。
- Global：
全局开关。即不会响应任何内容
- Responder：
关键词触发功能开关。包括求签、兽设、骰子、吃什么等。
- Game：
游戏开关。包括钓鱼、赌场、银行、猜麻将。
- Casino：
赌场开关。
- Fish：
钓鱼开关。
- Lottery：
群内抽奖开关。包括 Bummer 和 C4。
  
使用 /blockmember 或者 /unblockmember 并@要屏蔽（解除屏蔽）的成员会将该名成员移入（移除）本群黑名单，支持同时@多人。此黑名单全局生效于该群聊中。

### 通用响应 Universal Responder
允许七筒开放版的运营者添加更多关键词响应。由于该功能通过``data/Chitung/UniversalResponder.json``文件实现，且目前不提供交互功能，运营者需要仔细检查其修改的json文件。

每个通用响应数据包含如下内容：
```java
MessageKind messageKind;
MessageKind listResponseKind;
ListKind listKind;
List<Long> userList;
TriggerKind triggerKind;
List<String> pattern;
List<String> answer;
```
```json
{
  "universalRespondList": [
    {
      "messageKind": "Any",
      "listResponseKind": "Any",
      "listKind": "White",
      "userList": [],
      "triggerKind": "Equal",
      "pattern": ["pattern1","pattern2"],
      "answer": ["answer1","answer2"]
    }
  ]
}
```
值得注意的是，如下内容对大小写敏感。

- messageKind | 响应的消息类型，三种可选：Friend / Group / Any，即响应好友消息、群消息和都响应。
- listResponseKind | 黑白名单的响应消息类型，两种可选：Friend / Group，即黑白名单是响应好友消息还是群消息。
- listKind | 黑白名单的类型，两种可选： Black / White，即黑名单、白名单。
- userList | 黑白名单的类型，填写QQ号或者群号，若有多个请使用英语逗号隔开。如 123456,234567
- triggerKind | 触发的条件类型，两种可选： Equal / Contain，即关键词是必须相等还是只要包含就能触发。
- pattern | 触发的关键词，使用英语引号将关键词包裹，若有多个请使用英语逗号隔开。如 "Chitung","Public"
- answer | 回复的内容，会随机选择其一回复。使用英语引号将回复的内容包裹，若有多个请使用英语逗号隔开。如 "Bonne Soiree","Ca marche","S'il vous plait"

### 运营者配置文件 Admin's Config

运营者配置文件``data/Chitung/Config.json``包含了大量可以定制的功能。由于该功能通过json文件实现，且除了全局消息目前不提供交互功能，运营者需要仔细检查其修改的json文件。
```json
{
    "botName": "", //机器人名字
    "devGroupID": [], //管理员群
    "adminID": [], // 管理员名单
    "minimumMembers": 7, //最小的群聊人数
    "friendFC": { //好友消息的响应开关
        "fish": true,
        "casino": true,
        "responder": true,
        "lottery": true,
        "game": true
    },
    "groupFC": { //群聊消息的响应开关
        "fish": true,
        "casino": true,
        "responder": true,
        "lottery": true,
        "game": true
    },
    "rc": { //全局消息的响应开关
        "answerFriend": true, //响应好友消息
        "answerGroup": true, //响应群聊消息
        "addFriend": true, //添加好友
        "addGroup": true, //添加群聊
        "autoAnswer": true //非主动触发消息
    },
    "cc": { //自定义文本
        "joinGroupText": "很高兴为您服务。",//加群文本
        "rejectGroupText": "抱歉，机器人暂时不接受加群请求。",//拒绝加入群聊文本
        "onlineText": "机器人已经上线。",//上线文本
        "welcomeText": "欢迎。",//新成员加群文本
        "permissionChangedText": "谢谢，各位将获得更多的乐趣。",//机器人权限更改文本
        "groupNameChangedText": "好名字。",//群名称更改文本
        "nudgeText": "啥事？"//被戳文本
    }
}
```
### 通用图库响应 Universal Image Responder

通用图库响应的文件由两部分组成：通用图库响应的配置文件 ``image/imagedata.json`` 和用户的图片文件夹。用户需要在 ``image/`` 的目录下创建
相应的图片目录，并更改 imagedata.json 内的配置文件，填写触发关键词、触发类型、图片目录名称。

配置文件包含如下内容：
```java
List<String> keyword; //触发关键词
String directoryName; //图片目录名称
String text; //在图片发送前一并发送的文字
ImageResponder.TriggerType triggerType; //触发类型，Equal或者Contain
ImageResponder.ResponseType responseType; //响应类型，Friend、Group或者Any
```
```json
{
  "dataList": [
    {
      "keyword": [
        "/qt"
      ],
      "directoryName": "qt",
      "text": "感谢使用七筒开放版。",
      "triggerType": "Equal",
      "responseType": "Any"
    }
  ]
}

```
为避免发送失败，请尽可能使用 png 和 jpeg 格式的图片放置在相应目录内。

### 其他
使用前请务必将管理员的QQ号输入进 ``/data/Chitung/Config.json`` 里的 ``adminID`` 数组内，如果有多个请使用半角逗号隔开。如：``"adminID": [123456,654321]``。

使用 /adminhelp 指令来查询详细的管理员指令。

## 正在开发
### 通用响应、运营者配置文件的交互
允许七筒开放版的运营者直接通过QQ平台进行管理。

### 游戏定制
允许七筒开放版的运营者对于游戏的一些基础数据进行更改。

### 克莱因先生的飞艇 The Zeppelin Tale of Klein B.Schloss
游戏功能。

## 版权

### 美术素材
七筒绝非是他的城市里唯一的居民。开发者为七筒设计了形象的同时，也为钓鱼场老板、赌场老板、以及未来其他功能出现的角色设计形象。我们感谢如下艺术创作者赋予七筒世界里的形象帅气的外表。

- 七筒角色设计：[青蛙奥利奥](https://weibo.com/u/2843849155)

- 七筒头像：[青蛙奥利奥](https://weibo.com/u/2843849155)

- Maverick角色设计：[凹布瑞](https://weibo.com/u/5163824559)

- 里格斯先生角色设计：[凹布瑞](https://weibo.com/u/5163824559)

- 克莱因先生角色设计：[凹布瑞](https://weibo.com/u/5163824559)

- 七筒开放版图标：[苟砳砳](https://weibo.com/u/3095618097)

- 麻将牌图标：[维基百科-麻将](https://zh.wikipedia.org/wiki/%E9%BA%BB%E5%B0%86)

- 其他美术素材：由七筒开发组购买或者设计。

我们允许用户自由在使用七筒开放版时，修改上述非开源美术素材；允许基于上述形象进行二次创作；不支持用户在其他渠道流通这些非开源美术素材。

### 涉及项目

- 基于 AGPLv3 协议的 [Mirai](https://github.com/mamoe/mirai)
- 基于 Apache License 2.0 协议的 [谷歌Gson](https://github.com/google/gson)
- [清华大学开放中文词库](http://thuocl.thunlp.org/)
