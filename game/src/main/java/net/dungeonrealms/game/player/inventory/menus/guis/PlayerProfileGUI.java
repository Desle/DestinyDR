package net.dungeonrealms.game.player.inventory.menus.guis;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.StatBoost;
import net.dungeonrealms.game.mastery.Stats;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.player.inventory.menus.ShopECashVendor;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.CategoryGUI;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveOpenProfile;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PlayerProfileGUI extends GUIMenu {

    private static DecimalFormat df = new DecimalFormat("##.###");

    public PlayerProfileGUI(Player player, GUIMenu menu) {
        super(player, 27, "Profile (" + player.getName() + ")", menu);
    }

    public PlayerProfileGUI(Player player) {
        this(player, null);
    }

    @Override
    protected void setItems() {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        setItem(17, new GUIItem(ItemManager.createItem(Material.BOOK_AND_QUILL, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Friend List",
                "", ChatColor.GRAY.toString() + "Manage your Friends List!"
        )).setClick(e -> new FriendGUI(player, this, false).open(player, e.getAction())));

        setItem(0, new GUIItem(ItemManager.createItem(Material.CHEST, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Unlockables",
                "",
                ChatColor.GRAY.toString() + "View all in game unlockables!"
        )).setClick(e -> new UnlockablesGUI(player, this).open(player, e.getAction())));

        setItem(18, new GUIItem(ItemManager.createItem(Material.ENDER_CHEST, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Purchasables",
                "",
                ChatColor.GRAY.toString() + "View all things available for purchase!"
        )).setClick(e -> new CategoryGUI(player).setPreviousGUI(this).setShouldOpenPreviousOnClose(true).open(player, e.getAction())));


        setItem(9, new GUIItem(ItemManager.createItem(Material.EMERALD, ChatColor.YELLOW + ChatColor.BOLD.toString() + "E-Cash Vendor",
                "",
                ChatColor.GRAY + "View all available E-Cash items!",
                "",
                ChatColor.GRAY + "Unlock " + ChatColor.GREEN + ChatColor.BOLD + "FREE" + ChatColor.GRAY + " Mystery Crates with " + ChatColor.GREEN + "/vote"
        )).setEnchanted(true).setClick(e -> {
            player.closeInventory();
            //NPCMenu.ECASH_VENDOR.open(player)
            ShopECashVendor gui = new ShopECashVendor(player);
            gui.setPreviousGUI(this);
            gui.setShouldOpenPreviousOnClose(true);
            gui.open();
        }));

        setItem(8, new GUIItem(ItemManager.createItem(Material.COMPASS, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Achievements",
                "",
                ChatColor.GRAY+  "View your Achievement progress",
                ChatColor.GRAY + "on your journey through Andalucia!"
        )).setClick(e -> new AchievementGUI(player, this).open(player, e.getAction())));

        setItem(26, new GUIItem(ItemManager.createItem(Material.REDSTONE_COMPARATOR, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Toggles",
                "",
                ChatColor.GRAY.toString() + "Adjust game toggles and preferences."
        )).setClick(e -> new TogglesGUI(player, this).open(player, e.getAction())));

        PlayerStats stats = wrapper.getPlayerStats();
        for (Stats stat : Stats.values()) {


            //Maps
            List<String> lore = new ArrayList<>();
//            lore.addAll(Arrays.asList(stat.getDescription()));
            lore.add("");
//            Utils.addChatColor(lore, ChatColor.GRAY);

            lore.add(ChatColor.AQUA + ChatColor.BOLD.toString() + "Stat Bonuses");
            for (StatBoost boost : stat.getStatBoosts()) {
                String prefix = ChatColor.stripColor(boost.getType().getPrefix());
                lore.add(" " + ChatColor.GOLD + prefix.replace("+", "") + ChatColor.AQUA + df.format(stats.getStat(stat) * (boost.getMultiplier() * 100)) + boost.getType().getSuffix() + " " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getTempStat(stat) * (boost.getMultiplier() * 100)) + "]" : ""));
            }

            int tempStats = stats.getTempStat(stat);
            if (stat.equals(Stats.STRENGTH)) {
                lore.add(ChatColor.GOLD + " AXE DMG: " + ChatColor.AQUA + df.format(stats.getAxeDMG(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getAxeDMG(true)) + "%]" : ""));
                lore.add(ChatColor.GOLD + " POLEARM DMG: " + ChatColor.AQUA + df.format(stats.getPolearmDMG(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getPolearmDMG(true)) + "%]" : ""));
            } else if (stat.equals(Stats.VITALITY)) {
                lore.add(ChatColor.GOLD + " HP: " + ChatColor.AQUA + df.format(stats.getHPPercent(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getHPPercent(true)) + "%]" : ""));
                lore.add(ChatColor.GOLD + " HP/s: " + ChatColor.AQUA + df.format(stats.getHealthRegen(false)) + " " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getHealthRegen(true)) + "]" : ""));
                lore.add(ChatColor.GOLD + " SWORD DMG: " + ChatColor.AQUA + df.format(stats.getSwordDMG(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getSwordDMG(true)) + "%]" : ""));
                lore.add(ChatColor.GOLD + " ELEMENTAL RESIST: " + ChatColor.AQUA + df.format(stats.getElementalResist(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getElementalResist(true)) + "%]" : ""));
            } else if (stat.equals(Stats.DEXTERITY)) {
                lore.add(ChatColor.GOLD + " DPS: " + ChatColor.AQUA + df.format(stats.getDPS(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getDPS(true)) + "%]" : ""));
                lore.add(ChatColor.GOLD + " BOW DMG: " + ChatColor.AQUA + df.format(stats.getBowDMG(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getBowDMG(true)) + "%]" : ""));
            } else if (stat.equals(Stats.INTELLECT)) {
                lore.add(ChatColor.GOLD + " STAFF DMG: " + ChatColor.AQUA + df.format(stats.getStaffDMG(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getStaffDMG(true)) + "%]" : ""));
                lore.add(ChatColor.GOLD + " CRITICAL DMG: " + ChatColor.AQUA + df.format(stats.getCriticalDamage(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getCriticalDamage(true)) + "%]" : ""));
                lore.add(ChatColor.GOLD + " ELEMENTAL DMG: " + ChatColor.AQUA + df.format(stats.getElementalDamage(false)) + "% " + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + "[+" + df.format(stats.getElementalDamage(true)) + "%]" : ""));

            }
            lore.add("");
            lore.add(ChatColor.GREEN + "Allocated Points: " + ChatColor.AQUA + stats.getStat(stat) + (stats.getTempStat(stat) > 0 ? ChatColor.GREEN + " [+" + stats.getTempStat(stat) + "]" : ""));
            lore.add(ChatColor.GREEN + "Available Points: " + ChatColor.AQUA + stats.getFreePoints());
            lore.add("");
            lore.add(ChatColor.AQUA + "Left Click " + ChatColor.GRAY + "to temporarily allocate " + ChatColor.AQUA + "1" + ChatColor.GRAY + " point.");

            if (tempStats > 0)
                lore.add(ChatColor.AQUA + "Right Click " + ChatColor.GRAY + "to un-allocate " + ChatColor.AQUA + "1" + ChatColor.GRAY + " point.");


            setItem(stat.getGuiSlot(), new GUIItem(Material.BOOK).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + stat.getDisplayName()).setLore(lore).setClick((evt) -> {
                if (evt.isRightClick()) {
                    if (stats.getTempStat(stat) <= 0) {
                        player.sendMessage(ChatColor.RED + "You do not have any points allocated to this Stat!");
                        return;
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_TOUCH, 1, .8F);
                    stats.removePoint(stat);
                }
                if (evt.isLeftClick()) {
                    if (stats.getFreePoints() <= 0) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.1F);
                        player.sendMessage(ChatColor.RED + "You do not have any stat points available!");
                        player.sendMessage(ChatColor.GRAY + "Gain Stat Points when leveling up your character!");
                        return;
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_TOUCH, 1, 1.1F);
                    stats.allocatePoint(stat);
                }
                reconstructGUI(player);
            }));
        }

        setItem(15, new GUIItem(Material.INK_SACK).setDurability(DyeColor.LIME.getDyeData()).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Confirm")
                .setLore(ChatColor.GRAY + "Click to confirm your stat", ChatColor.GRAY + "point allocations.", "",
                        ChatColor.GRAY + "Close this menu to " + ChatColor.RED + ChatColor.BOLD + "CANCEL").setClick(evt -> {
                    if (stats.confirmStats()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1.4F);
                        setItems();
                        player.updateInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "You have no temporary stats to confirm.");
                        player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, .3F, 1.4F);
                    }
                }));
    }

    @Override
    public void onRemove() {
        super.onRemove();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper != null) wrapper.getPlayerStats().resetTemp();
    }

    @Override
    public void open(Player player, InventoryAction action) {
        super.open(player, action);
        Quests.getInstance().triggerObjective(player, ObjectiveOpenProfile.class);
    }
}
