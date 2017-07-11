package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import lombok.Getter;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 5/10/2017.
 */
@Getter
public enum WebstoreCategories {

    SUBSCRIPTIONS("Subscriptions", "\nUnlock access to all " + ChatColor.GREEN + ChatColor.BOLD + "SUB " + ChatColor.GRAY + "shards\nalong with access to many\nCosmetics and other perks!\n \nClick here to view all subscriptions!", Material.EMERALD,0, ChatColor.GREEN),
    GLOBAL_BUFFS("Global Buffs and Auras", "\nClick here to view all global buffs and auras!", Material.DIAMOND,8, ChatColor.AQUA),
    PETS("Pets", "\nClick here to view all pets!", Material.NAME_TAG,3, ChatColor.GREEN),
    HATS("Cosmetics", "\nStand out from the rest with\nwith these Cosmetic Overrides!\n \nClick here to view all cosmetic gear items!", Material.SAPLING,4, ChatColor.AQUA, ChatColor.GRAY),

    PLAYER_EFFECTS("Player Effects", "\nStand out from the rest with\nwith these Player Effects!\n \nClick here to view all player effects!", Material.FIREWORK,1, ChatColor.DARK_GREEN, ChatColor.GRAY),
    CHEST_EFFECTS("Chest Effects", "\nStand out from the rest with\nwith these Chest Effects!\n \nClick here to view all chest effects!", Material.FIREWORK,-1, ChatColor.DARK_GREEN, ChatColor.GRAY),
    REALM_EFFECTS("Realm Effects", "\nStand out from the rest with\nwith these Realm Effects!\n \nClick here to view all realm effects!", Material.FIREWORK,-1, ChatColor.DARK_GREEN, ChatColor.GRAY),

    CRATES("Mystery Crates", "\nGet some cool rewards\nout of these crates!", Material.ENDER_CHEST,-1, ChatColor.AQUA, ChatColor.GRAY),

    MISCELLANEOUS("Misc Items", "\nClick here to view all miscellaneous items!", Material.BLAZE_ROD,5, ChatColor.GOLD);

    private String name;
    private String description;
    private Material displayItem;
    private ChatColor displayNameColor;
    private ChatColor displayDescriptionColor;
    private int guiSlot;

    WebstoreCategories(String name, String description, Material displayItem, int guiSlot) {
        this(name,description,displayItem, guiSlot,ChatColor.WHITE);
    }

    WebstoreCategories(String name, String description, Material displayItem,int guiSlot, ChatColor displayNameColor) {
        this(name,description,displayItem, guiSlot,displayNameColor, ChatColor.GRAY);
    }

    WebstoreCategories(String name, String description, Material displayItem,int guiSlot, ChatColor displayNameColor, ChatColor displayDescColor) {
        this.name = name;
        this.description = description;
        this.displayItem = displayItem;
        this.displayNameColor = displayNameColor;
        this.displayDescriptionColor = displayDescColor;
        this.guiSlot = guiSlot;
    }

    public List<String> getDescription(boolean includeColor) {
        List<String> toReturn = new ArrayList<>();
        toReturn.addAll(Arrays.asList(description.split("\n")));
        if(includeColor) {
            for (int index = 0; index < toReturn.size(); index++) {
                String line = toReturn.get(index);
                toReturn.set(index, getDisplayDescriptionColor().toString() + line);
            }
        }
        return toReturn;
    }

    public List<String> getDescription() {
        return getDescription(true);
    }

    public static GUIMenu getGUI(WebstoreCategories category, Player player) {
        if(category == GLOBAL_BUFFS) return new GlobalBuffGUI(player);
        if(category == SUBSCRIPTIONS) return new SubscriptionsGUI(player);
        if(category == MISCELLANEOUS) return new MiscGUI(player);
        if(category == HATS) return new HatGUI(player);
        if(category == PETS) return new PetSelectionGUI(player,null);
        if(category == PLAYER_EFFECTS) return new PlayerEffectsGUI(player);
        if(category == CHEST_EFFECTS) return new ChestEffectsGUI(player);
        if(category == REALM_EFFECTS) return new RealmEffectsGUI(player);
        return null;
    }

}
