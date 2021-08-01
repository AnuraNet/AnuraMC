package de.mc_anura.core.listeners;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.Money;
import de.mc_anura.core.database.DB;
import de.mc_anura.core.events.AnuraLeaveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JoinEvent implements Listener {

    private static final Map<Player, PermissionAttachment> attachments = new WeakHashMap<>();

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerJoinEarly(PlayerJoinEvent event) {
        try {
            UUID uuid = event.getPlayer().getUniqueId();
            ResultSet rs = DB.querySelect("SELECT id FROM players WHERE uuid = ?", uuid.toString());
            if (rs.next()) {
                DB.queryUpdate("UPDATE players SET name = ? WHERE id = ?", event.getPlayer().getName(), rs.getInt("id"));
            } else {
                DB.queryUpdate("INSERT INTO players (uuid, name, money) VALUES (?, ?, ?)", uuid.toString(), event.getPlayer().getName(), Money.INITIAL_MONEY);
            }
        } catch (SQLException ex) {
            Logger.getLogger(JoinEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @EventHandler(ignoreCancelled=true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        //event.setJoinMessage(null);
        addAttachment(event.getPlayer());
    }

    @EventHandler
    public void onQuit(AnuraLeaveEvent event) {
        if (attachments.containsKey(event.getPlayer())) {
            event.getPlayer().removeAttachment(attachments.remove(event.getPlayer()));
        }
    }

    public static void addAttachment(Player P) {
        AnuraThread.queueSync(() -> {
            PermissionAttachment att = P.addAttachment(AnuraCore.getInstance());
            attachments.put(P, att);
            att.setPermission("minecraft.command.me", false);
            att.setPermission("minecraft.command.tell", false);
            att.setPermission("minecraft.command.help", false);
            att.setPermission("bukkit.command.version", false);
            att.setPermission("bukkit.command.plugins", false);
            att.setPermission("bukkit.command.help", false);
        });
    }
}
