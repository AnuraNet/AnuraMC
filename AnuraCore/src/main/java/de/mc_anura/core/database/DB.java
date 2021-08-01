package de.mc_anura.core.database;

import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DB {

    private static final String DEFAULT_DB = "anura_freebuild";
    
    private static final HashMap<String, MySQL> dbs = new HashMap<>();
    
    public static MySQL get() {
        return get(DEFAULT_DB);
    }

    public static MySQL get(String dbName) {
        synchronized (dbs) {
            if (dbs.containsKey(dbName)) {
                return dbs.get(dbName);
            } else {
                MySQL m = new MySQL(dbName);
                dbs.put(dbName, m);
                return m;
            }
        }
    }
    
    public static void queryUpdate(boolean async, String query, Object... args) {
        get().queryUpdate(async, query, args);
    }

    public static void queryUpdate(String query, Object... args) {
        get().queryUpdate(query, args);
    }

    public static void queryUpdate(boolean async, Consumer<ResultSet> generatedKeys, String query, Object... args) {
        get().queryUpdate(async, generatedKeys, query, args);
    }

    public static void queryUpdate(Consumer<ResultSet> generatedKeys, String query, Object... args) {
        get().queryUpdate(generatedKeys, query, args);
    }

    @Nullable
    public static MySQL.PreparedUpdate queryPrepUpdate(String query) {
        return get().queryPrepUpdate(query);
    }

    @Nullable
    public static MySQL.PreparedUpdate queryPrepUpdate(Consumer<ResultSet> generatedKeys, String query) {
        return get().queryPrepUpdate(generatedKeys, query);
    }

    public static ResultSet querySelect(String query, Object... args) {
        return get().querySelect(query, args);
    }

    public static void closeResources(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
            }
        }
    }

    public static Consumer<ResultSet> getFirstKey(Consumer<Integer> callback) {
        return keys -> {
            try {
                if (!keys.next()) {
                    callback.accept(null);
                    return;
                }
                callback.accept(keys.getInt(1));
            } catch (SQLException e) {
                callback.accept(null);
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
            }
        };
    }

    public static void stop() {
        synchronized (dbs) {
            for (MySQL sql : dbs.values()) {
                sql.stop();
            }
        }
    }
}
