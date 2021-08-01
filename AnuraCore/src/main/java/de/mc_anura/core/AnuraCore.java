package de.mc_anura.core;

import de.mc_anura.core.commands.*;
import de.mc_anura.core.database.DB;
import de.mc_anura.core.database.ErrorAppender;
import de.mc_anura.core.listeners.*;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.msg.Msg.MsgType;
import de.mc_anura.core.msg.Msg.PluginType;
import de.mc_anura.core.tools.Potions;
import de.mc_anura.core.tools.Villagers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AnuraCore extends JavaPlugin {

    private static AnuraCore instance;
    private static ErrorAppender errorAppender;

    @Override
    public void onEnable() {
        instance = this;
        
        Msg.register(this, "SYSTEM", PluginType.SYSTEM);
        Logger logger = (Logger) LogManager.getRootLogger();
        errorAppender = new ErrorAppender();
        logger.addAppender(errorAppender);
        
        registerEvents();
        registerCommands();
        
        AnuraThread.init();
        Villagers.init();
        Potions.init();
        Money.init();
        
        for (Player P : Bukkit.getOnlinePlayers()) {
            JoinEvent.addAttachment(P);
            Potions.join(P);
        }
        
        Msg.send(this, MsgType.SUCCESS, "Server wurde erfolgreich neu geladen!");
    }

    @Override
    public void onDisable() {
        Msg.send(this, MsgType.SPECIAL, "Server wird neu geladen!");
        
        Money.save();
        
        AnuraThread.shutdown();
        Potions.disable();
        Logger logger = (Logger) LogManager.getRootLogger();
        logger.removeAppender(errorAppender);
        DB.stop();
    }

    public static AnuraCore getInstance() {
        return AnuraCore.instance;
    }
    
    private void registerEvents() {
        PluginManager man = Bukkit.getPluginManager();
        man.registerEvents(new EntityDamage(), this);
        man.registerEvents(new EntityFish(), this);
        man.registerEvents(new InteractEntity(), this);
        man.registerEvents(new InventoryEvent(), this);
        man.registerEvents(new JoinEvent(), this);
        man.registerEvents(new LeaveEvent(), this);
        man.registerEvents(new PlayerChat(), this);
        man.registerEvents(new PotionEvents(), this);
    }
    
    @SuppressWarnings({"ConstantConditions"})
    private void registerCommands() {
        getCommand("r").setExecutor(new Answer());
        getCommand("gm").setExecutor(new GameMode());
        getCommand("money").setExecutor(new MoneyCmd());
        getCommand("spawn").setExecutor(new Spawn());
        
        MessageCmd msgCmd = new MessageCmd();
        getCommand("msg").setExecutor(msgCmd);
        getCommand("msg").setTabCompleter(msgCmd);
    }
}
