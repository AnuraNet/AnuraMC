package de.mc_anura.core.tools;

import com.google.common.base.Stopwatch;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

public class AnuraInventory {

    private final HashMap<Integer, InvItem> items = new HashMap<>();
    private final HashSet<ClickItemCallback> itemClicks = new HashSet<>();

    private Inventory inv;
    private Component title;
    private int size;
    private final boolean destroy;
    private Consumer<Player> closeListener = null;

    public AnuraInventory(Component title, int size, boolean destroyOnEnd) {
        assert(size % 9 == 0);
        destroy = destroyOnEnd;
        settings(title, size);
        invs.add(this);
    }

    public AnuraInventory(Component title, boolean destroyOnEnd) {
        this(title, 9, destroyOnEnd);
    }

    public AnuraInventory(Component title) {
        this(title, 9, false);
    }

    public AnuraInventory(Component title, int size) {
        this(title, size, false);
    }

    public Component getName() {
        return title;
    }

    public AnuraInventory settings(Component name, int size) {
        title = name;
        this.size = size;
        createInventory();
        return this;
    }

    public AnuraInventory settings(int size) {
        return settings(title, size);
    }

    public AnuraInventory putItem(int slot, InvItem item) {
        assert(slot < size);
        items.put(slot, item);
        inv.setItem(slot, item.getItem());
        return this;
    }

    public AnuraInventory open(Player P) {
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
        inv = Bukkit.createInventory(null, size, title);
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
        Iterator<Entry<Player, AnuraInventory>> invsIt = openInvs.entrySet().iterator();
        while (invsIt.hasNext()) {
            Entry<Player, AnuraInventory> pInv = invsIt.next();
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
            this(stack, null);
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

        //@SuppressWarnings("unchecked")
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
                        if (data instanceof AnuraInventory inv) {
                            inv.open(P);
                        }
                        break;
                    case TELEPORT:
                        if (data instanceof Location loc) {
                            P.teleport(loc);
                        }
                        break;
                    case MESSAGE:
                        if (data instanceof String msg) {
                            P.sendMessage(msg);
                        }
                        break;
                    case COMMAND:
                        if (data instanceof String cmd) {
                            Bukkit.getServer().dispatchCommand(P, cmd);
                        }
                        break;
                    case EXEC:
                        if (data instanceof Consumer) {
                            //noinspection unchecked
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

    public enum Action {
        OPEN_INV,
        TELEPORT,
        MESSAGE,
        COMMAND,
        EXEC
    }

    public interface ClickItemCallback {
        void onClick(Player P, int slot, InventoryAction action, InventoryClickEvent event);
    }

    static final HashSet<AnuraInventory> invs = new HashSet<>();
    private static final HashMap<Player, AnuraInventory> openInvs = new HashMap<>();

    public static HashSet<AnuraInventory> getInvs() {
        return invs;
    }

    public static AnuraInventory getOpenInv(Player P) {
        return openInvs.get(P);
    }
}
