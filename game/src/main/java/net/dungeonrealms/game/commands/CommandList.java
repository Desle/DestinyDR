package net.dungeonrealms.game.commands;

import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 9/11/2015.
 */
public class CommandList extends BasicCommand {

    public CommandList(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String string, String[] args) {

        if (commandSender instanceof ConsoleCommandSender || !Rank.isGM((Player) commandSender))
            return false;

        StringBuilder players = new StringBuilder();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (players.length() > 0) {
                players.append(ChatColor.AQUA).append(", ").append(ChatColor.GOLD);
            }
            players.append(player.getDisplayName());
        }

        String onlinePlayers = Bukkit.getOnlinePlayers().size() + "";
        commandSender.sendMessage(ChatColor.GREEN + "Players Online: " + ChatColor.LIGHT_PURPLE + onlinePlayers + ChatColor.GRAY + "/" + ChatColor.LIGHT_PURPLE + Bukkit.getMaxPlayers());
        commandSender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + players.toString() + ChatColor.GRAY + "]");

        return true;
    }
}
