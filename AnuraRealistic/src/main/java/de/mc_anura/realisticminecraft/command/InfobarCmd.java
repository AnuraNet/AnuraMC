package de.mc_anura.realisticminecraft.command;

import de.mc_anura.core.msg.Msg;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import de.mc_anura.realisticminecraft.infobar.BarStatus;
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

public class InfobarCmd implements TabExecutor {

    public InfobarCmd() {
//        new Help("infobar", "Konfigurieret die Anzeige der Infobars")
//                .addArgument("durst | temp", "Wählt Durst oder Temperatur aus")
//                .addArgument("an | standard", "Schaltet die Bar auf AN oder STANDARD")
//                .build();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command cmnd, @NotNull String label, String[] args) {
        if (!(cs instanceof Player)) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "This is a player command!");
            return false;
        }
        if (args.length < 2 || args.length > 3) {
            return false;
        }
        if (!((HumanEntity) cs).getGameMode().equals(GameMode.SURVIVAL)) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Dieses Command kann nur im SURVIVAL Mode verwendet werden");
            return true;
        }
        BarStatus status;
        try {
            status = BarStatus.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
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
        switch (args[0].toLowerCase()) {
            case "durst":
                thirstPlayer.getBar().setBarStatus(status);
                Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.SUCCESS, "Die Durst Infobar wird dir nun" + (status.equals(BarStatus.STANDARD) ? " nicht " : " ") + "immer angezeigt!");
                return true;
            case "temp":
            case "temperatur":
                temperaturePlayer.getBar().setBarStatus(status);
                Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.SUCCESS, "Die Temperatur Infobar wird dir nun" + (status.equals(BarStatus.STANDARD) ? " nicht " : " ") + "immer angezeigt!");
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender cs, @NotNull Command cmnd, @NotNull String label, String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                list.add("durst");
                list.add("temp");
            }
            case 2 -> {
                list.add("an");
                list.add("standard");
            }
        }
        return list.stream().filter((s) -> s.startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
    }
}
