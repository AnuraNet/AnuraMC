package de.mc_anura.realisticminecraft.infobar;

import de.mc_anura.core.msg.Msg;
import de.mc_anura.realisticminecraft.RealisticMinecraft;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class InfoBarCommandHelper {

    @Contract("_, _, _ -> new")
    public static @NotNull Result getRealisticPlayer(@NotNull CommandSender cs, @NotNull Player p, @NotNull String type) {
        if (!p.getGameMode().equals(GameMode.SURVIVAL)) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Dieses Command kann nur im SURVIVAL Mode verwendet werden");
            return new Result(true, null);
        }
        ValueHolder valueHolder = ValueHolder.getValueHolder(p);
        if (valueHolder == null) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Es ist ein interner Fehler aufgetreten (0)");
            return new Result(true, null);
        }
        RealisticPlayer rp;
        switch (type.toLowerCase()) {
            case "durst":
                rp = valueHolder.getPlayer(ThirstPlayer.class);
                break;
            case "temp":
            case "temperatur":
                rp = valueHolder.getPlayer(TemperaturePlayer.class);
                break;
            default:
                return new Result(false, null);
        }
        if (rp == null) {
            Msg.send(cs, RealisticMinecraft.PLUGIN_DATA, Msg.MsgType.ERROR, "Es ist ein interner Fehler aufgetreten (1)");
            return new Result(true, null);
        }
        return new Result(true, rp);
    }

    public record Result(boolean bool, RealisticPlayer realisticPlayer) {
    }
}
