package de.mc_anura.realisticminecraft.infobar;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TemperaturePlayer extends RealisticPlayer {

    private static final short MIN_TEMP_WARNING = 20;
    private static final short MAX_TEMP_WARNING = 40;
    private static final short MIN_TEMP = 15;
    private static final short MAX_TEMP = 45;
    private static final short DEFAULT_TEMP = 30;

    private static final short MIN_TEMP_DRINK = 30;
    private static final short MAX_TEMP_EAT = 35;

    public TemperaturePlayer(@NotNull Player p, float temp, @NotNull BarStatus status) {
        super(p, temp, MIN_TEMP, MAX_TEMP, DEFAULT_TEMP);
        bar = new TemperatureBar(this, status);
    }

    public boolean isWarning() {
        return value >= MAX_TEMP_WARNING || value <= MIN_TEMP_WARNING;
    }

    @Override
    public float calculateNewValue(Location l) {
        float modifier = TemperatureModifier.getModifier(player, l, this);
        float returnField = modifier == 0 ? getValue() : addValue(modifier);
        if (value >= MAX_TEMP_WARNING) {
            ValueHolder vh = ValueHolder.getValueHolder(player);
            if (vh != null) {
                ThirstPlayer tp = vh.getPlayer(ThirstPlayer.class);
                if (tp != null) {
                    tp.addValue(-1);
                }
            }
        }
        return returnField;
    }

    public float drink() {
        return value > MIN_TEMP_DRINK ? addValue(TemperatureModifier.DRINK_MOD) : getValue();
    }

    public float eatSoup() {
        return value < MAX_TEMP_EAT ? addValue(TemperatureModifier.EAT_MOD) : getValue();
    }

    @Override
    protected boolean blockModify() {
        return false;
    }

    @Override
    public String getTableName() {
        return getStaticTableName();
    }

    public static String getStaticTableName() {
        return "playerTemperature";
    }
}
