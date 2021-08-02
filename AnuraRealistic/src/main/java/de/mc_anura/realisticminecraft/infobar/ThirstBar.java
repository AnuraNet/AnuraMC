package de.mc_anura.realisticminecraft.infobar;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public class ThirstBar extends Infobar<ThirstPlayer> {

    public ThirstBar(ThirstPlayer p, BarStatus status) {
        super(p, "Durst", BarColor.BLUE, BarStyle.SOLID, status);
    }

    @Override
    protected boolean stayCondition() {
        return super.stayCondition() || player.isNoSprint();
    }
}
