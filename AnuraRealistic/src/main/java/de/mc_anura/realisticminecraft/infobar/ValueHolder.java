package de.mc_anura.realisticminecraft.infobar;

import de.mc_anura.core.database.DB;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValueHolder {

    private static final List<Class<? extends RealisticPlayer>> PLAYERS = Arrays.asList(TemperaturePlayer.class, ThirstPlayer.class);
    private static final Map<Player, ValueHolder> ALL = new ConcurrentHashMap<>();
    private final Map<Class<? extends RealisticPlayer>, RealisticPlayer> values = new ConcurrentHashMap<>();
    private final Player player;

    public ValueHolder(Player p) {
        Objects.requireNonNull(p);
        ALL.put(p, this);
        player = p;
        PLAYERS.forEach(this::createPlayer);
    }

    public Player getPlayer() {
        return player;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends RealisticPlayer> T getPlayer(Class<T> clazz) {
        return (T) values.get(clazz);
    }

    public Collection<RealisticPlayer> getPlayersList() {
        return values.values();
    }

    private <T extends RealisticPlayer> void createPlayer(Class<T> clazz) {
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

    private <T extends RealisticPlayer> void resetPlayer(Class<T> clazz) {
        values.get(clazz).setValue(values.get(clazz).getDEFAULT());
    }

    public void resetHoldings() {
        PLAYERS.forEach(this::resetPlayer);
    }

    @Nullable
    public static ValueHolder getValueHolder(Player p) {
        return ALL.get(p);
    }

    public static void removeValueHolder(Player p) {
        remove(ALL.remove(p));
    }
    
    public static void destroyAll() {
        ALL.values().forEach(ValueHolder::remove);
        ALL.clear();
    }
    
    private static void remove(ValueHolder vh) {
        if (vh != null) {
            vh.getPlayersList().forEach((rp) -> {
                rp.updateDatabase(false);
                rp.getBar().destroy();
                rp.changeValue(rp.getDEFAULT());
            });
        }
    }
}
