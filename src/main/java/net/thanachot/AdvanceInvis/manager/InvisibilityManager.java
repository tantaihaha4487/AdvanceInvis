package net.thanachot.AdvanceInvis.manager;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.thanachot.AdvanceInvis.AdvanceInvis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvisibilityManager {

    private static InvisibilityManager instance;
    private final Map<UUID, Long> invisiblePlayers;

    private InvisibilityManager() {
        this.invisiblePlayers = new HashMap<>();
        startCleanupTask();
    }

    public static synchronized InvisibilityManager getInstance() {
        if (instance == null) {
            instance = new InvisibilityManager();
        }
        return instance;
    }

    public void addInvisiblePlayer(Player player, int durationTicks) {
        long expiryTime = System.currentTimeMillis() + (durationTicks * 50L);
        invisiblePlayers.put(player.getUniqueId(), expiryTime);
    }

    public boolean isPlayerInvisible(Player player) {
        if (!invisiblePlayers.containsKey(player.getUniqueId())) {
            return false;
        }

        long expiryTime = invisiblePlayers.get(player.getUniqueId());
        if (System.currentTimeMillis() > expiryTime) {
            invisiblePlayers.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                invisiblePlayers.entrySet().removeIf(entry -> now > entry.getValue());
            }
        }.runTaskTimer(AdvanceInvis.getInstance(), 20L, 20L * 60); // Run every minute
    }
}
