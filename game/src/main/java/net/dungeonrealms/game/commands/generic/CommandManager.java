package net.dungeonrealms.game.commands.generic;

/**
 * Created by Nick on 10/24/2015.
 */
public class CommandManager {

    public void registerCommand(BasicCommand command) {
        command.register();
    }

}
