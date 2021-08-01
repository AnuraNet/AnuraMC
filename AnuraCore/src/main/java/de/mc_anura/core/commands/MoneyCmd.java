package de.mc_anura.core.commands;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.Money;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.msg.Msg.MsgType;
import de.mc_anura.core.util.UUIDManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoneyCmd implements CommandExecutor {

    @Override
    //@SuppressWarnings("null")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        Player P = (sender instanceof Player) ? (Player) sender : null;

        if (args.length == 0) {
            if (P == null) {
                Msg.send(sender, AnuraCore.getInstance(), MsgType.ERROR, "Dieser Befehl ist nur für Spieler!");
                return true;
            }
            Money.getMoney(P.getUniqueId(), (money) -> {
                Msg.send(P, AnuraCore.getInstance(), MsgType.SUCCESS, "Du hast %m", money);
            });
            return true;
        } else if ((args[0].equalsIgnoreCase("pay") || args[0].equalsIgnoreCase("send")) && (args.length == 3)) {
            Integer money;
            try {
                money = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                Msg.send(P, AnuraCore.getInstance(), MsgType.ERROR, "Das Geld muss eine Zahl sein!");
                return true;
            }
            
            String name = args[1];
            
            Runnable payAction = () -> {
                UUIDManager.getUUID(name, true, (uuid2) -> {
                    if (uuid2 == null) {
                        Msg.send(P, AnuraCore.getInstance(), MsgType.ERROR, "Dieser Spieler ist unbekannt!");
                        return;
                    }
                    Money.getMoney(uuid2, (money2) -> {
                        if (money2 != -1) {
                            Money.payMoney(uuid2, money);
                            if (P != null && !P.hasPermission("core.money.endless")) {
                                Money.payMoney(P.getUniqueId(), -money);
                            }
                            Player targetP = Bukkit.getPlayer(uuid2);
                            String target = (targetP == null) ? name : targetP.getDisplayName();
                            Msg.send(P, AnuraCore.getInstance(), MsgType.SUCCESS, "Du hast %s %m überwiesen!", target, money);
                            Msg.send(uuid2, AnuraCore.getInstance(), MsgType.SUCCESS, "%s hat dir %m überwiesen!", (P != null) ? P.getDisplayName() : sender.getName(), money);
                        } else {
                            Msg.send(P, AnuraCore.getInstance(), MsgType.ERROR, "Ein Fehler ist aufgetreten");
                        }
                    });
                });
            };
            
            if (sender.hasPermission("core.money.endless")) {
                payAction.run();
            } else {
                if (P == null) {
                    Msg.send(sender, AnuraCore.getInstance(), MsgType.ERROR, "Dieser Befehl ist nur für Spieler!");
                    return true;
                }
                if (P.getName().equals(args[1])) {
                    Msg.send(P, AnuraCore.getInstance(), MsgType.ERROR, "Du kannst dir nicht selbst Geld überweisen!");
                    return true;
                }
                if (money <= 0) {
                    Msg.send(P, AnuraCore.getInstance(), MsgType.ERROR, "Du musst einen positiven Wert angeben!");
                    return true;
                }
                Money.getMoney(P.getUniqueId(), (currentMoney) -> {
                    if (currentMoney < money) {
                        Msg.send(P, AnuraCore.getInstance(), MsgType.ERROR, "Du hast nicht genug Geld!");
                    } else {
                        payAction.run();
                    }
                });
            }
        }
        return true;
    }
}
