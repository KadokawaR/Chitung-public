package lielietea.mirai.plugin.administration.config;

import com.google.gson.annotations.SerializedName;
import lielietea.mirai.plugin.core.responder.universalrespond.CustomizedConfig;
import lielietea.mirai.plugin.core.responder.universalrespond.FunctionConfig;
import lielietea.mirai.plugin.core.responder.universalrespond.ResponseConfig;

import java.util.ArrayList;
import java.util.List;

public class Config {
    String botName;
    List<Long> devGroupID;
    List<Long> adminID;
    int minimumMembers;
    FunctionConfig fc;
    ResponseConfig rc;
    CustomizedConfig cc;

    Config(){
        this.botName = "机器人";
        this.devGroupID = new ArrayList<>();
        this.adminID = new ArrayList<>();
        this.fc = new FunctionConfig();
        this.rc = new ResponseConfig();
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

    public FunctionConfig getFc() {
        return fc;
    }

    public void setFc(FunctionConfig fc) {
        this.fc = fc;
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
}




