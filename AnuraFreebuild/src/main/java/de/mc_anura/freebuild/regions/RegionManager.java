package de.mc_anura.freebuild.regions;

import de.mc_anura.core.AnuraThread;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RegionManager {

    private static final List<Region> regions = new ArrayList<>();
    
    public static void init() {
        AnuraThread.async(Region::loadRegions);
    }

    public static boolean isFree(Location l) {
        for (Region r : getRegions()) {
            if (r.isInside(l)) return false;
        }
        return true;
    }

    public static UUID getOwner(Location l) {
        for (Region r : getRegions()) {
            if (r.isInside(l)) return r.getOwner();
        }
        return null;
    }

    public static Boolean isOwner(Player P, Location l) {
        UUID own = getOwner(l);
        if (own == null) return null;
        else return own.equals(P.getUniqueId());
    }

    public static List<Region> getRegions() {
        return regions;
    }

    public static void addRegion(Region r) {
        regions.add(r);
    }
}
