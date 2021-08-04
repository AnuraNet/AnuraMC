package de.mc_anura.realisticminecraft.timber.event;

import de.mc_anura.core.events.AnuraPlayerEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TreeCutEvent extends AnuraPlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Block block;
    private boolean cancel = false;
    private short saplingDropChance = 25;
    private short appleDropChance = 110;
    private short leavesDropChance = 45;

    public TreeCutEvent(@NotNull Player p, @NotNull Block b) {
        super(p, true);
        this.block = b;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull Block getBlock() {
        return block;
    }

    public short getSaplingDropChance() {
        return saplingDropChance;
    }

    public void setSaplingDropChance(short dropChance) {
        this.saplingDropChance = dropChance;
    }

    public short getAppleDropChance() {
        return appleDropChance;
    }

    public void setAppleDropChance(short dropChance) {
        this.appleDropChance = dropChance;
    }

    public short getLeavesDropChance() {
        return leavesDropChance;
    }

    public void setLeavesDropChance(short dropChance) {
        this.leavesDropChance = dropChance;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
