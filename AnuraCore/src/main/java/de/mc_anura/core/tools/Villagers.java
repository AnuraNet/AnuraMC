package de.mc_anura.core.tools;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.AnuraThread;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import java.util.HashMap;
import java.util.function.Consumer;

public abstract class Villagers {

    private static Team t;
    private static final HashMap<String, Consumer<Player>> clicks = new HashMap<>();

    public static void init() {
        AnuraThread.add(Bukkit.getScheduler().runTaskLater(AnuraCore.getInstance(), () -> { // The scoreboard seems to be initialized later
            ScoreboardManager boardMan = Bukkit.getScoreboardManager();
            if (boardMan == null)
                return;

            t = boardMan.getMainScoreboard().getTeam("noMoveEntity");
            if (t == null) {
                t = boardMan.getMainScoreboard().registerNewTeam("noMoveEntity");
                t.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
            }
        }, 1));
    }

    public static void spawn(Location loc, String id, String name, Profession profession) {
        if (loc == null || loc.getWorld() == null)
            return;

        Villager v = loc.getWorld().spawn(loc, Villager.class);
        v.teleport(loc);
        v.setAI(false);
        v.setProfession(profession);
        v.setCanPickupItems(false);
        v.setCustomName(name);
        v.setCustomNameVisible(true);
        v.setSilent(true);
        v.setGravity(false);
        v.addScoreboardTag(id);
        v.addScoreboardTag("anuraVillager");
        t.addEntry(v.getUniqueId().toString());
    }

    public static void spawn(Location loc, String id, String name) {
        spawn(loc, id, name, Profession.LIBRARIAN);
    }

    public static void removeIfMatch(Villager v) {
        if (is(v)) {
            t.removeEntry(v.getUniqueId().toString());
            v.remove();
        }
    }

    public static void onClick(String id, Consumer<Player> cb) {
        clicks.put(id, cb);
    }

    public static boolean doClick(Villager v, Player P) {
        if (!t.hasEntry(v.getUniqueId().toString())) return false;
        boolean done = false;
        for (String s : v.getScoreboardTags()) {
            if (clicks.containsKey(s)) {
                clicks.get(s).accept(P);
                done = true;
            }
        }
        return done;
    }

    public static boolean is(Villager v) {
        return v.getScoreboardTags().contains("anuraVillager");
    }
}
