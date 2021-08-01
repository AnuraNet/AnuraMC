package de.mc_anura.core.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.database.DB;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UUIDManager {

    private static final BiMap<String, UUID> cache = HashBiMap.create();
    private static final Set<UUID> inDB = new HashSet<>();

    public static void getName(UUID uuid, Consumer<String> cb) {
        getName(uuid, false, cb);
    }

    public static void getName(UUID uuid, boolean onlyDatabase, Consumer<String> cb) {
        if (uuid == null) {
            cb.accept(null);
            return;
        }
        boolean sync = Bukkit.isPrimaryThread();
        if (Bukkit.getPlayer(uuid) != null) {
            cb.accept(Bukkit.getPlayer(uuid).getName());
        } else if (cache.inverse().containsKey(uuid)) {
            if (!onlyDatabase || inDB.contains(uuid)) {
                cb.accept(cache.inverse().get(uuid));
            } else {
                cb.accept(null);
            }
        } else {
            Runnable requestUser = () -> {
                ResultSet rs = DB.querySelect("SELECT name FROM players WHERE uuid = ?", uuid.toString());
                try {
                    if (rs != null && rs.next()) {
                        String name = rs.getString("name");
                        cache.put(name, uuid);
                        inDB.add(uuid);
                        if (sync) {
                            AnuraThread.syncCB(cb, name);
                        } else {
                            cb.accept(name);
                        }
                        return;
                    }
                } catch (SQLException e) {
                    Logger.getLogger(UUIDManager.class.getName()).log(Level.SEVERE, null, e);
                }
                if (onlyDatabase) {
                    if (sync) {
                        AnuraThread.syncCB(cb, null);
                    } else {
                        cb.accept(null);
                    }
                    return;
                }

                OfflinePlayer P = Bukkit.getOfflinePlayer(uuid);
                cache.put(P.getName(), uuid);
                if (sync) {
                    AnuraThread.syncCB(cb, P.getName());
                } else {
                    cb.accept(P.getName());
                }
            };
            if (sync) {
                AnuraThread.async(requestUser);
            } else {
                requestUser.run();
            }
        }
    }

    public static void getUUID(String name, Consumer<UUID> cb) {
        getUUID(name, false, cb);
    }

    public static void getUUID(String name, boolean onlyDatabase, Consumer<UUID> cb) {
        if (name == null) {
            cb.accept(null);
            return;
        }
        boolean sync = Bukkit.isPrimaryThread();
        if (cache.containsKey(name)) {
            if (!onlyDatabase || inDB.contains(cache.get(name))) {
                cb.accept(cache.get(name));
            } else {
                cb.accept(null);
            }
        } else if (Bukkit.getPlayerExact(name) == null) {
            Runnable requestUser = () -> {
                ResultSet rs = DB.querySelect("SELECT uuid FROM players WHERE name = ?", name);
                try {
                    if (rs != null && rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        cache.inverse().remove(uuid);
                        cache.put(name, uuid);
                        inDB.add(uuid);
                        if (sync) {
                            AnuraThread.syncCB(cb, uuid);
                        } else {
                            cb.accept(uuid);
                        }
                        return;
                    }
                } catch (SQLException ex) {
                }
                if (onlyDatabase) {
                    if (sync) {
                        AnuraThread.syncCB(cb, null);
                    } else {
                        cb.accept(null);
                    }
                } else {
                    requestUUID(name, cb, sync);
                }
            };
            if (sync) {
                AnuraThread.async(requestUser);
            } else {
                requestUser.run();
            }
        } else {
            UUID uuid = Bukkit.getPlayerExact(name).getUniqueId();
            cache.inverse().remove(uuid);
            cache.put(name, uuid);
            inDB.add(uuid);
            cb.accept(uuid);
        }
    }

    @SuppressWarnings("unchecked")
    private static void requestUUID(String name, Consumer<UUID> cb, boolean wasSync) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            String answer;
            InputStream is = connection.getInputStream();
            if (is != null) {
                Writer writer = new StringWriter();
                char[] buffer = new char[1024];
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    int n;
                    while ((n = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, n);
                    }
                } finally {
                    is.close();
                }
                answer = writer.toString();
            } else {
                answer = "";
            }
            JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(answer);
            connection.disconnect();
            String id = (String) jsonObject.get("id");
            UUID uuid = UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
            cache.put(name, uuid);
            if (wasSync) {
                AnuraThread.syncCB(cb, uuid);
            } else {
                cb.accept(uuid);
            }
        } catch (IOException | ParseException ex) {
            if (wasSync) {
                AnuraThread.syncCB(cb, null);
            } else {
                cb.accept(null);
            }
        }
    }
}
