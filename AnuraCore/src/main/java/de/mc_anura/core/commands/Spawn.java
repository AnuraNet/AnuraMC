package de.mc_anura.core.commands;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.msg.Msg.MsgType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Spawn implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        Player P;
        if (args.length > 0 && sender.hasPermission("core.commands.spawn.others")) {
            P = Bukkit.getPlayerExact(args[0]);
        } else if (sender instanceof Player) {
            P = (Player) sender;
        } else {
            return false;
        }
        if (P == null) {
            Msg.send(sender, AnuraCore.getInstance(), MsgType.ERROR, "Spieler nicht gefunden!");
            return true;
        }

        // TODO: Wait time + sound
        P.teleport(P.getWorld().getSpawnLocation());
        Msg.send(sender, AnuraCore.getInstance(), MsgType.SUCCESS, "Zum Spawn teleportiert!");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String whatever, String[] args) {
        List<String> possible = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("core.commands.spawn.others")) {
            possible.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList());
        }
        String search = args[args.length - 1].toLowerCase();
        return possible.stream().filter((name) -> name.toLowerCase().startsWith(search)).toList();
    }
}
