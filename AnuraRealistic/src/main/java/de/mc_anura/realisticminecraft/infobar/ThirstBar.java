package de.mc_anura.realisticminecraft.infobar;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.NotNull;

public class ThirstBar extends InfoBar<ThirstPlayer> {

    public ThirstBar(@NotNull ThirstPlayer p, @NotNull BarStatus status) {
        super(p, "Durst", BarColor.BLUE, BarStyle.SOLID, status);
    }

    @Override
    protected boolean stayCondition() {
        return super.stayCondition() || player.isNoSprint();
    }
}
