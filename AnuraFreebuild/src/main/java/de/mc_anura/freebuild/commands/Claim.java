package de.mc_anura.freebuild.commands;

import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.msg.Msg.MsgType;
import de.mc_anura.freebuild.AnuraFreebuild;
import de.mc_anura.freebuild.ClaimManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Claim implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player P)) {
            return false;
        }
        if (args.length == 0) {

            if (ClaimManager.isClaiming(P)) {
                ClaimManager.endClaimMode(P);
            } else {
                ClaimManager.startClaiming(P);
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("confirm")) {
            if (!ClaimManager.isClaiming(P)) {
                Msg.send(P, AnuraFreebuild.getInstance(), MsgType.ERROR, "Du bist nicht im Claim-Modus! " + ChatColor.YELLOW + "/claim");
                return true;
            }
            
            ClaimManager.confirm(P);
            return true;
        }
        return false;
    }
}
