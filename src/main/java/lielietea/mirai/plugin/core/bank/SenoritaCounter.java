package lielietea.mirai.plugin.core.bank;

import lielietea.thevaultnorthkinchovsk.vault.BalanceDisplayFormat;
import lielietea.thevaultnorthkinchovsk.vault.Currency;
import lielietea.thevaultnorthkinchovsk.vault.EasyVault;
import lielietea.thevaultnorthkinchovsk.vault.Vault;
import net.mamoe.mirai.event.events.MessageEvent;

import java.io.File;
import java.math.BigDecimal;

public class SenoritaCounter {
    final static String BANK_RECORD_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "bankrecord.json";
    private static final Vault<Long, Currency> VAULT;

    static{
        // 初始化 Vault实例
        VAULT = new EasyVault(new File(BANK_RECORD_PATH), BalanceDisplayFormat.INTEGER);
    }

    //通过ID和kind获得具体数量
    public static BigDecimal getCertainNumber(long ID, Currency kind){
        return VAULT.get(ID,kind);
    }

    //获取格式化的展示数量
    public static String getDisplayNumber(long ID, Currency kind){
        return VAULT.balance(ID,kind);
    }


    public static void go(MessageEvent event){
        PumpkinPesoWindow.checkMoney(event);
        PumpkinPesoWindow.moneyLaundry(event);
    }

    public static Vault<Long, Currency> getVAULT() {
        return VAULT;
    }
}
