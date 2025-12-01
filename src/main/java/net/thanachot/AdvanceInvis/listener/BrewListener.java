package net.thanachot.AdvanceInvis.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.thanachot.AdvanceInvis.AdvanceInvis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

import java.util.List;

public class BrewListener implements Listener {

    @EventHandler
    public void onBrew(BrewEvent event) {
        BrewerInventory inv = event.getContents();
        ItemStack ingredient = inv.getIngredient();

        // Recipe: Phantom Membrane + Potion of Invisibility = Advanced Invis
        if (ingredient != null && ingredient.getType() == Material.PHANTOM_MEMBRANE) {

            List<ItemStack> results = event.getResults();
            for (int i = 0; i < 3; i++) {
                ItemStack potion = inv.getItem(i);
                if (potion == null || potion.getType() == Material.AIR)
                    continue;

                if (potion.getItemMeta() instanceof PotionMeta) {
                    PotionMeta meta = (PotionMeta) potion.getItemMeta();
                    // Check if the source was a standard Potion of Invisibility (Long or Normal)
                    if (meta.getBasePotionType() == PotionType.INVISIBILITY
                            || meta.getBasePotionType() == PotionType.LONG_INVISIBILITY) {
                        boolean isSplash = potion.getType() == Material.SPLASH_POTION;
                        // Ensure the result is our custom potion
                        results.set(i, createAdvancedInvisPotion(isSplash));
                    } else {
                        // If the input was NOT invisibility, but the recipe triggered (due to broad
                        // PotionMix),
                        // we must revert the result to the original item so it doesn't change.
                        results.set(i, potion);
                    }
                }
            }
        }
    }

    public static ItemStack createAdvancedInvisPotion(boolean isSplash) {
        ItemStack potion = new ItemStack(isSplash ? Material.SPLASH_POTION : Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        // Base visual effect (standard invisibility)
        meta.setBasePotionType(PotionType.INVISIBILITY);

        // Custom Name and Lore
        meta.displayName(Component.text("Advanced Invisibility").color(NamedTextColor.DARK_PURPLE));
        meta.lore(List.of(
                Component.text("Masks your identity on kill.").color(NamedTextColor.GRAY),
                Component.text("Duration: 3:00").color(NamedTextColor.GRAY)));

        // Tag the item with persistent data so we know it's ours
        meta.getPersistentDataContainer().set(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.BYTE, (byte) 1);

        potion.setItemMeta(meta);
        return potion;
    }
}
