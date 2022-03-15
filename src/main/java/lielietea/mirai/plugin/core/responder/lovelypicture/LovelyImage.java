package lielietea.mirai.plugin.core.responder.lovelypicture;

import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import lielietea.mirai.plugin.utils.exception.NoHandlerMethodMatchException;
import lielietea.mirai.plugin.utils.image.AnimalImageURLResolver;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.regex.Pattern;


public class LovelyImage implements MessageResponder<MessageEvent> {

    static class ImageSource {
        static final String DOG_CEO_HUSKY = "https://dog.ceo/api/breed/husky/images/random";
        static final String DOG_CEO_BERNESE = "https://dog.ceo/api/breed/mountain/bernese/images/random";
        static final String DOG_CEO_MALAMUTE = "https://dog.ceo/api/breed/malamute/images/random";
        static final String DOG_CEO_GSD = "https://dog.ceo/api/breed/germanshepherd/images/random";
        static final String DOG_CEO_SAMOYED = "https://dog.ceo/api/breed/samoyed/images/random";
        static final String DOG_CEO_DOBERMAN = "https://dog.ceo/api/breed/doberman/images/random";
        static final String SHIBE_ONLINE_SHIBA = "https://shibe.online/api/shibes";
        static final String SHIBE_ONLINE_CAT = "https://shibe.online/api/cats";
        static final String RANDOM_DOG = "https://random.dog/woof.json";
    }

    static final List<MessageType> TYPES = new ArrayList<>(Collections.singletonList(MessageType.GROUP));
    static final Pattern DOG_REG_PATTERN = Pattern.compile("((/[Dd]og)|([oO][kK] [Dd]og))|(((来点)|/)((狗子)|狗|(狗狗)))");
    static final Pattern CAT_REG_PATTERN = Pattern.compile("((/[Cc]at)|([oO][kK] [Cc]at))|(((来点)|/)((猫猫)|猫|(猫咪)|(喵喵)))");
    static final Pattern SHIBA_REG_PATTERN = Pattern.compile("((/[Ss]hiba)|([oO][kK] [Ss]hiba))|(((来点)|/)((柴犬)|(柴柴)))");
    static final Pattern HUKSY_REG_PATTERN = Pattern.compile("((/[Hh]usky)|([oO][kK] [Hh]usky))|(((来点)|/)((哈士奇)|(二哈)))");
    static final Pattern BERNESE_REG_PATTERN = Pattern.compile("((/[Bb]ernese)|([oO][kK] 伯恩山))|(((来点)|/)((伯恩山)|(伯恩山犬)))");
    static final Pattern MALAMUTE_REG_PATTERN = Pattern.compile("((/[Mm]alamute)|([oO][kK] 阿拉))|(((来点)|/)(阿拉斯加))");
    static final Pattern GSD_REG_PATTERN = Pattern.compile("(((/([Gg]sd)|(GSD))|([oO][kK] 德牧))|(((来点)|/)((德牧)|(黑背))))");
    static final Pattern DOBERMAN_REG_PATTERN = Pattern.compile("(((/[Dd]obermann?)|([oO][kK] 杜宾))|(((来点)|/)(杜宾)))");
    static final Pattern SAMOYED_REG_PATTERN = Pattern.compile("((/[Ss]amoyed)|([oO][kK] 萨摩耶))|(((来点)|/)(萨摩耶))");

    static final Map<Pattern, Function<MessageEvent, RespondTask>> PATTERN_SUPPLIER_MAP = new HashMap<>();

    static {
        {
            PATTERN_SUPPLIER_MAP.put(DOG_REG_PATTERN, LovelyImage::getDog);
            PATTERN_SUPPLIER_MAP.put(CAT_REG_PATTERN, LovelyImage::getCat);
            PATTERN_SUPPLIER_MAP.put(SHIBA_REG_PATTERN, LovelyImage::getShiba);
            PATTERN_SUPPLIER_MAP.put(HUKSY_REG_PATTERN, LovelyImage::getHusky);
            PATTERN_SUPPLIER_MAP.put(BERNESE_REG_PATTERN, LovelyImage::getBernese);
            PATTERN_SUPPLIER_MAP.put(MALAMUTE_REG_PATTERN, LovelyImage::getMalamute);
            PATTERN_SUPPLIER_MAP.put(GSD_REG_PATTERN, LovelyImage::getGSD);
            PATTERN_SUPPLIER_MAP.put(SAMOYED_REG_PATTERN, LovelyImage::getSamoyed);
            PATTERN_SUPPLIER_MAP.put(DOBERMAN_REG_PATTERN, LovelyImage::getDoberman);

        }
    }

    static final LovelyImage INSTANCE = new LovelyImage();

    public static LovelyImage getINSTANCE() {
        return INSTANCE;
    }

    final ExecutorService executor;

    LovelyImage() {
        this.executor = Executors.newCachedThreadPool();
    }


    static RespondTask getDog(MessageEvent event) {
        INSTANCE.executor.submit(new AnimalImagePusher(event, ImageSource.RANDOM_DOG, "狗", AnimalImageURLResolver.Source.RADNOM_DOG));
        return RespondTask.of(event, "正在获取狗狗>>>>>>>", INSTANCE);
    }

    static RespondTask getShiba(MessageEvent event) {
        INSTANCE.executor.submit(new AnimalImagePusher(event, ImageSource.SHIBE_ONLINE_SHIBA, "柴犬", AnimalImageURLResolver.Source.SHIBE_ONLINE));
        return RespondTask.of(event, "正在获取柴犬>>>>>>>", INSTANCE);
    }

    static RespondTask getHusky(MessageEvent event) {
        INSTANCE.executor.submit(new AnimalImagePusher(event, ImageSource.DOG_CEO_HUSKY, "哈士奇", AnimalImageURLResolver.Source.DOG_CEO));
        return RespondTask.of(event, "正在获取哈士奇>>>>>>>", INSTANCE);
    }

    static RespondTask getBernese(MessageEvent event) {
        INSTANCE.executor.submit(new AnimalImagePusher(event, ImageSource.DOG_CEO_BERNESE, "伯恩山", AnimalImageURLResolver.Source.DOG_CEO));
        return RespondTask.of(event, "正在获取伯恩山>>>>>>>", INSTANCE);
    }

    static RespondTask getMalamute(MessageEvent event) {
        INSTANCE.executor.submit(new AnimalImagePusher(event, ImageSource.DOG_CEO_MALAMUTE, "阿拉斯加", AnimalImageURLResolver.Source.DOG_CEO));
        return RespondTask.of(event, "正在获取阿拉斯加>>>>>>>", INSTANCE);
    }

    static RespondTask getGSD(MessageEvent event) {
        INSTANCE.executor.submit(new AnimalImagePusher(event, ImageSource.DOG_CEO_GSD, "德牧", AnimalImageURLResolver.Source.DOG_CEO));
        return RespondTask.of(event, "正在获取德牧>>>>>>>", INSTANCE);
    }

    static RespondTask getSamoyed(MessageEvent event) {
        INSTANCE.executor.submit(new AnimalImagePusher(event, ImageSource.DOG_CEO_SAMOYED, "萨摩耶", AnimalImageURLResolver.Source.DOG_CEO));
        return RespondTask.of(event, "正在获取萨摩耶>>>>>>>", INSTANCE);
    }

    static RespondTask getDoberman(MessageEvent event) {
        INSTANCE.executor.submit(new AnimalImagePusher(event, ImageSource.DOG_CEO_DOBERMAN, "杜宾", AnimalImageURLResolver.Source.DOG_CEO));
        return RespondTask.of(event, "正在获取杜宾>>>>>>>", INSTANCE);
    }

    static RespondTask getCat(MessageEvent event) {
        INSTANCE.executor.submit(new AnimalImagePusher(event, ImageSource.SHIBE_ONLINE_CAT, "猫", AnimalImageURLResolver.Source.SHIBE_ONLINE));
        return RespondTask.of(event, "正在获取猫咪>>>>>>>", INSTANCE);
    }


    @Override
    public String getName() {
        return "OK Animal";
    }


    @Override
    public boolean match(MessageEvent event) {
        for (Pattern pattern : PATTERN_SUPPLIER_MAP.keySet()) {
            if (pattern.matcher(event.getMessage().contentToString()).matches())
                return true;
        }
        return false;
    }

    @Override
    public RespondTask handle(MessageEvent event) throws NoHandlerMethodMatchException {
        for (Map.Entry<Pattern, Function<MessageEvent, RespondTask>> entry : PATTERN_SUPPLIER_MAP.entrySet()) {
            if (entry.getKey().matcher(event.getMessage().contentToString()).matches()) {
                return entry.getValue().apply(event);
            }
        }
        throw new NoHandlerMethodMatchException("匹配动物图片", event);
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return TYPES;
    }


    @Override
    public void onclose() {
        executor.shutdown();
    }


}
