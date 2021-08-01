package de.mc_anura.core.commands;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.msg.Msg.MsgType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Spawn implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        Player P;
        if (args.length > 0) {
            P = Bukkit.getPlayerExact(args[0]);
        } else if (sender instanceof Player) {
            P = (Player) sender;
        } else {
            return false;
        }
        if (P == null) {
            return true;
        }
        
        
        // TODO: Wait time
        P.teleport(P.getWorld().getSpawnLocation());
        Msg.send(sender, AnuraCore.getInstance(), MsgType.SUCCESS, "Zum Spawn teleportiert!");
        return true;
    }
}
