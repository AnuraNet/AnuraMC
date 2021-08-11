package de.mc_anura.freebuild.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import static de.mc_anura.freebuild.events.BlockBreak.log;

public class PistonEvents implements Listener {

    @EventHandler
    public void onPistonOut(BlockPistonExtendEvent event) {
        log(event);
    }

    @EventHandler
    public void onPistonIn(BlockPistonRetractEvent event) {
        log(event);
    }
}
