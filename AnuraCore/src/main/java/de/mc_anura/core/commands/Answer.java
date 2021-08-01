package de.mc_anura.core.commands;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.msg.Messenger;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.msg.Msg.MsgType;
import de.mc_anura.core.util.UUIDManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class Answer implements CommandExecutor {
    
    public static Map<Player, UUID> chatPartners = new WeakHashMap<>();
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        if (!(sender instanceof Player P)) return true;
        if (!chatPartners.containsKey(P)) {
            Msg.send(P, AnuraCore.getInstance(), MsgType.INFO, "Starte eine Konversation mit /msg");
            return true;
        }
        if (args.length == 0) {
            Msg.send(P, AnuraCore.getInstance(), MsgType.ERROR, "Gib eine Nachricht an!");
            return true;
        }
        StringBuilder s = new StringBuilder();
        for (String st : args) {
            s.append(st).append(" ");
        }
        String msg = s.toString();
        UUID partner = chatPartners.get(P);
        boolean sent = Messenger.receiveMsg(P.getUniqueId(), partner, msg);
        Player p2 = Bukkit.getPlayer(partner);
        if (p2 == null) {
            UUIDManager.getName(partner, (name) -> send(P, sent, name, msg));
        } else {
            send(P, sent, p2.getDisplayName(), msg);
        }
        return true;
    }

    private static void send(Player P, boolean sent, String name, String msg) {
        P.sendMessage(P.getDisplayName() + ChatColor.GRAY + " -> " + name + ChatColor.GRAY + ": " + ChatColor.RESET + msg);
        if (!sent) Msg.send(P, AnuraCore.getInstance(), MsgType.INFO, "(Noch nicht zugestellt, Spieler ist nicht mehr online, Nachricht wird in seine Mailbox verschoben)");
    }
}
