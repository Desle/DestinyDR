package net.dungeonrealms.game.handler;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Nick on 10/14/2015.
 */
public class MailHandler {

    static MailHandler instance = null;

    public static MailHandler getInstance() {
        if (instance == null) {
            instance = new MailHandler();
        }
        return instance;
    }

    /**
     * Will give the player the item they have clicked on.
     *
     * @param item   serialized mail item.
     * @param player who to give item to?
     * @since 1.0
     */
    public void giveItemToPlayer(ItemStack item, Player player) {
        if (isMailItem(item)) {
            player.closeInventory();
            String serializedItem = CraftItemStack.asNMSCopy(item).getTag().getString("item");
            String from = serializedItem.split(",")[0];
            long unix = Long.valueOf(serializedItem.split(",")[1]);
            String rawItem = serializedItem.split(",")[2];

            ItemStack actualItem = ItemSerialization.itemStackFromBase64(rawItem);


            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.MAILBOX, from + "," + String.valueOf(unix) + "," + rawItem, true);
            player.getInventory().addItem(actualItem);
            sendMailMessage(player, ChatColor.GREEN + "You opened mail from " + ChatColor.AQUA + from + ChatColor.GREEN + ".");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 1f, 63f);
        }
    }

    /**
     * Checks of the item that is specified is a mail item containing NBT.
     *
     * @param item specify if the item is of the serialized item type.
     * @return boolean
     * @since 1.0
     */
    private boolean isMailItem(ItemStack item) {
        return !(item == null || item.getType() == null || item.getType().equals(Material.AIR)) && CraftItemStack.asNMSCopy(item).hasTag() && CraftItemStack.asNMSCopy(item).getTag() != null && CraftItemStack.asNMSCopy(item).getTag().hasKey("item");
    }

    /**
     * Will apply the hidden data to an item.
     *
     * @param itemStack              item applying hidden date to..
     * @param base64SerializedString The encrypted string.
     * @return
     * @since 1.0
     */
    public ItemStack setItemAsMail(ItemStack itemStack, String base64SerializedString) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("mail"));
        tag.set("item", new NBTTagString(base64SerializedString));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * @param player    From e.g. (xFinityPro PLAYER OBJECT)
     * @param to        To e.g. (Proxying)
     * @param itemStack The package!
     * @since 1.0
     */
    public boolean sendMail(Player player, String to, ItemStack itemStack) {
        UUID toUUID;
        if (!GameAPI.isItemTradeable(itemStack)) {
            player.sendMessage(ChatColor.RED + "This item cannot be sent via mail.");
            return false;
        }
        String result = DatabaseAPI.getInstance().getUUIDFromName(to);
        if (result.equals("")) {
            player.sendMessage(ChatColor.RED + "This player does not exist.");
            return false;
        } else toUUID = UUID.fromString(result);

        String serializedItem = ItemSerialization.itemStackToBase64(itemStack);

        String mailIdentification = player.getName() + "," + (System.currentTimeMillis() / 1000L) + "," + serializedItem;

        if (GameAPI.isOnline(toUUID)) {
            DatabaseAPI.getInstance().update(toUUID, EnumOperators.$PUSH, EnumData.MAILBOX, mailIdentification,
                    true);
            sendMailMessage(Bukkit.getPlayer(toUUID), getMailMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " has sent you Mail."));
        } else {
            DatabaseAPI.getInstance().update(toUUID, EnumOperators.$PUSH, EnumData.MAILBOX, mailIdentification, true, doAfter -> {
                GameAPI.updatePlayerData(toUUID);
                BungeeUtils.sendPlayerMessage(to, getMailMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " has sent you Mail."));
            });
        }
        sendMailMessage(player, ChatColor.GREEN + "You have sent " + ChatColor.GOLD + to + ChatColor.GREEN + " Mail.");
        return true;
    }

    /**
     * @param player  who to send message to?
     * @param message string message.
     * @since 1.0N
     */
    public void sendMailMessage(Player player, String message) {
        player.sendMessage(getMailMessage(message));
    }

    private String getMailMessage(String message) {
        return ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "Mail" + ChatColor.WHITE + "]" + " " + message;
    }

}
