package de.mc_anura.core.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Head {

    private static final Method profile;

    static {
        Method prof = null;
        try {
            prof = Head.class.getClassLoader().loadClass("org.bukkit.craftbukkit.v1_17_R1.inventory.CraftMetaSkull").getDeclaredMethod("setProfile", GameProfile.class);
            prof.setAccessible(true);
        } catch (SecurityException | ClassNotFoundException | NoSuchMethodException ex) {
            Logger.getLogger(Head.class.getName()).log(Level.SEVERE, "Failed reflection in Head util", ex);
        }
        profile = prof;
    }

    public static ItemStack get(String name) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) is.getItemMeta();
        if (meta == null)
            return is;

        meta.setOwningPlayer(Bukkit.getOfflinePlayer(name));
        is.setItemMeta(meta);
        return is;
    }

    public static ItemStack getWithTexture(String textureString) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        addTexture(stack, textureString);
        return stack;
    }

    private static void addTexture(ItemStack stack, String textureString) {
        try {
            if (stack == null || !stack.getType().equals(Material.PLAYER_HEAD)) return;

            ItemMeta meta = stack.getItemMeta();
            GameProfile gprofile = new GameProfile(UUID.randomUUID(), null);
            gprofile.getProperties().put("textures", new Property("textures", textureString));
            profile.invoke(meta, gprofile);
            stack.setItemMeta(meta);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(Head.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
