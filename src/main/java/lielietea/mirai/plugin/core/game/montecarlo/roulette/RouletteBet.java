package lielietea.mirai.plugin.core.game.montecarlo.roulette;

public class RouletteBet {
    IndicatorProcessor.Indicator indicator;
    Integer location;
    IndicatorProcessor.Status status;

    RouletteBet(IndicatorProcessor.Indicator indicator, Integer location, IndicatorProcessor.Status status){
        this.indicator=indicator;
        this.location=location;
        this.status=status;
    }
}
