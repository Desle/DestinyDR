package net.dungeonrealms.game.ui;

import org.bukkit.event.Event;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */

public interface VolatileGUI {


    /**
     * This method will be called when the gui is destroyed
     *
     * @param event Event that fired the destruction
     */
    void onDestroy(Event event);

}
