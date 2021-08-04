package de.mc_anura.realisticminecraft.infobar;

import de.mc_anura.core.selections.CuboidSelection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TemperatureModifier {

    private static final float RAIN_MOD = -0.075f;
    private static final float WATER_MOD = -0.5f;
    private static final float FIRE_MOD = 0.5f;
    private static final float WARM_MOD = 0.2f;

    public static final float DRINK_MOD = -1f;
    public static final float EAT_MOD = 2f;

    public static float getModifier(@NotNull Player p, @NotNull Location l, @NotNull TemperaturePlayer tp) {
        float modifier = Biome.getModifier(l.getBlock());
        if (modifier == 0) {
            if (tp.getValue() < tp.getDEFAULT()) {
                modifier += 0.1f;
            } else if (tp.getValue() > tp.getDEFAULT()) {
                modifier -= 0.1f;
            }
        } else if (modifier > 0) {
            World w = l.getWorld();
            if (w != null && w.hasStorm() && tp.getValue() > tp.getDEFAULT() && tp.isWarning()) {
                modifier = 0;
            }
        }
        modifier += getRainModifier(l);
        modifier += getWaterModifier(l, tp);
        modifier += getFireModifier(p);
        modifier += getLavaModifier(l);
        modifier += getWarmModifier(l);
        modifier += Equipment.getModifier(p.getEquipment(), modifier);
        return modifier;
    }

    private static float getRainModifier(@NotNull Location l) {
        World w = l.getWorld();
        return w != null && w.hasStorm() && l.getBlock().getRelative(BlockFace.UP).getLightFromSky() == 0xF ? RAIN_MOD : 0;
    }

    private static float getWaterModifier(@NotNull Location l, @NotNull TemperaturePlayer tp) {
        if (l.getWorld() != null) {
            Block b = l.getBlock();
            if (b.getTemperature() >= 0.9f && tp.getValue() < tp.getDEFAULT()) {
                return 0f;
            }
            return b.getType().equals(Material.WATER) ? WATER_MOD : 0;
        }
        return 0f;
    }

    private static float getFireModifier(@NotNull Player p) {
        return p.getFireTicks() > 0 ? FIRE_MOD : 0;
    }

    private static float getLavaModifier(@NotNull Location l) {
        return new CuboidSelection(l.getBlock(), 3).getCount(Material.LAVA) > 0 ? FIRE_MOD : 0;
    }

    private static float getWarmModifier(@NotNull Location l) {
        CuboidSelection sel = new CuboidSelection(l.getBlock(), 3);
        ArrayList<Block> blocks = sel.getBlocks(Material.FURNACE);
        boolean lit_furnace = false;
        for (Block b : blocks) {
            BlockData bd = b.getBlockData();
            if (bd instanceof Furnace f) {
                if (f.isLit()) {
                    lit_furnace = true;
                }
            }
        }
        return lit_furnace || sel.getCount(Material.FIRE) > 0 ? WARM_MOD : 0;
    }

    private enum Biome {
        COLD(-1f, 0.5f, 1.5f, 1f, -0.05f, 0.5f),
        WARM(0.9f, 1f, 1.5f, 1f, 0.05f, 1f),
        HOT(1f, 2f, 2f, 1f, 0.05f, 1.5f);

        private final float minTemp, maxTemp, dayScalar, nightScalar, modifier, sunModifier;

        Biome(float min, float max, float day, float night, float modifier, float sun) {
            minTemp = min;
            maxTemp = max;
            dayScalar = day;
            nightScalar = night;
            this.modifier = modifier;
            sunModifier = sun;
        }

        public float getMinTemp() {
            return minTemp;
        }

        public float getMaxTemp() {
            return maxTemp;
        }

        public float getDayScalar() {
            return dayScalar;
        }

        public float getNightScalar() {
            return nightScalar;
        }

        public float getModifier() {
            return modifier;
        }

        public float getSunModifier() {
            return sunModifier;
        }

        public static float getModifier(Block b) {
            float modifier = 0;
            for (Biome biome : values()) {
                if (b.getTemperature() >= biome.getMinTemp() && b.getTemperature() <= biome.getMaxTemp()) {
                    if (isDay(b)) {
                        if (b.getLightFromSky() == 0xF && !b.getWorld().hasStorm()) {
                            modifier += biome.getModifier() * biome.getDayScalar() * biome.getSunModifier();
                        } else {
                            modifier += biome.getModifier() * biome.getDayScalar();
                        }
                    } else {
                        modifier += biome.getModifier() * biome.getNightScalar();
                    }
                    break;
                }
            }
            return modifier;
        }

        public static boolean isDay(Block b) {
            return b.getWorld().getTime() > 500 && b.getWorld().getTime() < 11500;
        }
    }

    private enum Equipment {
        LEATHER_BHL(5, Material.LEATHER_BOOTS, Material.LEATHER_HELMET, Material.LEATHER_LEGGINGS),
        LEATHER_C(15, Material.LEATHER_CHESTPLATE),
        IRON_BHL(7.5f, Material.IRON_BOOTS, Material.IRON_HELMET, Material.IRON_LEGGINGS),
        IRON_C(17.5f, Material.IRON_CHESTPLATE),
        GOLD_BHL(10, Material.GOLDEN_BOOTS, Material.GOLDEN_HELMET, Material.GOLDEN_LEGGINGS),
        GOLD_C(20, Material.GOLDEN_CHESTPLATE),
        DIAMOND_BHL(12.5f, Material.DIAMOND_BOOTS, Material.DIAMOND_HELMET, Material.DIAMOND_LEGGINGS),
        DIAMOND_C(22.5f, Material.DIAMOND_CHESTPLATE);

        Equipment(float percentage, Material... mat) {
            this.percentage = percentage;
            this.material = Arrays.asList(mat);
        }

        private final List<Material> material;
        private final float percentage;

        private boolean contains(Material m) {
            return material.contains(m);
        }

        private float getPercentage() {
            return percentage;
        }

        private float getModifier(float tempModifier) {
            return tempModifier * getPercentage() / 100;
        }

        private static float getModifier(Material m, float tempModifier) {
            for (Equipment e : values()) {
                if (e.contains(m)) {
                    return e.getModifier(tempModifier);
                }
            }
            return 0;
        }

        public static float getModifier(@Nullable EntityEquipment ee, float tempModifier) {
            if (ee == null) {
                return 0;
            }
            float modifier = 0;
            ItemStack boots = ee.getBoots();
            if (boots != null) {
                modifier += getModifier(boots.getType(), tempModifier);
            }
            ItemStack chestplate = ee.getChestplate();
            if (chestplate != null) {
                modifier += getModifier(chestplate.getType(), tempModifier);
            }
            ItemStack helmet = ee.getHelmet();
            if (helmet != null) {
                modifier += getModifier(helmet.getType(), tempModifier);
            }
            ItemStack leggings = ee.getLeggings();
            if (leggings != null) {
                modifier += getModifier(leggings.getType(), tempModifier);
            }
            return modifier * -1;
        }
    }
}
