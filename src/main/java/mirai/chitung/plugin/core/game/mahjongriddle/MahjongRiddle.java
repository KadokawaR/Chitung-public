package mirai.chitung.plugin.core.game.mahjongriddle;

import mirai.chitung.plugin.core.responder.mahjong.FortuneTeller;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MahjongRiddle {
    static final Lock lock = new ReentrantLock(true);
    static final int RIDDLE_LENGTH = 5;

    static final Random rand = new Random();

    static class RiddleFactor {
        int[] answerNum;
        boolean[] isGuessed;
        int id;
    }
    static Timer timer = new Timer();

    //static final Timer timer = new Timer(true);
    static final Map<Long, RiddleFactor> riddleSessionHolder = new HashMap<>();

    static final ArrayList<String> chineseNum = new ArrayList<>(Arrays.asList(
            "一", "二", "三", "四", "五", "六", "七", "八", "九"));

    //生成小于108(只到筒条万)的若干个随机数，用于生成麻将牌
    public static int[] getRandomNum(int num) {
        int[] randomNum = new int[num];
        for (int i = 0; i < num; i++) {
            randomNum[i] = rand.nextInt(108);
        }
        Arrays.sort(randomNum);
        return randomNum;
    }

    //通过数组获得正式麻将名称
    public static String[] resolveRandomTiles(int[] num) {
        String[] Tiles = new String[num.length];
        for (int i = 0; i < num.length; i++) {
            Tiles[i] = FortuneTeller.getMahjong(num[i]);
        }
        return Tiles;
    }

    //用一个String生成连图
    public static BufferedImage getTileImage(String[] Tiles) throws IOException {

        boolean randomBool = new Random().nextBoolean();
        BufferedImage img0 = null;

        for (String tile : Tiles) {
            String MAHJONG_PIC_PATH = "/pics/mahjong/";

            if(randomBool){
                MAHJONG_PIC_PATH += "Red/";
            } else {
                MAHJONG_PIC_PATH += "Yellow/";
            }
            MAHJONG_PIC_PATH = MAHJONG_PIC_PATH + tile + ".png";

            InputStream is = MahjongRiddle.class.getResourceAsStream(MAHJONG_PIC_PATH);
            BufferedImage img = ImageIO.read(is);
            int w1 = 0;
            int h1 = img.getHeight();
            if (img0 != null) {
                w1 = img0.getWidth();
            }
            int w2 = img.getWidth();
            int h2 = img.getHeight();
            // 从图片中读取RGB
            int[] ImageArrayOne = new int[w1 * h1];
            if (img0 != null) {
                ImageArrayOne = img0.getRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 逐行扫描图像中各个像素的RGB到数组中
            }
            int[] ImageArrayTwo = new int[w2 * h2];
            ImageArrayTwo = img.getRGB(0, 0, w2, h2, ImageArrayTwo, 0, w2);

            // 生成新图片
            BufferedImage img1;
            img1 = new BufferedImage(w1 + w2, h1, BufferedImage.TYPE_INT_RGB);
            if (img0 != null) {
                img1.setRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            }
            img1.setRGB(w1, 0, w2, h2, ImageArrayTwo, 0, w2);
            img0 = img1;
        }
        return img0;
    }

    //麻将图片发送测试
    public static void sendTileImage(BufferedImage image, GroupMessageEvent event) throws IOException {
        InputStream is;
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageOutputStream imOut;
        imOut = ImageIO.createImageOutputStream(bs);
        ImageIO.write(image, "png", imOut);
        is = new ByteArrayInputStream(bs.toByteArray());
        event.getSubject().sendMessage(Contact.uploadImage(event.getSubject(), is));
    }


    //判定消息里面是否有答案
    public static boolean gotAnswer(int[] answerNum, GroupMessageEvent event) {
        String[] answer = transformAnswer(answerNum);
        for (String s : answer) {
            if (event.getMessage().contentToString().equals(s)) {
                return true;
            }
        }
        return false;
    }

    //如果猜中了则改变rf里面的boolean数组
    public static RiddleFactor setIsGuessed(RiddleFactor rf, GroupMessageEvent event) {
        String[] answer = transformAnswer(rf.answerNum);
        for (int i = 0; i < rf.answerNum.length; i++) {
            if (event.getMessage().contentToString().contains(answer[i])) {
                rf.isGuessed[i] = true;
            }
        }
        return rf;
    }

    //把缺德的万字和东字转成简体
    public static String[] transformAnswer(int[] answerNum) {
        String[] transformedAnswer = new String[answerNum.length];
        for (int i = 0; i < answerNum.length; i++) {
            if ((answerNum[i] >= 72) && (answerNum[i] < 108)) {
                transformedAnswer[i] = (chineseNum.get(Math.toIntExact(answerNum[i] % 9)) + "万");
            } else if (((answerNum[i] >= 108) && (answerNum[i] < 124)) && (answerNum[i] % 4 == 0)) {
                transformedAnswer[i] = "东风";
            } else {
                transformedAnswer[i] = FortuneTeller.getMahjong(answerNum[i]);
            }
        }
        return transformedAnswer;
    }

    //boolean数组里如果全部true就返回true
    public static boolean isAllTrue(boolean[] isGuessed) {
        for (boolean i : isGuessed) {
            if (!i) {
                return false;
            }
        }
        return true;
    }

    //根据boolean[] isGuessed 来替换 String[] answer
    public static String[] displayAnswer(boolean[] isGuessed, String[] transformAnswer) {
        for (int i = 0; i < transformAnswer.length; i++) {
            if (!isGuessed[i]) {
                transformAnswer[i] = "无字";
            }
        }
        return transformAnswer;
    }

    //读取字符串里出现了几次特定字符
    public static int matchNumber(String string, String foundString){
        String[] split = string.split("");
        return (int) Arrays.stream(split).filter(s -> s.equals(foundString)).count();
    }

    //临时性地检测是否是麻将牌用语
    public static boolean isMahjongTile(GroupMessageEvent event) {
        String str = event.getMessage().contentToString();
        int sum = matchNumber(str,"风")+matchNumber(str,"万")+matchNumber(str,"筒")+matchNumber(str,"条");
        //判断是否出现且只出现了一次
        return sum==1;
    }

    //将int[]转换成一个存储中文数字的String[]
    public static String turnIntoChineseNum(RiddleFactor rf) {
        StringBuilder shuziStr = new StringBuilder();
        for (int i = 0; i < rf.answerNum.length; i++) {
            shuziStr.append(chineseNum.get(rf.answerNum[i] % 9));
            if (i != rf.answerNum.length - 1) {
                shuziStr.append("、");
            }
        }
        return shuziStr.toString();
    }

    public static void riddleStart(GroupMessageEvent event){

        //lock.lock();
        //try{
        if (event.getMessage().contentToString().contains("猜麻将")) {
            event.getSubject().sendMessage("来猜麻将吧！\n\n七筒会随机生成5张麻将牌（只含筒牌、条牌和万字牌），猜中最后一张的会是赢家！" +
                    "\n请注意，只有形式诸如“三条”、“五筒”、“七万”的答案会触发判定。\n如果没有人猜中，本轮游戏会在180秒内自动关闭。");

            //检测是否有该群的flag，如果没有则重新生成并在180s之后清空
            if (!riddleSessionHolder.containsKey(event.getGroup().getId())) {

                RiddleFactor rf = new RiddleFactor();
                rf.answerNum = getRandomNum(RIDDLE_LENGTH);
                rf.isGuessed = new boolean[RIDDLE_LENGTH];
                int sessionId = rand.nextInt(10086);
                rf.id = sessionId;

                riddleSessionHolder.put(event.getGroup().getId(), rf);
                BufferedImage img = null;
                try {
                    img = getTileImage(displayAnswer(rf.isGuessed, transformAnswer(rf.answerNum)));
                    sendTileImage(img, event);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                event.getSubject().sendMessage("麻将牌上的数字分别为：" + turnIntoChineseNum(rf));

                //180s清空谜语重置标记
                timer.schedule(new EndSessionTimerTask(sessionId, event), 180 * 1000);
                return;
            }
        }

        if (isMahjongTile(event) && riddleSessionHolder.containsKey(event.getGroup().getId())) {
            riddleSessionHolder.get(event.getGroup().getId()).isGuessed = setIsGuessed(riddleSessionHolder.get(event.getGroup().getId()), event).isGuessed;
            //
            if (gotAnswer(riddleSessionHolder.get(event.getGroup().getId()).answerNum, event)) {
                //检测这次结束之后是否全中，全中了则删除该flag
                if (isAllTrue(riddleSessionHolder.get(event.getGroup().getId()).isGuessed)) {
                    event.getSubject().sendMessage((new At(event.getSender().getId())).plus("猜中了！恭喜！"));
                    BufferedImage img = null;
                    try {
                        img = getTileImage(displayAnswer(riddleSessionHolder.get(event.getGroup().getId()).isGuessed, resolveRandomTiles(riddleSessionHolder.get(event.getGroup().getId()).answerNum)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        sendTileImage(img, event);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    MahjongRiddle.riddleSessionHolder.remove(event.getGroup().getId());
                    return;
                }
                event.getSubject().sendMessage((new At(event.getSender().getId())).plus("中了!"));
                return;
            }

        }
    }

}
