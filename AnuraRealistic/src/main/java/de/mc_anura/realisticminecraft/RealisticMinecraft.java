package de.mc_anura.realisticminecraft;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.database.DB;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.msg.Msg.PluginData;
import de.mc_anura.realisticminecraft.command.InfobarCmd;
import de.mc_anura.realisticminecraft.command.InfobarSetCmd;
import de.mc_anura.realisticminecraft.fishing.FishingChunk;
import de.mc_anura.realisticminecraft.infobar.InfobarUtil;
import de.mc_anura.realisticminecraft.listener.Chairs;
import de.mc_anura.realisticminecraft.util.ChairManager;
import de.mc_anura.realisticminecraft.infobar.ValueHolder;
import de.mc_anura.realisticminecraft.listener.Fishing;
import de.mc_anura.realisticminecraft.listener.Infobar;
import de.mc_anura.realisticminecraft.listener.Timber;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class RealisticMinecraft extends JavaPlugin {

    private static RealisticMinecraft instance;
    public static final PluginData PLUGIN_DATA = new PluginData("RealisticMC", Msg.PluginType.SYSTEM);

    @Override
    public void onEnable() {
        instance = this;
        DB.queryUpdate("CREATE TABLE IF NOT EXISTS `playerTemperature` ("
                + "  `playerId` INT(10) UNSIGNED NOT NULL,"
                + "  `value` DECIMAL(7,5) NOT NULL,"
                + "  `bar` BIT(1) NOT NULL DEFAULT b'0',"
                + "  PRIMARY KEY (`playerId`),"
                + "  INDEX `playerTemperature_playerId` (`playerId`),"
                + "  CONSTRAINT `playerTemperature_playerId` FOREIGN KEY (`playerId`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                + ") ENGINE=InnoDB COLLATE='utf8_general_ci' COMMENT='RealisticMinecraft | hibo98'");
        DB.queryUpdate("CREATE TABLE IF NOT EXISTS `playerThirst` ("
                + "  `playerId` INT(10) UNSIGNED NOT NULL,"
                + "  `value` INT(11) NOT NULL,"
                + "  `bar` BIT(1) NOT NULL DEFAULT b'0',"
                + "  PRIMARY KEY (`playerId`),"
                + "  INDEX `playerTemperature_playerId` (`playerId`),"
                + "  CONSTRAINT `playerThirst_playerId` FOREIGN KEY (`playerId`) REFERENCES `players` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                + ") ENGINE=InnoDB COLLATE='utf8_general_ci' COMMENT='RealisticMinecraft | hibo98'");
        DB.queryUpdate("CREATE TABLE IF NOT EXISTS `fishingChunks` ( "
                + "  `primeKey` INT(11) NOT NULL AUTO_INCREMENT, "
                + "  `x` INT(11) NOT NULL, "
                + "  `z` INT(11) NOT NULL, "
                + "  `world` VARCHAR(50) COLLATE latin1_german1_ci NOT NULL, "
                + "  `catched` INT(11) NOT NULL, "
                + "  `lastUpdated` INT(11) NOT NULL, "
                + "  PRIMARY KEY (`primeKey`) "
                + ") ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_german1_ci COMMENT='RealisticMinecraft | LukBukkit'");
        // Chairs
        Bukkit.getPluginManager().registerEvents(new Chairs(), instance);
        // Infobar
        AnuraThread.async(() -> Bukkit.getOnlinePlayers().stream().filter((p) -> p.getGameMode().equals(GameMode.SURVIVAL)).forEach(ValueHolder::new));
        InfobarCmd infobarCmd = new InfobarCmd();
        PluginCommand infobar = instance.getCommand("infobar");
        if (infobar != null) {
            infobar.setExecutor(infobarCmd);
            infobar.setTabCompleter(infobarCmd);
        }
        InfobarSetCmd infobarSetCmd = new InfobarSetCmd();
        PluginCommand infobarset = instance.getCommand("infobarset");
        if (infobarset != null) {
            infobarset.setExecutor(infobarSetCmd);
            infobarset.setTabCompleter(infobarSetCmd);
        }
        InfobarUtil.enableInfobarTasks();
        Bukkit.getPluginManager().registerEvents(new Infobar(), instance);
        // Fishing
        FishingChunk.init();
        Bukkit.getPluginManager().registerEvents(new Fishing(), instance);
        // Trees
        Bukkit.getPluginManager().registerEvents(new Timber(), instance);
    }

    @Override
    public void onDisable() {
        ValueHolder.destroyAll();
        ChairManager.destroyAll();
    }

    public static RealisticMinecraft getInstance() {
        return instance;
    }
    
    public static boolean hasLogBlock() {
        return Bukkit.getServer().getPluginManager().getPlugin("LogBlock") != null;
    }
}