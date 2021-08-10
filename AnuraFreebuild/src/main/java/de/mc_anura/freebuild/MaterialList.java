package de.mc_anura.freebuild;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.Tag;

public class MaterialList {

    public static final List<Material> droppingTop = new ArrayList<>();

    static {
        droppingTop.addAll(Arrays.asList(
                Material.BLACK_BANNER,
                Material.BLUE_BANNER,
                Material.BROWN_BANNER,
                Material.CYAN_BANNER,
                Material.GRAY_BANNER,
                Material.GREEN_BANNER,
                Material.LIGHT_BLUE_BANNER,
                Material.LIGHT_GRAY_BANNER,
                Material.LIME_BANNER,
                Material.MAGENTA_BANNER,
                Material.ORANGE_BANNER,
                Material.PINK_BANNER,
                Material.PURPLE_BANNER,
                Material.RED_BANNER,
                Material.WHITE_BANNER,
                Material.YELLOW_BANNER,

                Material.BAMBOO_SAPLING,
                Material.BAMBOO,
                Material.MOSS_CARPET,
                Material.BROWN_MUSHROOM,
                Material.RED_MUSHROOM,

                Material.DEAD_BRAIN_CORAL,
                Material.DEAD_BRAIN_CORAL_FAN,
                Material.DEAD_BUBBLE_CORAL,
                Material.DEAD_BUBBLE_CORAL_FAN,
                Material.DEAD_FIRE_CORAL,
                Material.DEAD_FIRE_CORAL_FAN,
                Material.DEAD_TUBE_CORAL,
                Material.DEAD_TUBE_CORAL_FAN,
                Material.DEAD_HORN_CORAL,
                Material.DEAD_HORN_CORAL_FAN,

                Material.CACTUS,
                Material.CAKE,
                Material.COMPARATOR,
                Material.GRASS,
                Material.TALL_GRASS,
                Material.FERN,
                Material.LARGE_FERN,
                Material.DEAD_BUSH,

                Material.SNOW,
                Material.TWISTING_VINES,
                Material.TWISTING_VINES_PLANT,

                Material.REDSTONE_WIRE,
                Material.REPEATER,
                Material.SUGAR_CANE,

                Material.NETHER_SPROUTS,
                Material.WARPED_ROOTS,
                Material.CRIMSON_ROOTS,
                Material.WARPED_FUNGUS,
                Material.CRIMSON_FUNGUS,
                Material.SEA_PICKLE,
                Material.SEAGRASS,
                Material.KELP,
                Material.KELP_PLANT,
                Material.BIG_DRIPLEAF_STEM,
                Material.BIG_DRIPLEAF,
                Material.SMALL_DRIPLEAF,
                Material.LILY_PAD,
                Material.SCAFFOLDING,

                Material.TORCH,
                Material.SOUL_TORCH,
                Material.REDSTONE_TORCH
        ));

        droppingTop.addAll(Tag.DOORS.getValues());
        droppingTop.addAll(Tag.PRESSURE_PLATES.getValues());
        droppingTop.addAll(Tag.SAPLINGS.getValues());
        droppingTop.addAll(Tag.STANDING_SIGNS.getValues());
        droppingTop.addAll(Tag.RAILS.getValues());
        droppingTop.addAll(Tag.CROPS.getValues());
        droppingTop.addAll(Tag.FLOWERS.getValues());
        droppingTop.addAll(Tag.CARPETS.getValues());
        droppingTop.addAll(Tag.CORALS.getValues());
    }

    public static final List<Material> droppingBottom = new ArrayList<>();

    static {
        droppingBottom.addAll(Arrays.asList(
                Material.SPORE_BLOSSOM,
                Material.HANGING_ROOTS,
                Material.WEEPING_VINES_PLANT,
                Material.WEEPING_VINES,
                Material.CAVE_VINES_PLANT,
                Material.CAVE_VINES,
                Material.TALL_GRASS,
                Material.LARGE_FERN
        ));

        droppingBottom.addAll(Tag.DOORS.getValues());
        droppingBottom.addAll(Tag.TALL_FLOWERS.getValues());
    }
}
