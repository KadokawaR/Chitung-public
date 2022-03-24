package lielietea.mirai.plugin.core.responder.universalrespond;

import java.util.ArrayList;
import java.util.List;

public class UniversalRespond {
    private MessageKind messageKind;
    private MessageKind listResponceKind;
    private ListKind listKind;
    private List<Long> userList;

    private TriggerKind triggerKind;
    private List<String> pattern;
    private List<String> answer;

    UniversalRespond(){
        this.messageKind=MessageKind.Any;
        this.listResponceKind=MessageKind.Any;
        this.listKind=ListKind.Black;
        this.userList=new ArrayList<Long>(){{add(0L);}};
        this.triggerKind=TriggerKind.Equal;
        this.pattern=new ArrayList<String>(){{add("");}};
        this.answer=new ArrayList<String>(){{add("");}};
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
        return listResponceKind;
    }

    public void setListResponceKind(MessageKind listResponceKind) {
        this.listResponceKind = listResponceKind;
    }
}
