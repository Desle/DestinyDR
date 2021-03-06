package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemGem extends ItemMoney {
	
	public ItemGem(int worth) {
		super(ItemType.GEM, worth, false);
		setAntiDupe(false);
	}
	
	public ItemGem(ItemStack item) {
		super(item);
		setGemValue(item.getAmount());
		setAntiDupe(false);
	}
	
	@Override
	public int getGemValue() {
		return getItem().getAmount();
	}
	
	@Override
	public void setGemValue(int amount) {
		super.setGemValue(amount); //Performs checks
		getItem().setAmount(amount);
	}

	@Override
	public ItemStack generateItem() {
		ItemStack toReturn = super.generateItem();
		toReturn.setAmount(this.gemValue);
		return toReturn;
	}

	@Override
	public void updateItem() {
		super.updateItem();
		getItem().setAmount(this.gemValue);
	}

	@Override
	public int getMaxStorage() {
		return getItem().getMaxStackSize();
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "Gem";
	}

	@Override
	protected String[] getLore() {
		return new String[] { "The currency of Andalucia.",
					"Deposit this in a bank for safekeeping."};
	}

	@Override
	protected ItemUsage[] getUsage() {
		return new ItemUsage[0];
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.EMERALD);
	}
	
	public static boolean isGem(ItemStack item) {
		return isType(item, ItemType.GEM);
	}
}
