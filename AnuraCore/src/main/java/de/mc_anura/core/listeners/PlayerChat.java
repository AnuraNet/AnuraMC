package de.mc_anura.core.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChat implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
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
                event.setFormat("%s" + ChatColor.GRAY + ":" + ChatColor.WHITE + " %s");
            }
     //   } catch (IllegalFormatException | SQLException ex) {
    //        Logger.getLogger(PlayerChat.class.getName()).log(Level.SEVERE, null, ex);
   //     }
    }
}
