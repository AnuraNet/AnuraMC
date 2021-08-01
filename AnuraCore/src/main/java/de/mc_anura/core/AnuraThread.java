package de.mc_anura.core;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AnuraThread {

    private static final Set<BukkitTask> tasks = Collections.synchronizedSet(new HashSet<>());
    private static Method taskClass = null;
    private static final Map<String, Integer> log = new ConcurrentHashMap<>();
    private static final Queue<Runnable> doSync = new ConcurrentLinkedQueue<>();

    public static void init() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(AnuraCore.getInstance(), AnuraThread::check, 20 * 30, 20 * 60 * 5);
        add(Bukkit.getScheduler().runTaskTimer(AnuraCore.getInstance(), () -> {
            long millis = System.currentTimeMillis();
            Runnable lastRun = null;
            for (int i = 0; i < 200; i++) {
                if (doSync.isEmpty()) return;
                try {
                    lastRun = doSync.poll();
                    lastRun.run();
                } catch (Throwable t) {
                    Logger.getLogger(AnuraThread.class.getName()).log(Level.SEVERE, "Caught exception while running sync task", t);
                }
                if (System.currentTimeMillis() - millis > 25) { // 50% of tick time
                    if (i > 0 && doSync.size() > 10) {
                        Logger.getLogger(AnuraThread.class.getName()).log(Level.INFO, "{0} sync tasks queued for execution!", doSync.size());
                    } else if (i == 0 && lastRun != null) {
                        Logger.getLogger(AnuraThread.class.getName()).log(Level.INFO, "{0} took too long ({1}ms) to execute!", new Object[] { lastRun.getClass().getName(), System.currentTimeMillis() - millis });
                    }
                    return;
                }
            }
        }, 3, 3));
    }

    public static void check() {
        BukkitScheduler s = Bukkit.getScheduler();
        synchronized (tasks) {
            int j = 0;
            Iterator<BukkitTask> taskIt = tasks.iterator();
            while (taskIt.hasNext()) {
                BukkitTask task = taskIt.next();
                if (!s.isCurrentlyRunning(task.getTaskId()) && !s.isQueued(task.getTaskId())) {
                    taskIt.remove();
                    j++;
                }
            }
            if (j > 10) Logger.getLogger(AnuraThread.class.getName()).log(Level.INFO, "Cleaned up {0} tasks!", j);
        }
        synchronized (log) {
            for (Entry<String, Integer> clazz : log.entrySet()) {
                if (clazz.getValue() > 200) {
                    Logger.getLogger(AnuraThread.class.getName()).log(Level.WARNING, "{0} newly registered tasks for {1}", new Object[] {clazz.getValue(), clazz.getKey()});
                }
            }
        }
        log.clear();
    }

    public static void queueSync(Runnable r) {
        doSync.add(r);
    }

    public static void sync(Runnable r) {
        if (!AnuraCore.getInstance().isEnabled()) {
            Logger.getLogger(AnuraThread.class.getName()).log(Level.WARNING, "Ignore newly registered task for {0}: Server is shutting down", r.getClass());
            return;
        }
        add(Bukkit.getScheduler().runTask(AnuraCore.getInstance(), r));
    }

    public static void async(Runnable r) {
        if (!AnuraCore.getInstance().isEnabled()) {
            Logger.getLogger(AnuraThread.class.getName()).log(Level.WARNING, "Ignore newly registered task for {0}: Server is shutting down", r.getClass());
            return;
        }
        add(Bukkit.getScheduler().runTaskAsynchronously(AnuraCore.getInstance(), r));
    }

    public static <A> void syncCB(Consumer<A> cb, A value) {
        sync(() -> cb.accept(value));
    }

    public static <A> void asyncCB(Consumer<A> cb, A value) {
        async(() -> cb.accept(value));
    }

    public static void shutdown() {
        synchronized (tasks) {
            BukkitScheduler s = Bukkit.getScheduler();
            tasks.forEach((t) -> {
                if (!s.isCurrentlyRunning(t.getTaskId()) && !s.isQueued(t.getTaskId())) return;
                Logger.getLogger(AnuraThread.class.getName()).log(Level.INFO, "Shutting down {0} ({1})", new Object[] {t.getTaskId(), getName(t)});
                t.cancel();
            });
            tasks.clear();
        }
        if (doSync.size() > 0) {
            Logger.getLogger(AnuraThread.class.getName()).log(Level.INFO, "Running {0} queued sync tasks", doSync.size());
            synchronized (doSync) {
                doSync.forEach(Runnable::run);
            }
            doSync.clear();
        }
    }

    public static void add(BukkitTask task) {
        tasks.add(task);
        String name = getName(task);
        log.put(name, log.containsKey(name) ? log.get(name) + 1 : 1);
    }

    private static String getName(BukkitTask task) {
        reflect();
        String name = null;
        try {
            name = taskClass.invoke(task).toString().split("\\$")[0];
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(AnuraThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (name == null) name = "unknown";
        return name;
    }

    private static void reflect() {
        if (taskClass == null) {
            try {
                taskClass = AnuraThread.class.getClassLoader().loadClass("org.bukkit.craftbukkit.v1_17_R1.scheduler.CraftTask").getDeclaredMethod("getTaskClass");
                taskClass.setAccessible(true);
            } catch (NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
                Logger.getLogger(AnuraThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
