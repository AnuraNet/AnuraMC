package de.mc_anura.freebuild;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.Money;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.tools.AnuraInventory;
import de.mc_anura.core.tools.Potions;
import de.mc_anura.freebuild.regions.Region;
import de.mc_anura.freebuild.regions.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Lectern;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClaimManager {
    
    private static final Map<Player, Claim> activeClaims = new ConcurrentHashMap<>();
    
    private static Scoreboard claimScb;

    public static void init() {
        claimScb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective o = claimScb.registerNewObjective("main", "dummy", Component.text("Legende", NamedTextColor.AQUA));
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.getScore(ChatColor.GREEN + "Dein Grundstück").setScore(8);
        o.getScore(ChatColor.RED + "Fremdes Grundstück").setScore(7);
        o.getScore(ChatColor.YELLOW + "Aktuelle Auswahl").setScore(6);
        o.getScore("").setScore(5);
        o.getScore(ChatColor.AQUA + "" + ChatColor.BOLD + "Steuerung:").setScore(4);
        o.getScore(ChatColor.DARK_AQUA + "  Linksklick" + ChatColor.GRAY + ": " + ChatColor.BLUE + "Punkt 1 setzen").setScore(3);
        o.getScore(ChatColor.DARK_AQUA + "Rechtsklick" + ChatColor.GRAY + ": " + ChatColor.BLUE + "Punkt 2 setzen").setScore(2);
        o.getScore(ChatColor.DARK_AQUA + "   Q-Taste" + ChatColor.GRAY + ": " + ChatColor.BLUE + "Menü (kaufen, etc.)").setScore(1);
        
        AnuraThread.add(Bukkit.getScheduler().runTaskTimerAsynchronously(AnuraFreebuild.getInstance(), () -> {
            for (Player P : activeClaims.keySet()) {
                drawAreas(P);
            }
        }, 20 * 5, 20 * 5));
        
        AnuraThread.add(Bukkit.getScheduler().runTaskTimerAsynchronously(AnuraFreebuild.getInstance(), () -> {
            for (Claim claim : activeClaims.values()) {
                Location[] loc = claim.corners;
                if (loc[0] == null || loc[1] == null) {
                    claim.player.sendActionBar(Component.text("Wähle die Region mit Links-/Rechtsklick", NamedTextColor.DARK_AQUA));
                    continue;
                }
                Location l1 = loc[0];
                Location l2 = loc[1];
                int cost = getCost(l1, l2);
                AnuraThread.queueSync(() -> {
                    int money = Money.get(claim.player);
                    claim.player.sendActionBar(getMoneyText(money, cost));
                });
            }
        }, 20, 20));
    }

    private static int getCost(Location l1, Location l2) {
        int x1 = l1.getBlockX();
        int x2 = l2.getBlockX();
        int z1 = l1.getBlockZ();
        int z2 = l2.getBlockZ();
        int cost = (Math.max(x1, x2) - Math.min(x1, x2)) * (Math.max(z1, z2) - Math.min(z1, z2));
        return cost / 10;
    }

    private static Component getMoneyText(int money, int cost) {
        return Component.text("Dein Geld: ", NamedTextColor.GREEN)
                .append(Component.text(money, NamedTextColor.DARK_GREEN))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("Kosten: ", NamedTextColor.YELLOW))
                .append(Component.text(cost, (money >= cost ? NamedTextColor.GREEN : NamedTextColor.RED)));
    }
    
    public static boolean isClaiming(Player P) {
        return activeClaims.containsKey(P);
    }

    public static Location getOldLocation(Player P) {
        if (isClaiming(P)) {
            return activeClaims.get(P).oldLoc;
        } else {
            return null;
        }
    }

    public static void shutdown() {
        activeClaims.values().forEach(ClaimManager::endClaimMode);
        activeClaims.clear();
    }
    
    public static void endClaimMode(Player P) {
        if (activeClaims.containsKey(P)) {
            endClaimMode(activeClaims.get(P));
            activeClaims.remove(P);
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
            int cost = getCost(locs[0], locs[1]);
            AnuraThread.sync(() -> {
                int money = Money.get(P);
                if (money < cost) {
                    Msg.send(P, AnuraFreebuild.getInstance(), Msg.MsgType.ERROR, "Du hast nicht genug Geld! " + ChatColor.GRAY + "("
                                  + ChatColor.YELLOW + "Dein Geld: " + money + ChatColor.GRAY + " | " 
                                  + ChatColor.RED + "Kosten: " + cost + ChatColor.GRAY + ")");
                    return;
                }
                Money.pay(P, -cost);
                Region r = new Region(P.getUniqueId(), locs[0], locs[1]);
                r.save();
                Msg.send(P, AnuraFreebuild.getInstance(), Msg.MsgType.SUCCESS, "Gebiet geclaimt!");
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
            l.setY(l.getWorld().getHighestBlockYAt(l));
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

        claim.oldLoc = P.getLocation();
        claim.itemInHand = P.getInventory().getItemInMainHand();

        Potions.addNativePotion(P, new PotionEffect(PotionEffectType.LEVITATION, 4, 100));
        AnuraThread.add(Bukkit.getScheduler().runTaskLater(AnuraFreebuild.getInstance(), () -> {
            P.setAllowFlight(true);
            P.setFlying(true);
        }, 4));

        P.setScoreboard(claimScb);
        ItemStack s = new ItemStack(Material.STICK);
        ItemMeta m = s.getItemMeta();
        m.displayName(Component.text("Claim-Tool", NamedTextColor.GOLD));
        s.setItemMeta(m);
        P.getInventory().setItemInMainHand(s);
        
        activeClaims.put(P, claim);
    }

    public static void qButton(Player P) {
        if (!activeClaims.containsKey(P))
            return;

        int money = Money.get(P);
        Component moneyComponent = Component.text("Dein Geld: ", NamedTextColor.GREEN)
                .append(Component.text(money, NamedTextColor.DARK_GREEN));

        Component cost = Component.text("Kosten", NamedTextColor.YELLOW);
        Component lore = null;
        Location[] corners = activeClaims.get(P).corners;
        boolean buyable = false;
        if (corners[0] == null || corners[1] == null) {
            lore = Component.text("Wähle zunächst zwei Ecken mit der linken und rechten Maustaste.");
        } else {
            buyable = true;
            int costNum = getCost(corners[0], corners[1]);
            cost = cost.append(Component.text(": ", NamedTextColor.GRAY)).append(Component.text(costNum, NamedTextColor.AQUA));
        }


        AnuraInventory inv = new AnuraInventory(Component.text("Claim-Menü", NamedTextColor.DARK_AQUA), true);
        inv.putItem(0, new AnuraInventory.InvItem(new ItemStack(Material.RED_DYE), Component.text("Abbrechen", NamedTextColor.RED))
                .addAction(new AnuraInventory.ActionData(AnuraInventory.Action.COMMAND, "claim")));
        inv.putItem(3, new AnuraInventory.InvItem(new ItemStack(Material.BOOK), cost, lore));
        inv.putItem(5, new AnuraInventory.InvItem(new ItemStack(Material.COMMAND_BLOCK), moneyComponent));
        if (buyable) {
            inv.putItem(8, new AnuraInventory.InvItem(new ItemStack(Material.GREEN_DYE), Component.text("Claim kaufen", NamedTextColor.GREEN))
                    .addAction(ClaimManager::confirm));
        }

        inv.open(P);
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
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK){
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
            drawBoundingBox(P, locs[0], locs[1], Material.YELLOW_WOOL.createBlockData());
        }
    }
    
    private static Location getLookingAt(Player P) {
        return P.getTargetBlock(null, 100).getLocation();
    }

    public static @Nullable PersistentDataContainer getPossibleBookData(@NotNull Block block) {
        if (block.getState() instanceof Lectern lectern) {
            ItemStack book = lectern.getInventory().getItem(0);
            if (book != null && book.getItemMeta() instanceof BookMeta bMeta) {
                PersistentDataContainer data = bMeta.getPersistentDataContainer();
                Integer claimId = getIntData(data, "claim");
                if (claimId != null) {
                    return data;
                }
            }
        }
        return null;
    }

    public static void manageClaim(Player P, PersistentDataContainer data) {
        Integer claim = getIntData(data, "claim");
        if (claim == null) {
            return;
        }
        if (claim == -1) {
            String owner = getStrData(data, "owner");
            if (owner == null) {
                P.sendActionBar(Component.text("Unbekannter Grenzstein. Bitte gib einem Admin Bescheid!", NamedTextColor.RED));
                return;
            }
            if (!P.getUniqueId().equals(UUID.fromString(owner))) {
                P.sendActionBar(Component.text("Dieser Grenzstein gehört dir nicht.", NamedTextColor.RED));
                return;
            }
            if (isClaiming(P)) {

            }
        }
    }

    private static NamespacedKey getKey(String path) {
        return Objects.requireNonNull(NamespacedKey.fromString(path, AnuraFreebuild.getInstance()));
    }

    private static Integer getIntData(PersistentDataContainer data, String path) {
        return data.get(getKey(path), PersistentDataType.INTEGER);
    }

    private static String getStrData(PersistentDataContainer data, String path) {
        return data.get(getKey(path), PersistentDataType.STRING);
    }

    public static ItemStack getNewClaimBook(Player P) {
        ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta book = (BookMeta) stack.getItemMeta();
        book.setGeneration(BookMeta.Generation.ORIGINAL);
        book.author(Component.text("Makler"))
            .title(P.displayName().append(Component.text("'s Grundbuch")))
            .addPages(Component.text("Mit diesem Buch kannst du dir ein neues Grundstück claimen.\n", NamedTextColor.BLUE)
                .append(Component.text("Begib dich in dein gewünschtes Gebiet" +
                        " und klicke auf den folgenden Link, um in den Claim-Modus zu wechseln: ", NamedTextColor.DARK_GREEN))
                .append(Component.text("Claim-Modus", NamedTextColor.GOLD).decorate(TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/claim"))));

        PersistentDataContainer data = book.getPersistentDataContainer();
        data.set(getKey("claim"), PersistentDataType.INTEGER, -1);
        data.set(getKey("owner"), PersistentDataType.STRING, P.getUniqueId().toString());

        stack.setItemMeta(book);
        return stack;
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
