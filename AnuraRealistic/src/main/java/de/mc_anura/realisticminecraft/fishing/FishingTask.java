
package de.mc_anura.realisticminecraft.fishing;

public record FishingTask(long timestamp, Runnable r) {

    public long getTimestamp() {
        return timestamp;
    }

    public Runnable getR() {
        return r;
    }
}
