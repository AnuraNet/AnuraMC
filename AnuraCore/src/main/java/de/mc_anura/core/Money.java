package de.mc_anura.core;

import de.mc_anura.core.tools.ItemSearch;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Money {
    
    public static final String CURRENCY = "Taler";
    public static final int INITIAL_MONEY = 20;
    private static final int CUSTOM_MODEL_DATA = 42424242;
    private static final Material COIN_MATERIAL = Material.COMMAND_BLOCK;

    private static final ItemStack coin = new ItemStack(COIN_MATERIAL);

    public static void init() {
        ItemMeta meta = coin.getItemMeta();
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.displayName(Component.text(CURRENCY, NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
        coin.setItemMeta(meta);
    }

    private static ItemStack createCoins(int amount) {
        ItemStack coins = coin.clone();
        coins.setAmount(amount);
        return coins;
    }

    private static boolean isCoin(ItemStack stack) {
        if (stack == null || stack.getType() != COIN_MATERIAL) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        return meta.getCustomModelData() == CUSTOM_MODEL_DATA;
    }

    public static void pay(Player player, int count) {
        ItemSearch.ItemSearchIterator items = new ItemSearch.ItemSearchIterator(player.getInventory(), count < 0);
        while (count != 0 && items.hasNext()) {
            ItemStack stack = items.next();
            if (!isCoin(stack)) {
                continue;
            }
            int amount = stack.getAmount();
            int actualAdd;
            if (amount + count < 0) {
                items.set(null);
                actualAdd = -amount;
            } else if (amount + count >= stack.getMaxStackSize()) {
                actualAdd = stack.getMaxStackSize() - amount;
                if (actualAdd != 0) {
                    stack.setAmount(stack.getMaxStackSize());
                    items.set(stack);
                }
            } else {
                stack.setAmount(amount + count);
                items.set(stack);
                actualAdd = count;
            }
            count -= actualAdd;
        }

        if (count > 0) {
            player.getInventory().addItem(createCoins(count));
        }
    }

    public static int get(Player player) {
        return ItemSearch.find(player.getInventory(), Money::isCoin).stream().map(ItemStack::getAmount).reduce(0, Integer::sum);
    }
}
