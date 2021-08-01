package de.mc_anura.core.commands;

//import de.mc_anura.core.API.Core;
//import de.mc_anura.core.Money;
//import de.mc_anura.core.API.Tools;
//import java.sql.ResultSet;
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//import org.bukkit.OfflinePlayer;
//import org.bukkit.World;
//import org.bukkit.command.BlockCommandSender;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//
//public class PlayerCommands implements CommandExecutor {
//
//    @Override
//    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
//        Player P = null;
//        if (sender instanceof Player) {
//            P = (Player) sender;
//        }
//
//        if (cmd.getName().equalsIgnoreCase("spawn")) {
//            if (args.length == 1) {
//                if (sender.hasPermission("core.commands.spawn-other") || sender instanceof BlockCommandSender) {
//                    if (Bukkit.getOfflinePlayer(args[0]).isOnline()) {
//                        P = Bukkit.getPlayer(args[0]);
//                    }
//                }
//            }
//            if (P == null) {
//                Core.statusMsg(sender, "only_player_cmd", false);
//                return true;
//            }
//            Core.toSpawn(P);
//            Core.statusMsg(P, "spawn_tp_done", true);
//            return true;
//        } else if (cmd.getName().equalsIgnoreCase("warp")) {
//            if (args.length == 0 || args.length > 2) {
//                Core.statusMsg(P, "wrong_args_count", false);
//                return false;
//            }
//            if (P == null) {
//                if (args.length != 2) {
//                    Core.statusMsg(P, "wrong_args_count", false);
//                    return false;
//                }
//
//            }
//            if (args.length == 2) {
//                String p = args[1];
//                if (!Bukkit.getOfflinePlayer(p).isOnline()) {
//                    Core.statusMsg(P, "player_not_online", false);
//                    return false;
//                }
//                P = Bukkit.getPlayer(p);
//            }
//            String warpName = args[0];
//            ResultSet rs = Core.getMySql().querySelect("SELECT world, X, Y, Z, userWarp, server FROM coreWarps WHERE name = '" + warpName + "'");
//            rs.last();
//            if (rs.getRow() == 0) {
//                Core.statusMsg(P, "warp_not_exist", false);
//                return true;
//            }
//            rs.first();
//            if (P == null) {
//                return false;
//            }
//            if (!rs.getBoolean("userWarp") && !P.hasPermission("core.commands.adminWarp")) {
//                Core.statusMsg(P, "no_perms", false);
//                return true;
//            }
//            int X = rs.getInt("X");
//            int Y = rs.getInt("Y");
//            int Z = rs.getInt("Z");
//            String server = rs.getString("server");
//            String world = rs.getString("world");
//            if (!server.equals(Core.getMainClass().getConfig().getString("server-name"))) {
//                Core.statusMsg(P, "warp_other_server", false);
//                return true;
//            }
//            World w = Bukkit.getWorld(world);
//            if (w == null) {
//                Core.statusMsg(P, "warp_not_avail", false);
//                return true;
//            }
//            Location loc = new Location(w, X, Y, Z);
//            P.teleport(loc);
//            Core.statusMsg(P, "warp_tp_done", true);
//            return true;
//        }
//        return false;
//    }
//}
