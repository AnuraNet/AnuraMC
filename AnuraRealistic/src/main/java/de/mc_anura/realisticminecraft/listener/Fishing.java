package de.mc_anura.realisticminecraft.listener;

import de.mc_anura.core.msg.Msg;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import de.mc_anura.realisticminecraft.fishing.FishingChunk;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.FishHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class Fishing implements Listener {

    private static final String FAILED_HOOK_NAME = ChatColor.DARK_RED + "✖✖✖";

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent ev) {

        FishHook hook = ev.getHook();
        Chunk chunk = hook.getLocation().getChunk();

        switch (ev.getState()) {
            case FISHING -> {
                if (!FishingChunk.contains(chunk)) {
                    FishingChunk.queueTask(0, () -> FishingChunk.get(chunk));
                }
                // Wir warten 5 Sekunden, da dieser State (FISHING) direkt aufgerufen wird, wenn man die Angel auswirft.
                FishingChunk.queueTask(5, () -> {
                    if (hook.isDead() || !hook.isValid()) return;

                    FishingChunk newHookChunk = FishingChunk.get(hook.getLocation().getChunk());
                    if (!newHookChunk.isOverfished()) return;
                    ev.getPlayer().sendActionBar(Msg.getMsg(RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, true, "Dieser Teil des Gewässer scheint überfischt zu sein!"));

                    hook.setCustomName(FAILED_HOOK_NAME);
                    hook.setCustomNameVisible(true);
                });
            }
            case BITE -> {
                FishingChunk fishingChunk = FishingChunk.get(chunk);
                if (fishingChunk.isOverfished()) {
                    ev.getPlayer().sendActionBar(Msg.getMsg(RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, true, "Dieser Teil des Gewässer scheint überfischt zu sein!"));
                    hook.setCustomName(FAILED_HOOK_NAME);
                    hook.setCustomNameVisible(true);
                    ev.setCancelled(true);
                }
            }
            case CAUGHT_FISH -> {
                FishingChunk fishingChunk = FishingChunk.get(chunk);
                if (fishingChunk.isOverfished()) {
                    ev.getPlayer().sendActionBar(Msg.getMsg(RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, true, "Dieser Teil des Gewässer scheint überfischt zu sein!"));
                    ev.setCancelled(true);
                    ev.setExpToDrop(0);
                } else {
                    fishingChunk.addCaught();
                }
            }
        }
    }
}
