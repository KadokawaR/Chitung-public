package lielietea.mirai.plugin.core.responder;

import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * 回复处理器接口，如果要让 {@link ResponderManager} 对回复处理器进行托管，那么必须实现该类并注册
 */
public interface MessageResponder<T extends MessageEvent> {

    boolean match(T event);

    RespondTask handle(T event);

    /**
     * 功能模块的UUID，默认根据名字自动生成
     */
    default UUID getUUID() {
        return UUID.nameUUIDFromBytes(this.getName().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 功能模块的名字
     */
    String getName();

    @NotNull
    List<MessageType> types();

    default void onclose() {}

    enum MessageType {
        GROUP,
        FRIEND,
        STRANGER,
        TEMP
    }
}
