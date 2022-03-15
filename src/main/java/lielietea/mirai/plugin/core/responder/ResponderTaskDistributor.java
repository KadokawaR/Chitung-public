package lielietea.mirai.plugin.core.responder;

import lielietea.mirai.plugin.utils.MessageUtil;
import lielietea.mirai.plugin.utils.StandardTimeUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ResponderTaskDistributor {
    final static int GROUP_MESSAGE_LIMIT_PER_MIN = 10;
    final static int PERSONAL_MESSAGE_LIMIT_PER_MIN = 5;
    final static int PERSONAL_MESSAGE_LIMIT_PER_DAY = 40;
    final static int DAILY_MESSAGE_LIMIT = 4800;
    final static ExecutorService ISOLATED_EXECUTOR = Executors.newSingleThreadExecutor();
    static final CacheThreshold groupThreshold = new CacheThreshold(GROUP_MESSAGE_LIMIT_PER_MIN);
    static final CacheThreshold personalThreshold = new CacheThreshold(PERSONAL_MESSAGE_LIMIT_PER_MIN);
    static final CacheThreshold dailyThreshold = new CacheThreshold(DAILY_MESSAGE_LIMIT);
    static final CacheThreshold personalDailyThreshold = new CacheThreshold(PERSONAL_MESSAGE_LIMIT_PER_DAY);
    final Timer thresholdReset1 = new Timer(true);
    final Timer thresholdReset2 = new Timer(true);
    final ExecutorService executor;
    final Bot bot;

    public ResponderTaskDistributor(Bot bot) {
        thresholdReset1.schedule(new TimerTask() {
                                     @Override
                                     public void run() {
                                         groupThreshold.clearCache();
                                         personalThreshold.clearCache();
                                     }
                                 }, StandardTimeUtil.getPeriodLengthInMS(0, 0, 0, 1),
                StandardTimeUtil.getPeriodLengthInMS(0, 0, 1, 0));
        thresholdReset2.schedule(new TimerTask() {
                                     @Override
                                     public void run() {
                                         dailyThreshold.clearCache();
                                         personalDailyThreshold.clearCache();
                                         MessageUtil.notifyDevGroup(bot.getNick()+"的 ResponderTaskDistributor 每日计数器已经重置。", bot);
                                     }
                                 }, StandardTimeUtil.getStandardFirstTime(0, 0, 1),
                StandardTimeUtil.getPeriodLengthInMS(1, 0, 0, 0));
        this.executor = Executors.newCachedThreadPool();
        this.bot = bot;
    }

    public void handleMessage(MessageEvent event) {
        // 需要没有达到消息数限制
        if (!reachLimit(event)) {
            Optional<UUID> boxedHandler = ResponderManager.getINSTANCE().match(event);
            if (boxedHandler.isPresent()) {
                {
                    RespondTask temp = ResponderManager.getINSTANCE().handle(event, boxedHandler.get());
                    addToThreshold(temp);
                    handleResponderTask(temp);
                }
            }
        }
    }

    public static void handleIsolatedResponderTask(RespondTask messageChainPackage){
        ISOLATED_EXECUTOR.submit(messageChainPackage::execute);
    }

    // 检测是否达到发送消息数量上限
    boolean reachLimit(MessageEvent event) {
        if (dailyThreshold.reachLimit(0)) return true;
        if (event instanceof GroupMessageEvent) {
            if (groupThreshold.reachLimit(event.getSubject().getId()))
                return true;
        }
        return personalThreshold.reachLimit(event.getSender().getId()) || personalDailyThreshold.reachLimit(event.getSender().getId());
    }

    void addToThreshold(RespondTask respondTask) {
        if (respondTask.getSource() instanceof Group)
            groupThreshold.count(respondTask.getSource().getId());
        personalThreshold.count(respondTask.getSender().getId());
        personalDailyThreshold.count(respondTask.getSender().getId());
        dailyThreshold.count(0);
    }

    void handleResponderTask(RespondTask respondTask) {
        executor.submit(respondTask::execute);
    }

    public void close() {
        executor.shutdown();
        if(!ISOLATED_EXECUTOR.isShutdown()){
            ISOLATED_EXECUTOR.shutdown();
        }
    }

    public static class CacheThreshold {
        final Map<Long, Integer> data = new HashMap<>();
        final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        final Lock readLock = readWriteLock.readLock();
        final Lock writeLock = readWriteLock.writeLock();
        final int limit;

        CacheThreshold(int limit) {
            this.limit = limit;
        }

        public void clearCache() {
            writeLock.lock();
            try {
                data.clear();
            } finally {
                writeLock.unlock();
            }
        }

        public void count(long id) {
            writeLock.lock();
            try {
                data.put(id, data.getOrDefault(id, 0) + 1);
            } finally {
                writeLock.unlock();
            }
        }

        public int get(long id) {
            readLock.lock();
            try {
                return data.getOrDefault(id, 0);
            } finally {
                readLock.unlock();
            }
        }

        public boolean reachLimit(long id) {
            readLock.lock();
            try {
                return data.getOrDefault(id, 0) >= limit;
            } finally {
                readLock.unlock();
            }
        }
    }
}
