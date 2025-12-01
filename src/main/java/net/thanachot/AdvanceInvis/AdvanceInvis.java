package net.thanachot.AdvanceInvis;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvanceInvis extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("AdvanceInvis Enabled!");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }


    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // If there is no killer, or the killer is the victim (suicide), ignore
        if (killer == null || killer.equals(victim)) return;

        Component originalMessage = event.deathMessage();
        if (originalMessage == null) return;

        // Convert component to plain text to do a simple string replacement
        // In a production plugin, you should manipulate the Component directly to preserve formatting.
        String textMsg = PlainTextComponentSerializer.plainText().serialize(originalMessage);

        String killerName = killer.getName();

        // Replace Killer's name with "Unknown"
        if (textMsg.contains(killerName)) {
            String newMsg = textMsg.replace(killerName, "Unknown");

            // Set the new message
            event.deathMessage(Component.text(newMsg, NamedTextColor.RED));
        }
    }
}
