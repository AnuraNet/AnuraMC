package de.mc_anura.core.database;

import de.mc_anura.core.AnuraThread;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQL {

    private static final int QUEUE_LIMIT = 5000;

    private static final Logger log = Logger.getLogger(MySQL.class.getName());
    private static long lastPrint = 0;
    private static int queryCounter = 0;
    private static final Map<String, Integer> queryCount = new ConcurrentHashMap<>();

    private Connection conn;
    private final FileConfiguration config;
    private volatile boolean reconnecting = false;
    private final String db;
    private ThreadPoolExecutor executor;

    MySQL(String dbName) {
        db = dbName;
        File file = new File("plugins/database/", "db_" + dbName + ".yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.addDefault("host", "localhost");
        cfg.addDefault("port", 3306);
        cfg.addDefault("username", "username");
        cfg.addDefault("pw", "pw");
        cfg.options().copyDefaults(true);
        try {
            cfg.save(file);
        } catch (IOException e) {
        }
        this.config = cfg;
        lastPrint = System.currentTimeMillis();
        initializeThreadPool();
        if (!this.openConnection()) {
            Bukkit.broadcast(Component.text("Keine Datenbankverbindung!", NamedTextColor.RED));
            connectFailed();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
    }

    private boolean openConnection() {
        try {
            String host = config.getString("host");
            int port = config.getInt("port");
            String username = config.getString("username");
            String pw = config.getString("pw");
            String dataB = config.getString("db", db);
            this.conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dataB + "?serverTimezone=Europe/Berlin", username, pw);
            return true;
        } catch (SQLException e) {
            log.log(Level.WARNING, e.getMessage());
            return false;
        }
    }

    public boolean hasConnection() {
        try {
            return this.conn != null && this.conn.isValid(1) && !reconnecting;
        } catch (SQLException e) {
            return false;
        }
    }

    public void queryUpdate(boolean async, String query, Object... args) {
        queryUpdate(async, null, query, args);
    }

    public void queryUpdate(String query, Object... args) {
        queryUpdate(false, null, query, args);
    }

    public void queryUpdate(boolean async, Consumer<ResultSet> generatedKeys, String query, Object... args) {
        if (async) executor.execute(() -> queryUpdate(generatedKeys, query, args));
        else queryUpdate(generatedKeys, query, args);
    }

    public void queryUpdate(Consumer<ResultSet> generatedKeys, String query, Object... args) {
        queryCount.put(query, queryCount.getOrDefault(query, 0) + 1);
        printThreadLog();
        if (!hasConnection()) {
            if (!tryReconnect()) {
                connectFailed();
                return;
            }
        }
        Connection connLoc = conn;
        PreparedStatement st = null;
        try {
            st = connLoc.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (Object o : args) {
                st.setObject(i, o);
                i++;
            }
            st.executeUpdate();
            if (generatedKeys != null) {
                generatedKeys.accept(st.getGeneratedKeys());
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to send update: {0} - {1}", new Object[] { query, e.getLocalizedMessage() });
        }
        closeRessources(st);
    }

    @Nullable
    public PreparedUpdate queryPrepUpdate(String query) {
        return queryPrepUpdate(null, query);
    }

    @Nullable
    public PreparedUpdate queryPrepUpdate(Consumer<ResultSet> generatedKeys, String query) {
        if (!hasConnection()) {
            if (!tryReconnect()) {
                connectFailed();
                return null;
            }
        }
        PreparedStatement st;
        try {
            st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            return new PreparedUpdate(st, generatedKeys);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to send update: " + query, e);
        }
        return null;
    }

    public ResultSet querySelect(String query, Object... args) {
        if (!hasConnection()) {
            if (!tryReconnect()) {
                connectFailed();
                return null;
            }
        }
        try {
            PreparedStatement st = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int i = 1;
            for (Object o : args) {
                st.setObject(i, o);
                i++;
            }
            return querySelect(st);
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Error trying to build Prepared Statement", ex);
        }
        return null;
    }

    private ResultSet querySelect(PreparedStatement st) {
        if (!hasConnection()) {
            if (!tryReconnect()) {
                connectFailed();
                return null;
            }
        }
        ResultSet rs;
        try {
            rs = st.executeQuery();
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to send SELECT query: " + st.toString(), e);
            return null;
        }
        return rs;
    }

    @SuppressWarnings("SleepWhileInLoop")
    private boolean tryReconnect() {
        int count = 0;
        while (!reconnect()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {}
            count++;
            if (count == 100) return false;
        }

        return true;
    }

    private boolean reconnect() {
        if (reconnecting) {
            return false;
        }
        if (hasConnection()) {
            return true;
        }
        reconnecting = true;
        log.log(Level.INFO, "Reconnecting...");
        closeConnection();
        if (!openConnection()) {
            reconnecting = false;
            Bukkit.broadcast(Component.text("Keine Datenbankverbindung!", NamedTextColor.RED));
            return false;
        } else {
            reconnecting = false;
            log.log(Level.INFO, "Database reconnect successful!");
            return true;
        }
    }

    private void connectFailed() {
        log.log(Level.WARNING, "Gave up database reconnect. Kicking all players...");
        AnuraThread.sync(() -> {
            for (Player P : Bukkit.getOnlinePlayers()) {
                P.kickPlayer(ChatColor.RED + "Datenbankverbindung fehlgeschlagen. Versuche es doch in ein paar Minuten erneut!");
            }
        });
    }

    private void closeRessources(PreparedStatement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {}
        }
    }

    public void closeConnection() {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (SQLException e) {
        } finally {
            this.conn = null;
        }
    }

    private void initializeThreadPool() {
        executor = new ThreadPoolExecutor(6, 6, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(QUEUE_LIMIT));
    }

    private void printThreadLog() {
        long now = System.currentTimeMillis();
        if (now - lastPrint > 60 * 1000) {
            lastPrint = now;
            synchronized (queryCount) {
                for (Entry<String, Integer> queries : queryCount.entrySet()) {
                    queryCounter += queries.getValue();
                    if (queries.getValue() < 30) continue;
                    Logger.getLogger(MySQL.class.getName()).log(queries.getValue() > 100 ? Level.WARNING : Level.INFO, "{0}: {1}", new Object[] {queries.getKey(), queries.getValue()});
                }
            }
            queryCount.clear();
            Logger.getLogger(MySQL.class.getName()).log(Level.INFO, getThreadInfo());
        }
    }

    void stop() {
        executor.shutdown();
    }

    private String getThreadInfo() {
        return "SQL Threads: Active: " + executor.getActiveCount() + " (Queue: " + executor.getQueue().size() + ") - Pool size: " + executor.getPoolSize() + " (Max: " + executor.getLargestPoolSize()+ ") - Completed: " + executor.getCompletedTaskCount() + " (Total: " + queryCounter + ")";
    }

    public class PreparedUpdate {

        private final PreparedStatement stmt;
        private final Consumer<ResultSet> keys;

        public PreparedUpdate(PreparedStatement statement, Consumer<ResultSet> generatedKeys) {
            stmt = statement;
            keys = generatedKeys;
        }

        public PreparedUpdate add(Object... args) {
            try {
                int i = 1;
                for (Object o : args) {
                    stmt.setObject(i, o);
                    i++;
                }
                stmt.executeUpdate();
            } catch (SQLException ex) {
                log.log(Level.SEVERE, null, ex);
            }
            return this;
        }

        public void done() {
            try {
                if (keys != null) {
                    keys.accept(stmt.getGeneratedKeys());
                }
            } catch (SQLException ex) {
                log.log(Level.SEVERE, null, ex);
            }
            closeRessources(stmt);
        }
    }
}
