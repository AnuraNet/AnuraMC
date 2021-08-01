package de.mc_anura.core.commands;//package de.mc_anura.core.commands;
//
//import de.mc_anura.core.API.Core;
//import de.mc_anura.core.API.Tools;
//import java.sql.ResultSet;
//import org.bukkit.Bukkit;
//import org.bukkit.GameMode;
//import org.bukkit.Location;
//import org.bukkit.World;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//
//public class TeamCommands implements CommandExecutor {
//
//    @Override
//    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
//        Player P = null;
//        if (sender instanceof Player) {
//            P = (Player) sender;
//        }
//        if (cmd.getName().equalsIgnoreCase("gm")) {
//            
//
//        } else if (cmd.getName().equalsIgnoreCase("sun")) {
//            if (P != null) {
//                if (P.hasPermission("core.commands.sun")) {
//                    World w = P.getLocation().getWorld();
//                    w.setTime(2000);
//                    w.setThundering(false);
//                    w.setStorm(false);
//                    Tools.sendStatusMsg(sender, "Sun!", true);
//                    return true;
//                }
//            }
//        } else if (cmd.getName().equalsIgnoreCase("setwarp")) {
//            if (P == null) {
//                Core.statusMsg(sender, "only_player_cmd", false);
//                return true;
//            }
//            if (!P.hasPermission("core.commands.setwarp")) {
//                Core.statusMsg(P, "no_perms", false);
//                return true;
//            }
//            if (args.length < 1) {
//                Core.statusMsg(P, "wrong_args_count", false);
//                return false;
//            }
//            String userWarp = "0";
//            if (args.length == 2) {
//                if (args[1].equalsIgnoreCase("true")) {
//                    userWarp = "1";
//                }
//            }
//            Location loc = P.getLocation();
//            int X = loc.getBlockX();
//            int Y = loc.getBlockY();
//            int Z = loc.getBlockZ();
//            String world = loc.getWorld().getName();
//            String server = Core.getMainClass().getConfig().getString("server-name");
//            String name = args[0];
//            ResultSet rs = Core.getMySql().querySelect("SELECT `name` FROM coreWarps WHERE name = '" + name + "'");
//            rs.last();
//            if (rs.getRow() != 0) {
//                Core.statusMsg(P, "warp_alr_exist", false);
//                return true;
//            }
//            Core.getMySql().queryUpdate("INSERT INTO coreWarps(`name`, server, world, X, Y, Z, userWarp) VALUES('" + name + "', '" + server + "', '" + world + "', '" + X + "', '" + Y + "', '" + Z + "', '" + userWarp + "')");
//            Core.statusMsg(P, "warp_set", true);
//            return true;
//        } else if (cmd.getName().equalsIgnoreCase("remwarp")) {
//            if (args.length != 1) {
//                Core.statusMsg(P, "wrong_args_count", false);
//                return false;
//            }
//            if (!sender.hasPermission("core.commands.remwarp")) {
//                Core.statusMsg(P, "no_perms", false);
//                return true;
//            }
//            String map = args[0];
//            ResultSet rs = Core.getMySql().querySelect("SELECT name FROM coreWarps WHERE name = '" + map + "'");
//            rs.last();
//            if (rs.getRow() == 0) {
//                Core.statusMsg(P, "warp_not_exist", false);
//                return true;
//            }
//            Core.getMySql().queryUpdate("DELETE FROM coreWarps WHERE name = '" + map + "'");
//            Core.statusMsg(P, "warp_rem_done", true);
//            return true;
//        } else if (cmd.getName().equalsIgnoreCase("warplist")) {
//            if (P == null) return false;
//            ResultSet rs = Core.getMySql().querySelect("SELECT name, userWarp FROM coreWarps WHERE server = '" + Core.getMainClass().getConfig().getString("server-name") + "'");
//            while (rs.next()) {
//                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + P.getName() + " {color:\"" + (rs.getBoolean("userWarp") ? "green" : "blue") + "\",text:\"" + rs.getString("name") + "\",clickEvent:{action:run_command,value:\"/warp " + rs.getString("name") + "\"}}");
//            }
//            return true;
//        } else if (cmd.getName().equalsIgnoreCase("mute")) {
//            if (sender.hasPermission("core.commands.mute")) {
//                if (args.length == 0) return false;
//                Player p = Bukkit.getPlayer(args[0]);
//                if (p == null) {
//                    sender.sendMessage("Spieler ist nicht online!");
//                    return true;
//                }
//                Core.getMySql().queryUpdate("UPDATE players SET muted = 1 WHERE uuid = '"+p.getUniqueId().toString()+"'");
//                sender.sendMessage("Spieler gemutet!");
//                return true;
//            }
//        } else if (cmd.getName().equalsIgnoreCase("unmute")) {
//            if (sender.hasPermission("core.commands.mute")) {
//                if (args.length == 0) return false;
//                Player p = Bukkit.getPlayer(args[0]);
//                if (p == null) {
//                    sender.sendMessage("Spieler ist nicht online!");
//                    return true;
//                }
//                Core.getMySql().queryUpdate("UPDATE players SET muted = 0 WHERE uuid = '"+p.getUniqueId().toString()+"'");
//                sender.sendMessage("Spieler entmutet!");
//                return true;
//            }
//        }
//        return false;
//    }
//}
