package lielietea.mirai.plugin.core.responder.imageresponder;

import java.util.ArrayList;
import java.util.List;

public class ImageResponderData {
    List<String> keyword;
    String directoryName;
    ImageResponder.TriggerType triggerType;
    ImageResponder.ResponseType responseType;

    ImageResponderData(String keyword, String directoryName, ImageResponder.TriggerType triggerType, ImageResponder.ResponseType responseType){
        this.keyword = new ArrayList<String>(){{add(keyword);}};
        this.triggerType = triggerType;
        this.responseType = responseType;
    }

    ImageResponderData(List<String> keyword, String directoryName, ImageResponder.TriggerType triggerType, ImageResponder.ResponseType responseType){
        this.keyword = keyword;
        this.triggerType = triggerType;
        this.responseType = responseType;
    }
}
