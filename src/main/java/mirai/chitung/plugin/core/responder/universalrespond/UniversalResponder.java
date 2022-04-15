package mirai.chitung.plugin.core.responder.universalrespond;

import java.util.ArrayList;
import java.util.List;

public class UniversalResponder {
    private MessageKind messageKind;
    private MessageKind listResponseKind;
    private ListKind listKind;
    private List<Long> userList;

    private TriggerKind triggerKind;
    private List<String> pattern;
    private List<String> answer;

    UniversalResponder(){
        this.messageKind=MessageKind.Any;
        this.listResponseKind =MessageKind.Any;
        this.listKind=ListKind.White;
        this.userList=new ArrayList<>();
        this.triggerKind=TriggerKind.Equal;
        this.pattern=new ArrayList<String>(){{add("早上好");add("早安");}};
        this.answer=new ArrayList<String>(){{add("早上好");add("早安");}};
    }

    UniversalResponder(UniversalResponder ur){
        this.messageKind=MessageKind.Any;
        this.listResponseKind =MessageKind.Any;
        this.listKind=ListKind.White;
        this.userList=new ArrayList<>();
        this.triggerKind=ur.getTriggerKind();
        this.pattern=ur.getPattern();
        this.answer=ur.getAnswer();
    }

    public MessageKind getMessageKind() {
        return messageKind;
    }

    public void setMessageKind(MessageKind messageKind) {
        this.messageKind = messageKind;
    }

    public ListKind getListKind() {
        return listKind;
    }

    public void setListKind(ListKind listKind) {
        this.listKind = listKind;
    }

    public List<Long> getUserList() {
        return userList;
    }

    public void setUserList(List<Long> userList) {
        this.userList = userList;
    }

    public TriggerKind getTriggerKind() {
        return triggerKind;
    }

    public void setTriggerKind(TriggerKind triggerKind) {
        this.triggerKind = triggerKind;
    }

    public List<String> getPattern() {
        return pattern;
    }

    public void setPattern(List<String> pattern) {
        this.pattern = pattern;
    }

    public List<String> getAnswer() {
        return answer;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }

    public MessageKind getListResponceKind() {
        return listResponseKind;
    }

    public void setListResponceKind(MessageKind listResponceKind) {
        this.listResponseKind = listResponceKind;
    }
}
