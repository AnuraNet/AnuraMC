package de.mc_anura.freebuild;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.type.CoralWallFan;

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

    public static final List<Material> droppingSideDirectional = new ArrayList<>();

    static {
        droppingSideDirectional.addAll(Arrays.asList(
                Material.BLACK_WALL_BANNER,
                Material.BLUE_WALL_BANNER,
                Material.BROWN_WALL_BANNER,
                Material.CYAN_WALL_BANNER,
                Material.GRAY_WALL_BANNER,
                Material.GREEN_WALL_BANNER,
                Material.LIGHT_BLUE_WALL_BANNER,
                Material.LIGHT_GRAY_WALL_BANNER,
                Material.LIME_WALL_BANNER,
                Material.MAGENTA_WALL_BANNER,
                Material.ORANGE_WALL_BANNER,
                Material.PINK_WALL_BANNER,
                Material.PURPLE_WALL_BANNER,
                Material.RED_WALL_BANNER,
                Material.WHITE_WALL_BANNER,
                Material.YELLOW_WALL_BANNER,
                Material.SMALL_AMETHYST_BUD,
                Material.MEDIUM_AMETHYST_BUD,
                Material.LARGE_AMETHYST_BUD,
                Material.AMETHYST_CLUSTER,
                Material.TRIPWIRE_HOOK,
                Material.WALL_TORCH,
                Material.REDSTONE_WALL_TORCH,
                Material.SOUL_WALL_TORCH,
                Material.LADDER,
                Material.DEAD_BRAIN_CORAL_WALL_FAN,
                Material.DEAD_BUBBLE_CORAL_WALL_FAN,
                Material.DEAD_FIRE_CORAL_WALL_FAN,
                Material.DEAD_HORN_CORAL_WALL_FAN,
                Material.DEAD_TUBE_CORAL_WALL_FAN
        ));
        droppingSideDirectional.addAll(Tag.WALL_SIGNS.getValues());
        droppingSideDirectional.addAll(Tag.WALL_CORALS.getValues());
    }
}
