package net.dungeonrealms.game.player.inventory.menus;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.item.items.core.ShopItem.ShopItemClick;
import net.dungeonrealms.game.item.items.functional.ecash.ItemHearthStoneRelocator;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ShopHearthstoneLocation extends GUIMenu {

	public ShopHearthstoneLocation(Player player) {
		super(player, 18, "Hearthstone Re-Location");
		open(player, null);
	}

	@Override
	protected void setItems() {
		ShopItemClick cb = (player, item) -> {
			PlayerWrapper wrapper = PlayerWrapper.getWrapper(player);
			ItemHearthStoneRelocator h = (ItemHearthStoneRelocator)(item.getSoldItem());
			TeleportLocation currentHearthstone = wrapper.getHearthstone();
			TeleportLocation newLocation = h.getLocation();
			if (currentHearthstone == newLocation) {
                player.sendMessage(ChatColor.RED + "Your Hearthstone is already at this location!");
                return false;
            }

			if (!newLocation.canSetHearthstone(player)) {
				player.sendMessage(ChatColor.RED + "You have not explored the surrounding area of this Hearthstone Location yet");
				return false;
			}

			wrapper.setHearthstone(newLocation);
			player.sendMessage(ChatColor.GREEN + "Hearthstone set to " + newLocation.getDisplayName() + ".");
			Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), player::closeInventory);
			return true;
		};

		int slot = 0;

		for (TeleportLocation tl : TeleportLocation.values())
			if (tl.isBook())
				setItem(slot++, new ShopItem(new ItemHearthStoneRelocator(tl), cb).setGems(tl.getPrice()));
	}
}
