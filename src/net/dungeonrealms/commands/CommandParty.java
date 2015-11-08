package net.dungeonrealms.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.party.Party;
import net.md_5.bungee.api.ChatColor;

public class CommandParty extends BasicCommand {

    public CommandParty(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) {
            return false;
        }

        Player player = (Player) s;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You do not have permissions for this!");
            return false;
        }
        if (args.length > 0) {
            String command = args[0].toLowerCase();
            switch (command) {
                case "create":
                    Party.getInstance().createParty(player);
                    break;
                case "invite":
                    if (Party.getInstance().isOwnerOfParty(player)) {
                        if (Bukkit.getPlayer(args[1]) == null) {
                            player.sendMessage(ChatColor.RED + "The player you specified doesn't exist!?");
                            return false;
                        }
                        if (Bukkit.getPlayer(args[1]).equals(player)) {
                            Party.RawParty p = Party.getInstance().getPlayerParty(player);
                            if (!p.getInviting().contains(Bukkit.getPlayer(args[1]))) {
                                p.invitePlayer(Bukkit.getPlayer(args[1]));
                                player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You can't invite yourself to your own party..?");
                                return true;
                            } else {
                                player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "That player has already been invited!?");
                            }
                        }
                        Party.getInstance().invitePlayer(Bukkit.getPlayer(args[1]), Party.getInstance().getPlayerParty(player));
                        player.sendMessage(ChatColor.GREEN + "You have invited " + ChatColor.GOLD + ChatColor.GREEN + " to your party!");
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You do not own a party!");
                    }
                    break;
                case "disband":
                    if (Party.getInstance().isOwnerOfParty(player)) {
                        Party.getInstance().disbandParty(Party.getInstance().getPlayerParty(player));
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You do not own a party!");
                    }
                    break;
                case "kick":
                    if (Party.getInstance().isInParty(Bukkit.getPlayer(args[1]))) {
                        if (Party.getInstance().isOwnerOfParty(player)) {
                            Party.getInstance().kickPlayer(Party.getInstance().getPlayerParty(player), Bukkit.getPlayer(args[1]));
                        } else {
                            player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You do not own a party!");
                        }
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "That player is not in your party!");
                    }
                    break;
                case "quit":
                    if (Party.getInstance().isInParty(player)) {
                        Party.getInstance().quitParty(player);
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You are not in a party?");
                    }
                    break;
                case "leave":
                    if (Party.getInstance().isInParty(player)) {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "Did you mean, '/party quit' ?");
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You're not in a party..?");
                    }
                    break;
                default:
                    player.sendMessage("ERROR DEFAULT CALLED()..");
            }
        } else
            s.sendMessage(ChatColor.RED + "/party (create, invite, kick, disband)");
        return false;
    }

}