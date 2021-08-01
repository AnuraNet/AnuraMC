package de.mc_anura.freebuild.events;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.freebuild.ClaimManager;
import java.util.HashSet;

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
        if (!ClaimManager.isClaiming(P))
            return;

        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        event.setCancelled(true);
        AnuraThread.async(() -> ClaimManager.interact(P, event.getAction()));
    }
}
