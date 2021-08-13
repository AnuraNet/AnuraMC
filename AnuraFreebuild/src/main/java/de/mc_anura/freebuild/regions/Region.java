package de.mc_anura.freebuild.regions;

import de.mc_anura.core.database.DB;
import de.mc_anura.core.selections.CuboidSelection;
import de.mc_anura.freebuild.ClaimManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Region {

    private final CuboidSelection sel;
    private final UUID owner;
    private int id;

    public Region(int id, UUID owner, Location loc1, Location loc2) {
        loc1.setY(0);
        loc2.setY(255);
        sel = new CuboidSelection(loc1, loc2);

        this.owner = owner;
        this.id = id;

        RegionManager.addRegion(this);
    }

    public Region(UUID owner, Location loc1, Location loc2) {
        this(-1, owner, loc1, loc2);
    }

    public void save() {
        String sql;
        if (id == -1) {
            sql = "INSERT INTO claims";
        } else {
            sql = "UPDATE claims";
        }
        sql += " SET player = (SELECT id FROM players WHERE uuid = ?), world = ?, X1 = ?, Z1 = ?, X2 = ?, Z2 = ?";
        if (id != -1) {
            sql += " WHERE id = ?";
        }
        Location min = sel.getMinCorner();
        Location max = sel.getMaxCorner();
        if (id == -1) {
            DB.queryUpdate(true, DB.getFirstKey((id) -> {
                this.id = id;
            }), sql, owner.toString(), sel.getWorld().getName(), min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ());
        } else {
            DB.queryUpdate(true, sql, owner.toString(), sel.getWorld().getName(), min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ(), id);
        }
    }

    public boolean isInside(Location loc) {
        if (loc.getWorld() != sel.getWorld()) {
            return false;
        }
        return sel.contains(loc);
    }

    // TODO: Quick and dirty, find better solution!
    public boolean isInRadius(Location loc, int radius) {
        if (loc.getWorld() != sel.getWorld()) return false;
        Location min = sel.getMinCorner();
        Location max = sel.getMaxCorner();
        min.setY(loc.getBlockY());
        max.setY(loc.getBlockY());
        int rSq = radius * radius;
        return loc.distanceSquared(min) < rSq || loc.distanceSquared(max) < rSq ||
                loc.distanceSquared(new Location(min.getWorld(), min.getBlockX(), min.getBlockY(), max.getBlockZ())) < rSq ||
                loc.distanceSquared(new Location(min.getWorld(), max.getBlockX(), min.getBlockY(), min.getBlockZ())) < rSq;
        
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getMin() {
        return sel.getMinCorner();
    }

    public Location getMax() {
        return sel.getMaxCorner();
    }

    public int getId() {
        return id;
    }
    
    public static void loadRegions() {
        try {
            ResultSet rs = DB.querySelect("SELECT claims.*, uuid FROM claims LEFT JOIN players ON players.id = claims.player");
            while (rs.next()) {
                World w = Bukkit.getWorld(rs.getString("world"));
                if (w == null) continue;
                Location l1 = new Location(w, rs.getInt("X1"), 0, rs.getInt("Z1"));
                Location l2 = new Location(w, rs.getInt("X2"), 255, rs.getInt("Z2"));
                new Region(rs.getInt("id"), UUID.fromString(rs.getString("uuid")), l1, l2);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClaimManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
