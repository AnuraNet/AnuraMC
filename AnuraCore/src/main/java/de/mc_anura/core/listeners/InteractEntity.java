package de.mc_anura.core.listeners;

import de.mc_anura.core.tools.Villagers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class InteractEntity implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            if (Villagers.doClick((Villager) event.getRightClicked(), event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }
}
