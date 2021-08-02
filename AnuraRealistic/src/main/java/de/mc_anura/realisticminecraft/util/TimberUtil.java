package de.mc_anura.realisticminecraft.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class TimberUtil {

    public static boolean isAxe(Material mat) {
        return switch (mat) {
            case WOODEN_AXE, STONE_AXE, IRON_AXE, GOLDEN_AXE, DIAMOND_AXE -> true;
            default -> false;
        };
    }

    public static void calculateDamage(ItemStack is, int damage) {
        ItemMeta im = is.getItemMeta();
        if (!(im instanceof Damageable d)) {
            return;
        }
        if (is.getType().getMaxDurability() - d.getDamage() - damage < 0) {
            is.setType(Material.AIR);
        } else {
            d.setDamage((short) (d.getDamage() + damage));
        }
    }
}
