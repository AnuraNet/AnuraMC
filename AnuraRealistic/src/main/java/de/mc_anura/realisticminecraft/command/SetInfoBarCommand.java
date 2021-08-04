package de.mc_anura.realisticminecraft.command;

import de.mc_anura.core.msg.Msg;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import de.mc_anura.realisticminecraft.infobar.InfoBarCommandHelper;
import de.mc_anura.realisticminecraft.infobar.RealisticPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SetInfoBarCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p;
        String type;
        String value;
        if (args.length == 3) {
            p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Spieler wurde nicht gefunden");
                return true;
            }
            type = args[1];
            value = args[2];
        } else if (args.length == 2) {
            if (cs instanceof Player player) {
                p = player;
            } else {
                Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "This is a player command!");
                return true;
            }
            type = args[0];
            value = args[1];
        } else {
            return false;
        }
        InfoBarCommandHelper.Result result = InfoBarCommandHelper.getRealisticPlayer(cs, p, type);
        RealisticPlayer rp = result.realisticPlayer();
        if (rp == null) {
            return result.bool();
        }
        float v;
        try {
            v = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "%s ist keine Zahl!", value);
            return true;
        }
        if (rp.getMIN() > v || rp.getMAX() < v) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.SUCCESS, "Der Wert liegt außerhalb des Wertebereich.");
            return true;
        }
        rp.setValue(v);
        Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.SUCCESS, "Der Wert wurde auf %i gesetzt!", v);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender cs, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                list.add("durst");
                list.add("temp");
                list.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList());
            }
            case 2 -> {
                if (!args[0].equals("durst") && !args[0].equals("temp")) {
                    list.add("durst");
                    list.add("temp");
                }
            }
        }
        return list.stream().filter((s) -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
    }
}
