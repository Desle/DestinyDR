package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.WorldType;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.dungeonrealms.game.world.teleportation.Teleportation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemTeleportBook extends FunctionalItem implements ItemClickListener {

	@Setter
	private TeleportLocation teleportLocation;
	
	public ItemTeleportBook() {
		this(TeleportLocation.getRandomBookTP());
	}
	
	public ItemTeleportBook(TeleportLocation tl) {
		super(ItemType.TELEPORT_BOOK);
		setTeleportLocation(tl);
		setAntiDupe(false);
	}
	
	public ItemTeleportBook(ItemStack item) {
		super(item);
		setTeleportLocation(getEnum("location", TeleportLocation.class));
	}
	
	@Override
	public void updateItem() {
		setEnum("location", getTeleportLocation());
		super.updateItem();
	}

	public TeleportLocation getTeleportLocation() {
		return teleportLocation != null ? teleportLocation : TeleportLocation.getRandomBookTP();
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		Player player = evt.getPlayer();
		GamePlayer gp = GameAPI.getGamePlayer(player);
		if (gp.isJailed()) {
            player.sendMessage(ChatColor.RED + "You have been jailed.");
            return;
        }
		
		if (CombatLog.isInCombat(player)) {
			player.sendMessage(ChatColor.RED + "You are in combat! " + "(" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player) + "s" + ChatColor.RED + ")");
			return;
		}
		
		if (!GameAPI.isMainWorld(player.getWorld())) {
            player.sendMessage(ChatColor.RED + "You can only use teleport books in the main world.");
            return;
        }
		
		if (!getTeleportLocation().canTeleportTo(player)) {
			player.sendMessage(ChatColor.RED + "You cannot use teleport books whilst chaotic.");
			return;
		}

		if (getTeleportLocation().getWorld() == WorldType.ANDALUCIA && !TeleportLocation.CYRENNICA.canSetHearthstone(player)) {
			player.sendMessage(ChatColor.RED + "You have not visited Andalucia yet. Talk to the Ship Captain at Netyli ");
			return;
		}
		
		evt.setUsed(true);
		
		if (!getTeleportLocation().isBook()) {
			player.sendMessage(ChatColor.RED + "This teleport book is invalid, so it has vanished into the wind.");
			GameAPI.sendWarning("Removed 1x " + getTeleportLocation().getDisplayName() + " teleport books from " + player.getName() + ".");
			return;
		}
		
		Teleportation.getInstance().teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.TELEPORT_BOOK, getTeleportLocation());
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.WHITE + "" + ChatColor.BOLD + "Teleport: " + ChatColor.WHITE + teleportLocation.getDisplayName();
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				"(Right-Click) Teleport to " + teleportLocation.getDisplayName()
        		+ ( getTeleportLocation().isChaotic() ? ChatColor.RED + " WARNING: CHAOTIC ZONE" : "")};
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.BOOK);
	}
	
	public static boolean isTeleportBook(ItemStack item) {
		return isType(item, ItemType.TELEPORT_BOOK);
	}

}
