package net.thanachot.AdvanceInvis;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.thanachot.AdvanceInvis.listener.BrewListener;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvanceInvis extends JavaPlugin implements Listener {

    private static NamespacedKey ADV_INVIS_KEY;

    @Override
    public void onEnable() {
        getLogger().info("AdvanceInvis Enabled!");
        ADV_INVIS_KEY = new NamespacedKey(this, "advanceinvis");
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BrewListener(), this);

        // Register PotionMix for Drinkable Potions
        NamespacedKey drinkableKey = new NamespacedKey(this, "advanced_invis_drinkable");
        org.bukkit.inventory.ItemStack drinkableResult = BrewListener.createAdvancedInvisPotion(false);
        io.papermc.paper.potion.PotionMix drinkableMix = new io.papermc.paper.potion.PotionMix(
                drinkableKey,
                drinkableResult,
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(org.bukkit.Material.POTION),
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(org.bukkit.Material.PHANTOM_MEMBRANE));
        getServer().getPotionBrewer().addPotionMix(drinkableMix);

        // Register PotionMix for Splash Potions
        NamespacedKey splashKey = new NamespacedKey(this, "advanced_invis_splash");
        org.bukkit.inventory.ItemStack splashResult = BrewListener.createAdvancedInvisPotion(true);
        io.papermc.paper.potion.PotionMix splashMix = new io.papermc.paper.potion.PotionMix(
                splashKey,
                splashResult,
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(org.bukkit.Material.SPLASH_POTION),
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(org.bukkit.Material.PHANTOM_MEMBRANE));
        getServer().getPotionBrewer().addPotionMix(splashMix);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // If there is no killer, or the killer is the victim (suicide), ignore
        if (killer == null || killer.equals(victim))
            return;

        Component originalMessage = event.deathMessage();
        if (originalMessage == null)
            return;

        // Convert component to plain text to do a simple string replacement
        // In a production plugin, you should manipulate the Component directly to
        // preserve formatting.
        String textMsg = PlainTextComponentSerializer.plainText().serialize(originalMessage);

        String killerName = killer.getName();

        // Replace Killer's name with "Unknown"
        if (textMsg.contains(killerName)) {
            String newMsg = textMsg.replace(killerName, "Unknown");

            // Set the new message
            event.deathMessage(Component.text(newMsg, NamedTextColor.RED));
        }
    }

    public static AdvanceInvis getInstance() {
        return JavaPlugin.getPlugin(AdvanceInvis.class);
    }

    public static NamespacedKey getADV_INVIS_KEY() {
        return ADV_INVIS_KEY;
    }
}
