package de.mc_anura.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AnuraPlayerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;

    public AnuraPlayerEvent(Player player) {
        this(player, false);
    }

    public AnuraPlayerEvent(Player player, boolean async) {
        super(async);
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
