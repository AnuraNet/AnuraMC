package de.mc_anura.core.tools;

import com.google.common.base.Stopwatch;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TownInventory {

    private final HashMap<Integer, InvItem> items = new HashMap<>();
    private final HashSet<ClickItemCallback> itemClicks = new HashSet<>();

    private Inventory inv;
    private String title;
    private TextColor color;
    private int size;
    private final boolean destroy;
    private Consumer<Player> closeListener = null;

    public TownInventory(String title, TextColor color, int size, boolean destroyOnEnd) {
        assert(size % 9 == 0);
        destroy = destroyOnEnd;
        settings(title, color, size);
        invs.add(this);
    }

    public TownInventory(String title, TextColor color, boolean destroyOnEnd) {
        this(title, color, 9, destroyOnEnd);
    }

    public TownInventory(String title) {
        this(title, 9, false);
    }

    public TownInventory(String title, int size) {
        this(title, size, false);
    }

    public TownInventory(String title, int size, boolean destroyOnEnd) {
        this(title, null, size, destroyOnEnd);
    }

    public String getName() {
        return color + title;
    }

    public TownInventory settings(String name, TextColor color, int size) {
        title = name;
        this.color = color;
        this.size = size;
        createInventory();
        return this;
    }

    public TownInventory settings(String name, int size) {
        return settings(name, null, size);
    }

    public TownInventory settings(int size) {
        return settings(title, color, size);
    }

    public TownInventory putItem(int slot, InvItem item) {
        assert(slot < size);
        items.put(slot, item);
        inv.setItem(slot, item.getItem());
        return this;
    }

    public TownInventory open(Player P) {
        P.openInventory(inv);
        openInvs.put(P, this);
        return this;
    }

    public boolean checkInvClick(int slot, Player P, boolean isShift) {
        if (!isOpen(P)) return false;
        if (!items.containsKey(slot)) return true;
        items.get(slot).click(P, isShift);
        return true;
    }

    public void checkPlayerInvClick(Player P, InventoryClickEvent event) {
        itemClicks.forEach((cb) -> cb.onClick(P, event.getSlot(), event.getAction(), event));
    }

    private void createInventory() {
        inv = Bukkit.createInventory(null, size, Component.text(title, color));
        items.entrySet().stream().filter((i) -> i.getKey() <= size).forEach((i) -> inv.setItem(i.getKey(), i.getValue().getItem()));
    }

    public void close(Player P) {
        if (isOpen(P)) {
            openInvs.remove(P);
            if (closeListener != null) {
                closeListener.accept(P);
            }
            if (destroy) {
                invs.remove(this);
            }
        }
    }

    public void delete() {
        Iterator<Entry<Player, TownInventory>> invsIt = openInvs.entrySet().iterator();
        while (invsIt.hasNext()) {
            Entry<Player, TownInventory> pInv = invsIt.next();
            if (pInv.getValue().equals(this)) {
                invsIt.remove();
                pInv.getKey().closeInventory();
                if (closeListener != null) {
                    closeListener.accept(pInv.getKey());
                }
            }
        }
        invs.remove(this);
    }

    public boolean isOpen(Player P) {
        return openInvs.containsKey(P) && openInvs.get(P) == this;
    }

    public void playerInvCallback(ClickItemCallback cb) {
        itemClicks.add(cb);
    }

    public void setCloseListener(Consumer<Player> cb) {
        closeListener = cb;
    }

    public static class InvItem {

        private final ItemStack stack;
        private final Component name;
        private final List<Component> lores = new ArrayList<>();
        private final HashSet<ActionData> actions = new HashSet<>();

        public InvItem(ItemStack stack) {
            this(stack, (Component[]) null);
        }

        public InvItem(ItemStack stack, Component... lores) {
            this(stack, null, lores);
        }

        public InvItem(ItemStack stack, Component name, Component... lores) {
            this.stack = stack;
            this.name = name;
            this.lores.addAll(Arrays.asList(lores));
        }

        public InvItem addAction(ActionData data) {
            actions.add(data);
            return this;
        }

        public InvItem addAction(Consumer<Player> cb) {
            actions.add(new ActionData(Action.EXEC, cb));
            return this;
        }

        public InvItem addLore(Component lore) {
            lores.add(lore);
            return this;
        }

        public ItemStack getItem() {
            ItemStack item = stack.clone();
            ItemMeta meta = item.getItemMeta();
            if (name != null) meta.displayName(name);
            if (!lores.isEmpty()) {
                meta.lore(new ArrayList<>(lores));
            }
            item.setItemMeta(meta);
            return item;
        }

        @SuppressWarnings("unchecked")
        public void click(Player P, boolean isShift) {
            for (ActionData action : actions) {
                if (action.isShift() != isShift) continue;
                Stopwatch stopwatch = Stopwatch.createStarted();
                Object data = action.getData();
                if (action.closeInv) {
                    P.closeInventory();
                }
                switch (action.getAction()) {
                    case OPEN_INV:
                        if (data instanceof TownInventory) {
                            ((TownInventory)data).open(P);
                        }
                        break;
                    case TELEPORT:
                        if (data instanceof Location) {
                            P.teleport((Location) data);
                        }
                        break;
                    case MESSAGE:
                        if (data instanceof String) {
                            P.sendMessage((String) data);
                        }
                        break;
                    case COMMAND:
                        if (data instanceof String) {
                            Bukkit.getServer().dispatchCommand(P, (String) data);
                        }
                        break;
                    case EXEC:
                        if (data instanceof Consumer) {
                            ((Consumer<Player>) data).accept(P);
                        }
                        break;
                }
                stopwatch.stop();
                if (stopwatch.elapsed(TimeUnit.MILLISECONDS) > 10) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Inventory click listener {0} took {1}",
                            new Object[]{action.data.getClass().getName(), stopwatch.toString()});
                }
            }
        }
    }

    public static class ActionData {

        private final Action action;
        private final Object data;
        private final boolean isShift;
        private final boolean closeInv;

        public ActionData(Action a, boolean shiftClick, Object d) {
            this(a, shiftClick, false, d);
        }

        public ActionData(Action a, boolean shiftClick, boolean close, Object d) {
            action = a;
            data = d;
            isShift = shiftClick;
            closeInv = close;
        }

        public ActionData(Action a, Object d) {
            this(a, false, d);
        }

        public Action getAction() {
            return action;
        }

        public Object getData() {
            return data;
        }

        public boolean isShift() {
            return isShift;
        }

        public boolean isCloseInv() {
            return closeInv;
        }
    }

    public static enum Action {
        OPEN_INV,
        TELEPORT,
        MESSAGE,
        COMMAND,
        EXEC
    }

    public interface ClickItemCallback {
        void onClick(Player P, int slot, InventoryAction action, InventoryClickEvent event);
    }

    static final HashSet<TownInventory> invs = new HashSet<>();
    private static final HashMap<Player, TownInventory> openInvs = new HashMap<>();

    public static HashSet<TownInventory> getInvs() {
        return invs;
    }

    public static TownInventory getOpenInv(Player P) {
        return openInvs.get(P);
    }
}
