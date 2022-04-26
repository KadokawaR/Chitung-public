package mirai.chitung.plugin.core.game.montecarlo.taisai;

import java.util.Map;

public class TaiSaiData {

    int specificNumber;
    TaiSaiBetType type;

    TaiSaiData(int specificNumber, TaiSaiBetType type){
        this.type = type;
        this.specificNumber=specificNumber;
    }

}
