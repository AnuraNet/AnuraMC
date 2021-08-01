package de.mc_anura.freebuild;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.Money;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.tools.Potions;
import de.mc_anura.freebuild.regions.Region;
import de.mc_anura.freebuild.regions.RegionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ClaimManager {
    
    private static final Map<Player, Claim> activeClaims = new ConcurrentHashMap<>();
    
    private static Scoreboard claimScb;

    public static void init() {
        claimScb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective o = claimScb.registerNewObjective("main", "dummy", Component.text("Legende", NamedTextColor.AQUA));
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.getScore(ChatColor.GREEN + "Dein Grundstück").setScore(3);
        o.getScore(ChatColor.RED + "Fremdes Grundstück").setScore(2);
        o.getScore(ChatColor.GRAY + "Aktuelle Auswahl").setScore(1);
        
        AnuraThread.add(Bukkit.getScheduler().runTaskTimerAsynchronously(AnuraFreebuild.getInstance(), () -> {
            for (Player P : activeClaims.keySet()) {
                drawAreas(P);
            }
        }, 20 * 5, 20 * 5));
        
        AnuraThread.add(Bukkit.getScheduler().runTaskTimerAsynchronously(AnuraFreebuild.getInstance(), () -> {
            for (Claim claim : activeClaims.values()) {
                Location[] loc = claim.corners;
                if (loc[0] == null || loc[1] == null) continue;
                Location l1 = loc[0];
                Location l2 = loc[1];
                int x1 = l1.getBlockX();
                int x2 = l2.getBlockX();
                int z1 = l1.getBlockZ();
                int z2 = l2.getBlockZ();
                int cost = (Math.max(x1, x2) - Math.min(x1, x2)) * (Math.max(z1, z2) - Math.min(z1, z2));
                Money.getMoney(claim.player.getUniqueId(), (money) -> {
                    Component msg = Component.text("Dein Geld: ", NamedTextColor.GREEN)
                            .append(Component.text(money, NamedTextColor.DARK_GREEN))
                            .append(Component.text(" | ", NamedTextColor.GRAY))
                            .append(Component.text("Kosten: ", NamedTextColor.YELLOW))
                            .append(Component.text(cost, (money >= cost ? NamedTextColor.GREEN : NamedTextColor.RED)));
                    claim.player.sendActionBar(msg);
                });
            }
        }, 20, 20));
    }
    
    public static boolean isClaiming(Player P) {
        return activeClaims.containsKey(P);
    }

    public static void shutdown() {
        activeClaims.values().forEach(ClaimManager::endClaimMode);
        activeClaims.clear();
    }
    
    public static void endClaimMode(Player P) {
        if (activeClaims.containsKey(P)) {
            endClaimMode(activeClaims.remove(P));
        }
    }
    
    public static void confirm(Player P) {
        if (!activeClaims.containsKey(P)) {
            return;
        }
        Claim claim = activeClaims.get(P);
        Location[] locs = claim.corners;
        if (locs[0] == null || locs[1] == null) {
            Msg.send(P, AnuraFreebuild.getInstance(), Msg.MsgType.ERROR, "Wähle erst eine Region aus! Rechts-/Linksklick");
            return;
        }
        AnuraThread.async(() -> {
            int x1 = locs[0].getBlockX();
            int x2 = locs[1].getBlockX();
            int z1 = locs[0].getBlockZ();
            int z2 = locs[1].getBlockZ();
            boolean isFree = true;
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x += 10) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z += 10) {
                    if (!RegionManager.isFree(new Location(locs[0].getWorld(), x, 50, z))) {
                        isFree = false;
                        break;
                    }
                }
                if (!isFree) {
                    break;
                }
            }
            if (!isFree) {
                Msg.send(P, AnuraFreebuild.getInstance(), Msg.MsgType.ERROR, "Ein Teil der Region gehört bereits jemandem!");
                return;
            }
            int cost = (Math.max(x1, x2) - Math.min(x1, x2)) * (Math.max(z1, z2) - Math.min(z1, z2));
            Money.getMoney(P.getUniqueId(), (money) -> {
                if (money < cost) {
                    Msg.send(P, AnuraFreebuild.getInstance(), Msg.MsgType.ERROR, "Du hast nicht genug Geld! " + ChatColor.GRAY + "("
                                  + ChatColor.YELLOW + "Dein Geld: " + money + ChatColor.GRAY + " | " 
                                  + ChatColor.RED + "Kosten: " + cost + ChatColor.GRAY + ")");
                    return;
                }
                Money.payMoney(P.getUniqueId(), -cost);
                Region r = new Region(P.getUniqueId(), locs[0], locs[1]);
                r.save();
                Msg.send(P, AnuraFreebuild.getInstance(), Msg.MsgType.SUCCESS, "Gebiet geclaimt!");
                AnuraThread.sync(() -> {
                    World w = locs[0].getWorld();
                    Location[] l = new Location[4];
                    l[0] = new Location(w, Math.min(x1, x2), 0, Math.min(z1, z2));
                    l[1] = new Location(w, Math.min(x1, x2), 0, Math.max(z1, z2));
                    l[2] = new Location(w, Math.max(x1, x2), 0, Math.min(z1, z2));
                    l[3] = new Location(w, Math.max(x1, x2), 0, Math.max(z1, z2));
                    for (Location loc : l) {
                        loc.setY(w.getHighestBlockYAt(loc));
                        loc.subtract(0, 1, 0).getBlock().setType(Material.STONE);
                        loc.add(0, 1, 0).getBlock().setType(Material.TORCH);
                    }
                    endClaimMode(P);
                    P.teleport(l[0]);
                });
            });
        });
    }
    
    private static void endClaimMode(Claim c) {
        Player P = c.player;
        Msg.send(P, AnuraFreebuild.getInstance(), Msg.MsgType.SUCCESS, "Claimmode deaktiviert!");
        
        Location[] locs = c.corners;
        if (locs[0] != null && locs[1] != null) drawBoundingBox(P, locs[0], locs[1], null);
        if (locs[0] != null) setCross(P, locs[0], null);
        if (locs[1] != null) setCross(P, locs[1], null);
        clearAreas(P);
        P.teleport(c.oldLoc);
        P.setFlying(false);
        if (P.getGameMode() == GameMode.SURVIVAL || P.getGameMode() == GameMode.ADVENTURE) {
            P.setAllowFlight(false);
        }
        P.setFallDistance(0);
        P.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        P.getInventory().setItemInMainHand(c.itemInHand);
    }

    public static void drawBoundingBox(Player P, Location loc1, Location loc2, BlockData data) {
        if (loc1.getWorld() != loc2.getWorld()) {
            return;
        }
        HashSet<Location> locs = new HashSet<>();
        int x1 = loc1.getBlockX();
        int x2 = loc2.getBlockX();
        int z1 = loc1.getBlockZ();
        int z2 = loc2.getBlockZ();
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            locs.add(new Location(loc1.getWorld(), x, 0, Math.min(z1, z2)));
            locs.add(new Location(loc1.getWorld(), x, 0, Math.max(z1, z2)));
        }
        for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
            locs.add(new Location(loc1.getWorld(), Math.min(x1, x2), 0, z));
            locs.add(new Location(loc1.getWorld(), Math.max(x1, x2), 0, z));
        }
        for (Location l : locs) {
            l.setY(l.getWorld().getHighestBlockYAt(l) - 1);
            P.sendBlockChange(l, data == null ? l.getBlock().getBlockData() : data);
        }
    }
    
    public static void setCross(Player P, Location l, BlockData data) {
        l = l.clone();
        l.add(0, 1, 0);
        P.sendBlockChange(l, data == null ? l.getBlock().getBlockData() : data);
        for (BlockFace f : Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST)) {
            Location newLoc = l.getBlock().getRelative(f).getLocation();
            P.sendBlockChange(newLoc, data == null ? newLoc.getBlock().getBlockData() : data);
        }
    }

    private static void drawAreas(Player P) {
        if (!activeClaims.containsKey(P)) return;
        
        clearAreas(P);
        for (Region r : RegionManager.getRegions()) {
            if (r.isInRadius(P.getLocation(), 50)) {
                Location min = r.getMin();
                Location max = r.getMax();
                activeClaims.get(P).drawnLocs.add(new Location[] {min, max});
                drawBoundingBox(P, min, max, P.getUniqueId().equals(r.getOwner()) ? Material.GREEN_WOOL.createBlockData() : Material.RED_WOOL.createBlockData());
            }
        }
    }

    private static void clearAreas(Player P) {
        if (!activeClaims.containsKey(P)) return;
        List<Location[]> drawnLocs = activeClaims.get(P).drawnLocs;
        for (Location[] loc : drawnLocs) {
            drawBoundingBox(P, loc[0], loc[1], null);
        }
        drawnLocs.clear();
    }

    public static void startClaiming(Player P) {
        Claim claim = new Claim(P);

        Msg.send(P, AnuraFreebuild.getInstance(), Msg.MsgType.INFO, "Wähle nun die zu claimende Region!");
        claim.oldLoc = P.getLocation();
        claim.itemInHand = P.getInventory().getItemInMainHand();

        P.setAllowFlight(true);
        P.setFlying(true);
        Potions.addNativePotion(P, new PotionEffect(PotionEffectType.LEVITATION, 4, 100));

        P.setScoreboard(claimScb);
        ItemStack s = new ItemStack(Material.STICK);
        ItemMeta m = s.getItemMeta();
        m.displayName(Component.text("Claim-Tool", NamedTextColor.GOLD));
        s.setItemMeta(m);
        P.getInventory().setItemInMainHand(s);
        
        activeClaims.put(P, claim);
    }

    public static void interact(Player P, Action action) {
        if (!activeClaims.containsKey(P))
            return;

        Location[] locs = activeClaims.get(P).corners;
        Location l = getLookingAt(P);
        Location old1 = null;
        Location old2 = null;
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (locs[0] != null) {
                setCross(P, locs[0], null);
                old1 = locs[0];
            }
            locs[0] = l;
            setCross(P, l, Material.END_STONE.createBlockData());
            if (locs[1] != null) {
                setCross(P, locs[1], Material.END_STONE.createBlockData());
            }
        } else {
            if (locs[1] != null) {
                setCross(P, locs[1], null);
                old2 = locs[1];
            }
            locs[1] = l;
            setCross(P, l, Material.END_STONE.createBlockData());
            if (locs[0] != null) {
                setCross(P, locs[0], Material.END_STONE.createBlockData());
            }
        }
        P.playSound(P.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        if (locs[0] != null && locs[1] != null) {
            if (old1 != null) {
                drawBoundingBox(P, old1, locs[1], null);
            } else if (old2 != null) {
                drawBoundingBox(P, old2, locs[0], null);
            }
            int x1 = locs[0].getBlockX();
            int x2 = locs[1].getBlockX();
            int z1 = locs[0].getBlockZ();
            int z2 = locs[1].getBlockZ();
            if (Math.max(x1, x2) - Math.min(x1, x2) < 10 || Math.max(z1, z2) - Math.min(z1, z2) < 10) {
                Msg.send(P, AnuraFreebuild.getInstance(), Msg.MsgType.ERROR, "Der gewählte Bereich ist zu klein!");
                return;
            }
            Msg.send(P, AnuraFreebuild.getInstance(), Msg.MsgType.SUCCESS, "Schreibe " + ChatColor.YELLOW + "/claim confirm " + ChatColor.GREEN + "um dein Gebiet zu kaufen.");
            drawBoundingBox(P, locs[0], locs[1], Material.YELLOW_WOOL.createBlockData());

        }
    }
    
    private static Location getLookingAt(Player P) {
        return P.getTargetBlock(null, 100).getLocation();
    }
    
    private static class Claim {
        
        private final Location[] corners;
        private final List<Location[]> drawnLocs;
        private final Player player;
        private Location oldLoc;
        private ItemStack itemInHand;
        
        public Claim(Player P) {
            player = P;
            corners = new Location[2];
            drawnLocs = new ArrayList<>();
        }
    }
}
