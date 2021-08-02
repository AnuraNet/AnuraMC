package de.mc_anura.realisticminecraft.infobar;

import de.mc_anura.core.AnuraThread;
import java.util.Objects;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;

public abstract class Infobar<T extends RealisticPlayer> {

    protected final T player;
    protected final BossBar bossbar;
    private BarStatus barstatus;
    private byte display_time = 0;
    private BukkitTask task = null;

    public Infobar(T p, String title, BarColor color, BarStyle style, BarStatus status) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(status);
        player = p;
        bossbar = Bukkit.createBossBar(title, color, style);
        bossbar.addPlayer(player.getPlayer());
        barstatus = status;
        update();
    }

    public BarStatus getBarStatus() {
        return barstatus;
    }

    public void setBarStatus(BarStatus status) {
        Objects.requireNonNull(status);
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
        startCountdown((byte) 5);
    }

    protected boolean stayCondition() {
        return getBarStatus().equals(BarStatus.AN);
    }

    protected void startCountdown(byte time) {
        display_time = time;
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
