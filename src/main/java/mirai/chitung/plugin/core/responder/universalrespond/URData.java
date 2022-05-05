package mirai.chitung.plugin.core.responder.universalrespond;

import mirai.chitung.plugin.core.responder.universalrespond.respondenum.MessageKind;
import mirai.chitung.plugin.core.responder.universalrespond.respondenum.TriggerKind;

import java.util.ArrayList;
import java.util.List;

public class URData {
    private MessageKind messageKind;
    private List<URListData> userList;
    private TriggerKind triggerKind;
    private List<String> pattern;
    private List<String> answer;

    URData(){
        this.messageKind=MessageKind.Any;
        this.userList=new ArrayList<>();
        this.triggerKind=TriggerKind.Equal;
        this.pattern=new ArrayList<String>(){{add("早上好");add("早安");}};
        this.answer=new ArrayList<String>(){{add("早上好");add("早安");}};
    }

    URData(URData ur){
        this.messageKind=MessageKind.Any;
        this.userList=ur.userList;
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

    public List<URListData> getUserList() { return userList; }

    public void setUserList(List<URListData> userList) { this.userList = userList; }

}
