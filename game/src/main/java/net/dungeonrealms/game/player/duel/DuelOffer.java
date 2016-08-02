package net.dungeonrealms.game.player.duel;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Chase on Nov 13, 2015
 */
public class DuelOffer {

    public UUID player1;
    public UUID player2;
    public Inventory sharedInventory = null;
    public boolean p1Ready;
    public boolean p2Ready;
    public Item.ItemTier tierArmor = Item.ItemTier.TIER_5;
    public Item.ItemTier tierWeapon = Item.ItemTier.TIER_5;
    public Location centerPoint = null;
    public boolean canFight = false;
    public int timerID = -1;

    public DuelOffer(Player player, Player player2) {
        this.player1 = player.getUniqueId();
        this.player2 = player2.getUniqueId();
        sharedInventory = Bukkit.createInventory(null, 36, player.getName() + "  VS. " + player2.getName());
        openInventory();
    }

    /**
     * Opens Duel Offer Window
     */
    private void openInventory() {
        Bukkit.getPlayer(player1).closeInventory();
        Bukkit.getPlayer(player2).closeInventory();
        ItemStack separator = ItemManager.createItem(Material.BONE, " ", null);
        ItemStack armorTier = ItemManager.createItem(Material.GOLD_CHESTPLATE, "Armor Tier Limit", null);
        ItemStack weaponTier = ItemManager.createItem(Material.GOLD_SWORD, "Weapon Tier Limit", null);
        ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY UP",
                null, DyeColor.GRAY.getDyeData());
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("status", "notready");
        nms.setTag(nbt);
        nms.c(ChatColor.YELLOW + "READY UP");
        sharedInventory.setItem(0, CraftItemStack.asBukkitCopy(nms));
        sharedInventory.setItem(8, CraftItemStack.asBukkitCopy(nms));
        sharedInventory.setItem(4, separator);
        sharedInventory.setItem(13, separator);
        sharedInventory.setItem(22, separator);
        sharedInventory.setItem(27, separator);
        sharedInventory.setItem(28, separator);
        sharedInventory.setItem(29, separator);
        sharedInventory.setItem(31, separator);
        sharedInventory.setItem(33, separator);
        sharedInventory.setItem(34, separator);
        sharedInventory.setItem(35, separator);
        sharedInventory.setItem(4, separator);
        sharedInventory.setItem(30, armorTier);
        sharedInventory.setItem(32, weaponTier);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Bukkit.getPlayer(player1).openInventory(sharedInventory);
            Bukkit.getPlayer(player2).openInventory(sharedInventory);
        }, 20);
    }

    public void endDuel(Player winner, Player loser) {
        canFight = false;
        for (int i = 1; i < sharedInventory.getSize(); i++) {
            if (!isRightSlot(i) && !isLeftSlot(i))
                continue;
            ItemStack current = sharedInventory.getItem(i);
            if (current != null && current.getType() != Material.AIR) {
                winner.getInventory().addItem(current);
            }
        }
        GamePlayer wGP = GameAPI.getGamePlayer(winner);
        GamePlayer lGP = GameAPI.getGamePlayer(loser);
        if (wGP != null) {
            wGP.getPlayerStatistics().setDuelsWon(wGP.getPlayerStatistics().getDuelsWon() + 1);
        }
        if (lGP != null) {
            lGP.getPlayerStatistics().setDuelsLost(lGP.getPlayerStatistics().getDuelsLost() + 1);
        }

        String winnerName = GameChat.getPreMessage(winner).replaceAll(":", "").trim().intern();
        if (ChatColor.stripColor(winnerName).startsWith("<G>")) {
            winnerName = winnerName.split(">")[1];
        }
        String loserName = GameChat.getPreMessage(loser).replaceAll(":", "").trim();
        if (ChatColor.stripColor(loserName).startsWith("<G>")) {
            loserName = loserName.split(">")[1];
        }
        final String finalWinnerName = winnerName;
        final String finalLoserName = loserName;
        GameAPI.getNearbyPlayers(winner.getLocation(), 100).forEach(player1 -> player1.sendMessage(finalWinnerName + ChatColor.GREEN + " has " + ChatColor.UNDERLINE + "DEFEATED" + ChatColor.RESET + " " + finalLoserName + ChatColor.GREEN + " in a duel!"));
        DuelingMechanics.removeOffer(this);
    }

    public Player getPlayer1() {
        return Bukkit.getPlayer(player1);
    }

    public boolean isLeftPlayer(Player p) {
        return p.getUniqueId().toString().equalsIgnoreCase(player1.toString());
    }

    public Player getPlayer2() {
        return Bukkit.getPlayer(player2);
    }

    /**
     * Return items to players
     */
    public void giveBackItems() {
        for (int i = 1; i < sharedInventory.getSize(); i++) {
            ItemStack current = sharedInventory.getItem(i);
            if (current != null && current.getType() != Material.AIR) {
                if (isLeftSlot(i)) {
                    Bukkit.getPlayer(player1).getInventory().addItem(current);
                } else if (isRightSlot(i)) {
                    Bukkit.getPlayer(player2).getInventory().addItem(current);
                }
            }
        }
    }

    /**
     * @param i
     * @return
     */
    private boolean isRightSlot(int i) {
        int[] right = new int[]{23, 24, 25, 26, 5, 6, 7, 14, 15, 16, 17};
        for (int aRight : right)
            if (aRight == i)
                return true;
        return false;
    }

    /**
     * @param slot
     * @return
     */
    public boolean isLeftSlot(int slot) {
        int[] left = new int[]{0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21};
        for (int aLeft : left)
            if (aLeft == slot)
                return true;
        return false;
    }

    /**
     * @param logOut
     */
    public void handleLogOut(Player logOut) {
        Player winner = null;
        if (logOut.getUniqueId().toString().equalsIgnoreCase(player1.toString())) {
            winner = Bukkit.getPlayer(player2);
        } else {
            winner = Bukkit.getPlayer(player1);
        }
        endDuel(winner, logOut);
    }

    public void cycleArmor() {
        Item.ItemTier[] list = Item.ItemTier.values();
        int j = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i] == tierArmor) {
                j = i + 1;
                if (j >= list.length) {
                    j = 0;
                    break;
                }
            }
        }
        tierArmor = list[j];
        sharedInventory.setItem(30, getArmorItem());
    }

    public void cycleItem() {
        Item.ItemTier[] list = Item.ItemTier.values();
        int j = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i] == tierWeapon) {
                j = i + 1;
                if (j >= list.length) {
                    j = 0;
                    break;
                }
            }
        }
        tierWeapon = list[j];
        sharedInventory.setItem(32, getWeaponItem());
    }

    /**
     * @return
     */
    private ItemStack getWeaponItem() {
        switch (tierWeapon) {
            case TIER_1:
                return ItemManager.createItem(Material.WOOD_SWORD, "Weapon Tier Limit", null);
            case TIER_2:
                return ItemManager.createItem(Material.STONE_SWORD, "Weapon Tier Limit", null);
            case TIER_3:
                return ItemManager.createItem(Material.IRON_SWORD, "Weapon Tier Limit", null);
            case TIER_4:
                return ItemManager.createItem(Material.DIAMOND_SWORD, "Weapon Tier Limit", null);
            case TIER_5:
                return ItemManager.createItem(Material.GOLD_SWORD, "Weapon Tier Limit", null);
        }
        return null;
    }

    public void updateOffer() {
        ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "NOT READY",
                null, DyeColor.GRAY.getDyeData());
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("status", "notready");
        nms.setTag(nbt);
        nms.c(ChatColor.YELLOW + "NOT READY");
        ItemStack newItem = CraftItemStack.asBukkitCopy(nms);
        sharedInventory.setItem(0, newItem);
        sharedInventory.setItem(8, newItem);
        p1Ready = false;
        p2Ready = false;

    }

    /**
     * @return
     */
    private ItemStack getArmorItem() {
        switch (tierArmor) {
            case TIER_1:
                return ItemManager.createItem(Material.LEATHER_CHESTPLATE, "Armor Tier Limit", null);
            case TIER_2:
                return ItemManager.createItem(Material.CHAINMAIL_CHESTPLATE, "Armor Tier Limit", null);
            case TIER_3:
                return ItemManager.createItem(Material.IRON_CHESTPLATE, "Armor Tier Limit", null);
            case TIER_4:
                return ItemManager.createItem(Material.DIAMOND_CHESTPLATE, "Armor Tier Limit", null);
            case TIER_5:
                return ItemManager.createItem(Material.GOLD_CHESTPLATE, "Armor Tier Limit", null);
        }
        return null;
    }

    /**
     *
     */
    public void checkReady() {
        if (p1Ready && p2Ready) {
            Bukkit.getPlayer(player1).closeInventory();
            Bukkit.getPlayer(player2).closeInventory();

            startFight();
        }
    }

    /**
     *
     */
    private void startFight() {
        centerPoint = this.getPlayer1().getLocation();
        this.getPlayer2().teleport(centerPoint);
        this.getPlayer1().sendMessage(ChatColor.YELLOW + "Battle begins in 10 seconds!");
        this.getPlayer2().sendMessage(ChatColor.YELLOW + "Battle begins in 10 seconds!");
        timerID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), this::checkArmorAndWeapon, 0, 10);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            canFight = true;
            this.getPlayer1().sendMessage(ChatColor.YELLOW + "Fight!");
            this.getPlayer2().sendMessage(ChatColor.YELLOW + "Fight!");
        }, 100);
    }

    /**
     * @param uniqueId
     */
    public void updateReady(UUID uniqueId) {
        if (player1.toString().equalsIgnoreCase(uniqueId.toString())) {
            p1Ready = !p1Ready;
        } else {
            p2Ready = !p2Ready;
        }
    }

    /**
     *
     */
    public void checkArmorAndWeapon() {
        Player pl = this.getPlayer1();
        if (pl != null) {
            int max_armor_tier = tierArmor.getTierId();
            ItemStack helmet = pl.getInventory().getHelmet();
            ItemStack chest = pl.getInventory().getChestplate();
            ItemStack legs = pl.getInventory().getLeggings();
            ItemStack boots = pl.getInventory().getBoots();

            if (helmet != null && helmet.getType() != Material.AIR) {
                if ((RepairAPI.getArmorOrWeaponTier(helmet) > max_armor_tier)) {
                    pl.sendMessage(ChatColor.RED + "Unequiped Illegal Helmet");
                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), helmet);
                    pl.getInventory().setHelmet(new ItemStack(Material.AIR));
                }
            }

            if (chest != null && chest.getType() != Material.AIR) {
                if ((RepairAPI.getArmorOrWeaponTier(chest) > max_armor_tier)) {
                    pl.sendMessage(ChatColor.RED + "Unequiped Illegal Chestplate");
                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), chest);
                    pl.getInventory().setChestplate(new ItemStack(Material.AIR));
                }
            }

            if (legs != null && legs.getType() != Material.AIR) {
                if ((RepairAPI.getArmorOrWeaponTier(legs) > max_armor_tier)) {
                    pl.sendMessage(ChatColor.RED + "Unequiped Illegal Leggings");
                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), legs);
                    pl.getInventory().setLeggings(new ItemStack(Material.AIR));
                }
            }

            if (boots != null && boots.getType() != Material.AIR) {
                if ((RepairAPI.getArmorOrWeaponTier(boots) > max_armor_tier)) {
                    pl.sendMessage(ChatColor.RED + "Unequiped Illegal Boots");
                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), boots);
                    pl.getInventory().setBoots(new ItemStack(Material.AIR));
                }
            }
            if (RepairAPI.isItemArmorOrWeapon(pl.getEquipment().getItemInMainHand()))
                if (pl.getEquipment().getItemInMainHand() != null && pl.getEquipment().getItemInMainHand().getType() != Material.AIR) {
                    int tier = RepairAPI.getArmorOrWeaponTier(pl.getEquipment().getItemInMainHand());
                    if (RepairAPI.getArmorOrWeaponTier(pl.getEquipment().getItemInMainHand()) != 0)
                        if (tier > tierWeapon.getTierId()) {
                            pl.sendMessage(ChatColor.RED + "Unequiped Illegal Weapon");
                            ItemStack stack = pl.getEquipment().getItemInMainHand();
                            pl.getInventory().setItem(pl.getInventory().firstEmpty(), stack);
                            pl.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                        }
                }

            HealthHandler.getInstance().setPlayerMaxHPLive(pl, HealthHandler.getInstance().calculateMaxHPFromItems(pl));
            HealthHandler.getInstance().setPlayerHPRegenLive(pl, GameAPI.getStaticAttributeVal(Item.ArmorAttributeType
                    .HEALTH_REGEN, pl) + 5);
            if (HealthHandler.getInstance().getPlayerHPLive(pl) > HealthHandler.getInstance().getPlayerMaxHPLive(pl)) {
                HealthHandler.getInstance().setPlayerHPLive(pl, HealthHandler.getInstance().getPlayerMaxHPLive(pl));
            }

            pl = this.getPlayer2();

            helmet = pl.getInventory().getHelmet();
            chest = pl.getInventory().getChestplate();
            legs = pl.getInventory().getLeggings();
            boots = pl.getInventory().getBoots();

            if (helmet != null && helmet.getType() != Material.AIR) {
                if ((RepairAPI.getArmorOrWeaponTier(helmet) > max_armor_tier)) {
                    pl.sendMessage(ChatColor.RED + "Unequiped Illegal Helmet");
                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), helmet);
                    pl.getInventory().setHelmet(new ItemStack(Material.AIR));
                }
            }

            if (chest != null && chest.getType() != Material.AIR) {
                if ((RepairAPI.getArmorOrWeaponTier(chest) > max_armor_tier)) {
                    pl.sendMessage(ChatColor.RED + "Unequiped Illegal Chestplate");
                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), chest);
                    pl.getInventory().setChestplate(new ItemStack(Material.AIR));
                }
            }

            if (legs != null && legs.getType() != Material.AIR) {
                if ((RepairAPI.getArmorOrWeaponTier(legs) > max_armor_tier)) {
                    pl.sendMessage(ChatColor.RED + "Unequiped Illegal Leggings");
                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), legs);
                    pl.getInventory().setLeggings(new ItemStack(Material.AIR));
                }
            }

            if (boots != null && boots.getType() != Material.AIR) {
                if ((RepairAPI.getArmorOrWeaponTier(boots) > max_armor_tier)) {
                    pl.sendMessage(ChatColor.RED + "Unequiped Illegal Boots");
                    pl.getInventory().setItem(pl.getInventory().firstEmpty(), boots);
                    pl.getInventory().setBoots(new ItemStack(Material.AIR));
                }
            }
            if (RepairAPI.isItemArmorOrWeapon(pl.getEquipment().getItemInMainHand()))
                if (pl.getEquipment().getItemInMainHand() != null && pl.getEquipment().getItemInMainHand().getType() != Material.AIR) {
                    if (RepairAPI.getArmorOrWeaponTier(pl.getEquipment().getItemInMainHand()) != 0)
                        if (RepairAPI.getArmorOrWeaponTier(pl.getEquipment().getItemInMainHand()) > tierWeapon.getTierId()) {
                            pl.sendMessage(ChatColor.RED + "Unequiped Illegal Weapon");
                            ItemStack stack = pl.getEquipment().getItemInMainHand();
                            pl.getInventory().setItem(pl.getInventory().firstEmpty(), stack);
                            pl.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                        }
                }

            HealthHandler.getInstance().setPlayerMaxHPLive(pl, HealthHandler.getInstance().calculateMaxHPFromItems(pl));
            HealthHandler.getInstance().setPlayerHPRegenLive(pl, GameAPI.getStaticAttributeVal(Item.ArmorAttributeType
                    .HEALTH_REGEN, pl) + 5);
            if (HealthHandler.getInstance().getPlayerHPLive(pl) > HealthHandler.getInstance().getPlayerMaxHPLive(pl)) {
                HealthHandler.getInstance().setPlayerHPLive(pl, HealthHandler.getInstance().getPlayerMaxHPLive(pl));
            }

        }
    }
}
