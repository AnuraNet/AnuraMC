package de.mc_anura.freebuild;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.freebuild.commands.Claim;
import de.mc_anura.freebuild.events.*;
import de.mc_anura.freebuild.events.*;
import de.mc_anura.freebuild.regions.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class AnuraFreebuild extends JavaPlugin {

    private static final String WORLD = "world";
    
    private static AnuraFreebuild instance;

    @Override
    public void onEnable() {
        instance = this;
        
        Msg.register(this, "Freebuild", Msg.PluginType.GAMEPLAY);

        registerListeners();
        setupTasks();
        registerCommands();
        
        ResetManager.init();
        ClaimManager.init();
        RegionManager.init();
    }

    @Override
    public void onDisable() {
        ClaimManager.shutdown();
        ResetManager.save(false);
    }

    public static AnuraFreebuild getInstance() {
        return instance;
    }

    private void registerListeners() {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new BlockBreak(), this);
        pm.registerEvents(new BlockPlace(), this);
        //pm.registerEvents(new RegionListener(), this);
        pm.registerEvents(new SandEvent(), this);
        pm.registerEvents(new EnDeathEvent(), this);
        pm.registerEvents(new InteractPhysical(), this);
        pm.registerEvents(new EntityExplode(), this);
        pm.registerEvents(new PlayerInteract(), this);
        pm.registerEvents(new PlayerQuit(), this);
        pm.registerEvents(new ClaimEvents(), this);
        pm.registerEvents(new LightningStrike(), this);
        pm.registerEvents(new BlockFade(), this);
        pm.registerEvents(new BlockFromTo(), this);
        pm.registerEvents(new FireSpread(), this);
        pm.registerEvents(new LeavesDecay(), this);
        pm.registerEvents(new BedEvents(), this);
    }

    private void setupTasks() {
        AnuraThread.add(Bukkit.getScheduler().runTaskTimer(this, () -> ResetManager.save(true), 20 * 60 * 5, 20 * 60 * 5));
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("claim")).setExecutor(new Claim());
    }
    
    public static World getWorld() {
        return Bukkit.getWorld(WORLD);
    }
}
