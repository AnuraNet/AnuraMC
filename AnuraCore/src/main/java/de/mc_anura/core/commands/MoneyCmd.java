package de.mc_anura.core.commands;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.Money;
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

public class MoneyCmd implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        Player P = (sender instanceof Player) ? (Player) sender : null;

        if (args.length == 0) {
            if (P == null) {
                Msg.send(sender, AnuraCore.getInstance(), MsgType.ERROR, "Dieser Befehl ist nur für Spieler!");
                return true;
            }
            int money = Money.get(P);
            Msg.send(P, AnuraCore.getInstance(), MsgType.SUCCESS, "Du hast %m", money);
            return true;
        } else if (args[0].equalsIgnoreCase("pay") && args.length == 3) {

            if (!sender.hasPermission("core.money.endless")) {
                Msg.noPerms(sender);
            }

            int money;
            try {
                money = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                Msg.send(P, AnuraCore.getInstance(), MsgType.ERROR, "Das Geld muss eine Zahl sein!");
                return true;
            }
            
            String name = args[1];

            Player p = Bukkit.getPlayerExact(name);
            if (p == null) {
                Msg.send(P, AnuraCore.getInstance(), MsgType.ERROR, "Dieser Spieler ist nicht online!");
                return true;
            }
            Money.pay(p, money);
            Msg.send(sender, AnuraCore.getInstance(), MsgType.SUCCESS, "Du hast %s %m überwiesen!", p.getDisplayName(), money);
            Msg.send(p, AnuraCore.getInstance(), MsgType.SUCCESS, "%s hat dir %m überwiesen!", (P != null) ? P.getDisplayName() : sender.getName(), money);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String whatever, String[] args) {
        List<String> possible = new ArrayList<>();
        if (args.length == 1) {
            possible.add("pay");
        } else if (args.length == 2) {
            possible.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList());
        }
        String search = args[args.length - 1].toLowerCase();
        return possible.stream().filter((name) -> name.toLowerCase().startsWith(search)).toList();
    }
}
