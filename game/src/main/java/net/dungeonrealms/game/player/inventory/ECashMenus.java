package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Kieran Quigley (Proxying) on 26-Jun-16.
 */
public class ECashMenus {

    public static void openEcashPets(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 18, "E-Cash Pets");
        inventory.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        for (EnumPets pet : EnumPets.values()) {
            if (pet == EnumPets.BABY_HORSE || pet == EnumPets.SNOWMAN || pet == EnumPets.CREEPER_OF_INDEPENDENCE || pet == EnumPets.SILVERFISH) {
                //special pets
                continue;
            }
            ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 1, (short) pet.getEggShortData());
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.setString("petType", pet.getRawName());
            tag.setInt("eCash", 550);
            nmsStack.setTag(tag);
            inventory.addItem(editItemWithShort(CraftItemStack.asBukkitCopy(nmsStack), (short) pet.getEggShortData(), ChatColor.YELLOW + pet.getDisplayName(), new String[]{
                    ChatColor.WHITE + "549" + ChatColor.GREEN + " E-Cash",
                    ChatColor.GRAY + "Display Item"
            }));
        }

        player.openInventory(inventory);
    }

    public static void openMountSkins(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "E-Cash Skins");
        inventory.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inventory.addItem(new ItemBuilder().setItem(Material.SKULL_ITEM, (short) 2, ChatColor.GREEN + "Zombie Horse Skin", new String[]{
                ChatColor.RED + "Requires a mount to purchase",
                ChatColor.WHITE + "1250" + ChatColor.GREEN + " E-Cash",
                ChatColor.GRAY + "Display Item"}).setNBTString("skinType", EnumMountSkins.ZOMBIE_HORSE.getRawName()).setNBTInt("eCash", 1250).build());
        inventory.addItem(new ItemBuilder().setItem(Material.SKULL_ITEM, (short) 0, ChatColor.GRAY + "Skeleton Horse Skin", new String[]{
                ChatColor.RED + "Requires a mount to purchase",
                ChatColor.WHITE + "1250" + ChatColor.GREEN + " E-Cash",
                ChatColor.GRAY + "Display Item"}).setNBTString("skinType", EnumMountSkins.SKELETON_HORSE.getRawName()).setNBTInt("eCash", 1250).build());

        player.openInventory(inventory);
    }

    public static void openEcashEffects(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 18, "E-Cash Effects");
        inventory.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        for (ParticleAPI.ParticleEffect effect : ParticleAPI.ParticleEffect.values()) {
            if (effect == ParticleAPI.ParticleEffect.BUBBLE || effect == ParticleAPI.ParticleEffect.SPELL || effect == ParticleAPI.ParticleEffect.LARGE_SMOKE || effect == ParticleAPI.ParticleEffect.LAVA) {
                //sketchy effects that don't do much.
                continue;
            }

            // Holiday Effects | Only to be sold in the BC shop.
            if (effect == ParticleAPI.ParticleEffect.VALENTINES)
                continue;

            int price = 650;
            if (effect == ParticleAPI.ParticleEffect.RED_DUST || effect == ParticleAPI.ParticleEffect.NOTE || effect == ParticleAPI.ParticleEffect.FLAME || effect == ParticleAPI.ParticleEffect.PORTAL
                    || effect == ParticleAPI.ParticleEffect.CLOUD || effect == ParticleAPI.ParticleEffect.SMALL_SMOKE) {
                price = 1250;
            }
            ItemStack itemStack = effect.getSelectionItem();
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.setString("effectType", effect.getRawName());
            tag.setInt("eCash", price);
            nmsStack.setTag(tag);
            inventory.addItem(editItemWithShort(CraftItemStack.asBukkitCopy(nmsStack), (short) 0, ChatColor.YELLOW + effect.getDisplayName(), new String[]{
                    ChatColor.WHITE + String.valueOf(price) + ChatColor.GREEN + " E-Cash",
                    ChatColor.GRAY + "Display Item"
            }));
        }

        player.openInventory(inventory);
    }

    public static void openEcashMisc(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "E-Cash Miscellaneous");
        inventory.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        inventory.addItem(new ItemBuilder().setItem(new ItemStack(Material.ENCHANTED_BOOK), ChatColor.GREEN + "Retraining Book", new String[]{
                ChatColor.GRAY + "Refund ALL Stat Points!",
                ChatColor.WHITE + "550" + ChatColor.GREEN + " E-Cash",
                ChatColor.GRAY + "Display Item"}).setNBTString("retrainingBook", "true").setNBTInt("eCash", 550).build());

        inventory.addItem(new ItemBuilder().setItem(new ItemStack(Material.FIREWORK), ChatColor.GOLD + "Global Messenger", new String[]{
                ChatColor.GOLD + "Uses: " + ChatColor.GRAY + "1", ChatColor.GRAY + "Sends a message to all players on " + ChatColor.UNDERLINE + "ALL SHARDS.",
                ChatColor.GRAY + "Permanent Untradeable",
                ChatColor.WHITE + "200" + ChatColor.GREEN + " E-Cash",
                ChatColor.GRAY + "Display Item"}).setNBTString("globalMessenger", "true").setNBTInt("eCash", 200).build());

        inventory.addItem(new ItemBuilder().setItem(ItemManager.createLootBuff(3600, 50)).addLore(ChatColor.WHITE + "3000" +
                ChatColor.GREEN + " E-Cash", ChatColor.GRAY + "Display Item").setNBTInt("eCash", 3000).build());

        inventory.addItem(new ItemBuilder().setItem(ItemManager.createProfessionBuff(3600, 50)).addLore(ChatColor.WHITE + "3000" +
                ChatColor.GREEN + " E-Cash", ChatColor.GRAY + "Display Item").setNBTInt("eCash", 3000).build());

        inventory.addItem(new ItemBuilder().setItem(ItemManager.createLevelBuff(3600, 50)).addLore(ChatColor.WHITE + "3000" +
                ChatColor.GREEN + " E-Cash", ChatColor.GRAY + "Display Item").setNBTInt("eCash", 3000).build());

        player.openInventory(inventory);
    }

    private static ItemStack editItemWithShort(ItemStack itemStack, short shortID, String name, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setDurability(shortID);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    private static ItemStack editItem(ItemStack itemStack, String name, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }
}
