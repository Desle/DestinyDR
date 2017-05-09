package net.dungeonrealms.common;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Constants {

    public static Logger log = Logger.getLogger("DungeonRealms");

    public static boolean debug;

    public static List<String> DEVELOPERS = Arrays.asList("Bradez1571", "Kneesnap", "iFamasssxD", "Ingot");

    public static String MOTD = "                   &6&lDUNGEON REALMS &r\n            &lThe #1 Minecraft MMORPG &f&l";

    public static String MAINTENANCE_MOTD = "                   &6&lDUNGEON REALMS &r\n            &lThe #1 Minecraft MMORPG &f&l";

    public static long MIN_GAME_TIME = 14100000L;

    public static long MAX_GAME_TIME = 21300000L;

    public static int PLAYER_SLOTS = 1300;

    // BACKEND SERVER SERVER PORT //
//    public static String MASTER_SERVER_IP = "158.69.23.169";
    public static String MASTER_SERVER_IP = "158.69.121.40";
    // BACKEND SERVER SERVER PORT //
    public static int MASTER_SERVER_PORT = 22965;

    public static int NET_READ_BUFFER_SIZE = 16384;
    public static int NET_WRITE_BUFFER_SIZE = 32768;

    // BUILD NUMBER //
    public static String BUILD_NUMBER = "#0";

    public static void build() {
        log = Logger.getLogger("DungeonRealms");
    }

}
