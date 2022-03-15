package lielietea.mirai.plugin.core.game.montecarlo.horserace;

import lielietea.mirai.plugin.utils.fileutils.Touch;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class HorseRace {
    private static final HorseRace INSTANCE;
    final private static String HORSE_RACE_RECORD_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "horserace.json";
    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    static {
        INSTANCE = new HorseRace();
        Touch.file(HORSE_RACE_RECORD_PATH);
    }

    public static HorseRace getINSTANCE() {
        return INSTANCE;
    }

    void wtf(){

    }
}
