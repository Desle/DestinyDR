package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.game.mastery.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent.ItemInventoryListener;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mechanic.data.ScrapTier;

public class ItemScrap extends FunctionalItem implements ItemInventoryListener {

	@Getter @Setter
	private ScrapTier tier;

	public ItemScrap(ScrapTier tier) {
		super(ItemType.SCRAP);
		setAntiDupe(false);
		setTier(tier);
	}

	public ItemScrap(ItemStack item) {
		super(item);
		setAntiDupe(false);
		setTier(ScrapTier.getScrapTier(getTagInt(TIER)));
	}

	@Override
	public void updateItem() {
		setTagInt(TIER, tier.getTier());
		super.updateItem();
	}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {
		if (!ItemGear.isCustomTool(evt.getSwappedItem()))
			return;
		ItemGear gear = (ItemGear)PersistentItem.constructItem(evt.getSwappedItem());
		Player player = evt.getPlayer();

		if (!gear.canRepair())
			return;

		evt.setCancelled(true);
		if (gear instanceof ProfessionItem && ((ProfessionItem)gear).getLevel() >= 100) {
			player.sendMessage(ChatColor.RED + "This tool has reach it's end and can no longer be repaired.");
			return;
		}

		if(getTier().getTier() != gear.getTier().getTierId()){
			return;
		}

		gear.scrapRepair(gear);
		evt.setUsed(true);

		int particleId = gear.getRepairParticle(getTier());

		for (int i = 0; i < 6; i++) {
            player.getWorld().playEffect(player.getLocation().add(0, 1.3, 0), Effect.TILE_BREAK, particleId, 12);
            player.getWorld().playEffect(player.getLocation().add(0, 1.15, 0), Effect.TILE_BREAK, particleId, 12);
            player.getWorld().playEffect(player.getLocation().add(0, 1, 0), Effect.TILE_BREAK, particleId, 12);
        }

		if (gear.getTier().getId() == 5) {
			Utils.sendCenteredDebug(player, ChatColor.GREEN + "+5% DURABILITY " + ChatColor.GREEN + ChatColor.BOLD + "-> " + ChatColor.GREEN + (int) Math.floor(gear.getDurabilityPercent()) + "% TOTAL");
		} else {
			Utils.sendCenteredDebug(player, ChatColor.GREEN + "+3% DURABILITY " + ChatColor.GREEN + ChatColor.BOLD + "-> " + ChatColor.GREEN + (int) Math.floor(gear.getDurabilityPercent()) + "% TOTAL");
		}

        //Utils.sendCenteredDebug(player, ChatColor.GREEN + "+3% DURABILITY " + ChatColor.GREEN + ChatColor.BOLD + "-> " + ChatColor.GREEN + (int) Math.floor(gear.getDurabilityPercent()) + "% TOTAL");

        evt.setSwappedItem(gear.generateItem());
    }

	@Override
	protected String getDisplayName() {
		return getTier().getName() + " Scrap";
	}

	@Override
	protected String[] getLore() {
		return new String[] { getTier().getTier() == 5 ? "Repairs 5% durability on " + getTier().getName() + ChatColor.GRAY + " equipment." : "Repairs 3% durability on " + getTier().getName() + ChatColor.GRAY + " equipment." };
		//return new String[] { "Repairs 3% durability on " + getTier().getName() + ChatColor.GRAY + " equipment." };
	}

	@Override
	protected ItemUsage[] getUsage() {
		return new ItemUsage[] { ItemUsage.INVENTORY_SWAP_PLACE };
	}

	@Override
	protected ItemStack getStack() {
		ItemStack i = getTier().getRawStack();
		i.setAmount(1);
		return i;
	}

	public static boolean isScrap(ItemStack item) {
		return isType(item, ItemType.SCRAP);
	}
}
