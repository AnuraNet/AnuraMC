package de.mc_anura.realisticminecraft.util;

import de.mc_anura.core.util.Blocks;
import de.mc_anura.core.util.Util;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public abstract class ChairManager {

    private static final Map<Player, ArmorStand> SITTING_PLAYERS = new WeakHashMap<>();
    private static final Map<Player, Block> SITTING_BLOCKS = new WeakHashMap<>();
//    private static Field f;
//
//    static {
//        try {
//            f = PlayerConnection.class.getDeclaredField("B");
//            f.setAccessible(true);
//        } catch (NoSuchFieldException | SecurityException ex) {
//            Logger.getLogger(ChairManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    public static void playerSitDown(Player p, Stairs s, Block b) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(s);
        Objects.requireNonNull(b);
        //Anti Fly-Kick
//        try {
//            f.set(((CraftPlayer) p).getHandle().playerConnection, false);
//        } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
//            Logger.getLogger(ChairManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
        //ArmorStand creation
        Location loc = b.getLocation().add(0.5, 0.25, 0.5);
        loc.setYaw(Blocks.faceToYaw(s.getFacing()));
        World w = loc.getWorld();
        if (w != null) {
            ArmorStand as = (ArmorStand) w.spawnEntity(loc, EntityType.ARMOR_STAND, CreatureSpawnEvent.SpawnReason.CUSTOM, (e) -> {
                ArmorStand a = (ArmorStand) e;
                a.setMarker(true);
                a.setInvisible(true);
            });
//            CraftWorld cw = (CraftWorld) w;
//            EntityArmorStand nms_as = new EntityArmorStand(cw.getHandle(), loc.getX(), loc.getY(), loc.getZ());
//            nms_as.setInvisible(true);
//            nms_as.setMarker(true);
//            ArmorStand as = (ArmorStand) cw.addEntity(nms_as, SpawnReason.CUSTOM);
            as.setGravity(false);
            AttributeInstance maxHealth = as.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(0);
            }
            //as.teleport(loc);
            as.addPassenger(p);
            SITTING_PLAYERS.put(p, as);
            SITTING_BLOCKS.put(p, b);
        }
    }

    public static void playerStandUp(Player p) {
        Objects.requireNonNull(p);
        ArmorStand as = SITTING_PLAYERS.remove(p);
        as.removePassenger(p);
        as.remove();
        SITTING_BLOCKS.remove(p);
    }

    public static void playerStandUp(Block b) {
        Objects.requireNonNull(b);
        playerStandUp(Util.getKeyByValue(SITTING_BLOCKS, b));
    }

    public static boolean isSittingAnyone(Block b) {
        Objects.requireNonNull(b);
        return SITTING_BLOCKS.containsValue(b);
    }

    public static boolean isSittingPlayer(Block b, Player p) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(b);
        return SITTING_BLOCKS.containsValue(b) && Objects.equals(Util.getKeyByValue(SITTING_BLOCKS, b), p);
    }

    public static boolean isSitting(Player p) {
        Objects.requireNonNull(p);
        return SITTING_PLAYERS.containsKey(p);
    }

    public static void destroyAll() {
        new HashSet<>(SITTING_PLAYERS.keySet()).forEach(ChairManager::playerStandUp);
    }
}
