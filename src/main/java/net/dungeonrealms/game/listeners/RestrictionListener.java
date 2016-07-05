package net.dungeonrealms.game.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 16-Jun-16.
 */
public class RestrictionListener implements Listener {

    public static int getLevelToUseTier(int tier) {
        switch (tier) {
            case 1:
                return 1;
            case 2:
                return 10;
            case 3:
                return 20;
            case 4:
                return 30;
            case 5:
                return 40;
        }
        return 1;
    }

    public static boolean canPlayerUseTier(Player p, int tier) {
        if (API.getGamePlayer(p) == null) return true;
        int level = API.getGamePlayer(p).getLevel();
        return tier == 1 || tier == 2 && level >= 10 || tier == 3 && level >= 20 || tier == 4 && level >= 30 || tier == 5 && level >= 40;
    }

    public void checkPlayersArmorIsValid(Player p) {
        boolean hadIllegalArmor = false;
        for (ItemStack is : p.getInventory().getArmorContents()) {
            if (is == null || is.getType() == Material.AIR || is.getType() == Material.SKULL_ITEM)
                continue;
            if (RepairAPI.getArmorOrWeaponTier(is) == 0) {
                continue;
            }
            if (!p.isOnline()) return;
            if (!canPlayerUseTier(p, RepairAPI.getArmorOrWeaponTier(is))) {
                hadIllegalArmor = true;
                if (p.getInventory().firstEmpty() == -1) {
                    // No space for the armor
                    p.getWorld().dropItem(p.getLocation(), is);
                } else {
                    p.getInventory().addItem(is);
                }
                if (Item.isItemType(Item.ItemType.HELMET, is.getType())) {
                    p.getInventory().setHelmet(new ItemStack(Material.AIR));
                }
                if (Item.isItemType(Item.ItemType.CHESTPLATE, is.getType())) {
                    p.getInventory().setChestplate(new ItemStack(Material.AIR));
                }
                if (Item.isItemType(Item.ItemType.LEGGINGS, is.getType())) {
                    p.getInventory().setLeggings(new ItemStack(Material.AIR));
                }
                if (Item.isItemType(Item.ItemType.BOOTS, is.getType())) {
                    p.getInventory().setBoots(new ItemStack(Material.AIR));
                }
            }
        }
        if (hadIllegalArmor) {
            p.updateInventory();
            HealthHandler.getInstance().setPlayerMaxHPLive(p, API.getStaticAttributeVal(Item.ArmorAttributeType.HEALTH_POINTS, p) + 50);
            HealthHandler.getInstance().setPlayerHPRegenLive(p, API.getStaticAttributeVal(Item.ArmorAttributeType.HEALTH_REGEN, p) + 5);
            if (HealthHandler.getInstance().getPlayerHPLive(p) > HealthHandler.getInstance().getPlayerMaxHPLive(p)) {
                HealthHandler.getInstance().setPlayerHPLive(p, HealthHandler.getInstance().getPlayerMaxHPLive(p));
            }
            p.sendMessage(ChatColor.RED + "You were found with armor that is not wearable at your level.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        checkPlayersArmorIsValid((Player) event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        checkPlayersArmorIsValid((Player) event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (player.getEquipment().getItemInMainHand() != null) {
                if (API.isWeapon(player.getEquipment().getItemInMainHand())) {
                    if (!canPlayerUseTier(player, RepairAPI.getArmorOrWeaponTier(player.getEquipment().getItemInMainHand()))) {
                        player.sendMessage(ChatColor.RED + "You must to be " + ChatColor.UNDERLINE + "at least" + ChatColor.RED + " level "
                                + getLevelToUseTier(RepairAPI.getArmorOrWeaponTier(player.getEquipment().getItemInMainHand())) + " to use this weapon.");
                        event.setCancelled(true);
                        event.setDamage(0);
                        EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), 1F);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType() == Material.FIRE) {
                event.setCancelled(true);
                event.getClickedBlock().setType(Material.FIRE);
                return;
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.hasBlock() && event.getClickedBlock().getType() == Material.CAKE_BLOCK) {
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinEventDelayed(PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            if (event.getPlayer() != null && event.getPlayer().isOnline()) {
                checkPlayersArmorIsValid(event.getPlayer());
            }
        }, 150L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerOpenEmptyMap(PlayerInteractEvent event) {
        if (event.hasItem() && event.getItem().getType() == Material.EMPTY_MAP) {
            Player player = event.getPlayer();
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            event.setUseInteractedBlock(Event.Result.DENY);
            player.sendMessage(ChatColor.RED + "To use a " + ChatColor.BOLD + "SCROLL" + ChatColor.RED + ", simply drag it on-top of the piece of equipment you wish to apply it to in your inventory.");
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void loggingOutOpenInventory(InventoryOpenEvent event) {
        if (DungeonRealms.getInstance().getLoggingOut().contains(event.getPlayer().getName())) {
            event.setCancelled(true);
            event.getPlayer().closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void loggingOutDropItem(PlayerDropItemEvent event) {
        if (DungeonRealms.getInstance().getLoggingOut().contains(event.getPlayer().getName())) {
            event.setCancelled(true);
            event.getPlayer().closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void loggingOutPickupItem(PlayerPickupItemEvent event) {
        if (DungeonRealms.getInstance().getLoggingOut().contains(event.getPlayer().getName())) {
            event.setCancelled(true);
            event.getPlayer().closeInventory();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player pl = event.getPlayer();
        Location from = event.getFrom();
        if (API.getRegionName(from).equalsIgnoreCase("tutorial_island")) {
            if (!Achievements.getInstance().hasAchievement(pl.getUniqueId(), Achievements.EnumAchievements.CYRENNICA)) {
                Location to = event.getTo();
                if (!API.getRegionName(to).equalsIgnoreCase("tutorial_island") && !API.getRegionName(to).equalsIgnoreCase("cityofcyrennica")) {
                    event.setCancelled(true);
                    pl.teleport(from);
                    pl.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "must" + ChatColor.RED + " either finish the tutorial or skip it with /skip to get off tutorial island.");
                }
            }
        }
    }

    @EventHandler
    public void onEntityTargetUntargettablePlayer(EntityTargetLivingEntityEvent event) {
        if (!API.isPlayer(event.getTarget())) return;
        GamePlayer gamePlayer = API.getGamePlayer((Player) event.getTarget());
        if (gamePlayer == null) return;
        if (gamePlayer.isTargettable()) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInvulnerablePlayerDamage(EntityDamageEvent event) {
        if (!API.isPlayer(event.getEntity())) return;
        if (!API.getGamePlayer((Player) event.getEntity()).isInvulnerable()) return;

        event.setDamage(0);
        event.setCancelled(true);
    }

    //TODO: Prevent players entering realms
}
