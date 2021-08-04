package de.mc_anura.realisticminecraft.command;

import de.mc_anura.core.msg.Msg;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import de.mc_anura.realisticminecraft.infobar.BarStatus;
import de.mc_anura.realisticminecraft.infobar.InfoBar;
import de.mc_anura.realisticminecraft.infobar.InfoBarCommandHelper;
import de.mc_anura.realisticminecraft.infobar.RealisticPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfoBarCommand implements CommandExecutor, TabExecutor {

//    public InfoBarCmd() {
//        new Help("infobar", "Konfigurieret die Anzeige der Infobars")
//                .addArgument("durst | temp", "Wählt Durst oder Temperatur aus")
//                .addArgument("an | standard", "Schaltet die Bar auf AN oder STANDARD")
//                .build();
//    }

    @Override
    public boolean onCommand(@NotNull CommandSender cs, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 2) {
            return false;
        }
        Player p;
        String type = args[0];
        String value = args[1];
        if (cs instanceof Player player) {
            p = player;
        } else {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "This is a player command!");
            return true;
        }
        InfoBarCommandHelper.Result result = InfoBarCommandHelper.getRealisticPlayer(cs, p, type);
        RealisticPlayer rp = result.realisticPlayer();
        if (rp == null) {
            return result.bool();
        }
        BarStatus status;
        try {
            status = BarStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        InfoBar<? extends RealisticPlayer> bar = rp.getBar();
        if (bar != null) {
            bar.setBarStatus(status);
            Msg.send(rp.getPlayer(), RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.SUCCESS, "Die Infobar wird nun" + (status.equals(BarStatus.STANDARD) ? " nicht " : " ") + "immer angezeigt!");
        } else {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Es ist ein interner Fehler aufgetreten (3)");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender cs, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                list.add("durst");
                list.add("temp");
            }
            case 2 -> list.addAll(Arrays.stream(BarStatus.values()).map(Enum::name).map(String::toLowerCase).toList());
        }
        return list.stream().filter((s) -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
    }
}
