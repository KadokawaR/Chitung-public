package lielietea.mirai.plugin.utils.multibot;

public class BotConfig {
    long ID;
    boolean acceptFriend;
    boolean acceptGroup;
    boolean answerFriend;
    boolean answerGroup;
    boolean sendNotice;

    BotConfig(long ID){
        this.ID = ID;
        this.acceptFriend=true;
        this.acceptGroup=true;
        this.answerFriend=true;
        this.answerGroup=true;
        this.sendNotice=false;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public boolean isAcceptFriend() {
        return acceptFriend;
    }

    public void setAcceptFriend(boolean acceptFriend) {
        this.acceptFriend = acceptFriend;
    }

    public boolean isAcceptGroup() {
        return acceptGroup;
    }

    public void setAcceptGroup(boolean acceptGroup) {
        this.acceptGroup = acceptGroup;
    }

    public boolean isAnswerFriend() {
        return answerFriend;
    }

    public void setAnswerFriend(boolean answerFriend) {
        this.answerFriend = answerFriend;
    }

    public boolean isAnswerGroup() {
        return answerGroup;
    }

    public void setAnswerGroup(boolean answerGroup) {
        this.answerGroup = answerGroup;
    }

    public boolean isSendNotice() {
        return sendNotice;
    }

    public void setSendNotice(boolean sendNotice) {
        this.sendNotice = sendNotice;
    }
}
