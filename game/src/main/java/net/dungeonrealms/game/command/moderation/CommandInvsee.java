package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.command.AccountInfo;
import net.dungeonrealms.game.player.inventory.menus.guis.support.CharacterSelectionGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Brad on 25/12/2016.
 */
public class CommandInvsee extends BaseCommand {
    public CommandInvsee() {
        super("invsee", "/<command> <player>", "View a player's inventory.", Collections.singletonList("mis"));
    }

    public static Map<UUID, AccountInfo> offline_inv_watchers = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player sender = (Player) s;

        if (!Rank.isGM(sender)) return false;

        if (args.length == 0) {
            s.sendMessage(usage);
            return true;
        }

        String playerName = args[0];
        if (Bukkit.getPlayer(playerName) != null) {
            sender.openInventory(Bukkit.getPlayer(playerName).getInventory());
        } else {

            SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, uuid -> {
                        if (uuid == null) {
                            sender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms");
                            return;
                        }

                        Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);
                        if (accountID == null) {
                            sender.sendMessage(ChatColor.RED + "This player has never logged in with Dungeon Realms");
                            return;
                        }

                        new CharacterSelectionGUI(sender, accountID, (charID) -> {

                            PlayerWrapper.getPlayerWrapper(uuid, charID, false, false, (wrapper) -> {
                                if (wrapper == null || wrapper.getPendingInventory() == null) {
                                    sender.sendMessage(ChatColor.RED + "An error occurred.");
                                    return;
                                }
                                sender.openInventory(wrapper.getPendingInventory());
                                offline_inv_watchers.put(sender.getUniqueId(), new AccountInfo(uuid, charID));
                            });
                        }).open(sender, null);
                    }
            );
        }
        return false;
    }
}
