package de.mc_anura.freebuild.regions;

import de.mc_anura.core.AnuraThread;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RegionManager {

    private static final Map<Integer, Region> regions = new ConcurrentHashMap<>();
    
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

    public static Collection<Region> getRegions() {
        return regions.values();
    }

    public static void addRegion(Region r) {
        regions.put(r.getId(), r);
    }
}
