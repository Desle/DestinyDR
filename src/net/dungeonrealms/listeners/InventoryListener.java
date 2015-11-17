package net.dungeonrealms.listeners;

import ca.thederpygolems.armorequip.ArmorEquipEvent;
import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.banks.Storage;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.duel.DuelOffer;
import net.dungeonrealms.duel.DuelingMechanics;
import net.dungeonrealms.handlers.ClickHandler;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.loot.LootManager;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.network.NetworkAPI;
import net.dungeonrealms.profession.Fishing;
import net.dungeonrealms.profession.Mining;
import net.dungeonrealms.shops.Shop;
import net.dungeonrealms.shops.ShopMechanics;
import net.dungeonrealms.stats.PlayerStats;
import net.dungeonrealms.stats.StatsManager;
import net.dungeonrealms.trade.Trade;
import net.dungeonrealms.world.glyph.Glyph;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Nick on 9/18/2015.
 */
public class InventoryListener implements Listener {

    /**
     * Handles important inventories (guilds, etc.)
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onImportantInventoryClick(InventoryClickEvent event) {

        if (event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR) && event.getCursor() != null && !event.getCursor().getType().equals(Material.AIR)) {
            if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
            if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
            Glyph.getInstance().applyGlyph(((Player)event.getWhoClicked()), event, event.getCursor(), event.getCurrentItem());
        }

        ClickHandler.getInstance().doClick(event);
    }

    /**
     * Disables the clicking of items that contain NBTTag `important` in `type`.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getCurrentItem());
        if (nmsItem == null)
            return;
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null || !tag.getString("type").equalsIgnoreCase("important"))
            return;
        event.setCancelled(true);
    }

    /**
     * Handling Shops inventory being clicked.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void shopInventoryClicked(InventoryClickEvent event) {
        if (event.getInventory().getTitle().contains("@")) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
            String owner = event.getInventory().getTitle().split("@")[1];
            UUID shopOwner = API.getUUIDFromName(owner);
            Player clicker = (Player) event.getWhoClicked();
            Shop shop = ShopMechanics.PLAYER_SHOPS.get(shopOwner);
            if(shop == null){
            	clicker.closeInventory();
            	event.setCancelled(true);
            	return;
            }
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() != Material.AIR) {
                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
                if (nms != null) {
                    if (clicker.getUniqueId().toString().equalsIgnoreCase(shopOwner.toString())) {
                        // Is Owner Clicking
                        if (nms.hasTag() && nms.getTag().hasKey("status")) {
                            // Clicking status off and on.
                            event.setCancelled(true);
                            if (nms.getTag().getString("status").equalsIgnoreCase("off")) {
                                shop.isopen = true;
                                shop.toggleHologram();
                                int slot = event.getRawSlot();
                                ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.LIME.getDyeData());
                                ItemMeta meta = button.getItemMeta();
                                meta.setDisplayName(ChatColor.RED.toString() + "Close Shop");
                                button.setItemMeta(meta);
                                net.minecraft.server.v1_8_R3.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
                                clicker.playSound(clicker.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                                nmsButton.getTag().setString("status", "on");
                                shop.inventory.setItem(slot, CraftItemStack.asBukkitCopy(nmsButton));
                            } else {
                                // Turn shop off;
                                shop.isopen = false;
                                shop.toggleHologram();
                                ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData());
                                ItemMeta meta = button.getItemMeta();
                                meta.setDisplayName(ChatColor.YELLOW.toString() + "Open Shop");
                                clicker.playSound(clicker.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                                button.setItemMeta(meta);
                                net.minecraft.server.v1_8_R3.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
                                nmsButton.getTag().setString("status", "off");
                                shop.inventory.setItem(8, CraftItemStack.asBukkitCopy(nmsButton));
                            }
                        } else {
                            // Clicking something not Turning shop off or
                            // on.
                            if (shop.isopen) {
                                // make sure shop is off.
                                clicker.sendMessage(ChatColor.RED + "You must close the shop before you can edit");
                                event.setCancelled(true);
                            } else {
                                // shop is off
                                int slot = event.getRawSlot();
                                if (slot < shop.inventory.getSize()) {
                                    ItemStack stackInSlot = event.getInventory().getItem(slot);
                                    ItemStack itemHeld = event.getCursor();
                                    if (stackInSlot != null && itemHeld.getType() != Material.AIR
                                            && itemHeld.getType() != stackInSlot.getType()) {
                                        // Swaping Items
                                        clicker.sendMessage(ChatColor.RED.toString() + "Move item in slot first.");
                                        event.setCancelled(true);
                                    } else {
                                        if (event.isLeftClick()) {
                                            ItemStack stack = stackInSlot.clone();
                                            ItemMeta meta = stack.getItemMeta();
                                            List<String> lore = meta.getLore();
                                            if (lore != null)
                                                for (int i = 0; i < lore.size(); i++) {
                                                    String current = lore.get(i);
                                                    if (current.contains("Price")) {
                                                        lore.remove(i);
                                                        break;
                                                    }
                                                }
                                            meta.setLore(lore);
                                            stack.setItemMeta(meta);
                                            event.setCancelled(true);
                                            net.minecraft.server.v1_8_R3.ItemStack nms2 = CraftItemStack
                                                    .asNMSCopy(stack);
                                            nms2.getTag().remove("worth");
                                            clicker.getInventory().addItem(CraftItemStack.asBukkitCopy(nms2));
                                            event.getInventory().setItem(slot, new ItemStack(Material.AIR, 1));
                                        } else if (event.isRightClick()) {
                                            event.setCancelled(true);
                                            Player player = clicker;
                                            player.closeInventory();
                                            AnvilGUIInterface gui = AnvilApi.createNewGUI(player, e -> {
                                                if (e.getSlot() == AnvilSlot.OUTPUT) {
                                                    int number = 0;
                                                    try {
                                                        number = Integer.parseInt(e.getName());
                                                        player.sendMessage("Price set");
                                                    } catch (Exception exc) {
                                                        e.setWillClose(true);
                                                        e.setWillDestroy(true);
                                                        Bukkit.getPlayer(e.getPlayerName())
                                                                .sendMessage("Please enter a valid number");
                                                        return;
                                                    }
                                                    if (number < 0) {
                                                        player.getPlayer()
                                                                .sendMessage("You can't ask for negative money!");
                                                    } else {
                                                        ItemStack stack = stackInSlot.clone();
                                                        ItemMeta meta = stackInSlot.getItemMeta();
                                                        ArrayList<String> lore = new ArrayList<>();
                                                        if (meta.hasLore()) {
                                                            lore = (ArrayList<String>) meta.getLore();
                                                        }
                                                        for (int i = 0; i < lore.size(); i++) {
                                                            String current = lore.get(i);
                                                            if (current.contains("Price")) {
                                                                lore.remove(i);
                                                                break;
                                                            }
                                                        }
                                                        lore.add(ChatColor.BOLD.toString() + ChatColor.GREEN.toString()
                                                                + "Price: " + ChatColor.WHITE.toString() + number
                                                                + "g");
                                                        meta.setLore(lore);
                                                        stack.setItemMeta(meta);
                                                        net.minecraft.server.v1_8_R3.ItemStack nms1 = CraftItemStack
                                                                .asNMSCopy(stack);
                                                        nms1.getTag().setInt("worth", number);
                                                        shop.inventory.setItem(slot, CraftItemStack.asBukkitCopy(nms1));
                                                        player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1,
                                                                1);
                                                        e.setWillClose(true);
                                                        e.setWillDestroy(true);
                                                    }
                                                }
                                            });
                                            ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
                                            ItemMeta meta = stack.getItemMeta();
                                            meta.setDisplayName("Price?");
                                            stack.setItemMeta(meta);
                                            gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
                                            gui.open();
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                    	//NOT OWNER
                        event.setCancelled(true);
                        if (!shop.isopen) {
                            clicker.closeInventory();
                            clicker.sendMessage(ChatColor.RED.toString() + "This shop is closed!");
                            event.setCancelled(true);
                            return;
                        }
                        if (event.getRawSlot() < shop.inventory.getSize()) {
                            if (event.isLeftClick()) {
                                    if (event.getRawSlot() == 9){
                                        return;
                                    }
                                    int price = nms.getTag().getInt("worth");
                                    if (BankMechanics.getInstance().takeGemsFromInventory(price, clicker)) {
                                        ItemStack stack = item.clone();
                                        ItemMeta meta = stack.getItemMeta();
                                        List<String> lore = meta.getLore();
                                        if (lore != null){
                                            for (int i = 0; i < lore.size(); i++) {
                                                String current = lore.get(i);
                                                if (current.contains("Price")) {
                                                    lore.remove(i);
                                                    break;
                                                }
                                            }
                                        }
                                        meta.setLore(lore);
                                        stack.setItemMeta(meta);
                                        stack.setAmount(1);
                                        event.setCancelled(true);
                                        net.minecraft.server.v1_8_R3.ItemStack nms2 = CraftItemStack.asNMSCopy(stack);
                                        nms2.getTag().remove("worth");
                                        clicker.getInventory().addItem(CraftItemStack.asBukkitCopy(nms2));
                                        ItemStack tempStack = event.getInventory().getItem(event.getRawSlot());
                                        tempStack.setAmount(1);
                                        BankMechanics.getInstance().addGemsToPlayerBank(shopOwner, price);
                                        NetworkAPI.getInstance().sendPlayerMessage(clicker.getDisplayName(), ChatColor.GREEN.toString() +"Bought a " + stack.getItemMeta().getDisplayName() + " for "+ ChatColor.BOLD + price + " Gems");
                                        
                                        Player shopowner = Bukkit.getPlayer(shopOwner);
                                        if(shopowner != null)
                                        NetworkAPI.getInstance().sendPlayerMessage(shopowner.getDisplayName(), ChatColor.GREEN.toString() + ChatColor.BOLD + price + " Gems" + ChatColor.GREEN + " added to your bank!");
                                        shop.inventory.remove(tempStack);
                                    } else {
                                        clicker.sendMessage(ChatColor.RED + "Not enough " + ChatColor.RED.toString() + ChatColor.BOLD + "gems!");
                                    }
                                }
                        }
                    }
                }
            } else { // Setting new item to shop
                if (clicker.getUniqueId().toString().equalsIgnoreCase(shopOwner.toString())) {
                    if (event.getRawSlot() < shop.inventory.getSize()) {
                        ItemStack itemHeld = event.getCursor();
                        if (itemHeld.getType() == Material.AIR || itemHeld == null)
                            return;
                        Player player = clicker;
                        if (player.getInventory().firstEmpty() < 0) {
                            player.sendMessage("Make more room in your inventory");
                            return;
                        }
                        event.setCancelled(true);
                        event.setCursor(null);
                        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemHeld);
                        if(nmsItem.hasTag() && nmsItem.getTag().hasKey("subtype") && nmsItem.getTag().getString("subtype").equalsIgnoreCase("starter")){
                        	event.setCancelled(true);
                        	player.sendMessage("Can't sell starter Items!");
                        	return;
                        }
                        player.getInventory().setItem(player.getInventory().firstEmpty(), itemHeld);
                        AnvilGUIInterface gui = AnvilApi.createNewGUI(player, event1 -> {
                            if (event1.getSlot() == AnvilSlot.OUTPUT) {
                                int number = 0;
                                try {
                                    number = Integer.parseInt(event1.getName());
                                } catch (Exception exc) {
                                    event1.setWillClose(true);
                                    event1.setWillDestroy(true);
                                    Bukkit.getPlayer(event1.getPlayerName()).sendMessage("Please enter a valid number");
                                    return;
                                }
                                event1.setWillClose(true);
                                event1.setWillDestroy(true);
                                if (number < 0) {
                                    player.getPlayer().sendMessage("You can't ask for negative money!");
                                } else {
                                    ItemStack stack = itemHeld.clone();
                                    ItemMeta meta = itemHeld.getItemMeta();
                                    ArrayList<String> lore = new ArrayList<>();
                                    if (meta.hasLore()) {
                                        lore = (ArrayList<String>) meta.getLore();
                                    }
                                    lore.add(ChatColor.BOLD.toString() + ChatColor.GREEN.toString() + "Price: "
                                            + ChatColor.WHITE.toString() + number + "g");
                                    meta.setLore(lore);
                                    stack.setItemMeta(meta);
                                    net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
                                    nms.getTag().setInt("worth", number);
                                    if (shop.inventory.firstEmpty() >= 0) {
                                        shop.inventory.addItem(CraftItemStack.asBukkitCopy(nms));
                                        player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);

                                        player.sendMessage(new String[]{
                                                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right click" + ChatColor.GREEN + " the item to edit price!",
                                                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left Click" + ChatColor.GREEN + " the item to remove!",
                                        });

                                        player.getInventory().remove(itemHeld);
                                    } else {
                                        player.getInventory().addItem(itemHeld);
                                        player.sendMessage("There is no room for this item in your Shop");
                                    }
                                }
                            }
                        });
                        ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
                        ItemMeta meta = stack.getItemMeta();
                        meta.setDisplayName("Price?");
                        stack.setItemMeta(meta);
                        gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
                        player.closeInventory();
                        gui.open();
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDuelOfferClick(InventoryClickEvent e) {
    	if(!e.getInventory().getTitle().contains("VS.")) return;
		if(e.getAction() == InventoryAction.COLLECT_TO_CURSOR){
    		e.setCancelled(true);
    		return;
    	}
    	Player p = (Player) e.getWhoClicked();
    	DuelOffer offer = DuelingMechanics.getOffer(p.getUniqueId());
    	if(offer == null){ p.closeInventory(); return;}
    	if(e.getRawSlot() > offer.sharedInventory.getSize()) return;
    	
    	if(e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BONE){
    		e.setCancelled(true);
    		return;
    	}
		int slot = e.getRawSlot();
		if (slot == 30) {
          e.setCancelled(true);
  		  offer.updateOffer();
          offer.cycleArmor();
          return;
		} else if (slot == 32) {
          e.setCancelled(true);
  		  offer.updateOffer();
          offer.cycleItem();
          return;
      	}
    	
       	if(offer.isLeftSlot(e.getRawSlot())){
    		if(!offer.isLeftPlayer(p)){
    			e.setCancelled(true);
    			return;
    		}
          }else{
        	  if(offer.isLeftPlayer(p)){
    			e.setCancelled(true);
    			return;
        	  }
    	}
       	
    	if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
    		return;
		ItemStack stackClicked = e.getCurrentItem();
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stackClicked);
		if(nms.hasTag() && nms.getTag().hasKey("status")){
			String status = nms.getTag().getString("status");
			e.setCancelled(true);
			if(status.equalsIgnoreCase("ready")){
				offer.updateReady(p.getUniqueId());
				ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "NOT READY",
				        null, DyeColor.GRAY.getDyeData());
				nms = CraftItemStack.asNMSCopy(item);
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("status", "notready");
				nms.setTag(nbt);
				nms.c(ChatColor.YELLOW + "NOT READY");
				e.getInventory().setItem(e.getRawSlot(), CraftItemStack.asBukkitCopy(nms));
				offer.checkReady();
				return;
			}else{
				offer.updateReady(p.getUniqueId());
				ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY",
				        null, DyeColor.LIME.getDyeData());
				nms = CraftItemStack.asNMSCopy(item);
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("status", "ready");
				nms.setTag(nbt);
				nms.c(ChatColor.YELLOW + "READY");
				e.getInventory().setItem(e.getRawSlot(), CraftItemStack.asBukkitCopy(nms));
				offer.checkReady();
				return;
			}
		}
		Utils.log.info("Updated");
		offer.updateOffer();
    }
    
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onDuelWagerClick(InventoryClickEvent e) {
//        if (e.getInventory().getTitle().contains("vs.")) {
//            if (e.isShiftClick()) {
//                e.setCancelled(true);
//                return;
//            }
//            Player p = (Player) e.getWhoClicked();
//            DuelOffer wager = DuelingMechanics.getOffer(p.getUniqueId());
//            int slot = e.getRawSlot();
//            ItemStack stack = e.getCurrentItem();
//            if (stack == null)
//                return;
//            if (stack.getType() == Material.BONE) {
//                e.setCancelled(true);
//            } else if (slot == 30) {
//                e.setCancelled(true);
//                wager.cycleArmor();
//            } else if (slot == 32) {
//                e.setCancelled(true);
//                wager.cycleWeapon();
//            } else if (slot == 0) {
//                if (wager.isLeft(p)) {
//                    // Left clicked
//                    e.setCancelled(true);
//                    if (CraftItemStack.asNMSCopy(stack).getTag().getString("state").equalsIgnoreCase("notready")) {
//                        ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
//                                ChatColor.YELLOW.toString() + "Ready", null, DyeColor.LIME.getDyeData());
//                        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
//                        NBTTagCompound nbt = new NBTTagCompound();
//                        nbt.setString("state", "ready");
//                        nms.setTag(nbt);
//                        nms.c(ChatColor.YELLOW.toString() + "Ready");
//                        wager.setItemSlot(0, CraftItemStack.asBukkitCopy(nms));
//                        if (CraftItemStack.asNMSCopy(e.getInventory().getItem(8)).getTag().getString("state")
//                                .equalsIgnoreCase("ready")) {
//                            wager.startDuel();
//                        }
//                    } else {
//                        ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
//                                ChatColor.YELLOW.toString() + "Not Ready", null, DyeColor.GRAY.getDyeData());
//                        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
//                        NBTTagCompound nbt = new NBTTagCompound();
//                        nbt.setString("state", "notready");
//                        nms.setTag(nbt);
//                        nms.c(ChatColor.YELLOW.toString() + "Not Ready");
//                        wager.setItemSlot(0, CraftItemStack.asBukkitCopy(nms));
//                    }
//                } else {
//                    e.setCancelled(true);
//                }
//            } else if (slot == 8) {
//                if (!wager.isLeft(p)) {
//                    // Right Clicked
//                    e.setCancelled(true);
//                    if (CraftItemStack.asNMSCopy(stack).getTag().getString("state").equalsIgnoreCase("notready")) {
//                        ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
//                                ChatColor.YELLOW.toString() + "Ready", null, DyeColor.LIME.getDyeData());
//                        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
//                        NBTTagCompound nbt = new NBTTagCompound();
//                        nbt.setString("state", "ready");
//                        nms.setTag(nbt);
//                        nms.c(ChatColor.YELLOW.toString() + "Ready");
//                        wager.setItemSlot(8, CraftItemStack.asBukkitCopy(nms));
//                        if (CraftItemStack.asNMSCopy(e.getInventory().getItem(0)).getTag().getString("state")
//                                .equalsIgnoreCase("ready")) {
//                            wager.startDuel();
//                        }
//                    } else {
//                        ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
//                                ChatColor.YELLOW.toString() + "Not Ready", null, DyeColor.GRAY.getDyeData());
//                        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
//                        NBTTagCompound nbt = new NBTTagCompound();
//                        nbt.setString("state", "notready");
//                        nms.setTag(nbt);
//                        nms.c(ChatColor.YELLOW.toString() + "Not Ready");
//                        wager.setItemSlot(8, CraftItemStack.asBukkitCopy(nms));
//                    }
//                } else {
//                    e.setCancelled(true);
//                }
//            } else if (slot < 36) {
//                if (e.isLeftClick()) {
//                    if (wager.isLeftSlot(slot)) {
//                        if (wager.isLeft(p)) {
//
//                        } else {
//                            e.setCancelled(true);
//                        }
//                    } else {
//                        if (!wager.isLeft(p)) {
//                        } else {
//                            e.setCancelled(true);
//                        }
//                    }
//                } else {
//                    e.setCancelled(true);
//                }
//            }
//        }
//    }

    /**
     * @param event
     * @since 1.0 Dragging is naughty.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDragItemInDuelWager(InventoryDragEvent event) {
        if (event.getInventory().getTitle().contains("VS.") || event.getInventory().getTitle().contains("Bank")
                || event.getInventory().getTitle().contains("@") || event.getInventory().getTitle().contains("Trade"))
            event.setCancelled(true);
    }

    /** Called when a player switches item
     *
     * @param event
     * @since 1.0
     */
    /*@EventHandler(priority = EventPriority.LOWEST)
    public void playerSwitchItem(PlayerItemHeldEvent event) {
		if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		int slot = event.getNewSlot();
		if (event.getPlayer().getInventory().getItem(slot) != null) {
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(event.getPlayer().getInventory().getItem(slot));
			if (nms.hasTag()) {
				if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("weapon")) {
					ItemTier tier = new Attribute(event.getPlayer().getInventory().getItem(slot)).getItemTier();
					int minLevel = tier.getRangeValues()[0];
					Player p = event.getPlayer();
					int pLevel = (int) DatabaseAPI.getInstance().getData(EnumData.LEVEL, p.getUniqueId());
					if (pLevel < minLevel) {
						p.sendMessage(ChatColor.RED + "You must be level " + ChatColor.YELLOW.toString() + minLevel
								+ ChatColor.RED.toString() + " to wield this weapon!");
						event.setCancelled(true);
					}
				}
			}
		}
	}*/

    /**
     * Called when a player equips armor
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerEquipArmor(ArmorEquipEvent event) {
        //TODO: Chase do this to prevent all forms of putting on armor if they are not the correct level.
        if (!CombatLog.isInCombat(event.getPlayer())) {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.NOTE_PLING, 1f, 1f);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                HealthHandler.getInstance().setPlayerMaxHPLive(event.getPlayer(), HealthHandler.getInstance().calculateMaxHPFromItems(event.getPlayer()));
                HealthHandler.getInstance().setPlayerHPRegenLive(event.getPlayer(), HealthHandler.getInstance().calculateHealthRegenFromItems(event.getPlayer()));
                if (HealthHandler.getInstance().getPlayerHPLive(event.getPlayer()) > HealthHandler.getInstance().getPlayerMaxHPLive(event.getPlayer())) {
                    HealthHandler.getInstance().setPlayerHPLive(event.getPlayer(), HealthHandler.getInstance().getPlayerMaxHPLive(event.getPlayer()));
                }
            }, 10L);
        } else {
            event.getPlayer().sendMessage(ChatColor.RED + "Equipping armor while in combat will not change your stats! Please re-equip out of combat!");
        }
    }

    /**
     * @param event
     * @since 1.0 Closes both players wager inventory.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClosed(InventoryCloseEvent event) {
        if (event.getInventory().getTitle().contains("VS.")) {
            Player p = (Player) event.getPlayer();
            DuelOffer offer = DuelingMechanics.getOffer(p.getUniqueId());
            if(offer == null) return;
            if(!offer.p1Ready || !offer.p2Ready){
            	offer.giveBackItems();
            	DuelingMechanics.removeOffer(offer);
            	Player p1 =Bukkit.getPlayer(offer.player1);
            	if(p1 != null)
            		p1.closeInventory();
            	Player p2 = Bukkit.getPlayer(offer.player2);
            	if(p2 != null)
            		p2.closeInventory();
            }
        } else if (event.getInventory().getTitle().contains("Storage Chest")) {
            Storage storage = BankMechanics.getInstance().getStorage(event.getPlayer().getUniqueId());
            storage.inv.setContents(event.getInventory().getContents());
        } else if (event.getInventory().getTitle().contains("Loot")) {
            Player p = (Player) event.getPlayer();
            Block block = p.getTargetBlock((Set<Material>) null, 100);
            LootManager.LOOT_SPAWNERS.stream().filter(loot -> loot.location.equals(block.getLocation())).forEach(net.dungeonrealms.loot.LootSpawner::update);
        } else if (event.getInventory().getTitle().contains("Trade Window")) {
            Player p = (Player) event.getPlayer();
            Trade t = net.dungeonrealms.trade.TradeManager.getTrade(p.getUniqueId());
            if(t != null)
            if (!t.p1Ready || !t.p2Ready) {
                t.handleClose();
            }
        } else if (event.getInventory().getTitle().contains("Stat Points")) {
            PlayerStats stat = API.getGamePlayer((Player) event.getPlayer()).getStats();
            if (stat.reset) {
                stat.resetTemp();
            }
            stat.reset = true;
        }
    }

    /**
     * @param event
     * @since 1.0 handles Trading inventory items.
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTradeInvClicked(InventoryClickEvent event) {
        if (event.getInventory().getTitle().contains("Trade Window")) {
    		if(event.getAction() == InventoryAction.COLLECT_TO_CURSOR){
    			event.setCancelled(true);
        		return;
        	}
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
            Trade trade = net.dungeonrealms.trade.TradeManager.getTrade(event.getWhoClicked().getUniqueId());
            if(trade == null){
            	return;
            }
            int slot = event.getRawSlot();
            if (slot >= 36)
                return;
            
            if(event.getCurrentItem() == null)
            	return;
            if (event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {
                event.setCancelled(true);
                return;
            }
            
            if(trade.isLeftSlot(slot)){
            	if(!trade.isLeftPlayer(event.getWhoClicked().getUniqueId())){
            		event.setCancelled(true);
            		return;
            	}
            }else if(trade.isRightSlot(slot)){
            	if(trade.isLeftPlayer(event.getWhoClicked().getUniqueId())){
            		event.setCancelled(true);
            		return;
            	}
            }
            
        	if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
        		return;
    		ItemStack stackClicked = event.getCurrentItem();
    		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stackClicked);
    		if(nms.hasTag() && nms.getTag().hasKey("status")){
    			String status = nms.getTag().getString("status");
    			event.setCancelled(true);
    			if(status.equalsIgnoreCase("ready")){
    				trade.updateReady(event.getWhoClicked().getUniqueId());
    				ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "NOT READY",
    				        null, DyeColor.GRAY.getDyeData());
    				nms = CraftItemStack.asNMSCopy(item);
    				NBTTagCompound nbt = new NBTTagCompound();
    				nbt.setString("status", "notready");
    				nms.setTag(nbt);
    				nms.c(ChatColor.YELLOW + "NOT READY");
    				event.getInventory().setItem(event.getRawSlot(), CraftItemStack.asBukkitCopy(nms));
    				trade.checkReady();
    				return;
    			}else{
    				trade.updateReady(event.getWhoClicked().getUniqueId());
    				ItemStack item = ItemManager.createItemWithData(Material.INK_SACK, ChatColor.YELLOW.toString() + "READY",
    				        null, DyeColor.LIME.getDyeData());
    				nms = CraftItemStack.asNMSCopy(item);
    				NBTTagCompound nbt = new NBTTagCompound();
    				nbt.setString("status", "ready");
    				nms.setTag(nbt);
    				nms.c(ChatColor.YELLOW + "READY");
    				event.getInventory().setItem(event.getRawSlot(), CraftItemStack.asBukkitCopy(nms));
    				trade.checkReady();
    				return;
    			}
    		}
            trade.changeReady();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerUseScrapItem(InventoryClickEvent event) {
        if (event.getCursor() == null) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getInventory().getName().equalsIgnoreCase("container.crafting")) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) return;
        ItemStack cursorItem = event.getCursor();
        ItemStack slotItem = event.getCurrentItem();
        if(slotItem == null || slotItem.getType() == Material.AIR) return;
        Player player = (Player) event.getWhoClicked();
        if(!RepairAPI.isItemArmorScrap(cursorItem))return;
        if ((RepairAPI.isItemArmorOrWeapon(slotItem)) || Mining.isDRPickaxe(slotItem) || Fishing.isDRFishingPole(slotItem)) {
            if (RepairAPI.canItemBeRepaired(slotItem)) {
                int scrapTier = RepairAPI.getScrapTier(cursorItem);
                int slotTier = 0;
                if(Mining.isDRPickaxe(slotItem) || Fishing.isDRFishingPole(slotItem)){
            		slotTier = CraftItemStack.asNMSCopy(slotItem).getTag().getInt("itemTier");
                    if (scrapTier != slotTier) return;
                    if (cursorItem.getAmount() == 1) {
                        event.setCancelled(true);
                        event.setCursor(new ItemStack(Material.AIR));
                    } else if (cursorItem.getAmount() > 1) {
                        event.setCancelled(true);
                        cursorItem.setAmount(cursorItem.getAmount() - 1);
                        event.setCursor(cursorItem);
                    }

                    double itemDurability = RepairAPI.getCustomDurability(slotItem);

                    if (itemDurability + 45.0D >= 1500.0D) {
                        RepairAPI.setCustomItemDurability(slotItem, 1500);
                        player.updateInventory();
                    } else if (itemDurability + 45.0D < 1500.0D) {
                        RepairAPI.setCustomItemDurability(slotItem, (itemDurability + 45.0D));
                        player.updateInventory();
                    }
                    player.updateInventory();
                    double newPercent = RepairAPI.getCustomDurability(slotItem);

                    int particleID = 1;
                    switch (scrapTier) {
                        case 1:
                            particleID = 25;
                            break;
                        case 2:
                            particleID = 30;
                            break;
                        case 3:
                            particleID = 42;
                            break;
                        case 4:
                            particleID = 57;
                            break;
                        case 5:
                            particleID = 41;
                            break;
                    }
                    if (slotItem.getType() == Material.BOW) {
                        particleID = 5;
                    }
                    Packet particles = new PacketPlayOutWorldEvent(2001, new BlockPosition((int) Math.round(player.getLocation().getX()), (int) Math.round(player.getLocation().getY() + 2), (int) Math.round(player.getLocation().getZ())), particleID, false);
                    ((CraftServer) DungeonRealms.getInstance().getServer()).getServer().getPlayerList().sendPacketNearby(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 36, ((CraftWorld) player.getWorld()).getHandle().dimension, particles);
                    if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                        player.sendMessage(ChatColor.GREEN + "You used an Item Scrap to repair 3% durability to " + newPercent + "/1500");
                    }
                }
                
                if(RepairAPI.isItemArmorOrWeapon(slotItem)){
                slotTier = RepairAPI.getArmorOrWeaponTier(slotItem);
                if (scrapTier != slotTier) return;
                if (slotItem.getDurability() == 0) return;
                if (cursorItem.getAmount() == 1) {
                    event.setCancelled(true);
                    event.setCursor(new ItemStack(Material.AIR));
                } else if (cursorItem.getAmount() > 1) {
                    event.setCancelled(true);
                    cursorItem.setAmount(cursorItem.getAmount() - 1);
                    event.setCursor(cursorItem);
                }

                double itemDurability = RepairAPI.getCustomDurability(slotItem);

                if (itemDurability + 45.0D >= 1500.0D) {
                    RepairAPI.setCustomItemDurability(slotItem, 1500);
                    player.updateInventory();
                } else if (itemDurability + 45.0D < 1500.0D) {
                    RepairAPI.setCustomItemDurability(slotItem, (itemDurability + 45.0D));
                    player.updateInventory();
                }
                player.updateInventory();
                double newPercent = RepairAPI.getCustomDurability(slotItem);

                int particleID = 1;
                switch (scrapTier) {
                    case 1:
                        particleID = 25;
                        break;
                    case 2:
                        particleID = 30;
                        break;
                    case 3:
                        particleID = 42;
                        break;
                    case 4:
                        particleID = 57;
                        break;
                    case 5:
                        particleID = 41;
                        break;
                }
                if (slotItem.getType() == Material.BOW) {
                    particleID = 5;
                }
                Packet particles = new PacketPlayOutWorldEvent(2001, new BlockPosition((int) Math.round(player.getLocation().getX()), (int) Math.round(player.getLocation().getY() + 2), (int) Math.round(player.getLocation().getZ())), particleID, false);
                ((CraftServer) DungeonRealms.getInstance().getServer()).getServer().getPlayerList().sendPacketNearby(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 36, ((CraftWorld) player.getWorld()).getHandle().dimension, particles);
                if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId()).toString())) {
                    player.sendMessage(ChatColor.GREEN + "You used an Item Scrap to repair 3% durability to " + newPercent + "/1500");
                }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerClickStatsInventory(InventoryClickEvent event) {
        if (event.getInventory().getTitle().contains("Stat Points")) {
            //Stat Points Inv
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                ItemStack clicked = event.getCurrentItem();
                Player p = (Player) event.getWhoClicked();
                PlayerStats stats = StatsManager.getPlayerStats(p);
                int slot = event.getRawSlot();
                Inventory inv = event.getInventory();
                switch (slot) {
                    case 2:
                        //Strength
                        if (event.isRightClick())
                            stats.removePoint("str", p, inv);
                        if (event.isLeftClick())
                            stats.allocatePoint("str", p, inv);
                        break;
                    case 3:
                        //Dexterity
                        if (event.isRightClick())
                            stats.removePoint("dex", p, inv);
                        if (event.isLeftClick())
                            stats.allocatePoint("dex", p, inv);
                        break;
                    case 4:
                        //Intellect
                        if (event.isRightClick())
                            stats.removePoint("int", p, inv);
                        if (event.isLeftClick())
                            stats.allocatePoint("int", p, inv);
                        break;
                    case 5:
                        //Vitality
                        if (event.isRightClick())
                            stats.removePoint("vit", p, inv);
                        if (event.isLeftClick())
                            stats.allocatePoint("vit", p, inv);
                        break;
                    case 6:
                        stats.dexPoints += stats.tempdexPoints;
                        stats.vitPoints += stats.tempvitPoints;
                        stats.strPoints += stats.tempstrPoints;
                        stats.intPoints += stats.tempintPoints;
                        stats.dexPoints += stats.tempdexPoints;
                        stats.freePoints = stats.tempFreePoints;
                        stats.reset = false;
                        stats.resetTemp();
                        stats.updateDatabase();
                        p.closeInventory();
                        //Confirm
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerDragItemInMerchant(InventoryClickEvent event) {
        if (event.getInventory().getName().equals("Merchant")) {
            int slot = event.getSlot();
            if (!(slot == 1 || slot == 2 || slot == 3 || slot == 9 || slot == 10 || slot == 11 || slot == 12 || slot == 18 || slot == 19
                    || slot == 20 || slot == 21) && !(slot > 27)) {
                if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Naughty Naughty Naughty!");
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    return;
                }
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.getWhoClicked().sendMessage(ChatColor.RED + "Naughty Naughty Naughty!");
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }
}
