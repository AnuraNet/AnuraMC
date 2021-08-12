package de.mc_anura.core.tools;

import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

public class ItemSearch {

    public static List<ItemStack> find(Inventory inv, Predicate<ItemStack> matcher) {
        List<ItemStack> result = new LinkedList<>();
        ItemSearchIterator stacks = new ItemSearchIterator(inv, true);
        while (stacks.hasNext()) {
            ItemStack stack = stacks.next();
            if (stack == null) {
                continue;
            }
            if (matcher.test(stack)) {
                result.add(stack);
            }
        }
        return result;
    }

    public static class ItemSearchIterator implements ListIterator<ItemStack> {
        private final Inventory rootInventory;
        private final BundleMeta rootBundle;
        private final ShulkerBox rootShulker;
        private final ItemStack rootItem;
        private final boolean includeBundles;

        private ListIterator<ItemStack> child = null;
        private ItemStack childItem = null;
        private int nextIndex = 0;
        private boolean isChild = false;
        private boolean nextIsChild = false;

        public ItemSearchIterator(Inventory inventory, boolean includeBundles) {
            this.rootInventory = inventory;
            this.rootBundle = null;
            this.rootShulker = null;
            this.rootItem = null;
            this.includeBundles = includeBundles;
        }

        public ItemSearchIterator(ItemStack item, BundleMeta bundle) {
            this.rootInventory = null;
            this.rootBundle = bundle;
            this.rootShulker = null;
            this.rootItem = item;
            this.includeBundles = true;
        }

        public ItemSearchIterator(ItemStack item, ShulkerBox shulkerBox, boolean includeBundles) {
            this.rootInventory = null;
            this.rootBundle = null;
            this.rootShulker = shulkerBox;
            this.rootItem = item;
            this.includeBundles = includeBundles;
        }

        @Override
        public boolean hasNext() {
            return nextIsChild || nextIndex < (rootInventory != null ? rootInventory.getSize() :
                                               rootBundle != null ? rootBundle.getItems().size() :
                                               rootShulker != null ? rootShulker.getInventory().getSize() : 0);
        }

        @Override
        public ItemStack next() {
            ItemStack result;
            if (!nextIsChild) {
                child = null;
                ItemStack item = rootInventory != null ? rootInventory.getItem(nextIndex) :
                                 rootBundle != null ? rootBundle.getItems().get(nextIndex) :
                                 rootShulker != null ? rootShulker.getInventory().getItem(nextIndex) : null;
                if (item != null && Tag.SHULKER_BOXES.isTagged(item.getType()) && item.getItemMeta() instanceof BlockStateMeta meta) {
                    if (meta.hasBlockState() && meta.getBlockState() instanceof ShulkerBox box) {
                        child = new ItemSearchIterator(item, box, includeBundles);
                    }
                } else if (includeBundles && item != null && item.getItemMeta() instanceof BundleMeta bundle) {
                    child = new ItemSearchIterator(item, bundle);
                }
                childItem = item;
                result = item;
                nextIndex++;
                isChild = false;
            } else {
                result = child.next();
                isChild = true;
            }

            nextIsChild = child != null && child.hasNext();

            return result;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public boolean hasPrevious() {
            throw new UnsupportedOperationException("Going to previous elements is not supported!");
        }

        @Override
        public ItemStack previous() {
            throw new UnsupportedOperationException("Going to previous elements is not supported!");
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        private void setInRoot(ItemStack item) {
            if (rootInventory != null) {
                rootInventory.setItem(nextIndex - 1, item);
            } else if (rootBundle != null) {
                List<ItemStack> items = new ArrayList<>(rootBundle.getItems());
                if (item == null || item.getType().isAir()) {
                    items.remove(nextIndex - 1);
                } else {
                    items.set(nextIndex - 1, item);
                }
                rootBundle.setItems(items);
                if (rootItem != null) {
                    rootItem.setItemMeta(rootBundle);
                }
            } else if (rootShulker != null) {
                rootShulker.getInventory().setItem(nextIndex - 1, item);
                if (rootItem != null) {
                    BlockStateMeta meta = (BlockStateMeta) rootItem.getItemMeta();
                    meta.setBlockState(rootShulker);
                    rootItem.setItemMeta(meta);
                }
            }
        }

        @Override
        public void set(ItemStack item) {
            if (nextIndex == 0) {
                throw new IllegalStateException("No current item!");
            }
            if (!isChild) {
                setInRoot(item);
            } else {
                child.set(item);
                setInRoot(childItem);
            }
        }

        @Override
        public void add(ItemStack item) {
            throw new UnsupportedOperationException("Can't change the size of an inventory!");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't change the size of an inventory!");
        }
    }
}
