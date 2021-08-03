package de.mc_anura.core.tools;

import de.mc_anura.core.Money;
import de.mc_anura.core.database.DB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Warps {

    public static final Map<String, Warp> warps = new HashMap<>();

    public static void load() {
        warps.clear();
        ResultSet rs = DB.querySelect("SELECT * FROM warps");
        try {
            while (rs.next()) {
                float x = rs.getFloat("x");
                float y = rs.getFloat("y");
                float z = rs.getFloat("z");
                float yaw = rs.getFloat("yaw");
                float p = rs.getFloat("pitch");
                UUID world = UUID.fromString(rs.getString("world"));
                World w = Bukkit.getWorld(world);
                if (w == null) {
                    continue;
                }
                Location loc = new Location(w, x, y, z, yaw, p);
                String name = rs.getString("name");
                boolean userWarp = rs.getBoolean("userWarp");
                Warp warp = new Warp(name, loc, userWarp);
                warps.put(name, warp);
            }

        } catch (SQLException ex) {
            Logger.getLogger(Warps.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Map<String, Warp> getWarps() {
        return warps;
    }

    public static boolean createWarp(Warp warp) {
        if (warps.containsKey(warp.name())) {
            return false;
        }
        warps.put(warp.name(), warp);
        Location l = warp.location();
        DB.queryUpdate(true, "INSERT INTO warps(name, x, y, z, yaw, pitch, world, userWarp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                warp.name(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch(), l.getWorld().getUID().toString(), warp.userWarp());
        return true;
    }

    public static boolean deleteWarp(String name) {
        if (!warps.containsKey(name)) {
            return false;
        }
        warps.remove(name);
        DB.queryUpdate(true, "DELETE FROM warps WHERE name = ?", name);
        return true;
    }

    public static Warp getWarp(String warpName) {
        return warps.get(warpName);
    }

    public static record Warp(String name, Location location, boolean userWarp) {}
}
