package de.mc_anura.realisticminecraft.command;

import de.mc_anura.core.msg.Msg;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import de.mc_anura.realisticminecraft.infobar.TemperaturePlayer;
import de.mc_anura.realisticminecraft.infobar.ThirstPlayer;
import de.mc_anura.realisticminecraft.infobar.ValueHolder;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InfobarSetCmd implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender cs, @NotNull Command cmnd, @NotNull String label, String[] args) {
        if (!cs.hasPermission("anura.realisticmc.infobarset")) {
            Msg.noPerms(cs);
            return true;
        }
        if (!(cs instanceof Player)) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "This is a player command!");
            return true;
        }
        if (args.length < 2 || args.length > 3) {
            return false;
        }
        if (!((HumanEntity) cs).getGameMode().equals(GameMode.SURVIVAL)) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Dieses Command kann nur im SURVIVAL Mode verwendet werden");
            return true;
        }
        ValueHolder valueHolder = ValueHolder.getValueHolder((Player) cs);
        if (valueHolder == null) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Es ist einer interner Fehler aufgetreten (0)");
            return true;
        }
        ThirstPlayer thirstPlayer = valueHolder.getPlayer(ThirstPlayer.class);
        TemperaturePlayer temperaturePlayer = valueHolder.getPlayer(TemperaturePlayer.class);
        if (thirstPlayer == null || temperaturePlayer == null) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Es ist einer interner Fehler aufgetreten (1)");
            return true;
        }
        float value;
        try {
            value = Float.parseFloat(args[1]);
        } catch (NumberFormatException e) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "%s ist keine Zahl!", args[1]);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "durst":
                if (thirstPlayer.getMIN() > value || thirstPlayer.getMAX() < value) {
                    Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.SUCCESS, "Der Wert liegt außerhalb des Wertebereich.");
                    return true;
                }
                thirstPlayer.setValue(value);
                Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.SUCCESS, "Der Durst wurde auf %i gesetzt!", value);
                return true;
            case "temp":
            case "temperatur":
                if (temperaturePlayer.getMIN() > value || temperaturePlayer.getMAX() < value) {
                    Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.SUCCESS, "Der Wert liegt außerhalb des Wertebereich.");
                    return true;
                }
                temperaturePlayer.setValue(value);
                Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.SUCCESS, "Die Temperatur wurde auf %i gesetzt!", value);
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender cs, @NotNull Command cmnd, @NotNull String label, String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1:
                list.add("durst");
                list.add("temp");
                break;
            case 2:
                return list;
        }
        return list.stream().filter((s) -> s.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }
}
