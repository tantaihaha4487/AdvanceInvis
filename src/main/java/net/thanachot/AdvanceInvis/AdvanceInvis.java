package net.thanachot.AdvanceInvis;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.thanachot.AdvanceInvis.listener.BrewListener;
import net.thanachot.AdvanceInvis.manager.InvisibilityManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvanceInvis extends JavaPlugin implements Listener {

    private static NamespacedKey ADV_INVIS_KEY;

    public static AdvanceInvis getInstance() {
        return JavaPlugin.getPlugin(AdvanceInvis.class);
    }

    public static NamespacedKey getADV_INVIS_KEY() {
        return ADV_INVIS_KEY;
    }

    @Override
    public void onEnable() {
        getLogger().info("AdvanceInvis Enabled!");
        ADV_INVIS_KEY = new NamespacedKey(this, "advanceinvis");

        // Initialize Manager (starts cleanup task)
        InvisibilityManager.getInstance();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BrewListener(), this);

        registerRecipes();
    }

    private void registerRecipes() {
        // Register PotionMix for Drinkable Potions
        NamespacedKey drinkableKey = new NamespacedKey(this, "advanced_invis_drinkable");
        ItemStack drinkableResult = BrewListener.createAdvancedInvisPotion(Material.POTION, 180);
        io.papermc.paper.potion.PotionMix drinkableMix = new io.papermc.paper.potion.PotionMix(
                drinkableKey,
                drinkableResult,
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.POTION),
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.DIAMOND));
        getServer().getPotionBrewer().addPotionMix(drinkableMix);

        // Register PotionMix for Splash Potions
        NamespacedKey splashKey = new NamespacedKey(this, "advanced_invis_splash");
        ItemStack splashResult = BrewListener.createAdvancedInvisPotion(Material.SPLASH_POTION, 180);
        io.papermc.paper.potion.PotionMix splashMix = new io.papermc.paper.potion.PotionMix(
                splashKey,
                splashResult,
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.SPLASH_POTION),
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.DIAMOND));
        getServer().getPotionBrewer().addPotionMix(splashMix);

        // Register PotionMix for Lingering Potions
        NamespacedKey lingeringKey = new NamespacedKey(this, "advanced_invis_lingering");
        ItemStack lingeringResult = BrewListener.createAdvancedInvisPotion(Material.LINGERING_POTION, 180);
        io.papermc.paper.potion.PotionMix lingeringMix = new io.papermc.paper.potion.PotionMix(
                lingeringKey,
                lingeringResult,
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.LINGERING_POTION),
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.DIAMOND));
        getServer().getPotionBrewer().addPotionMix(lingeringMix);
    }

    @Override
    public void onDisable() {
        // Cleanup if needed
    }

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
