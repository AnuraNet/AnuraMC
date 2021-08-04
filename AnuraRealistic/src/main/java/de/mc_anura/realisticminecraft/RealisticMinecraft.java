package de.mc_anura.realisticminecraft;

import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.database.DB;
import de.mc_anura.core.msg.Msg;
import de.mc_anura.core.msg.Msg.PluginData;
import de.mc_anura.realisticminecraft.command.ChairCommand;
import de.mc_anura.realisticminecraft.command.InfoBarCommand;
import de.mc_anura.realisticminecraft.command.SetInfoBarCommand;
import de.mc_anura.realisticminecraft.fishing.FishingChunk;
import de.mc_anura.realisticminecraft.infobar.InfoBarUtil;
import de.mc_anura.realisticminecraft.infobar.ValueHolder;
import de.mc_anura.realisticminecraft.listener.Chairs;
import de.mc_anura.realisticminecraft.listener.Fishing;
import de.mc_anura.realisticminecraft.listener.InfoBar;
import de.mc_anura.realisticminecraft.listener.Timber;
import de.mc_anura.realisticminecraft.util.ChairManager;
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
        // InfoBar
        AnuraThread.async(() -> Bukkit.getOnlinePlayers().stream().filter((p) -> p.getGameMode().equals(GameMode.SURVIVAL)).forEach(ValueHolder::new));
        InfoBarCommand infoBarCommand = new InfoBarCommand();
        PluginCommand infoBar = instance.getCommand("infobar");
        if (infoBar != null) {
            infoBar.setExecutor(infoBarCommand);
            infoBar.setTabCompleter(infoBarCommand);
        }
        SetInfoBarCommand setInfoBarCommand = new SetInfoBarCommand();
        PluginCommand setInfoBar = instance.getCommand("setinfobar");
        if (setInfoBar != null) {
            setInfoBar.setExecutor(setInfoBarCommand);
            setInfoBar.setTabCompleter(setInfoBarCommand);
        }
        ChairCommand chairCommand = new ChairCommand();
        PluginCommand chair = instance.getCommand("chair");
        if (chair != null) {
            chair.setExecutor(chairCommand);
        }
        InfoBarUtil.enableInfoBarTasks();
        Bukkit.getPluginManager().registerEvents(new InfoBar(), instance);
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
