package mirai.chitung.plugin.core.responder.imageresponder;

import java.util.ArrayList;
import java.util.List;

public class ImageResponderData {
    List<String> keyword;
    String directoryName;
    String text;
    ImageResponder.TriggerType triggerType;
    ImageResponder.ResponseType responseType;

    ImageResponderData(String keyword, String directoryName, String text, ImageResponder.TriggerType triggerType, ImageResponder.ResponseType responseType){
        this.keyword = new ArrayList<String>(){{add(keyword);}};
        this.directoryName = directoryName;
        this.text = text;
        this.triggerType = triggerType;
        this.responseType = responseType;
    }

    ImageResponderData(List<String> keyword, String directoryName, String text, ImageResponder.TriggerType triggerType, ImageResponder.ResponseType responseType){
        this.keyword = keyword;
        this.directoryName=directoryName;
        this.text = text;
        this.triggerType = triggerType;
        this.responseType = responseType;
    }
}
