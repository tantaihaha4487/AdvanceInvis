package net.thanachot.AdvanceInvis;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
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
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

import java.util.List;

public class AdvanceInvis extends JavaPlugin implements Listener {

    private static AdvanceInvis instance;
    private static NamespacedKey ADV_INVIS_KEY;

    public static AdvanceInvis getInstance() {
        return instance;
    }

    public static NamespacedKey getADV_INVIS_KEY() {
        return ADV_INVIS_KEY;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("AdvanceInvis Enabled!");
        ADV_INVIS_KEY = new NamespacedKey(this, "advanceinvis");

        // Initialize Manager (starts cleanup task)
        InvisibilityManager.getInstance();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BrewListener(), this);

        registerRecipes();
    }

    private ItemStack createBasePotion(Material material, PotionType type) {
        ItemStack stack = new ItemStack(material);
        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        meta.setBasePotionType(type);
        stack.setItemMeta(meta);
        return stack;
    }

    private void registerRecipes() {
        // Create strict inputs (Vanilla Invisibility Only)
        // This prevents "Advance Invisible Potion" (which has extra NBT) from being accepted.

        // Drinkable Inputs
        ItemStack drinkableNormal = createBasePotion(Material.POTION, PotionType.INVISIBILITY);
        ItemStack drinkableLong = createBasePotion(Material.POTION, PotionType.LONG_INVISIBILITY);
        RecipeChoice.ExactChoice drinkableInput = new RecipeChoice.ExactChoice(List.of(drinkableNormal, drinkableLong));

        // Splash Inputs
        ItemStack splashNormal = createBasePotion(Material.SPLASH_POTION, PotionType.INVISIBILITY);
        ItemStack splashLong = createBasePotion(Material.SPLASH_POTION, PotionType.LONG_INVISIBILITY);
        RecipeChoice.ExactChoice splashInput = new RecipeChoice.ExactChoice(List.of(splashNormal, splashLong));

        // Lingering Inputs
        ItemStack lingeringNormal = createBasePotion(Material.LINGERING_POTION, PotionType.INVISIBILITY);
        ItemStack lingeringLong = createBasePotion(Material.LINGERING_POTION, PotionType.LONG_INVISIBILITY);
        RecipeChoice.ExactChoice lingeringInput = new RecipeChoice.ExactChoice(List.of(lingeringNormal, lingeringLong));

        // Register PotionMix for Drinkable Potions
        NamespacedKey drinkableKey = new NamespacedKey(this, "advanced_invis_drinkable");
        ItemStack drinkableResult = BrewListener.createAdvancedInvisPotion(Material.POTION, 180);
        io.papermc.paper.potion.PotionMix drinkableMix = new io.papermc.paper.potion.PotionMix(
                drinkableKey,
                drinkableResult,
                drinkableInput,
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.DIAMOND));
        getServer().getPotionBrewer().addPotionMix(drinkableMix);

        // Register PotionMix for Splash Potions
        NamespacedKey splashKey = new NamespacedKey(this, "advanced_invis_splash");
        ItemStack splashResult = BrewListener.createAdvancedInvisPotion(Material.SPLASH_POTION, 180);
        io.papermc.paper.potion.PotionMix splashMix = new io.papermc.paper.potion.PotionMix(
                splashKey,
                splashResult,
                splashInput,
                new org.bukkit.inventory.RecipeChoice.MaterialChoice(Material.DIAMOND));
        getServer().getPotionBrewer().addPotionMix(splashMix);

        // Register PotionMix for Lingering Potions
        NamespacedKey lingeringKey = new NamespacedKey(this, "advanced_invis_lingering");
        ItemStack lingeringResult = BrewListener.createAdvancedInvisPotion(Material.LINGERING_POTION, 180);
        io.papermc.paper.potion.PotionMix lingeringMix = new io.papermc.paper.potion.PotionMix(
                lingeringKey,
                lingeringResult,
                lingeringInput,
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

            // Preserve formatting by using TextReplacement
            Component newMessage = originalMessage.replaceText(TextReplacementConfig.builder()
                    .matchLiteral(killer.getName())
                    .replacement("unknown")
                    .build());

            event.deathMessage(newMessage);
        }
    }
}
