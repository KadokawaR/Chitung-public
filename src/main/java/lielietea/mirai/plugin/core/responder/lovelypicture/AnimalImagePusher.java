package lielietea.mirai.plugin.core.responder.lovelypicture;

import lielietea.mirai.plugin.core.responder.ResponderTaskDistributor;
import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.utils.image.AnimalImageURLResolver;
import lielietea.mirai.plugin.utils.image.ImageSender;
import net.mamoe.mirai.event.events.MessageEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

class AnimalImagePusher implements Runnable {
    final MessageEvent event;
    final String imageSource;
    final AnimalImageURLResolver.Source uRLResolver;
    final String type;

    public AnimalImagePusher(MessageEvent event, String imageSource, String type, AnimalImageURLResolver.Source uRLResolver) {
        this.event = event;
        this.imageSource = imageSource;
        this.type = type;
        this.uRLResolver = uRLResolver;
    }

    @Override
    public void run() {
        RespondTask.Builder builder = new RespondTask.Builder(event, LovelyImage.INSTANCE);
        try {
            Optional<URL> url = AnimalImageURLResolver.resolve(imageSource, uRLResolver);
            url.ifPresent(url1 -> builder.addTask(() -> ImageSender.sendImageFromURL(event.getSubject(), url1)));
        } catch (IOException e) {
            builder.addMessage("非常抱歉，获取" + type + "图的渠道好像出了一些问题，图片获取失败");
            builder.addNote(e.toString());
        } finally {
            //发送给MessageDispatcher去处理
            ResponderTaskDistributor.handleIsolatedResponderTask(builder.build());
        }

    }
}
