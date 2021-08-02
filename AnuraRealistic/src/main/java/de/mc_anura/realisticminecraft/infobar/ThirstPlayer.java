package de.mc_anura.realisticminecraft.infobar;

import de.mc_anura.core.tools.Potions;
import de.mc_anura.core.tools.Potions.CustomPotion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ThirstPlayer extends RealisticPlayer {

    private static final CustomPotion POTION_THIRSTY = new CustomPotion(new PotionEffect(PotionEffectType.CONFUSION, 0, 0), true);
    private static final CustomPotion POTION_WEAK_I = new CustomPotion(new PotionEffect(PotionEffectType.WEAKNESS, 0, 0), true);
    private static final CustomPotion POTION_WEAK_II = new CustomPotion(new PotionEffect(PotionEffectType.SLOW, 0, 0), true);
    
    private static final short THIRSTY = 5;
    private static final short WEAK = 10;
    private static final short NO_SPRINT = 20;
    
    private static final short MIN_V = 0;
    private static final short MAX_V = 100;
    
    private static final float BASIC_MOD = -0.015f;
    private static final float DRINK_MOD = 10f;
    private static final float RAIN_MOD = 1f;
    private static final float FOOD_LEVEL_MOD = -1f;
    
    private static final float NOMADEN_MOD = 0.25f;

    public ThirstPlayer(Player p) {
        this(p, -1, BarStatus.STANDARD);
    }

    public ThirstPlayer(Player p, float thirst, BarStatus status) {
        super(p, thirst, MIN_V, MAX_V, MAX_V);
        bar = new ThirstBar(this, status);
    }

    @Override
    protected boolean blockModify() {
        return false;
    }

    public boolean isThirsty() {
        return value <= THIRSTY;
    }

    public boolean isWeak() {
        return value <= WEAK;
    }

    public boolean isNoSprint() {
        return value <= NO_SPRINT;
    }

    @Override
    public float calculateNewValue(Location l) {
        float mod = BASIC_MOD;
//        if (TheTownAPI.isInWarriors() && WarriorsProvider.isNomaden(player)) {
//            mod *= NOMADEN_MOD;
//        }
        World w = l.getWorld();
        if (w != null && w.hasStorm() && l.getPitch() < -75 && l.getBlock().getRelative(BlockFace.UP).getLightFromSky() == 0xF) {
            mod += RAIN_MOD;
        }
        return addValue(mod);
    }

    @Override
    protected void changeValue(float newValue) {
        if (!isWeak() && newValue <= WEAK) {
            Potions.addCustomPotion(player, POTION_WEAK_I);
            Potions.addCustomPotion(player, POTION_WEAK_II);
        } else if (isWeak() && newValue > WEAK) {
            Potions.removeCustomPotion(player, POTION_WEAK_I);
            Potions.removeCustomPotion(player, POTION_WEAK_II);
        }
        if (!isThirsty() && newValue <= THIRSTY) {
            Potions.addCustomPotion(player, POTION_THIRSTY);
        } else if (isThirsty() && newValue > THIRSTY) {
            Potions.removeCustomPotion(player, POTION_THIRSTY);
        }
    }

    public float drink() {
        return addValue(DRINK_MOD);
    }

    public float foodLevelChange() {
//        if (TheTownAPI.isInWarriors() && WarriorsProvider.isNomaden(player)) {
//            return addValue(FOODLEVEL_MOD * NOMADEN_MOD);
//        }
        return addValue(FOOD_LEVEL_MOD);
    }
    
    @Override
    public String getTableName() {
        return getStaticTableName();
    }
    
    public static String getStaticTableName() {
        return "playerThirst";
    }
    
    public static boolean isOcean(Biome b) {
        return switch (b) {
            case OCEAN, DEEP_OCEAN, FROZEN_OCEAN -> true;
            default -> false;
        };
    }
}