package de.mc_anura.freebuild;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bell;
import org.bukkit.block.data.type.Bell.Attachment;

public class BlockTools {

    public static boolean isBreakingTop(BlockData data) {
        if (data.getMaterial() == Material.BELL) {
            return ((Bell) data).getAttachment() == Attachment.FLOOR;
        }
        return MaterialList.droppingTop.contains(data.getMaterial());
    }

    public static boolean isBreakingSide(BlockData data, BlockFace broken) {
        // todo: BED
        if (data.getMaterial() == Material.BELL) {
            Bell bell = (Bell) data;
            return bell.getAttachment() == Attachment.SINGLE_WALL && bell.getFacing() == broken;
        }
        return MaterialList.droppingOnSide.contains(data.getMaterial());
    }

    public static boolean isBreakingBottom(BlockData data) {
        if (data.getMaterial() == Material.BELL) {
            return ((Bell) data).getAttachment() == Attachment.CEILING;
        }
        return MaterialList.droppingBottom.contains(data.getMaterial());
    }

    public static boolean isOre(Material m) {
        return MaterialList.ores.contains(m);
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

    public static Location getNewOreLocation(Location l) {
        Random r = new Random();
        return getNewOreLocation(l, r, 1);
    }

    private static Location getNewOreLocation(Location origL, Random r, int its) {
        int x = r.nextInt(50) * (r.nextBoolean() ? -1 : 1);
        int y = r.nextInt(5) *  (r.nextBoolean() ? -1 : 1);
        int z = r.nextInt(50) * (r.nextBoolean() ? -1 : 1);
        Location l = origL.clone().add(x, y, z);
        if (l.getBlock().getType().equals(Material.STONE)) {
            return l;
        } else {
            its++;
            if (its == 1000) {
                return origL;
            }
            return getNewOreLocation(origL, r, its);
        }
    }

}
