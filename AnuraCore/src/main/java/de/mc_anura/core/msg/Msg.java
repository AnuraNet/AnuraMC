package de.mc_anura.core.msg;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.Money;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public abstract class Msg {

    private static final HashMap<Plugin, PluginData> plugins = new HashMap<>();

    public static void register(Plugin plugin, String name, PluginType type) {
        plugins.put(plugin, new PluginData(name, type));
    }

    public static void noPerms(CommandSender sender) {
        send(sender, AnuraCore.getInstance(), MsgType.ERROR, "Du hast keine Berechtigung diesen Befehl auszuführen!");
    }

    public static void send(Plugin plugin, MsgType type, String msg, Object... data) {
        if (!plugins.containsKey(plugin)) {
            throw new RuntimeException("Plugin has to be registered (Msg::register)!");
        }
        send(plugins.get(plugin), type, msg, data);
    }

    public static void send(PluginData pData, MsgType type, String msg, Object... data) {
        Component message = getMsg(pData, type, false, msg, data);
        Bukkit.broadcast(message);
    }

    public static void send(CommandSender sender, Plugin plugin, MsgType type, String msg, Object... data) {
        if (!plugins.containsKey(plugin)) {
            throw new RuntimeException("Plugin has to be registered (Msg::register)!");
        }
        send(sender, plugins.get(plugin), type, msg, data);
    }

    public static void send(CommandSender sender, PluginData pData, MsgType type, String msg, Object... data) {
        sender.sendMessage(getMsg(pData, type, false, msg, data));
    }

    public static void send(UUID uuid, Plugin plugin, MsgType type, String msg, Object... data) {
        if (!plugins.containsKey(plugin)) {
            throw new RuntimeException("Plugin has to be registered (Msg::register)!");
        }
        send(uuid, plugins.get(plugin), type, msg, data);
    }

    public static void send(UUID uuid, PluginData pData, MsgType type, String msg, Object... data) {
        send(uuid, getPrefix(pData, true), getMsg(pData, type, true, msg, data));
    }

    private static void send(UUID uuid, Component prefix, Component msg) {
        Messenger.send(prefix, uuid, msg);
    }

    public static Component getMsg(Plugin plugin, MsgType type, boolean noPrefix, String msg, Object... data) {
        if (!plugins.containsKey(plugin)) {
            throw new RuntimeException("Plugin has to be registered (Msg::register)!");
        }
        return getMsg(plugins.get(plugin), type, noPrefix, msg, data);
    }

    public static Component getMsg(PluginData pData, MsgType type, boolean noPrefix, String msg, Object... data) {
        TextColor baseColor = type.getColor(pData.getType());
        Component result = Component.empty();
        if (!noPrefix) {
            result = result.append(getPrefix(pData, false));
        }
        return result.append(buildValues(baseColor, msg, data));
    }

    private static Component buildValues(TextColor baseColor, String msg, Object... data) {
        if (data.length == 0) return Component.text(msg);
        Component finalMsg = Component.empty();
        StringBuilder normalText = new StringBuilder();
        int dataAt = 0;
        boolean hadChar = false;
        boolean hadAll = false;
        for (char c : msg.toCharArray()) {
            if (!hadAll && hadChar) {
                hadChar = false;
                Component append;
                switch (c) {
                    case 's' -> append = Component.text(String.valueOf(data[dataAt])).color(NamedTextColor.BLUE);
                    case 'i' -> append = Component.text(String.valueOf(data[dataAt])).color(NamedTextColor.GOLD);
                    case 'm' -> append = Component.text(data[dataAt] + " " + Money.CURRENCY).color(NamedTextColor.GOLD);
                    default -> {
                        normalText.append("%").append(c);
                        continue;
                    }
                }
                finalMsg = finalMsg.append(Component.text(normalText.toString()).color(baseColor)).append(append);
                normalText = new StringBuilder();

                dataAt++;
                if (data.length == dataAt) hadAll = true;
            } else if (!hadAll && c == '%') {
                hadChar = true;
            } else {
                normalText.append(c);
            }
        }
        return finalMsg.append(Component.text(normalText.toString()));
    }

    public static Component getPrefix(Plugin plugin, boolean shortPrefix) {
        if (!plugins.containsKey(plugin)) {
            throw new RuntimeException("Plugin has to be registered (Msg::register)!");
        }
        return getPrefix(plugins.get(plugin), shortPrefix);
    }

    public static Component getPrefix(PluginData data, boolean shortPrefix) {
        Component shortC = Component.text(data.getName()).color(data.getType().getColor());
        if (shortPrefix) {
            return shortC;
        } else {
            if (data.getType() != PluginType.GAMEPLAY) {
                return Component.text("[", NamedTextColor.GRAY).append(shortC).append(Component.text("]").color(NamedTextColor.GRAY));
            } else {
                return shortC.append(Component.text("> ").color(NamedTextColor.GRAY));
            }
        }
    }

    public enum PluginType {
        SYSTEM(NamedTextColor.RED),
        ADMIN(NamedTextColor.DARK_PURPLE),
        GAMEPLAY(NamedTextColor.DARK_AQUA);

        PluginType(NamedTextColor color) {
            this.color = color;
        }

        private final NamedTextColor color;

        public NamedTextColor getColor() {
            return color;
        }
    }

    public enum MsgType {
        SUCCESS(NamedTextColor.GREEN, NamedTextColor.DARK_GREEN),
        ERROR(NamedTextColor.RED, NamedTextColor.RED),
        INFO(NamedTextColor.YELLOW, NamedTextColor.AQUA),
        SPECIAL(NamedTextColor.LIGHT_PURPLE, NamedTextColor.LIGHT_PURPLE),
        CRITICAL(NamedTextColor.DARK_RED, NamedTextColor.DARK_RED);

        private final NamedTextColor systemColor, gameplayColor;

        MsgType(NamedTextColor systemColor, NamedTextColor gameplayColor) {
            this.gameplayColor = gameplayColor;
            this.systemColor = systemColor;
        }

        public TextColor getColor(PluginType type) {
            return type == PluginType.GAMEPLAY ? gameplayColor : systemColor;
        }
    }

    public record PluginData(String name, PluginType type) {

        public String getName() {
            return name;
        }

        public PluginType getType() {
            return type;
        }
    }
}
