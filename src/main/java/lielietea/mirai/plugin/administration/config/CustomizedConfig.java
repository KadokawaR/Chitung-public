package lielietea.mirai.plugin.administration.config;

public class CustomizedConfig {
    String joinGroupText;
    String rejectGroupText;
    String onlineText;
    String welcomeText;
    String permissionChangedText;
    String groupNameChangedText;
    String nudgeText;

    public CustomizedConfig(){
        this.joinGroupText = "很高兴为您服务。在使用本 bot 之前，请仔细阅读下方的免责协议。";
        this.rejectGroupText = "抱歉，机器人暂时不接受加群请求。";
        this.onlineText = "机器人已经上线。";
        this.welcomeText = "欢迎。";
        this.permissionChangedText = "谢谢，各位将获得更多的乐趣。";
        this.groupNameChangedText = "好名字。";
        this.nudgeText = "啥事？";
    }

    public String getJoinGroupText() {
        return joinGroupText;
    }
    public String getRejectGroupText() {
        return rejectGroupText;
    }
    public String getOnlineText() {
        return onlineText;
    }
    public String getWelcomeText() {
        return welcomeText;
    }
    public String getPermissionChangedText() {
        return permissionChangedText;
    }
    public String getGroupNameChangedText() {
        return groupNameChangedText;
    }
    public String getNudgeText() { return nudgeText;}
}

