package de.mc_anura.realisticminecraft.infobar;

import de.mc_anura.core.database.DB;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValueHolder {

    private static final List<Class<? extends RealisticPlayer>> PLAYERS = Arrays.asList(TemperaturePlayer.class, ThirstPlayer.class);
    private static final Map<Player, ValueHolder> ALL = new ConcurrentHashMap<>();
    private final Map<Class<? extends RealisticPlayer>, RealisticPlayer> values = new ConcurrentHashMap<>();
    private final Player player;

    public ValueHolder(@NotNull Player p) {
        ALL.put(p, this);
        player = p;
        PLAYERS.forEach(this::createPlayer);
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T extends RealisticPlayer> T getPlayer(@NotNull Class<T> clazz) {
        return (T) values.get(clazz);
    }

    public @NotNull Collection<RealisticPlayer> getPlayersList() {
        return values.values();
    }

    private <T extends RealisticPlayer> void createPlayer(@NotNull Class<T> clazz) {
        ResultSet rs = null;
        try {
            rs = DB.querySelect("SELECT value, bar FROM " + clazz.getDeclaredMethod("getStaticTableName").invoke(null) + " WHERE playerId = (SELECT id FROM players WHERE uuid = ?)", player.getUniqueId().toString());
            if (rs.next()) {
                T rp = clazz.getConstructor(Player.class, float.class, BarStatus.class).newInstance(player, rs.getFloat("value"), BarStatus.values()[rs.getInt("bar")]);
                values.put(clazz, rp);
            } else {
                values.put(clazz, clazz.getConstructor(Player.class).newInstance(player));
            }
        } catch (SQLException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            Logger.getLogger(ValueHolder.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DB.closeResources(rs);
        }
    }

    private <T extends RealisticPlayer> void resetPlayer(@NotNull Class<T> clazz) {
        values.get(clazz).setValue(values.get(clazz).getDEFAULT());
    }

    public void resetHoldings() {
        PLAYERS.forEach(this::resetPlayer);
    }

    public static @Nullable ValueHolder getValueHolder(@NotNull Player p) {
        return ALL.get(p);
    }

    public static void removeValueHolder(@NotNull Player p) {
        removeAll(ALL.remove(p));
    }

    public static void destroyAll() {
        ALL.values().forEach(ValueHolder::removeAll);
        ALL.clear();
    }

    private static void removeAll(@Nullable ValueHolder vh) {
        if (vh != null) {
            vh.getPlayersList().forEach((rp) -> {
                rp.updateDatabase(false);
                InfoBar<? extends RealisticPlayer> bar = rp.getBar();
                if (bar != null) {
                    bar.destroy();
                }
                rp.changeValue(rp.getDEFAULT());
            });
        }
    }
}
