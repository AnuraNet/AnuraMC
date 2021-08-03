package de.mc_anura.core.commands;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.tools.Warps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (args[0].equalsIgnoreCase("list")) {
            Collection<Warps.Warp> warps;
            if (sender.hasPermission("core.commands.warp.admin")) {
                warps = Warps.getWarps().values();
            } else {
                warps = Warps.getWarps().values().stream().filter(Warps.Warp::userWarp).toList();
            }
            for (Warps.Warp warp : warps) {
                sender.sendMessage(Component.text(warp.name()).color(warp.userWarp() ? NamedTextColor.GREEN : NamedTextColor.BLUE)
                        .clickEvent(ClickEvent.runCommand("/warp " + warp.name())));
            }
        } else if (args[0].equalsIgnoreCase("create")) {
            if (args.length != 3) {
                return false;
            }
            if (!(sender instanceof Player)) {
                Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.ERROR, "Kann nur als Spieler ausgeführt werden!");
                return true;
            }
            if (!sender.hasPermission("core.commands.warp.manage")) {
                Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.ERROR, "Keine Permissions!");
                return true;
            }
            String name = args[1];
            boolean userWarp;
            if (args[2].equalsIgnoreCase("user")) {
                userWarp = true;
            } else if (args[2].equalsIgnoreCase("admin")) {
                userWarp = false;
            } else {
                return false;
            }
            Warps.Warp warp = new Warps.Warp(name, ((Player) sender).getLocation(), userWarp);
            boolean success = Warps.createWarp(warp);
            if (success) {
                Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.SUCCESS, "Warp erstellt!");
            } else {
                Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.ERROR, "Warp existiert bereits!");
            }
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (args.length != 2) {
                return false;
            }
            if (!sender.hasPermission("core.commands.warp.manage")) {
                Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.ERROR, "Keine Permissions!");
                return true;
            }
            boolean success = Warps.deleteWarp(args[1]);
            if (success) {
                Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.SUCCESS, "Warp entfernt!");
            } else {
                Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.ERROR, "Warp existiert nicht!");
            }
        } else {
            Player P;
            if (sender instanceof Player) {
                if (args.length != 1) {
                    return false;
                }
                P = (Player) sender;
            } else {
                if (args.length != 2) {
                    return false;
                }
                if (!sender.hasPermission("core.commands.warp.others")) {
                    Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.ERROR, "Keine Permissions!");
                    return false;
                }
                String p = args[1];
                P = Bukkit.getPlayer(p);
                if (P == null) {
                    Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.ERROR, "Spieler nicht gefunden!");
                    return true;
                }
            }

            Warps.Warp w = Warps.getWarp(args[0]);
            if (w == null) {
                Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.ERROR, "Warp nicht gefunden!");
                return true;
            }
            if (!w.userWarp() && !sender.hasPermission("core.commands.warp.admin")) {
                Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.ERROR, "Keine Permissions!");
                return true;
            }
            P.teleport(w.location());
            // TODO: Sound
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String whatever, String[] args) {
        List<String> possible = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("core.commands.warp.admin")) {
                possible.addAll(Warps.getWarps().keySet());
            } else {
                possible.addAll(Warps.getWarps().values().stream().filter(Warps.Warp::userWarp).map(Warps.Warp::name).toList());
            }

            if (sender.hasPermission("core.commands.warp.manage")) {
                possible.add("create");
                possible.add("delete");
            }
            possible.add("list");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create") && sender.hasPermission("core.commands.warp.manage")) {
                // No autocomplete for name
            } else if (args[0].equalsIgnoreCase("delete") && sender.hasPermission("core.commands.warp.manage")) {
                possible.addAll(Warps.getWarps().keySet());
            } else if (sender.hasPermission("core.commands.warp.others")) {
                possible.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create") && sender.hasPermission("core.commands.warp.manage")) {
                possible.add("user");
                possible.add("admin");
            }
        }
        String search = args[args.length - 1].toLowerCase();
        return possible.stream().filter((name) -> name.toLowerCase().startsWith(search)).toList();
    }
}
