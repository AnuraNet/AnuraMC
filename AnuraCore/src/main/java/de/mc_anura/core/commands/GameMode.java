package de.mc_anura.core.commands;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.msg.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameMode implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        if (args.length < 1) {
            return false;
        }

        Player player;
        if (args.length > 1) {
            player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
                Msg.send(sender, AnuraCore.getInstance(), Msg.MsgType.ERROR, "Spieler nicht gefunden!");
                return true;
            }
        } else if (sender instanceof Player p) {
            player = p;
        } else {
            return false;
        }
        try {
            org.bukkit.GameMode mode = org.bukkit.GameMode.getByValue(Integer.parseInt(args[0]));

            if (mode != null) {
                player.setGameMode(mode);
                return true;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        return false;    
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String whatever, String[] args) {
        List<String> possible = new ArrayList<>();
        if (args.length == 1) {
            possible.addAll(Arrays.stream(org.bukkit.GameMode.values()).map(v -> String.valueOf(v.getValue())).toList());
        }
        if (args.length == 2) {
            possible.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList());
        }
        String search = args[args.length - 1].toLowerCase();
        return possible.stream().filter((name) -> name.toLowerCase().startsWith(search)).toList();
    }
}
