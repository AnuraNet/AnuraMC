package de.mc_anura.realisticminecraft.timber.event;

import de.mc_anura.core.events.AnuraPlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AppleDropEvent extends AnuraPlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final short amount;

    public AppleDropEvent(Player p, short amount) {
        super(p, true);
        this.amount = amount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public int getAmount() {
        return amount;
    }
}
