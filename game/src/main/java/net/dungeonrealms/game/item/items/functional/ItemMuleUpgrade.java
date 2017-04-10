package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMuleMount;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

public class ItemMuleUpgrade extends FunctionalItem {

	@Getter @Setter
	private MuleTier tier;
	
	public ItemMuleUpgrade(MuleTier tier) {
		super(ItemType.MULE_UPGRADE);
		setTier(tier);
		setPermUntradeable(true);
	}
	
	public ItemMuleUpgrade(ItemStack item) {
		super(item);
	}
	
	@Override
	public void loadItem() {
		setTier(MuleTier.getByTier(getTagInt(TIER)));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		setTagInt(TIER, getTier().getTier());
		super.updateItem();
	}

	@Override
	public void onClick(ItemClickEvent evt) {}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {
		if(!ItemMuleMount.isMule(evt.getSwappedItem()))
			return;
		
		Player pl = evt.getPlayer();
		MuleTier muleTier = MuleTier.getByTier((int) DatabaseAPI.getInstance().getData(EnumData.MULELEVEL, pl.getUniqueId()));
		
		if (muleTier.getLast() != getTier()) {
			pl.sendMessage(ChatColor.RED + "This mule upgrade is for a different mule tier.");
			return;
		}
		
		evt.setUsed(true);
		evt.setCancelled(true);
		evt.getPlayer().sendMessage(ChatColor.GREEN + "Mule upgraded to " + getTier().getName() + ChatColor.GREEN + "!");
		DatabaseAPI.getInstance().update(pl.getUniqueId(), EnumOperators.$SET, EnumData.MULELEVEL, muleTier.getTier(), true);
		
		if (MountUtils.inventories.containsKey(pl.getUniqueId())) {
            Inventory inv = MountUtils.inventories.get(pl.getUniqueId());
            //Close all people viewing this inventory.
            Lists.newArrayList(inv.getViewers()).forEach(HumanEntity::closeInventory);

            if (muleTier.getSize() != inv.getSize()) {
                Inventory upgradeInventory = Bukkit.createInventory(null, muleTier.getSize(), "Mule Storage");
                upgradeInventory.setContents(inv.getContents());
                inv.clear();
                MountUtils.inventories.put(pl.getUniqueId(), upgradeInventory);
            }
        }

		evt.setSwappedItem(new ItemMuleMount(pl).generateItem());
        pl.updateInventory();
        pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4F);
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.AQUA + getTier().getName() + " Chest";
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				ChatColor.RED + "" + getTier().getSize() + " Max Storage Size",
				"Apply to your " + getTier().getLast().getName(),
				"to " + (getTier() == MuleTier.values()[0] ? "" : "further ") + "expand its inventory"};
	}

	@Override
	protected ItemUsage[] getUsage() {
		return new ItemUsage[] { ItemUsage.INVENTORY_SWAP_PLACE };
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.CHEST);
	}

}
