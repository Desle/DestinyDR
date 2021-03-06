package net.dungeonrealms.game.handler;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ItemUtilityWeapon;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Kieran on 9/24/2015.
 */
public class EnergyHandler implements GenericMechanic {

    @Getter
    private static EnergyHandler instance = new EnergyHandler();

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
     * Handles players logging in,
     * adds metadata to the player if
     * applicable (no food level).
     *
     * @param player
     * @since 1.0
     */
    public void handleLoginEvents(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;
        int foodLevel = wrapper.getStoredFoodLevel();
        if (foodLevel < 0) {
            foodLevel = 0;
        }
        player.setFoodLevel(foodLevel);
        if (foodLevel <= 0) {
            if (Rank.isTrialGM(player) || player.getGameMode() == GameMode.CREATIVE) {
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
            if (!GameAPI.isPlayer(player) || !player.isOnline()) {
                continue;
            }
            PlayerWrapper playerWrapper = PlayerWrapper.getPlayerWrapper(player);
            if (playerWrapper == null || !playerWrapper.isAttributesLoaded() || playerWrapper.getAttributes() == null) {
                continue; // player data not yet loaded
            }

//            if(GameAPI.isCooldown(player, MetadataUtils.Metadata.REGEN_ABILITY))continue;

            if (getPlayerCurrentEnergy(player) == 1.0F) {
                continue;
            }
            if (getPlayerCurrentEnergy(player) > 1.0F) {
                player.setExp(1.0F);
                updatePlayerEnergyBar(player);
                continue;
            }
            // get regenAmount, 10% base energy regen (calculated here because it's hidden)

            ModifierRange range = playerWrapper.getAttributes().getAttribute(ArmorAttributeType.ENERGY_REGEN);
            if(range == null)return;
            float regenAmount = (float) range.getValue() / 100.0F + 0.20F;
            if (!(player.hasPotionEffect(PotionEffectType.SLOW_DIGGING))) {
                if (player.hasMetadata("starving")) {
                    regenAmount = 0.05F;
                }
                regenAmount = regenAmount / 18.9F;
                if (playerWrapper.getPlayerStats() == null) return;

                if(GameAPI.isCooldown(player, MetadataUtils.Metadata.REGEN_ABILITY)){
                    regenAmount = (float) (regenAmount * .25D);
                }
//                regenAmount = (float)(regenAmount * (1 + (playerWrapper.getAttributes().getAttribute(ArmorAttributeType.INTELLECT).getValue() * 0.00015)));
                //regenAmount += (int) (regenAmount * playerWrapper.getPlayerStats().getEnergyRegen());
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
    public static void updatePlayerEnergyBar(Player player) {
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
            removeEnergyFromPlayerAndUpdate(player, 0.15F);
            if (getPlayerCurrentEnergy(player) <= 0 || player.hasMetadata("starving")) {
                player.setSprinting(false);
//                Utils.stopSprint(player);
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
     * @param amountToRemove
     * @since 1.0
     */
    public static void removeEnergyFromPlayerAndUpdate(Player player, float amountToRemove, boolean duel) {
        if (!PlayerWrapper.getWrapper(player).isVulnerable())
            return;

        if (GameAPI.isInSafeRegion(player.getLocation()) && !duel)
            return;

        if (getPlayerCurrentEnergy(player) <= 0) {
            return;
        }
        if (getPlayerCurrentEnergy(player) - amountToRemove <= 0) {
            player.setExp(0.0F);
            updatePlayerEnergyBar(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 50, 4)), 0L);
            return;
        }
        player.setExp(getPlayerCurrentEnergy(player) - amountToRemove);
        updatePlayerEnergyBar(player);
    }

    public static void removeEnergyFromPlayerAndUpdate(Player player, float amountToRemove) {
        removeEnergyFromPlayerAndUpdate(player, amountToRemove, false);
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
     * Calaculate energy swing cost when hitting in the air
     *
     * @param itemStack The itemstack
     * @return Calculated float
     */
    public static float handleAirSwingItem(ItemStack itemStack) {
        switch (itemStack.getType()) {
            case AIR:
                return 0.025f;
            case WOOD_SWORD:
                return 0.03f;
            case STONE_SWORD:
                return 0.0035f;
            case IRON_SWORD:
                return 0.00825f;
            case DIAMOND_SWORD:
                return 0.0625f;
            case GOLD_SWORD:
                return 0.0675f;
            case WOOD_AXE:
                return 0.0360F * 1.1F;
            case STONE_AXE:
                return 0.0416F * 1.1F;
            case IRON_AXE:
                return 0.05F * 1.1F;
            case DIAMOND_AXE:
                return 0.0625F * 1.1F;
            case GOLD_AXE:
                return 0.0675F * 1.1F;
            case WOOD_SPADE:
                return 0.0360F;
            case STONE_SPADE:
                return 0.0415F;
            case IRON_SPADE:
                return 0.05F;
            case DIAMOND_SPADE:
                return 0.0625F;
            case GOLD_SPADE:
                return 0.0675F;
            case WOOD_HOE:
                return 0.05F / 1.1F;
            case STONE_HOE:
                return 0.06F / 1.1F;
            case IRON_HOE:
                return 0.065F / 1.1F;
            case DIAMOND_HOE:
                return 0.07F / 1.1F;
            case GOLD_HOE:
                return 0.075F / 1.1F;
            case BOW:
                ItemGear gear = (ItemGear) PersistentItem.constructItem(itemStack);
                return 0.0525F + (gear.getTier().getId() * 0.01F);
            default:
                return 0.10F;
        }
    }

    public static float getSwingCost(ItemStack item) {
        return getWeaponSwingEnergyCost(item);
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
        switch (itemStack.getType()) {
            case AIR:
                return 0.05f;
            case WOOD_SWORD:
                return 0.06F;
            case STONE_SWORD:
                return 0.075F;
            case IRON_SWORD:
                return 0.1000F;
            case DIAMOND_SWORD:
                return 0.127F;
            case GOLD_SWORD:
                return 0.137F;

            case WOOD_AXE:
                return 0.09373F;
            case STONE_AXE:
                return 0.09996F;
            case IRON_AXE:
                return 0.13F;
            case DIAMOND_AXE:
                return 0.15F;
            case GOLD_AXE:
                return 0.162F;

            /* OLD DR VALUES
          		if(m == Material.WOOD_SWORD) { return 0.06F; }
		if(m == Material.STONE_SWORD) { return 0.071F; }
		if(m == Material.IRON_SWORD) { return 0.0833F; }
		if(m == Material.DIAMOND_SWORD) { return 0.125F; }
		if(m == Material.GOLD_SWORD) { return 0.135F; }

		if(m == Material.WOOD_AXE) { return 0.0721F * 1.1F; } //.07931
		if(m == Material.STONE_AXE) { return 0.0833F * 1.1F; } //.09163
		if(m == Material.IRON_AXE) { return 0.10F * 1.1F; } //.11
		if(m == Material.DIAMOND_AXE) { return 0.125F * 1.1F; } //.1375
		if(m == Material.GOLD_AXE) { return 0.135F * 1.1F; } //.1485

		if(m == Material.WOOD_SPADE) { return 0.0721F; }
		if(m == Material.STONE_SPADE) { return 0.0833F; }
		if(m == Material.IRON_SPADE) { return 0.10F; }
		if(m == Material.DIAMOND_SPADE) { return 0.125F; }
		if(m == Material.GOLD_SPADE) { return 0.135F; }
            */

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
                return 0.09090909F;
            case STONE_HOE:
                return 0.10909091F;
            case IRON_HOE:
                return 0.11818182F;
            case DIAMOND_HOE:
                return 0.12727273F;
            case GOLD_HOE:
                return 0.13636364F;
            case BOW:
                ItemGear gear = (ItemGear) PersistentItem.constructItem(itemStack);
                if(ItemUtilityWeapon.isUtilityWeaponRanged(itemStack)){
                    return 0.5F;
                } else
                return 0.105F + gear.getTier().getId() * 0.02F;
            default:
                return 0.10F;
        }
    }
}
