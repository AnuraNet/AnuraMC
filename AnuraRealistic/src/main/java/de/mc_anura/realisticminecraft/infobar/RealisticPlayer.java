package de.mc_anura.realisticminecraft.infobar;

import de.mc_anura.core.database.DB;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public abstract class RealisticPlayer {

    protected final short MIN;
    protected final short MAX;
    protected final short DEFAULT;
    protected float value;
    protected final Player player;
    protected Infobar<? extends RealisticPlayer> bar;

    protected RealisticPlayer(Player p, float value, short MIN, short MAX, short DEFAULT) {
        Objects.requireNonNull(p);
        player = p;
        this.MIN = MIN;
        this.MAX = MAX;
        this.DEFAULT = DEFAULT;
        if (value == -1) {
            this.value = DEFAULT;
        } else {
            this.value = value;
        }
    }

    public float getValue() {
        return ((int) (value * 10)) / 10f;
    }

    public float setValue(float value) {
        boolean updated = this.value != value;
        if (updated) {
            changeValue(value);
            if (value > MAX) {
                this.value = MAX;
            } else if (value < MIN) {
                this.value = MIN;
            } else {
                this.value = value;
            }
            bar.update();
        }
        return getValue();
    }

    public float addValue(float modifier) {
        if (blockModify() || modifier == 0) {
            return getValue();
        }
        return setValue(value + modifier);
    }

    public Player getPlayer() {
        return player;
    }

    public short getMIN() {
        return MIN;
    }
    
    public boolean isMIN() {
        return value == MIN;
    }

    public short getMAX() {
        return MAX;
    }

    public boolean isMAX() {
        return value == MAX;
    }

    public short getDEFAULT() {
        return DEFAULT;
    }
    
    public boolean isExtremeValue() {
        return isMIN() || isMAX();
    }
    
    public Infobar<? extends RealisticPlayer> getBar() {
        return bar;
    }
    
    protected void changeValue(float newValue) {
    }

    protected abstract boolean blockModify();

    protected abstract float calculateNewValue(Location l);

    protected void updateDatabase(boolean async) {
        DB.queryUpdate(async, "INSERT INTO " + getTableName() + " (`playerId`, `value`, `bar`) VALUES((SELECT id FROM players WHERE uuid = ?),?,?) ON DUPLICATE KEY UPDATE value = ?, bar = ?", player.getUniqueId().toString(), value, bar.getBarStatus().ordinal(), value, bar.getBarStatus().ordinal());
    }

    public abstract String getTableName();
}
