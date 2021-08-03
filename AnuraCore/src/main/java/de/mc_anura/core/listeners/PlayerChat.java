package de.mc_anura.core.listeners;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerChat implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
     //   try {
            // TODO: Correct muting implementation
//            ResultSet rs = DB.querySelect("SELECT muted FROM players WHERE uuid = ?", event.getPlayer().getUniqueId().toString());
//            rs.first();
//            if (rs.getBoolean("muted")) {
//                event.setCancelled(true);
//                Msg.send(event.getPlayer(), AnuraCore.getInstance(), MsgType.ERROR, "Du bist gemuted!");
//                return;
//            }
            if (event.isAsynchronous()) {
                event.renderer((source, sourceDisplayName, message, viewer) ->
                        sourceDisplayName.append(Component.text(": ", NamedTextColor.GRAY)).append(message.color(NamedTextColor.WHITE)));
            }
     //   } catch (IllegalFormatException | SQLException ex) {
    //        Logger.getLogger(PlayerChat.class.getName()).log(Level.SEVERE, null, ex);
   //     }
    }
}
