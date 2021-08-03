package de.mc_anura.core;

import de.mc_anura.core.database.DB;
import de.mc_anura.core.database.MySQL.PreparedUpdate;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Money {
    
    public static final String CURRENCY = "Hundewelpen";
    public static final int INITIAL_MONEY = 20;

    private static final Map<UUID, Integer> playerMoney = new ConcurrentHashMap<>();
    
    private static final Set<UUID> toSave = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    public static void init() {
        AnuraThread.add(Bukkit.getScheduler().runTaskTimerAsynchronously(AnuraCore.getInstance(), Money::save, 20 * 60, 20 * 60));
    }

    public static void payMoney(UUID uuid, int count) {
        // TODO: Thread safety!
        Runnable exec = () -> {
            int money = playerMoney.get(uuid) + count;
            if (money < 0) money = 0;
            playerMoney.put(uuid, money);
            toSave.add(uuid);
        };
        
        if (playerMoney.containsKey(uuid)) {
            exec.run();
        } else {
            AnuraThread.async(() -> {
                if (loadMoney(uuid)) {
                    exec.run();
                }
            });
        }
    }

    public static void save() {
        if (toSave.isEmpty())
            return;
        
        PreparedUpdate prep = DB.queryPrepUpdate("UPDATE players SET money = ? WHERE uuid = ?");

        if (prep == null) {
            return;
        }
        
        for (UUID uuid : toSave) {
            if (playerMoney.containsKey(uuid)) {
                prep.add(playerMoney.remove(uuid), uuid.toString());
            }
        }
        prep.done();
        
        toSave.clear();
    }

    public static boolean loadMoney(UUID uuid) {
        try {
            ResultSet rs = DB.querySelect("SELECT money FROM players WHERE uuid = ?", uuid.toString());
            rs.next();
            playerMoney.put(uuid, rs.getInt("money"));
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Money.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static void getMoney(UUID uuid, Consumer<Integer> moneyCB) {
        if (playerMoney.containsKey(uuid)) {
            moneyCB.accept(playerMoney.get(uuid));
        } else {
            AnuraThread.async(() -> {
                if (!loadMoney(uuid)) {
                    moneyCB.accept(-1);
                }
                moneyCB.accept(playerMoney.get(uuid));
            });
        }
    }
}
