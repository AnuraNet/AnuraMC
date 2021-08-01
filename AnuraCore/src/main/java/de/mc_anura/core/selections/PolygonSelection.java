package de.mc_anura.core.selections;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

public class PolygonSelection extends Selection {

    public PolygonSelection(List<Location> locations) {
        this.locations = locations;
    }

    public void addLocation(Location loc) {
        this.locations.add(loc);
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;
    }

    @Override
    public String getTypeID() {
        return "polygon2d";
    }

    @Override
    public boolean expand(int size, BlockFace bf) {
        Location loc = locations.get(0);
        switch (bf) {
            case DOWN:
                loc.setY(loc.getY() - size);
                break;

            case UP:
                loc.setY(loc.getY() + size);
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean contains(BlockVector bv) {
        updateMinMax();
        int targetX = bv.getBlockX(); // Width
        int targetY = bv.getBlockY(); // Height
        int targetZ = bv.getBlockZ(); // Depth

        if (targetY < min.getY() || targetY > max.getY()) {
            return false;
        }
        //Quick and dirty check.
        if (targetX < min.getBlockX() || targetX > max.getBlockX() || targetZ < min.getBlockZ() || targetZ > max.getBlockZ()) {
            return false;
        }
        boolean inside = false;
        int npoints = locations.size();
        int xNew, zNew;
        int xOld, zOld;
        int x1, z1;
        int x2, z2;
        long crossproduct;
        int i;

        xOld = locations.get(npoints - 1).getBlockX();
        zOld = locations.get(npoints - 1).getBlockZ();

        for (i = 0; i < npoints; i++) {
            xNew = locations.get(i).getBlockX();
            zNew = locations.get(i).getBlockZ();
            //Check for corner
            if (xNew == targetX && zNew == targetZ) {
                return true;
            }
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                z1 = zOld;
                z2 = zNew;
            } else {
                x1 = xNew;
                x2 = xOld;
                z1 = zNew;
                z2 = zOld;
            }
            if (x1 <= targetX && targetX <= x2) {
                crossproduct = ((long) targetZ - (long) z1) * (long) (x2 - x1)
                        - ((long) z2 - (long) z1) * (long) (targetX - x1);
                if (crossproduct == 0) {
                    if ((z1 <= targetZ) == (targetZ <= z2)) return true; // on edge
                } else if (crossproduct < 0 && (x1 != targetX)) {
                    inside = !inside;
                }
            }
            xOld = xNew;
            zOld = zNew;
        }

        return inside;
    }
}
