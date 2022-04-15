package chitung.plugin.administration.config;

public class ResponseConfig{
    boolean answerFriend;
    boolean answerGroup;
    boolean addFriend;
    boolean addGroup;
    boolean autoAnswer;
    public ResponseConfig(){
        this.answerFriend=true;
        this.addFriend=true;
        this.addGroup=true;
        this.answerGroup=true;
        this.autoAnswer=true;
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

    public boolean isAddFriend() {
        return addFriend;
    }

    public void setAddFriend(boolean addFriend) {
        this.addFriend = addFriend;
    }

    public boolean isAddGroup() {
        return addGroup;
    }

    public void setAddGroup(boolean addGroup) {
        this.addGroup = addGroup;
    }

    public boolean isAutoAnswer() {
        return autoAnswer;
    }

    public void setAutoAnswer(boolean autoAnswer) {
        this.autoAnswer = autoAnswer;
    }
}