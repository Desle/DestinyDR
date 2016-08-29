package net.dungeonrealms.game.handler;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Created by Kieran on 9/24/2015.
 */
public class EnergyHandler implements GenericMechanic {

    private static EnergyHandler instance = null;

    public static EnergyHandler getInstance() {
        if (instance == null) {
            instance = new EnergyHandler();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
	public void startInitialization() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::regenerateAllPlayerEnergy, 40, 1L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::removePlayerEnergySprint, 40, 10L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::addStarvingPotionEffect, 40, 15L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::regenerateFoodInSafezones, 40, 40L);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Handles players logging out,
     * removes metadata from the player
     *
     * @param player
     * @since 1.0
     */
    public void handleLogoutEvents(Player player) {
        if (player.hasMetadata("starving")) {
            player.removeMetadata("starving", DungeonRealms.getInstance());
        }
        if (player.hasMetadata("sprinting")) {
            player.removeMetadata("sprinting", DungeonRealms.getInstance());
        }
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_FOOD, player.getFoodLevel(), true);
    }

    /**
     * Handles players logging in,
     * adds metadata to the player if
     * applicable (no food level).
     *
     * @param player
     * @since 1.0
     */
    public void handleLoginEvents(Player player) {
        int foodLevel = Integer.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_FOOD, player.getUniqueId())));
        if (foodLevel < 0) {
            foodLevel = 0;
        }
        player.setFoodLevel(foodLevel);
        if (foodLevel <= 0) {
            if (Rank.isGM(player) || player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            if (!(player.hasMetadata("starving"))) {
                player.setMetadata("starving", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "**STARVING**"), 20L);
            }
        }
    }

    private void regenerateFoodInSafezones() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!GameAPI.isInSafeRegion(player.getLocation())) {
                continue;
            }
            if (player.getFoodLevel() >= 20) {
                continue;
            }
            player.setFoodLevel(player.getFoodLevel() + 8);
        }
    }

    /**
     * Handles the regeneration of energy
     * for all players applicable on the
     * server.
     *
     * @since 1.0
     */
    private void regenerateAllPlayerEnergy() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!GameAPI.isPlayer(player)) {
                continue;
            }
            GamePlayer gp = GameAPI.getGamePlayer(player);
            if (gp == null || !gp.isAttributesLoaded()) {
                continue; // player data not yet loaded
            }
            if (getPlayerCurrentEnergy(player) == 1.0F) {
                continue;
            }
            if (getPlayerCurrentEnergy(player) > 1.0F) {
                player.setExp(1.0F);
                updatePlayerEnergyBar(player);
                continue;
            }
            // get regenAmount, 10% base energy regen (calculated here because it's hidden)
            float regenAmount = (((float) GameAPI.getStaticAttributeVal(Item.ArmorAttributeType.ENERGY_REGEN, player)) / 100.0F) + 0.10F;
            if (!(player.hasPotionEffect(PotionEffectType.SLOW_DIGGING))) {
                if (player.hasMetadata("starving")) {
                    regenAmount = 0.05F;
                }
                regenAmount = regenAmount / 18.9F;
                if (gp == null || gp.getStats() == null) return;
                regenAmount += (int) (regenAmount * gp.getStats().getEnergyRegen());
                addEnergyToPlayerAndUpdate(player, regenAmount);
            }
        }
    }

    /**
     * Returns the players current
     * energy value
     *
     * @param player
     * @return float
     * @since 1.0
     */
    public static float getPlayerCurrentEnergy(Player player) {
        return player.getExp();
    }

    /**
     * Updates the players Minecraft EXP bar
     * with our custom energy values
     *
     * @param player
     * @since 1.0
     */
    private static void updatePlayerEnergyBar(Player player) {
        float currExp = getPlayerCurrentEnergy(player);
        double percent = currExp * 100.00D;
        if (percent > 100) {
            percent = 100;
        }
        if (percent < 0) {
            percent = 0;
        }
        player.setLevel(((int) percent));
    }

    /**
     * Adds energy to the defined player
     *
     * @param player
     * @param amountToAdd
     * @since 1.0
     */
    private static void addEnergyToPlayerAndUpdate(Player player, float amountToAdd) {
        if (getPlayerCurrentEnergy(player) == 1) {
            return;
        }
        player.setExp(player.getExp() + amountToAdd);
        updatePlayerEnergyBar(player);
    }

    /**
     * Handles the removal of energy while
     * players are sprinting
     *
     * @return float
     * @since 1.0
     */
    private void removePlayerEnergySprint() {
        Bukkit.getOnlinePlayers().stream().filter(Player::isSprinting).forEach(player -> {
            removeEnergyFromPlayerAndUpdate(player.getUniqueId(), 0.15F);
            if (getPlayerCurrentEnergy(player) <= 0 || player.hasMetadata("starving")) {
                player.setSprinting(false);
                player.removeMetadata("sprinting", DungeonRealms.getInstance());
                if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
                    int foodLevel = player.getFoodLevel();
                    if (player.getFoodLevel() > 1) {
                        player.setFoodLevel(1);
                    }
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "**EXHAUSTED**");
                    if (foodLevel > 1) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.setFoodLevel(foodLevel), 40L);
                    }
                }
            }
        });
    }

    /**
     * Handles the removal of energy from
     * a player and updates their bar.
     * Used when auto-attacking etc.
     *
     * @param uuid
     * @param amountToRemove
     * @since 1.0
     */
    public static void removeEnergyFromPlayerAndUpdate(UUID uuid, float amountToRemove) {
        Player player = Bukkit.getPlayer(uuid);
        if (Rank.isGM(player)) {
            GamePlayer gp = GameAPI.getGamePlayer(player);
            // check if they have allow fight on
            if (gp != null && gp.isInvulnerable() && !gp.isTargettable()) return;
        }
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (GameAPI.isInSafeRegion(player.getLocation())) return;
        if (player.hasMetadata("last_energy_remove")) {
            if ((System.currentTimeMillis() - player.getMetadata("last_energy_remove").get(0).asLong()) < 80) {
                return;
            }
        }
        player.setMetadata("last_energy_remove", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        if (getPlayerCurrentEnergy(player) <= 0) return;
        if ((getPlayerCurrentEnergy(player) - amountToRemove) <= 0) {
            player.setExp(0.0F);
            updatePlayerEnergyBar(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 50, 4)), 0L);
            return;
        }
        player.setExp(getPlayerCurrentEnergy(player) - amountToRemove);
        updatePlayerEnergyBar(player);
    }

    /**
     * Adds the hunger potion effect
     * to a player and "starving" as
     * metadata when they have 0 food level.
     *
     * @since 1.0
     */
    private void addStarvingPotionEffect() {
        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPotionEffect(PotionEffectType.HUNGER) && player.hasMetadata("starving")).forEach(player -> {
            if (player.getFoodLevel() <= 0) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0)), 0L);
            } else {
                player.removeMetadata("starving", DungeonRealms.getInstance());
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.removePotionEffect(PotionEffectType.HUNGER), 0L);
            }
        });
    }

    /**
     * Returns the energy cost
     * of an item
     *
     * @param itemStack
     * @return float
     * @since 1.0
     */
    public static float getWeaponSwingEnergyCost(ItemStack itemStack) {
        Material material = itemStack.getType();
        switch (material) {
            case AIR:
                return 0.05f;
            case WOOD_SWORD:
                return 0.06f;
            case STONE_SWORD:
                return 0.071f;
            case IRON_SWORD:
                return 0.0833f;
            case DIAMOND_SWORD:
                return 0.125f;
            case GOLD_SWORD:
                return 0.135f;
            case WOOD_AXE:
                return 0.0721F * 1.1F;
            case STONE_AXE:
                return 0.0833F * 1.1F;
            case IRON_AXE:
                return 0.10F * 1.1F;
            case DIAMOND_AXE:
                return 0.125F * 1.1F;
            case GOLD_AXE:
                return 0.135F * 1.1F;
            case WOOD_SPADE:
                return 0.0721F;
            case STONE_SPADE:
                return 0.0833F;
            case IRON_SPADE:
                return 0.10F;
            case DIAMOND_SPADE:
                return 0.125F;
            case GOLD_SPADE:
                return 0.135F;
            case WOOD_HOE:
                return 0.10F / 1.1F;
            case STONE_HOE:
                return 0.12F / 1.1F;
            case IRON_HOE:
                return 0.13F / 1.1F;
            case DIAMOND_HOE:
                return 0.14F / 1.1F;
            case GOLD_HOE:
                return 0.15F / 1.1F;
            case BOW:
                switch (RepairAPI.getArmorOrWeaponTier(itemStack)) {
                    case 1:
                        return 0.125F;
                    case 2:
                        return 0.145F;
                    case 3:
                        return 0.165F;
                    case 4:
                        return 0.185F;
                    case 5:
                        return 0.205F;
                }
        }
        return 0.10F;
    }
}
