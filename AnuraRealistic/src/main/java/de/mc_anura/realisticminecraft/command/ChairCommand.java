package de.mc_anura.realisticminecraft.command;

import de.mc_anura.core.msg.Msg;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import de.mc_anura.realisticminecraft.util.ChairManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChairCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (cs instanceof Player p) {
            if (ChairManager.isDisabled(p)) {
                ChairManager.enableChair(p);
                Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.INFO, "Hocker wurden eingeschaltet");
            } else {
                ChairManager.disableChair(p);
                Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.INFO, "Hocker wurden ausgeschaltet");
            }
        } else {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "This is a player command!");
        }
        return true;
    }
}
