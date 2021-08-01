package de.mc_anura.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameMode implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {

        if (args.length < 1) {
            return false;
        }

        if (!sender.hasPermission("core.commands.gm")) {
            return false;
        }
        
        Player player = (sender instanceof Player) ? (Player) sender : null;
        if (player == null) {
            if (args.length < 2) {
                return false;
            }
            player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
                return false;
            }
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
}
