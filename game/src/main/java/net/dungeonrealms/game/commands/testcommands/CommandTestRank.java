package net.dungeonrealms.game.commands.testcommands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTestRank extends BasicCommand {

    public CommandTestRank(String command, String usage, String description) {
    	super(command, usage, description);
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player)sender;

		// This command can only be executed from US-0 or if the player is an OP on a live shard.
		if (!DungeonRealms.getInstance().isMasterShard && !p.isOp()) return false;

		p.sendMessage("Developer: " + Rank.isDev(p));
		p.sendMessage("Game Master: " + Rank.isGM(p));
		p.sendMessage("Support Agent: " + Rank.isSupport(p));
		p.sendMessage("Player Moderator: " + Rank.isPMOD(p));
		p.sendMessage("YouTuber: " + Rank.isYouTuber(p));
		p.sendMessage("Subscriber: " + Rank.isSubscriber(p));

		return true;
    }

}
