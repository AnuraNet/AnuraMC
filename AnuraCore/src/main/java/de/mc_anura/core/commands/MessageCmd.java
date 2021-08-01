package de.mc_anura.core.commands;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.msg.Messenger;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.msg.Msg.MsgType;
import de.mc_anura.core.util.UUIDManager;
import de.mc_anura.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageCmd implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (!(sender instanceof Player P)) return true;
        if (args[0].equalsIgnoreCase("read")) {
            int page = 1;
            if (args.length > 1 && Util.isInteger(args[1])) page = Integer.parseInt(args[1]);
            Messenger.readMsgs(P, true, page);
        } else if (args[0].equalsIgnoreCase("list")) {
            int page = 1;
            if (args.length > 1 && Util.isInteger(args[1])) page = Integer.parseInt(args[1]);
            Messenger.readMsgs(P, false, page);
        } else if (args[0].equalsIgnoreCase("delete")) {
            Messenger.deleteMsgs(P);
        } else if (args.length == 1) {
            Msg.send(sender, AnuraCore.getInstance(), MsgType.ERROR, "Gib eine Nachricht an!");
        } else {
            String player = args[0];
            UUIDManager.getUUID(player, true, (uuid) -> {
                if (uuid == null) {
                    Msg.send(sender, AnuraCore.getInstance(), MsgType.ERROR, "Spieler nicht gefunden!");
                    return;
                }
                StringBuilder s = new StringBuilder();
                for (String st : Arrays.copyOfRange(args, 1, args.length)) {
                    s.append(st).append(" ");
                }
                String display = P.getDisplayName();
                boolean sent = Messenger.receiveMsg(P.getUniqueId(), uuid, s.toString());
                Player p2 = Bukkit.getPlayer(uuid);
                P.sendMessage(display + ChatColor.GRAY + " -> " + (p2 == null ? player : p2.getDisplayName()) + ChatColor.GRAY + ": " + ChatColor.RESET + s);
                if (!sent) Msg.send(P, AnuraCore.getInstance(), MsgType.INFO, "(Noch nicht zugestellt, Spieler ist nicht online, Nachricht wird in seine Mailbox verschoben)");
            });
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String whatever, String[] args) {
        List<String> matches = new ArrayList<>();
        if (args.length != 1) {
            return matches;
        }
        String search = args[0].toLowerCase();
        if ("read".startsWith(search)) {
            matches.add("read");
        }
        if ("list".startsWith(search)) {
            matches.add("list");
        }
        if ("delete".startsWith(search)) {
            matches.add("delete");
        }
        Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter((name) -> name.toLowerCase().startsWith(search)).forEach(matches::add);
        return matches;
    }
}
