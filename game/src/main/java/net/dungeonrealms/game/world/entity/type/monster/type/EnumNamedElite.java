package net.dungeonrealms.game.world.entity.type.monster.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.WorldType;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster.CustomEntityType;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Created by Kieran Quigley (Proxying) on 09-Jun-16.
 */
@AllArgsConstructor
public enum EnumNamedElite {

    // TIER 1 //
    MITSUKI("Mitsuki The Dominator", 1, EnumMonster.Bandit, CustomEntityType.MELEE_ZOMBIE, -406, 137, 28, 300, 1, WorldType.ELORA),
    OLD_WHEAT_FARMER("Old Wheat Farmer", 1, EnumMonster.Bandit1, CustomEntityType.MELEE_SKELETON, -612, 74, 249, 400, 1, WorldType.ELORA),
    OLD_CARROT_FARMER("Old Carrot Farmer", 1, EnumMonster.Bandit1, CustomEntityType.MELEE_SKELETON, -680, 71, 275, 400, 1, WorldType.ELORA),

    // TIER 2 //
    COPJAK("Cop'jak", 2 ,EnumMonster.Troll1, CustomEntityType.MELEE_ZOMBIE, -82, 99, 1270, 1500, 2),
    LORD_TAYLOR("Lord Taylor", 2, EnumMonster.Zombie, CustomEntityType.MELEE_WITHER, 93, 97, -50, 400, 2, WorldType.ELORA),
    QUEEN_OF_THE_NEST("Queen of the Nest", 2, EnumMonster.Spider1, CustomEntityType.LARGE_SPIDER, 212, 28, 258, 500, 1, WorldType.ELORA),

    // TIER 3 //
    IMPA("Impa The Impaler", 3, EnumMonster.Imp, CustomEntityType.MELEE_WITHER, -144, 144, -3533, 1800, 1),
    GREED_KING("The King of Greed", 3, EnumMonster.Goblin, CustomEntityType.MELEE_ZOMBIE, 650, 120, 836, 2400, 3),
    ZION("Skeleton King Zion", 3, EnumMonster.Skeleton, CustomEntityType.MELEE_SKELETON, 841, 61, 1311, 2000, 2),
    PYRO_BANDIT("Pyro Bandit", 3, EnumMonster.Bandit, CustomEntityType.MELEE_WITHER, -331, 67, -407, 1900, 1), //650 - 810 Chest
    REDEYE_THE_CRUEL("Redeye The Cruel", 3, EnumMonster.Bandit, CustomEntityType.MELEE_WITHER, 188, 61, -594, 2100, 1), //790-920 HP Chest
    YAHOVA("Yahova the Officer", 3, EnumMonster.Skeleton, CustomEntityType.BOW_SKELETON, -1112, 183, -695, 2100, 1),

    // TIER 4 //
    BLAYSHAN("Blayshan The Naga", 4, EnumMonster.Naga, CustomEntityType.MELEE_ZOMBIE, -278, 85, -202, 2500, 3),
    DURANOR("Duranor the Cruel", 4, EnumMonster.Imp, CustomEntityType.MELEE_SKELETON, -124, 162, -3167, 1300, 1),
    //    MOUNTAIN_KING("Mountain King", 4, EnumMonster.Skeleton2, CustomEntityType.BOW_SKELETON, -1649, 149, 1388, 1300, 1),
    MOTHER_OF_DOOM("Mother of Doom", 4, EnumMonster.Spider2, CustomEntityType.LARGE_SPIDER, -191, 144, -3621, 1200, 2),
    BLOOD_BUTCHER("Blood Butcher", 4, EnumMonster.Skeleton1, CustomEntityType.MELEE_WITHER, 230, 35, -3542, 2600, 1), //Better then blayshan pretty much
    //AZEMAR_THE_GREAT("Azemar The Great", 4, EnumMonster.Skeleton, CustomEntityType.BOW_SKELETON, -465, 27, 1512, 2600, 1),

    // TIER 5 //
    KILATAN("Daemon Lord Kilatan", 5, EnumMonster.Imp, CustomEntityType.BOW_SKELETON, -411, 33, -3487, 1800, 1),
    LIBRARIAN("The Librarian", 5, EnumMonster.Skeleton, CustomEntityType.BOW_SKELETON, -1590, 63, 1218, 2500, 1),
    PRISCILLA("Priscilla Queen of Ice", 5, EnumMonster.IceLord, CustomEntityType.MELEE_WITHER, -1273, 45, -673, 2500, 1),
    FORGOTTEN_LIBRARIAN("The Forgotten Librarian", 5, EnumMonster.Monk, CustomEntityType.MELEE_WITHER, -981, 108, -842, 2500, 1),
    CYRITH("Cyrith The Spider God", 5, EnumMonster.Spider2, CustomEntityType.LARGE_SPIDER, 788, 30, 842, 2500, 1);

    @Getter private String displayName;
    @Getter private int tier;
    @Getter private EnumMonster monster;
    @Getter private CustomEntityType entity;

    private int x;
    private int y;
    private int z;

    @Getter private int respawnDelay;
    @Getter private int spread;
    @Getter private WorldType world;

    EnumNamedElite(String eliteName, int tier, EnumMonster monster, CustomEntityType type, int x, int y, int z, int respawn, int spread) {
        this(eliteName, tier, monster, type, x, y ,z , respawn, spread, WorldType.ANDALUCIA);
    }

    /**
     * Return the location to spawn this elite at in the main world.
     */
    public Location getLocation() {
        return getLocation(getWorld().getWorld());
    }

    /**
     * Generates a random level for this elite.
     */
    public int randomLevel() {
        return Utils.getRandomFromTier(getTier(), "low");
    }

    /**
     * Return the location to spawn this elite at.
     */
    public Location getLocation(World w) {
        return new Location(w, x, y, z);
    }

    public static EnumNamedElite getFromName(String name) {
        for (EnumNamedElite elite : values())
            if (elite.name().equalsIgnoreCase(name))
                return elite;
        return null;
    }
}
