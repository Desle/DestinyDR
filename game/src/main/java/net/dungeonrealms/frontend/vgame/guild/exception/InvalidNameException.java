package net.dungeonrealms.frontend.vgame.guild.exception;

/**
 * Created by Giovanni on 9-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class InvalidNameException extends Exception {

    public InvalidNameException(String name, String error) {
        super(name + " is not a valid name, " + error);
    }
}
