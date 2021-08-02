package de.mc_anura.realisticminecraft.infobar;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;

public class InfobarUtil {

    private static int round = 1;

    public static void enableInfobarTasks() {
        JavaPlugin instance = RealisticMinecraft.getInstance();
        AnuraThread.add(Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            Bukkit.getOnlinePlayers().stream().filter((p) -> (p.getGameMode().equals(GameMode.SURVIVAL) && !p.isDead())).forEach((p) -> {
                ValueHolder vh = ValueHolder.getValueHolder(p);
                if (vh == null) {
                    return;
                }
                ThirstPlayer thP = vh.getPlayer(ThirstPlayer.class);
                if (thP != null && vh.getPlayer(TemperaturePlayer.class) != null) {
                    thP.calculateNewValue(p.getLocation());
                }
                if (round == 5) {
                    TemperaturePlayer tempP = vh.getPlayer(TemperaturePlayer.class);
                    if (tempP != null) {
                        tempP.calculateNewValue(p.getLocation());
                        if (tempP.isMIN()) {
                            p.sendActionBar(Component.text("Dir ist zu kalt!", NamedTextColor.RED));
                            AnuraThread.queueSync(() -> p.damage(0.5));
                        } else if (tempP.isMAX()) {
                            p.sendActionBar(Component.text("Dir ist zu heiß!", NamedTextColor.RED));
                            AnuraThread.queueSync(() -> p.damage(0.5));
                        }
                    }
                    if (thP != null) {
                        if (thP.getValue() == 0) {
                            p.sendActionBar(Component.text("Du bist durstig, trinke etwas!", NamedTextColor.RED));
                            if (p.getHealth() - 0.5 <= 0) {
                            }
                            AnuraThread.queueSync(() -> p.damage(0.5));
                        } else if (thP.isThirsty() || thP.isWeak()) {
                            p.sendActionBar(Component.text("Du bist durstig, trinke etwas!", NamedTextColor.YELLOW));
                        }
                    }
                }
            });
            if (round == 5) {
                round = 0;
            }
            round++;
        }, 20, 20));

        AnuraThread.add(Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> Bukkit.getOnlinePlayers().stream().filter((p) -> (p.getGameMode().equals(GameMode.SURVIVAL) && !p.isDead())).forEach((p) -> {
            ValueHolder vh = ValueHolder.getValueHolder(p);
            if (vh == null) {
                return;
            }
            TemperaturePlayer tmp = vh.getPlayer(TemperaturePlayer.class);
            if (tmp != null) {
                tmp.updateDatabase(false);
            }
            ThirstPlayer thp = vh.getPlayer(ThirstPlayer.class);
            if (thp != null) {
                thp.updateDatabase(false);
            }
        }), 20 * 60, 20 * 60));
    }
}
