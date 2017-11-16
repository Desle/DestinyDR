package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent.ItemInventoryListener;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueUtils;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ItemEnchantScroll extends FunctionalItem implements ItemClickListener, ItemInventoryListener {

    @Getter
    private ItemTier tier;

    protected String enchantType;

    public ItemEnchantScroll(ItemTier tier, ItemType type, String enchantType) {
        super(type);
        setTier(tier);
        this.enchantType = enchantType;
    }

    public ItemEnchantScroll(ItemStack stack) {
        super(stack);
        if (hasTag(TIER))
            setTier(ItemTier.getByTier(getTagInt(TIER)));
    }

    public ItemEnchantScroll setTier(ItemTier tier) {
        this.tier = tier;
        return this;
    }

    protected abstract boolean isApplicable(ItemStack item);

    @Override
    public void updateItem() {
        if (getTier() != null)
            setTagInt(TIER, getTier().getId());
        super.updateItem();
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        evt.getPlayer().sendMessage(ChatColor.RED + "To use a " + ChatColor.BOLD + "SCROLL" + ChatColor.RED + ", simply drag it on-top of the piece of equipment you wish to apply it to in your inventory.");
    }

    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        ItemStack upgradeItem = evt.getSwappedItem();
        evt.setCancelled(true);
        if (!isApplicable(upgradeItem))
            return;

        ItemGear gear = (ItemGear) PersistentItem.constructItem(upgradeItem);

        if (gear.getTier() != getTier() && !(this instanceof ItemEnchantProfession)) {
            evt.getPlayer().sendMessage(ChatColor.RED + "This enchant scroll is not meant for this weapon tier.");
            return;
        }

        if (gear.getEnchantCount() >= 8 && gear.isProtected() && gear.getEnchantCount() < 12);

        else if(gear.getEnchantCount() >= 8 && gear.getEnchantCount() < 12) {
            evt.getPlayer().sendMessage(ChatColor.RED + "This item cannot be enchanted further unless you use a white scroll.");
            return;
        } else if(gear.getEnchantCount() >= 12) {
            evt.getPlayer().sendMessage(ChatColor.RED + "This item has reached it's maximum level!");
            return;
        }

        NBTWrapper wrapper = new NBTWrapper(upgradeItem);
        if (wrapper.hasTag("customId")) {
            String id = wrapper.getString("customId");

            //Healer set.
            if(id != null && id.startsWith("healer_")){

                if(gear.getEnchantCount() >= 3){
                    evt.getPlayer().sendMessage(ChatColor.RED + "You cannot add more than 3 Enchants to Healer Armor!");
                    return;
                }
            }
        }

        //  ENCHANT ITEM  //
            evt.setUsed(true);
            enchant(evt.getPlayer(), gear);
            evt.setSwappedItem(gear.generateItem());

            ClueUtils.handleAddEnchantToItem(evt.getPlayer(), this, evt.getSwappedItem());
    }

    public void enchant(Player player, ItemGear gear) {
        gear.enchantItem(player);
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.WHITE + "" + ChatColor.BOLD + "Scroll: " + tier.getColor() + "Enchant " + (this instanceof ItemEnchantArmor ? tier.getArmorName() : tier.getWeaponName()) + " " + this.enchantType;
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[]{ItemUsage.INVENTORY_SWAP_PLACE, ItemUsage.LEFT_CLICK_AIR,
                ItemUsage.LEFT_CLICK_ENTITY, ItemUsage.RIGHT_CLICK_ENTITY,
                ItemUsage.LEFT_CLICK_BLOCK, ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_BLOCK};
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.EMPTY_MAP);
    }

    public static boolean isScroll(ItemStack item) {
        return ItemEnchantWeapon.isEnchant(item) || ItemEnchantArmor.isEnchant(item) || ItemProtectionScroll.isEnchant(item)
                || ItemEnchantPickaxe.isEnchant(item) || ItemEnchantFishingRod.isEnchant(item);
    }
}
