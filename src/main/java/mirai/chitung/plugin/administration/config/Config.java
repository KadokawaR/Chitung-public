package mirai.chitung.plugin.administration.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
    String botName;
    List<Long> devGroupID;
    List<Long> adminID;
    int minimumMembers;
    FunctionConfig friendFC;
    FunctionConfig groupFC;
    ResponseConfig rc;
    CustomizedConfig cc;

    Config(){
        this.botName = "";
        this.devGroupID = new ArrayList<>();
        this.adminID = new ArrayList<>();
        this.minimumMembers = 7;
        this.friendFC = new FunctionConfig();
        this.groupFC = new FunctionConfig();
        this.rc = new ResponseConfig();
        this.cc = new CustomizedConfig();
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public List<Long> getDevGroupID() {
        return devGroupID;
    }

    public void setDevGroupID(List<Long> devGroupID) {
        this.devGroupID = devGroupID;
    }

    public List<Long> getAdminID() {
        return adminID;
    }

    public void setAdminID(List<Long> adminID) {
        this.adminID = adminID;
    }

    public ResponseConfig getRc() {
        return rc;
    }

    public void setRc(ResponseConfig rc) {
        this.rc = rc;
    }

    public void setAnswerFriend(boolean answerFriend){
        this.getRc().setAnswerFriend(answerFriend);
    }

    public void setAddFriend(boolean addFriend){
        this.getRc().setAddFriend(addFriend);
    }

    public void setAddGroup(boolean addGroup){
        this.getRc().setAddGroup(addGroup);
    }

    public void setAnswerGroup(boolean answerGroup){
        this.getRc().setAnswerGroup(answerGroup);
    }

    public void setAutoAnswer(boolean autoAnswer){
        this.getRc().setAutoAnswer(autoAnswer);
    }

    public CustomizedConfig getCc() {
        return this.cc;
    }

    public void setCc(CustomizedConfig cc) {
        this.cc = cc;
    }

    public int getMinimumMembers() {
        return minimumMembers;
    }

    public void setMinimumMembers(int minimumMembers) {
        this.minimumMembers = minimumMembers;
    }

    public FunctionConfig getFriendFC() {
        return friendFC;
    }

    public void setFriendFC(FunctionConfig friendFC) {
        this.friendFC = friendFC;
    }

    public FunctionConfig getGroupFC() {
        return groupFC;
    }

    public void setGroupFC(FunctionConfig groupFC) {
        this.groupFC = groupFC;
    }
}




