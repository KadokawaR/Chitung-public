package lielietea.thevaultnorthkinchovsk.vault;

import java.math.RoundingMode;

public final class BalanceDisplayFormat {
    private final int scale;
    private final RoundingMode roundingMode;

    public BalanceDisplayFormat(int scale, RoundingMode roundingMode) {
        this.scale = scale;
        this.roundingMode = roundingMode;
    }

    public static BalanceDisplayFormat STANDARD = new BalanceDisplayFormat(2,RoundingMode.FLOOR);
    public static BalanceDisplayFormat INTEGER = new BalanceDisplayFormat(0,RoundingMode.FLOOR);

    public int getScale() {
        return scale;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
}
