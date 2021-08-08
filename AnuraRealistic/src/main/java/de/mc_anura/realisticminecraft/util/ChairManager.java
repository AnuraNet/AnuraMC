package de.mc_anura.realisticminecraft.util;

import de.mc_anura.core.util.Blocks;
import de.mc_anura.core.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class ChairManager {

    private static final Set<Player> DISABLED_CHAIR = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<Player, ArmorStand> SITTING_PLAYERS = new WeakHashMap<>();
    private static final Map<Player, Block> SITTING_BLOCKS = new WeakHashMap<>();
    private static final Map<Player, Location> ENTER_LOC = new WeakHashMap<>();

    public static void playerSitDown(@NotNull Player p, @NotNull Stairs s, @NotNull Block b) {
        ENTER_LOC.put(p, p.getLocation());
        //ArmorStand creation
        Location loc = b.getLocation().add(0.5, 0.25, 0.5);
        loc.setYaw(Blocks.faceToYaw(s.getFacing()));
        World w = loc.getWorld();
        if (w != null) {
            ArmorStand as = (ArmorStand) w.spawnEntity(loc, EntityType.ARMOR_STAND, CreatureSpawnEvent.SpawnReason.CUSTOM, (e) -> {
                ArmorStand a = (ArmorStand) e;
                a.setMarker(true);
                a.setInvisible(true);
                a.setGravity(false);
                AttributeInstance maxHealth = a.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealth != null) {
                    maxHealth.setBaseValue(0);
                }
            });
            Location pLoc = p.getLocation();
            pLoc.setYaw(loc.getYaw());
            p.teleport(pLoc);
            as.addPassenger(p);
            SITTING_PLAYERS.put(p, as);
            SITTING_BLOCKS.put(p, b);
        }
    }

    public static void playerStandUp(@NotNull Player p) {
        ArmorStand as = SITTING_PLAYERS.remove(p);
        as.removePassenger(p);
        as.remove();
        SITTING_BLOCKS.remove(p);
        Location loc = ENTER_LOC.remove(p);
        if (loc != null) {
            p.teleport(loc);
        }
    }

    public static void changeSeat(@NotNull Player p, @NotNull Stairs s, @NotNull Block b) {
        Block sb = SITTING_BLOCKS.get(p);
        Location enterLoc = null;
        if (sb != null && allowedAbove(sb.getRelative(BlockFace.UP, 2).getType())) {
            ENTER_LOC.remove(p);
            enterLoc = sb.getRelative(BlockFace.UP).getLocation();
            enterLoc.setYaw(p.getLocation().getYaw());
            enterLoc.setPitch(p.getLocation().getPitch());
        }
        playerStandUp(p);
        if (enterLoc != null) {
            p.teleport(enterLoc);
        }
        playerSitDown(p, s, b);
    }

    public static void playerStandUp(@NotNull Block b) {
        Player p = Util.getKeyByValue(SITTING_BLOCKS, b);
        if (p != null) {
            playerStandUp(p);
        }
    }

    public static boolean isSittingAnyone(@NotNull Block b) {
        return SITTING_BLOCKS.containsValue(b);
    }

    public static boolean isSittingPlayer(@NotNull Block b, @NotNull Player p) {
        return SITTING_BLOCKS.containsValue(b) && Objects.equals(Util.getKeyByValue(SITTING_BLOCKS, b), p);
    }

    public static boolean isSitting(@NotNull Player p) {
        return SITTING_PLAYERS.containsKey(p);
    }

    public static void destroyAll() {
        new HashSet<>(SITTING_PLAYERS.keySet()).forEach(ChairManager::playerStandUp);
    }

    public static boolean isDisabled(@NotNull Player p) {
        return DISABLED_CHAIR.contains(p);
    }

    public static void disableChair(@NotNull Player p) {
        DISABLED_CHAIR.add(p);
    }

    public static void enableChair(@NotNull Player p) {
        DISABLED_CHAIR.remove(p);
    }

    public static boolean allowedAbove(@NotNull Material m) {
        return !m.isSolid() || Tag.BANNERS.isTagged(m) || Tag.WALL_SIGNS.isTagged(m) || Tag.TRAPDOORS.isTagged(m);
    }
}
