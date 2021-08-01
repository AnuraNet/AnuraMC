package de.mc_anura.core.msg;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.commands.Answer;
import de.mc_anura.core.database.DB;
import de.mc_anura.core.msg.Msg.MsgType;
import de.mc_anura.core.util.UUIDManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Messenger {
    
    private static final Map<Player, Queue<MessengerMsg>> queue = new HashMap<>();

    public static boolean send(Component from, UUID to, Component msg) {
        return receiveMsg(from, to, msg);
    }

    public static boolean send(UUID from, UUID to, String msg) {
        return receiveMsg(from, to, msg);
    }
    
    public static boolean receiveMsg(Component from, UUID to, Component msg) {
        if (to == null) return false;
        Player P = Bukkit.getPlayer(to);
        if (P != null) {
            P.sendMessage(from
                    .append(Component.text(" -> ").color(NamedTextColor.GRAY))
                    .append(P.displayName())
                    .append(Component.text(": ").color(NamedTextColor.GRAY))
                    .append(msg));
            saveToMailbox(to, from.toString(), msg.toString(), true);
            return true;
        } else {
            saveToMailbox(to, from.toString(), msg.toString());
            return false;
        }
    }

    public static boolean receiveMsg(UUID from, UUID to, String msg) {
        Player toP = Bukkit.getPlayer(to);
        if (toP != null) {
            Answer.chatPartners.put(toP, from);
            Player fromP = Bukkit.getPlayer(from);
            if (fromP != null) {
                Answer.chatPartners.put(fromP, to);
                toP.sendMessage(fromP.displayName()
                        .append(Component.text(" -> ").color(NamedTextColor.GRAY))
                        .append(toP.displayName())
                        .append(Component.text(": ").color(NamedTextColor.GRAY))
                        .append(Component.text(msg)));
            } else {
                UUIDManager.getName(from, (name) -> toP.sendMessage(Component.text(name)
                        .append(Component.text(" -> ").color(NamedTextColor.GRAY))
                        .append(toP.displayName())
                        .append(Component.text(": ").color(NamedTextColor.GRAY))
                        .append(Component.text(msg))));
            }
            saveToMailbox(to, from, msg, true);
            return true;
        } else {
            saveToMailbox(to, from, msg);
            return false;
        }
    }

    private static void saveToMailbox(UUID owner, String from, String msg) {
        saveToMailbox(owner, from, msg, false);
    }

    private static void saveToMailbox(UUID owner, UUID from, String msg) {
        saveToMailbox(owner, from, msg, false);
    }

    private static void saveToMailbox(UUID owner, UUID from, String msg, boolean read) {
        String name = null;
        Player tempPlayer = Bukkit.getPlayer(from);
        if (tempPlayer != null) {
            name = tempPlayer.getDisplayName();
        }
        DB.queryUpdate(true, "INSERT INTO playermailbox (toId, sender, fromId, msg, isRead, time) VALUES ((SELECT id FROM players WHERE uuid = ?), ?, (SELECT id FROM players WHERE uuid = ?), ?, ?, ?)",
          owner.toString(), name, from.toString(), msg, read, (int) (System.currentTimeMillis() / 1000));
    }

    private static void saveToMailbox(UUID owner, String from, String msg, boolean read) {
        DB.queryUpdate(true, "INSERT INTO playermailbox (toId, sender, msg, isRead, time) VALUES ((SELECT id FROM players WHERE uuid = ?), ?, ?, ?, ?)", owner.toString(), from, msg, read, (int) (System.currentTimeMillis() / 1000));
    }

    public static void checkMsgs(Player P) {
        AnuraThread.async(() -> {
            ResultSet rs = DB.querySelect("SELECT count(*) as count FROM playermailbox WHERE isRead = 0 AND toId = (SELECT id FROM players WHERE uuid = ?)", P.getUniqueId().toString());
            try {
                if (!rs.next()) return;
                int count = rs.getInt("count");
                if (count == 0) return;
                Msg.send(P, AnuraCore.getInstance(), MsgType.INFO, "Du hast %i neue Nachricht" + (count > 1 ? "en" : "") + "! %s", count, "(/msg read)");
            } catch (SQLException ex) {
                Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public static void readMsgs(Player P, boolean onlyUnread, int page) {
        if (page < 1) return;
        AnuraThread.async(() -> {
            String read = (onlyUnread ? "isRread = 0 AND " : "");
            ResultSet num = DB.querySelect("SELECT count(*) as num FROM playermailbox WHERE " + read + "toId = (SELECT id FROM players WHERE uuid = ?)", P.getUniqueId().toString());
            try {
                if (!num.next() || num.getInt("num") == 0) {
                    Msg.send(P, AnuraCore.getInstance(), MsgType.INFO, "Keine " + (onlyUnread ? "ungelesenen " : "") + "Nachrichten!");
                    return;
                }
                int perPage = 9;
                ResultSet rs = DB.querySelect("SELECT players.uuid as fromP, sender, msg, time, playermailbox.id as msgid FROM playermailbox "
                                                            + "LEFT JOIN players ON players.id = playermailbox.fromId "
                                                            + "WHERE " + read + "toId = (SELECT id FROM players WHERE uuid = ?) ORDER BY time DESC LIMIT ? OFFSET ?",
                                                            P.getUniqueId().toString(), perPage, (page - 1) * perPage);
                if (!rs.last()) {
                    Msg.send(P, AnuraCore.getInstance(), MsgType.INFO, "Keine " + (onlyUnread ? "ungelesenen " : "") + "Nachrichten!");
                    return;
                }
                int lastTime = rs.getInt("time");
                int lastId = rs.getInt("msgid");
                rs.first();
                int firstTime = rs.getInt("time");
                int firstId = rs.getInt("msgid");
                DB.queryUpdate("UPDATE playermailbox SET isRead = 1 WHERE " + read + "toId = (SELECT id FROM players WHERE uuid = ?) "
                                              + "AND time BETWEEN ? AND ? AND id BETWEEN ? AND ? ORDER BY time DESC LIMIT ?",
                                              P.getUniqueId().toString(), lastTime, firstTime, lastId, firstId, perPage);
                rs.beforeFirst();
                int count = 0;
                while (rs.next()) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis((long) rs.getInt("time") * 1000);
                    Calendar now = Calendar.getInstance();
                    String time;
                    if (c.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) && c.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                        String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
                        hour = hour.length() == 1 ? "0" + hour : hour;
                        String min = String.valueOf(c.get(Calendar.MINUTE));
                        min = min.length() == 1 ? "0" + min : min;
                        time = hour + ":" + min;
                    } else {
                        time = c.get(Calendar.DAY_OF_MONTH) + "." + (c.get(Calendar.MONTH) + 1) + "." + c.get(Calendar.YEAR);
                    }
                    count++;
                    String msg = rs.getString("msg");
                    UUID fromUU = rs.getString("fromP") == null ? null : UUID.fromString(rs.getString("fromP"));
                    if (!queue.containsKey(P)) queue.put(P, new ConcurrentLinkedQueue<>());
                    queue.get(P).add(new MessengerMsg(fromUU, rs.getString("from"), time, msg, P));
                }
                while (!queue.get(P).isEmpty()) {
                    queue.get(P).poll().send();
                }
                if (count > 0) Msg.send(P, AnuraCore.getInstance(), MsgType.INFO, "Seite %i von %i", page, (int) Math.ceil((double) num.getInt("num") / perPage));
                else Msg.send(P, AnuraCore.getInstance(), MsgType.INFO, "Keine " + (onlyUnread ? "ungelesenen " : "") + "Nachrichten!");
            } catch (SQLException ex) {
                Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public static void deleteMsgs(Player P) {
        AnuraThread.async(() -> {
            DB.queryUpdate("DELETE FROM playermailbox WHERE toId = (SELECT id FROM players WHERE uuid = ?)", P.getUniqueId().toString());
            Msg.send(P, AnuraCore.getInstance(), MsgType.SUCCESS, "Nachrichten gelöscht!");
        });
    }

    private static class MessengerMsg {

        private String resolved = null;
        private final String time, msg;
        private final Player to;

        @SuppressWarnings("null")
        MessengerMsg(UUID fromUU, String from, String time, String msg, Player to) {
            this.time = time;
            this.msg = msg;
            this.to = to;
            if (fromUU == null) {
                resolved = from;
            } else if (Bukkit.getPlayer(fromUU) != null) {
                resolved = Bukkit.getPlayer(fromUU).getDisplayName();
            } else if (from != null) {
                resolved = from;
            } else {
                UUIDManager.getName(fromUU, (name) -> {
                    resolved = name;
                });
            }
        }

        @SuppressWarnings("SleepWhileInLoop")
        public void send() {
            while (resolved == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Messenger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            to.sendMessage(ChatColor.DARK_GRAY + "[" + time + "] " + ChatColor.RESET + resolved + ChatColor.GRAY + " -> " + to.getDisplayName() + ChatColor.GRAY + ": " + ChatColor.RESET + msg);
        }
    }
}
