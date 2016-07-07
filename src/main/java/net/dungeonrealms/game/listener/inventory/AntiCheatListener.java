package net.dungeonrealms.game.listener.inventory;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Created by Nick on 10/1/2015.
 */
public class AntiCheatListener implements Listener {

    /**
     * This event is used to checking if the player is duplicating
     * an item or item(s).
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        if (AntiCheat.getInstance().watchForDupes(event)) {
            Utils.log.warning("[ANTI-CHEAT] [DUPE] Player: " + event.getWhoClicked().getName());
            //Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(player -> Bukkit.broadcastMessage(ChatColor.RED + "(" + ChatColor.RED.toString() + ChatColor.BOLD + "!" + ChatColor.RED + ")" + ChatColor.BLUE + " Type:" + ChatColor.AQUA + " Duplication " + ChatColor.BLUE + "User: " + ChatColor.AQUA + event.getWhoClicked().getName() + ChatColor.BLUE + " Time: " + ChatColor.AQUA + System.currentTimeMillis()));
        }
    }

}
