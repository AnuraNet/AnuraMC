package de.mc_anura.realisticminecraft.infobar;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.NotNull;

public class TemperatureBar extends InfoBar<TemperaturePlayer> {

    public TemperatureBar(@NotNull TemperaturePlayer p, @NotNull BarStatus status) {
        super(p, "Temperatur", BarColor.GREEN, BarStyle.SOLID, status);
    }

    @Override
    public void update() {
        super.update();
        bossbar.setTitle("Temperatur (" + player.getValue() + "°M)");
        if (player.isExtremeValue()) {
            bossbar.setColor(BarColor.RED);
        } else if (player.isWarning()) {
            bossbar.setColor(BarColor.YELLOW);
        } else {
            bossbar.setColor(BarColor.GREEN);
        }
    }

    @Override
    protected boolean stayCondition() {
        return super.stayCondition() || player.isWarning();
    }
}
