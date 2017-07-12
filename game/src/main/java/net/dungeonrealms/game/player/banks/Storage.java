package net.dungeonrealms.game.player.banks;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.ItemSerialization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by Chase on Sep 25, 2015
 */
public class Storage {

    public UUID ownerUUID;
    @Getter
    private int characterID;

    private int cachedSize;
    public Inventory inv;
    public Inventory collection_bin = null;

    public Storage(UUID owner, int accountID, int cachedSize) {
        ownerUUID = owner;
        this.cachedSize = cachedSize;
        inv = getNewStorage();
        this.characterID = accountID;
    }

    /**
     * @param uuid
     * @param inventory
     */
    public Storage(UUID uuid, Inventory inventory, int characterID, int level) {
        ownerUUID = uuid;
        this.cachedSize = level;
        this.characterID = characterID;
        this.inv = getNewStorage();

        for (int i = 0; i < this.inv.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                inv.setItem(i, item);
            }
        }
        //Loading auto on create? Why?
//        update();
    }

    public void clearCollectionBin() {
        if (collection_bin == null)
            return;

        if (this.characterID != 0)
            SQLDatabaseAPI.getInstance().getSqlQueries().add("UPDATE characters SET collection_storage = null WHERE character_id = '" + this.characterID + "'");

        //        DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true, true);
        //VV Clears the current inventory so any viewers don't get to take it.
        collection_bin.clear();
        collection_bin = null;
    }

    /**
     * @return
     */
    private Inventory getNewStorage() {
//        Player p = Bukkit.getPlayer(ownerUUID);
        int size = getStorageSize();
        return Bukkit.createInventory(null, size, "Storage Chest");
    }

    /**
     * @param p
     * @return
     */
    private int getStorageSize() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(ownerUUID);
        if (wrapper == null) {
            Bukkit.getLogger().info("Player Wrapper null for " + ownerUUID + " Returning  " + cachedSize + " on Bank Size...");
            return Math.max(9, cachedSize * 9);
        }
        return 9 * wrapper.getBankLevel();
    }

    private PlayerWrapper getPlayerWrapper() {
        return PlayerWrapper.getPlayerWrapper(this.ownerUUID);
    }

    /**
     * Used to update inventory size when upgraded.
     */
    public void update(boolean storage, boolean async, Consumer<Inventory> callback) {
        if (storage) {
            Inventory inventory = getNewStorage();
            if (inv != null)
                inventory.setContents(inv.getContents());
            this.inv = inventory;
        }
        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_COLLECTION_BIN.getQuery(this.characterID), rs -> {
            try {
                if (rs.first()) {
                    String newBin = rs.getString("characters.collection_storage");
                    if (newBin != null && newBin.length() > 1 && !newBin.equals("null")) {
                        //We have some collection bin data..
                        Inventory inv = ItemSerialization.fromString(newBin);
                        Bukkit.getLogger().info("Loading " + newBin + " for " + this.characterID);
                        if (!async) {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                Player p = Bukkit.getPlayer(ownerUUID);
                                if (p != null)
                                    p.sendMessage(ChatColor.RED + "You have items in your collection bin!");

                                updateBin(inv, callback);
                            });
                        } else {
                            updateBin(inv, callback);
                        }
                        return;
                    } else {
                        this.collection_bin = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (callback != null)
                callback.accept(this.collection_bin);
        });
    }

    public void updateBin(Inventory newBin, Consumer<Inventory> callback) {
        //Clear old bin?
        if (this.collection_bin != null) {
            this.collection_bin.clear();
            //Close thier views..
            Lists.newArrayList(this.collection_bin.getViewers()).forEach(HumanEntity::closeInventory);
        }

        this.collection_bin = newBin;
        SQLDatabaseAPI.getInstance().addQuery(QueryType.UPDATE_COLLECTION_BIN, "", this.characterID);
        if (callback != null)
            callback.accept(this.collection_bin);
    }

    public boolean hasSpace() {
        for (ItemStack stack : inv.getContents())
            if (stack == null || stack.getType() == Material.AIR)
                return true;
        return false;
    }

    public void upgrade() {
        ItemStack[] bankItems = inv.getContents();
        inv = getNewStorage();
        for (int i = 0; i < bankItems.length; i++) {
            ItemStack item = bankItems[i];
            if (item == null || item.getType() == Material.AIR) continue;
            inv.setItem(i, item);
        }
    }

    public void openBank(Player player) {
        if (collection_bin != null) {
            player.sendMessage(ChatColor.RED + "You have item(s) waiting in your collection bin.");
            player.sendMessage(ChatColor.GRAY + "Access your bank chest to claim them.");
            return;
        }

        player.openInventory(inv);
    }
}
