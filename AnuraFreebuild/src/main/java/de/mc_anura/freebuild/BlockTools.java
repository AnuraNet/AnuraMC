package de.mc_anura.freebuild;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.*;
import org.bukkit.block.data.type.Bell.Attachment;
import org.bukkit.material.Button;

public class BlockTools {

    public static boolean isBreakingTop(BlockData data) {
        Material mat = data.getMaterial();
        if (data instanceof Bell bell) {
            return bell.getAttachment() == Attachment.FLOOR;
        }
        if (Tag.BUTTONS.isTagged(data.getMaterial()) || mat == Material.LEVER) {
            return ((FaceAttachable) data).getAttachedFace() == FaceAttachable.AttachedFace.FLOOR;
        }
        if (data instanceof Lantern lantern) {
            return !lantern.isHanging();
        }
        if (mat == Material.SMALL_AMETHYST_BUD || mat == Material.MEDIUM_AMETHYST_BUD ||
                mat == Material.LARGE_AMETHYST_BUD || mat == Material.AMETHYST_CLUSTER) {
            return ((Directional) data).getFacing() == BlockFace.UP;
        }
        if (data instanceof PointedDripstone drip) {
            return drip.getVerticalDirection() == BlockFace.UP;
        }
        if (mat == Material.GLOW_LICHEN) {
            return ((MultipleFacing) data).hasFace(BlockFace.DOWN);
        }
        return MaterialList.droppingTop.contains(mat);
    }

    public static boolean isBreakingSide(BlockData data, BlockFace broken) {
        Material mat = data.getMaterial();
        boolean directional = false;
        if (mat.toString().contains("WALL_BANNER") ||
            mat == Material.SMALL_AMETHYST_BUD || mat == Material.MEDIUM_AMETHYST_BUD ||
            mat == Material.LARGE_AMETHYST_BUD || mat == Material.AMETHYST_CLUSTER ||
            Tag.WALL_SIGNS.isTagged(mat) || mat == Material.TRIPWIRE_HOOK ||
            mat == Material.WALL_TORCH || mat == Material.REDSTONE_WALL_TORCH ||
            mat == Material.SOUL_WALL_TORCH || data instanceof CoralWallFan ||
            mat == Material.LADDER) {
            directional = true;
        } else if (Tag.BUTTONS.isTagged(mat) || mat == Material.LEVER) {
            directional = ((FaceAttachable) data).getAttachedFace() == FaceAttachable.AttachedFace.WALL;
        }

        if (directional) {
            return ((Directional) data).getFacing() == broken.getOppositeFace();
        }

        if (mat == Material.COCOA) { // Cocoa beans are dumb (looking in the opposite direction)
            return ((Directional) data).getFacing() == broken;
        }

        if (mat == Material.GLOW_LICHEN || mat == Material.VINE) {
            return ((MultipleFacing) data).hasFace(broken);
        }

        if (data instanceof Bed bed) {
            if (bed.getPart() == Bed.Part.FOOT) {
                return bed.getFacing() == broken;
            } else {
                return bed.getFacing() == broken.getOppositeFace();
            }
        }

        if (data instanceof Bell bell) {
            return (bell.getAttachment() == Attachment.SINGLE_WALL && bell.getFacing() == broken)
                || (bell.getAttachment() == Attachment.DOUBLE_WALL && (bell.getFacing() == broken || bell.getFacing() == broken.getOppositeFace()));
        }
        return false;
    }

    public static boolean isBreakingBottom(BlockData data) {
        Material mat = data.getMaterial();
        if (data instanceof Bell bell) {
            return bell.getAttachment() == Attachment.CEILING;
        }
        if (Tag.BUTTONS.isTagged(data.getMaterial()) || mat == Material.LEVER) {
            return ((FaceAttachable) data).getAttachedFace() == FaceAttachable.AttachedFace.CEILING;
        }
        if (data instanceof Lantern lantern) {
            return lantern.isHanging();
        }
        if (mat == Material.SMALL_AMETHYST_BUD || mat == Material.MEDIUM_AMETHYST_BUD ||
            mat == Material.LARGE_AMETHYST_BUD || mat == Material.AMETHYST_CLUSTER) {
            return ((Directional) data).getFacing() == BlockFace.DOWN;
        }
        if (data instanceof PointedDripstone drip) {
            return drip.getVerticalDirection() == BlockFace.DOWN;
        }
        if (mat == Material.GLOW_LICHEN || mat == Material.VINE) {
            return ((MultipleFacing) data).hasFace(BlockFace.UP);
        }

        return MaterialList.droppingBottom.contains(mat);
    }

    public static Location getPossibleSpawn(Location loc) {
        World w = loc.getWorld();
        Random r = new Random();
        loc.add(r.nextInt(15), 0, r.nextInt(15));
        Block hb = w.getHighestBlockAt(loc);
        if (hb.getType().equals(Material.LAVA)) {
            for (int i = hb.getX(), j = 0; j < 50; i++, j++) {
                Location loc2 = new Location(w, i, hb.getY(), hb.getZ());
                Block tg = w.getHighestBlockAt(loc2);
                if (tg.getType().equals(Material.LAVA)) continue;
                return tg.getRelative(BlockFace.UP).getLocation();
            }
        }
        return hb.getRelative(BlockFace.UP).getLocation();
    }

    public static boolean respawnFood(Block b) {
        return false;
    }

    public static Location getNewOreLocation(Location l, Material base) {
        Random r = new Random();
        return getNewOreLocation(l, base, r, 1);
    }

    private static Location getNewOreLocation(Location origL, Material base, Random r, int its) {
        int x = r.nextInt(50) * (r.nextBoolean() ? -1 : 1);
        int y = r.nextInt(5) *  (r.nextBoolean() ? -1 : 1);
        int z = r.nextInt(50) * (r.nextBoolean() ? -1 : 1);
        Location l = origL.clone().add(x, y, z);
        if (l.getBlock().getType().equals(base)) {
            return l;
        } else {
            its++;
            if (its == 1000) {
                return origL;
            }
            return getNewOreLocation(origL, base, r, its);
        }
    }

}
