package de.mc_anura.realisticminecraft.infobar;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class InfoBar<T extends RealisticPlayer> {

    private static final byte TIMEOUT = 5;

    protected final T player;
    protected final BossBar bossbar;
    private BarStatus barstatus;
    private byte display_time = 0;
    private BukkitTask task = null;

    public InfoBar(@NotNull T p, @Nullable String title, @NotNull BarColor color, @NotNull BarStyle style, @NotNull BarStatus status) {
        player = p;
        bossbar = Bukkit.createBossBar(title, color, style);
        bossbar.addPlayer(player.getPlayer());
        barstatus = status;
        update();
    }

    public @NotNull BarStatus getBarStatus() {
        return barstatus;
    }

    public void setBarStatus(@NotNull BarStatus status) {
        barstatus = status;
        update();
        player.updateDatabase(true);
    }

    private void setBarVisible(boolean visible) {
        if (stayCondition()) {
            bossbar.setVisible(true);
        } else {
            bossbar.setVisible(visible);
        }
    }

    protected void destroy() {
        if (bossbar.getPlayers().contains(player.getPlayer())) {
            bossbar.removePlayer(player.getPlayer());
        }
        task.cancel();
    }

    public void update() {
        bossbar.setProgress((player.getValue() - player.getMIN()) / (player.getMAX() - player.getMIN()));
        setBarVisible(true);
        startCountdown();
    }

    protected boolean stayCondition() {
        return getBarStatus().equals(BarStatus.IMMER);
    }

    protected void startCountdown() {
        display_time = TIMEOUT;
        if (task == null) {
            task = Bukkit.getScheduler().runTaskTimerAsynchronously(RealisticMinecraft.getInstance(), () -> {
                if (display_time > 0) {
                    display_time--;
                } else {
                    setBarVisible(false);
                }
            }, 20, 20);
            AnuraThread.add(task);
        }
    }
}
