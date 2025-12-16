package net.thanachot.AdvanceInvis.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.thanachot.AdvanceInvis.AdvanceInvis;
import net.thanachot.AdvanceInvis.manager.InvisibilityManager;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;

public class BrewListener implements Listener {

    public static ItemStack createAdvancedInvisPotion(Material material, int durationSeconds) {
        ItemStack potion = new ItemStack(material);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        // Fix Duplicate Data: Do not add custom effect. Rely on BasePotionType.
        // meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationSeconds * 20, 0), true);

        if (durationSeconds > 180) { // Assume > 3:00 means Long
             meta.setBasePotionType(PotionType.LONG_INVISIBILITY);
        } else {
             meta.setBasePotionType(PotionType.INVISIBILITY);
        }

        String name = "Advance Invisible Potion";
        if (material == Material.SPLASH_POTION) name = "Advance Invisible Splash Potion";
        else if (material == Material.LINGERING_POTION) name = "Advance Invisible Lingering Potion";

        meta.displayName(Component.text(name).color(NamedTextColor.DARK_PURPLE));

        String timeString = String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60);

        meta.lore(List.of(
                Component.text("Masks your identity on kill.").color(NamedTextColor.GRAY),
                Component.text("Duration: " + timeString).color(NamedTextColor.GRAY)
        ));

        // Store duration in PDC (Persistent Data Container)
        meta.getPersistentDataContainer().set(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER, durationSeconds * 20);

        potion.setItemMeta(meta);
        return potion;
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        BrewerInventory inv = event.getContents();
        ItemStack ingredient = inv.getIngredient();

        if (ingredient != null && ingredient.getType() == Material.DIAMOND) {
            List<ItemStack> results = event.getResults();
            for (int i = 0; i < 3; i++) {
                ItemStack potion = inv.getItem(i);
                if (potion == null || potion.getType() == Material.AIR) continue;

                if (potion.getItemMeta() instanceof PotionMeta meta) {
                    // Fix Re-brewing Loop: Check if already advanced
                    if (meta.getPersistentDataContainer().has(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER)) {
                        results.set(i, potion); // Keep original, do not re-brew
                        continue;
                    }

                    if (meta.getBasePotionType() == PotionType.INVISIBILITY || meta.getBasePotionType() == PotionType.LONG_INVISIBILITY) {

                        int duration = 180; // 3:00 default
                        if (meta.getBasePotionType() == PotionType.LONG_INVISIBILITY) {
                            duration = 480; // 8:00 Long
                        }

                        // Inherit type (Drinkable, Splash, Lingering)
                        Material newType = potion.getType();
                        results.set(i, createAdvancedInvisPotion(newType, duration));
                    } else {
                        // Keep original if not an invisibility potion
                        results.set(i, potion);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER)) {
            Integer durationTicks = item.getItemMeta().getPersistentDataContainer().get(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER);
            if (durationTicks != null) {
                InvisibilityManager.getInstance().addInvisiblePlayer(event.getPlayer(), durationTicks);
            }
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof ThrownPotion potion) {
            ItemStack item = potion.getItem();
            if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER)) {
                Integer duration = item.getItemMeta().getPersistentDataContainer().get(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER);
                if (duration != null) {
                    // Pass data from Item to Projectile
                    potion.getPersistentDataContainer().set(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER, duration);
                }
            }
        }
    }

    @EventHandler
    public void onSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        if (potion.getPersistentDataContainer().has(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER)) {
            Integer durationTicks = potion.getPersistentDataContainer().get(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER);
            if (durationTicks != null) {
                for (LivingEntity entity : event.getAffectedEntities()) {
                    if (entity instanceof Player player) {
                        InvisibilityManager.getInstance().addInvisiblePlayer(player, durationTicks);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLingeringSplash(LingeringPotionSplashEvent event) {
        ThrownPotion potion = event.getEntity();
        AreaEffectCloud cloud = event.getAreaEffectCloud();

        if (potion.getPersistentDataContainer().has(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER)) {
            Integer durationTicks = potion.getPersistentDataContainer().get(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER);
            if (durationTicks != null) {
                // Pass data from Projectile to Cloud
                cloud.getPersistentDataContainer().set(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER, durationTicks);
            }
        }
    }

    @EventHandler
    public void onCloudApply(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud cloud = event.getEntity();
        if (cloud.getPersistentDataContainer().has(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER)) {
            Integer durationTicks = cloud.getPersistentDataContainer().get(AdvanceInvis.getADV_INVIS_KEY(), PersistentDataType.INTEGER);
            if (durationTicks != null) {
                // For lingering, the effect is re-applied frequently while standing in it.
                // We should refresh the duration for players.
                for (LivingEntity entity : event.getAffectedEntities()) {
                    if (entity instanceof Player player) {
                        InvisibilityManager.getInstance().addInvisiblePlayer(player, durationTicks);
                    }
                }
            }
        }
    }
}
