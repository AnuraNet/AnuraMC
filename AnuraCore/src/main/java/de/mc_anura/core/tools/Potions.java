package de.mc_anura.core.tools;

import de.mc_anura.core.AnuraThread;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Potions {

    private static final Map<Player, Map<PotionEffectType, Set<CustomPotion>>> customPotions = new ConcurrentHashMap<>();
    private static final Map<Player, Map<PotionEffectType, Set<NativePotion>>> nativePotions = new ConcurrentHashMap<>();
    private static final Map<Player, Map<PotionEffectType, Long>> needsRefresh = new ConcurrentHashMap<>();
    private static final Set<Player> isReady = Collections.synchronizedSet(new HashSet<>());

    private static boolean end = false, init = false;

    @SuppressWarnings("NestedSynchronizedStatement")
    public static void init() {
        if (init) {
            return;
        }
        init = true;

        AnuraThread.async(() -> {
            while (!end) {
                synchronized (needsRefresh) {
                    for (Entry<Player, Map<PotionEffectType, Long>> refreshPlayers : needsRefresh.entrySet()) {
                        if (!isReady.contains(refreshPlayers.getKey())) continue;
                        Set<Entry<PotionEffectType, Long>> pots = refreshPlayers.getValue().entrySet();
                        synchronized (pots) {
                            Iterator<Entry<PotionEffectType, Long>> refreshIt = pots.iterator();
                            while (refreshIt.hasNext()) {
                                Entry<PotionEffectType, Long> refresh = refreshIt.next();
                                if (refresh.getValue() <= System.currentTimeMillis()) {
                                    long next = recalculate(refreshPlayers.getKey(), refresh.getKey(), true);
                                    if (next < 0) {
                                        refreshIt.remove();
                                    } else {
                                        refresh.setValue(next);
                                    }
                                }
                            }
                        }
                    }
                }

                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Potions.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public static void addCustomPotion(Player P, CustomPotion holder) {
        PotionEffectType type = holder.potion.getType();
        initMaps(P, type);
        if (customPotions.get(P).get(type).add(holder)) {
            setNeedsRefresh(P, type);
        }
    }

    public static void addNativePotion(Player P, PotionEffect effect) {
        PotionEffectType type = effect.getType();
        initMaps(P, type);
        nativePotions.get(P).get(type).add(makeNative(effect));
        setNeedsRefresh(P, type);
    }

    public static void removeCustomPotion(Player P, CustomPotion potion) {
        PotionEffectType type = potion.potion.getType();
        if (!customPotions.containsKey(P) || !customPotions.get(P).containsKey(type)) {
            return;
        }

        if (customPotions.get(P).get(type).remove(potion)) {
            setNeedsRefresh(P, type);
        }
    }

    public static void removeNativePotion(Player P, PotionEffect effect, boolean removeAll) {
        PotionEffectType type = effect.getType();
        if (removeAll && nativePotions.containsKey(P)) {
            nativePotions.get(P).remove(type);
        }
        setNeedsRefresh(P, type);
    }

    public static void join(Player P) {
        for (PotionEffect effect : P.getActivePotionEffects()) {
            addNativePotion(P, effect);
        }
        isReady.add(P);
    }

    public static void logout(Player P) {
        isReady.remove(P);
        customPotions.remove(P);
        if (needsRefresh.containsKey(P)) {
            for (PotionEffectType type : needsRefresh.get(P).keySet()) {
                recalculate(P, type, false);
            }
        }
        nativePotions.remove(P);
        needsRefresh.remove(P);
    }

    public static void refresh(Player P) {
        if (!needsRefresh.containsKey(P)) return;
        for (PotionEffectType type : needsRefresh.get(P).keySet()) {
            setNeedsRefresh(P, type);
        }
    }

    public static void disable() {
        end = true;
        for (Player P : Bukkit.getOnlinePlayers()) {
            logout(P);
        }
    }

    private static long recalculate(Player P, PotionEffectType type, boolean resync) {
        clearOld(P);
        boolean hasCustomPot = customPotions.containsKey(P) && customPotions.get(P).containsKey(type);
        boolean hasNativePot = nativePotions.containsKey(P) && nativePotions.get(P).containsKey(type);

        int bestLevel = 0, bestTime = 0;
        HandledPotion bestPotion = null;
        if (hasCustomPot) {
            Set<CustomPotion> pots = customPotions.get(P).get(type);
            synchronized (pots) {
                for (CustomPotion pot : pots) {
                    if (hasNativePot && !pot.overrideNative) {
                        continue;
                    }
                    if (bestPotion == null || pot.potion.getAmplifier() > bestLevel) {
                        bestPotion = pot;
                        bestLevel = pot.potion.getAmplifier();
                        bestTime = Integer.MAX_VALUE;
                    }
                }
            }
        }

        if (hasNativePot) {
            Set<NativePotion> pots = nativePotions.get(P).get(type);
            synchronized (pots) {
                for (NativePotion pot : pots) {
                    int time = (int) (pot.until - System.currentTimeMillis() / 50);
                    if (bestPotion == null || pot.potion.getAmplifier() > bestLevel) {
                        bestPotion = pot;
                        bestLevel = pot.potion.getAmplifier();
                        bestTime = time;
                    } else if (pot.potion.getAmplifier() == bestLevel && time > bestTime) {
                        bestPotion = pot;
                        bestTime = time;
                    }
                }
            }
        }

        PotionEffect effect = null;
        if (bestPotion != null) {
            effect = new PotionEffect(type, bestTime, bestLevel, bestPotion.potion.isAmbient(), bestPotion.potion.hasParticles(), bestPotion.potion.hasIcon());
        }

        PotionEffect finalEffect = effect;
        Runnable toDo;
        if (finalEffect == null) {
            toDo = () -> P.removePotionEffect(type);
        } else {
            toDo = () -> P.addPotionEffect(finalEffect, true);
        }

        if (resync) {
            AnuraThread.queueSync(toDo);
        } else {
            toDo.run();
        }

        if (bestTime == 0) {
            return -1;
        }

        long refreshTarget = bestTime > 20 * 60 * 10 ? 20 * 60 * 10 : bestTime + 3;
        return System.currentTimeMillis() + refreshTarget * 50;
    }

    private static NativePotion makeNative(PotionEffect effect) {
        long until = System.currentTimeMillis() / 50 + effect.getDuration();
        return new NativePotion(until, effect);
    }

    private static void setNeedsRefresh(Player P, PotionEffectType type) {
        if (!needsRefresh.containsKey(P)) {
            needsRefresh.put(P, new ConcurrentHashMap<>());
        }

        needsRefresh.get(P).put(type, 0L);
    }

    @SuppressWarnings("NestedSynchronizedStatement")
    private static void clearOld(Player P) {
        if (!nativePotions.containsKey(P)) return;
        Collection<Set<NativePotion>> natPots = nativePotions.get(P).values();
        synchronized (natPots) {
            for (Set<NativePotion> pots : natPots) {
                synchronized (pots) {
                    pots.removeIf(pot -> pot.until <= System.currentTimeMillis() / 50);
                }
            }
        }
    }

    private static void initMaps(Player P, PotionEffectType type) {
        if (!nativePotions.containsKey(P)) {
            nativePotions.put(P, new ConcurrentHashMap<>());
        }
        if (!nativePotions.get(P).containsKey(type)) {
            nativePotions.get(P).put(type, ConcurrentHashMap.newKeySet());
        }
        if (!customPotions.containsKey(P)) {
            customPotions.put(P, new ConcurrentHashMap<>());
        }
        if (!customPotions.get(P).containsKey(type)) {
            customPotions.get(P).put(type, ConcurrentHashMap.newKeySet());
        }
    }

    private static class NativePotion extends HandledPotion {
        private final long until;

        private NativePotion(long until, PotionEffect potion) {
            super(potion);
            this.until = until;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (int) (this.until ^ (this.until >>> 32));
            hash = 97 * hash + this.potion.getAmplifier();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NativePotion other = (NativePotion) obj;
            if (this.until != other.until) {
                return false;
            }
            return this.potion.getAmplifier() == other.potion.getAmplifier();
        }
    }

    public static class CustomPotion extends HandledPotion {
        private final boolean overrideNative;

        public CustomPotion(PotionEffect potion, boolean overrideNative) {
            super(potion);
            this.overrideNative = overrideNative;
        }

        public CustomPotion(PotionEffect potion) {
            this(potion, true);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + Objects.hashCode(this.potion);
            hash = 47 * hash + (this.overrideNative ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CustomPotion other = (CustomPotion) obj;
            if (this.overrideNative != other.overrideNative) {
                return false;
            }
            return Objects.equals(this.potion, other.potion);
        }
    }

    public static abstract class HandledPotion {
        protected final PotionEffect potion;

        public HandledPotion(PotionEffect potion) {
            this.potion = potion;
        }
    }
}
