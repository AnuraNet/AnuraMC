package de.mc_anura.realisticminecraft.listener;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.events.AnuraLeaveEvent;
import de.mc_anura.core.tools.Potions;
import de.mc_anura.core.tools.Potions.CustomPotion;
import de.mc_anura.realisticminecraft.infobar.InfoBarUtil;
import de.mc_anura.realisticminecraft.infobar.TemperaturePlayer;
import de.mc_anura.realisticminecraft.infobar.ThirstPlayer;
import de.mc_anura.realisticminecraft.infobar.ValueHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class InfoBar implements Listener {

    private static final CustomPotion POTION_HUNGER_I = new CustomPotion(new PotionEffect(PotionEffectType.SLOW_DIGGING, 0, 0), true);
    private static final CustomPotion POTION_HUNGER_II = new CustomPotion(new PotionEffect(PotionEffectType.SLOW, 0, 0), true);

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent e) {
        if (e.getEntity().getHealth() <= 0) {
            InfoBarUtil.DeathCause deathCause = InfoBarUtil.popDeathCause(e.getEntity());
            if (deathCause != null) {
                String message = switch (deathCause) {
                    case HOT -> " starb den Hitzetod";
                    case COLD -> " ist erfroren";
                    case THIRST -> " ist verdurstet";
                };
                e.deathMessage(e.getEntity().displayName().append(Component.text(message)));
            }
            AnuraThread.async(() -> {
                final ValueHolder vh = ValueHolder.getValueHolder(e.getEntity());
                if (vh != null) {
                    vh.resetHoldings();
                }
                Potions.removeCustomPotion(e.getEntity(), POTION_HUNGER_I);
                Potions.removeCustomPotion(e.getEntity(), POTION_HUNGER_II);
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemConsume(@NotNull PlayerItemConsumeEvent e) {
        final ValueHolder vh = ValueHolder.getValueHolder(e.getPlayer());
        if (vh == null) {
            return;
        }
        final TemperaturePlayer tp = vh.getPlayer(TemperaturePlayer.class);
        final ThirstPlayer thp = vh.getPlayer(ThirstPlayer.class);
        switch (e.getItem().getType()) {
            case MILK_BUCKET:
            case POTION:
                if (tp != null) {
                    tp.drink();
                }
                if (thp != null) {
                    thp.drink();
                }
                break;
            case MUSHROOM_STEW:
                if (tp != null) {
                    tp.eatSoup();
                }
                break;
        }
    }

    @EventHandler
    public void onFoodLevelChange(@NotNull FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (e.getFoodLevel() < p.getFoodLevel()) {
                final ValueHolder vh = ValueHolder.getValueHolder(p);
                if (vh != null) {
                    ThirstPlayer tP = vh.getPlayer(ThirstPlayer.class);
                    if (tP != null) {
                        tP.foodLevelChange();
                    }
                }
            }
            if (e.getFoodLevel() < 4) {
                Potions.addCustomPotion(p, POTION_HUNGER_I);
                Potions.addCustomPotion(p, POTION_HUNGER_II);
            } else {
                Potions.removeCustomPotion(p, POTION_HUNGER_I);
                Potions.removeCustomPotion(p, POTION_HUNGER_II);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLogin(@NotNull PlayerJoinEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            AnuraThread.async(() -> new ValueHolder(e.getPlayer()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChangeGameMode(PlayerGameModeChangeEvent e) {
        AnuraThread.async(() -> {
            if (e.getNewGameMode().equals(GameMode.SURVIVAL)) {
                new ValueHolder(e.getPlayer());
                if (e.getPlayer().getFoodLevel() < 4) {
                    Potions.addCustomPotion(e.getPlayer(), POTION_HUNGER_I);
                    Potions.addCustomPotion(e.getPlayer(), POTION_HUNGER_II);
                }
            } else {
                ValueHolder.removeValueHolder(e.getPlayer());
                Potions.removeCustomPotion(e.getPlayer(), POTION_HUNGER_I);
                Potions.removeCustomPotion(e.getPlayer(), POTION_HUNGER_II);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onSprint(@NotNull PlayerToggleSprintEvent e) {
        if (e.isSprinting()) {
            ValueHolder vh = ValueHolder.getValueHolder(e.getPlayer());
            if (vh != null) {
                ThirstPlayer tp = vh.getPlayer(ThirstPlayer.class);
                if (tp != null) {
                    e.setCancelled(tp.isNoSprint());
                }
            }
        }
    }

    @EventHandler
    public void onDisconnect(AnuraLeaveEvent e) {
        AnuraThread.async(() -> ValueHolder.removeValueHolder(e.getPlayer()));
    }
}
