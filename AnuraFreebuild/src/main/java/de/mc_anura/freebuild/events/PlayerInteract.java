package de.mc_anura.freebuild.events;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.freebuild.AnuraFreebuild;
import de.mc_anura.freebuild.ClaimManager;
import java.util.HashSet;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player P = event.getPlayer();
        if (P.getName().equals("kaenganxt") && P.getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_AXE) && event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            HashSet<Material> mats = new HashSet<>();
            mats.add(Material.AIR);
            P.getWorld().spigot().strikeLightning(P.getTargetBlock(mats, 200).getLocation(), false);
            return;
        }

        if (event.getAction() == Action.PHYSICAL) {
            return;
        }

        /*if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock() != null) {
            PersistentDataContainer data = ClaimManager.getPossibleBookData(event.getClickedBlock());
            if (data != null) {
                ClaimManager.manageClaim(event.getPlayer(), data);
                event.setCancelled(true);
                return;
            }
        }*/

        if (!ClaimManager.isClaiming(P))
            return;

        event.setCancelled(true);
        int tick = Bukkit.getCurrentTick();
        AnuraThread.add(Bukkit.getScheduler().runTaskLaterAsynchronously(AnuraFreebuild.getInstance(), () -> {
            // Drops to air fire an interact left AIR, prevent this
            if (Objects.equals(ClaimEvents.dropped.remove(P), tick)) {
                return;
            }
            ClaimManager.interact(P, event.getAction());
        }, 1));
    }
}
