package net.thanachot.AdvanceInvis.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.thanachot.AdvanceInvis.manager.InvisibilityManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        // Check if killer is masked
        if (InvisibilityManager.getInstance().isPlayerInvisible(killer)) {
            Component originalMessage = event.deathMessage();
            if (originalMessage == null) return;

            String textMsg = PlainTextComponentSerializer.plainText().serialize(originalMessage);
            String killerName = killer.getName();

            if (textMsg.contains(killerName)) {
                String newMsg = textMsg.replace(killerName, "unknown");
                event.deathMessage(Component.text(newMsg)); // Using default color or white, as requested "unknown"
            }
        }
    }
}
