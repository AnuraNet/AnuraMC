package de.mc_anura.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class AnuraLeaveEvent extends AnuraPlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public AnuraLeaveEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
