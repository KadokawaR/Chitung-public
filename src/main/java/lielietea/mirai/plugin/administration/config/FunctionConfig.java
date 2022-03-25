package lielietea.mirai.plugin.administration.config;

public class FunctionConfig{
    boolean FursonaPunk;
    boolean LotteryWinner;
    boolean LotteryBummer;
    boolean LotteryC4;
    boolean MealPicker;
    boolean FortuneTeller;
    boolean PlayDice;
    boolean AntiOverwatch;
    boolean AntiDirtyWord;
    boolean GreetingAndGoodbye;
    boolean HeroLinesSelector;
    boolean LovelyImage;
    boolean FurryGamesIndex;
    boolean FeedBack;

    public FunctionConfig(){
        this.FursonaPunk=true;
        this.LotteryWinner=true;
        this.LotteryBummer=true;
        this.LotteryC4=true;
        this.MealPicker=true;
        this.FortuneTeller=true;
        this.PlayDice=true;
        this.AntiOverwatch=true;
        this.AntiDirtyWord=true;
        this.GreetingAndGoodbye=true;
        this.HeroLinesSelector=false;
        this.LovelyImage=false;
        this.FurryGamesIndex=false;
        this.FeedBack=false;
    }

    public boolean isFursonaPunk() {
        return FursonaPunk;
    }

    public void setFursonaPunk(boolean fursonaPunk) {
        FursonaPunk = fursonaPunk;
    }

    public boolean isLotteryWinner() {
        return LotteryWinner;
    }

    public void setLotteryWinner(boolean lotteryWinner) {
        LotteryWinner = lotteryWinner;
    }

    public boolean isLotteryBummer() {
        return LotteryBummer;
    }

    public void setLotteryBummer(boolean lotteryBummer) {
        LotteryBummer = lotteryBummer;
    }

    public boolean isLotteryC4() {
        return LotteryC4;
    }

    public void setLotteryC4(boolean lotteryC4) {
        LotteryC4 = lotteryC4;
    }

    public boolean isMealPicker() {
        return MealPicker;
    }

    public void setMealPicker(boolean mealPicker) {
        MealPicker = mealPicker;
    }

    public boolean isFortuneTeller() {
        return FortuneTeller;
    }

    public void setFortuneTeller(boolean fortuneTeller) {
        FortuneTeller = fortuneTeller;
    }

    public boolean isPlayDice() {
        return PlayDice;
    }

    public void setPlayDice(boolean playDice) {
        PlayDice = playDice;
    }

    public boolean isAntiOverwatch() {
        return AntiOverwatch;
    }

    public void setAntiOverwatch(boolean antiOverwatch) {
        AntiOverwatch = antiOverwatch;
    }

    public boolean isAntiDirtyWord() {
        return AntiDirtyWord;
    }

    public void setAntiDirtyWord(boolean antiDirtyWord) {
        AntiDirtyWord = antiDirtyWord;
    }

    public boolean isGreetingAndGoodbye() {
        return GreetingAndGoodbye;
    }

    public void setGreetingAndGoodbye(boolean greetingAndGoodbye) {
        GreetingAndGoodbye = greetingAndGoodbye;
    }

    public boolean isHeroLinesSelector() {
        return HeroLinesSelector;
    }

    public void setHeroLinesSelector(boolean heroLinesSelector) {
        HeroLinesSelector = heroLinesSelector;
    }

    public boolean isLovelyImage() {
        return LovelyImage;
    }

    public void setLovelyImage(boolean lovelyImage) {
        LovelyImage = lovelyImage;
    }

    public boolean isFurryGamesIndex() {
        return FurryGamesIndex;
    }

    public void setFurryGamesIndex(boolean furryGamesIndex) {
        FurryGamesIndex = furryGamesIndex;
    }

    public boolean isFeedBack() {
        return FeedBack;
    }

    public void setFeedBack(boolean feedBack) {
        FeedBack = feedBack;
    }
}