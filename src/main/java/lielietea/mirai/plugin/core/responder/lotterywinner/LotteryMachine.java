package lielietea.mirai.plugin.core.responder.lotterywinner;

import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.utils.StandardTimeUtil;
import lielietea.mirai.plugin.utils.image.ImageCreater;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.PlainText;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LotteryMachine {
    static final Timer TIMER = new Timer(true);
    static final Map<Long, Boolean> C4_ACTIVATION_FLAGS = new HashMap<>();
    static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(10);
    static final Random rand = new Random();

    static {
        //每日6点定时清空C4触发标记
        TIMER.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               LotteryMachine.C4_ACTIVATION_FLAGS.clear();
                           }
                       },
                StandardTimeUtil.getStandardFirstTime(0, 0, 1),
                StandardTimeUtil.getPeriodLengthInMS(1, 0, 0, 0));
    }

    public static boolean botPermissionChecker(GroupMessageEvent event) {
        return ((event.getGroup().getBotPermission().equals(MemberPermission.ADMINISTRATOR)) || (event.getGroup().getBotPermission().equals(MemberPermission.OWNER)));
    }

    public static boolean senderPermissionChecker(GroupMessageEvent event) {
        return ((event.getSender().getPermission().equals(MemberPermission.ADMINISTRATOR)) || (event.getSender().getPermission().equals(MemberPermission.OWNER)));
    }

    public static RespondTask okBummer(GroupMessageEvent event, RespondTask.Builder builder) {
        if (botPermissionChecker(event)) {
            //抽取倒霉蛋
            List<NormalMember> candidates = event.getGroup().getMembers().stream().filter(normalMember -> normalMember.getPermission().equals(MemberPermission.MEMBER)).collect(Collectors.toList());
            if(candidates.isEmpty()){
                builder.addMessage("全都是管理员的群你让我抽一个普通成员禁言？别闹。");
                return builder.build();
            }
            NormalMember victim = candidates.get(rand.nextInt(candidates.size()));

            //禁言倒霉蛋
            builder.addTask(() -> victim.mute(120));

            //如果发送者不是管理员，那么发送者也将被禁言
            if (!(senderPermissionChecker(event))) {
                builder.addTask(() -> event.getSender().mute(120));
            }

            if (victim.getId() == event.getSender().getId()) {
                builder.addMessage("Ok Bummer! " + victim.getNick() + "\n" +
                        event.getSender().getNick() + "尝试随机极限一换一。他成功把自己换出去了！");
            } else if ((senderPermissionChecker(event))) {
                //如果发送者是管理员，那么提示
                builder.addMessage(new PlainText("Ok Bummer! " + victim.getNick() + "\n管理员")
                        .plus(new At(event.getSender().getId()))
                        .plus(new PlainText(" 随机带走了 "))
                        .plus(new At(victim.getId())));
            } else {
                //如果发送者不是管理员，那么提示
                builder.addMessage(new PlainText("Ok Bummer! " + victim.getNick() + "\n")
                        .plus(new At(event.getSender().getId()))
                        .plus(new PlainText(" 以自己为代价随机带走了 "))
                        .plus(new At(victim.getId())));
            }
            return builder.build();
        } else {
            builder.addMessage("七筒目前还没有管理员权限，请授予七筒权限解锁更多功能。");
            builder.addNote("群 " + event.getGroup().getId() + " 尝试发起Bummer功能，但该群未授予Bot管理员权限。");
            return builder.build();
        }
    }

    public static RespondTask okWinner(GroupMessageEvent event, RespondTask.Builder builder) {

        //获取当日幸运数字
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        long numOfTheDay = (year + month * 10000 + date * 1000000) * 100000000000L / event.getGroup().getId();

        //获取当日幸运儿
        List<NormalMember> candidates = new ArrayList<>(event.getGroup().getMembers());
        long guyOfTheDay = numOfTheDay % candidates.size();

        builder.addMessage("Ok Winner! " + candidates.get(Math.toIntExact(guyOfTheDay)).getNick());
        builder.addTask(() -> {
            try {
                ImageCreater.sendImage(ImageCreater.createWinnerImage(candidates.get(Math.toIntExact(guyOfTheDay))), event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return builder.build();
    }

    public static RespondTask okC4(GroupMessageEvent event, RespondTask.Builder builder) {
        if (botPermissionChecker(event)) {
            if (!C4_ACTIVATION_FLAGS.getOrDefault(event.getGroup().getId(), false)) {
                double ratio = 1D / Math.sqrt(event.getGroup().getMembers().size());

                if (rand.nextDouble() < ratio) {
                    //禁言全群
                    builder.addTask(() -> event.getGroup().getSettings().setMuteAll(true));
                    builder.addMessage("中咧！");
                    builder.addMessage(new At(event.getSender().getId()).plus("成功触发了C4！大家一起恭喜TA！"));
                    C4_ACTIVATION_FLAGS.put(event.getGroup().getId(), true);

                    //设置5分钟后解禁
                    builder.addTask(() -> EXECUTOR.schedule(() -> event.getGroup().getSettings().setMuteAll(false), StandardTimeUtil.getPeriodLengthInMS(0, 0, 5, 0), TimeUnit.MILLISECONDS));


                } else {
                    builder.addMessage(new At(event.getSender().getId()).plus("没有中！"));
                }
            } else {
                builder.addMessage(new At(event.getSender().getId()).plus("今日的C4已经被触发过啦！请明天再来尝试作死！"));
            }
            return builder.build();
        } else {
            builder.addMessage("七筒目前还没有管理员权限，请授予七筒权限解锁更多功能。");
            builder.addNote("群 " + event.getGroup().getId() + " 尝试发起C4功能，但该群未授予Bot管理员权限。");
            return builder.build();
        }
    }
}