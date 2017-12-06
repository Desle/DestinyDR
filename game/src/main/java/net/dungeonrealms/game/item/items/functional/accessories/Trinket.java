package net.dungeonrealms.game.item.items.functional.accessories;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.mechanic.TrinketMechanics;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@AllArgsConstructor
public enum Trinket {
    //    MULTI_LINE(Item.ItemRarity.RARE, "Multirod", new AbstractTrinketData("Cast two lines at once"), 20),
    FISH_TRIPLE_FISH(Item.ItemRarity.UNCOMMON, "Bountiful", new EnchantTrinketData(Item.FishingAttributeType.TRIPLE_CATCH, 1, 3), 15),
    FISH_JUNK_FIND(Item.ItemRarity.UNCOMMON, "Spelunker", new EnchantTrinketData(Item.FishingAttributeType.JUNK_FIND, 1, 5), 15),
    FISH_CATCH_SUCCESS(Item.ItemRarity.UNCOMMON, "Trusty", new EnchantTrinketData(Item.FishingAttributeType.CATCH_SUCCESS, 4, 8), 15),
    FISH_DURABILITY(Item.ItemRarity.COMMON, "Sturdy", new EnchantTrinketData(Item.FishingAttributeType.DURABILITY, 5, 15), 30),
    FISH_DOUBLE_FISH(Item.ItemRarity.COMMON, "Ample", new EnchantTrinketData(Item.FishingAttributeType.DOUBLE_CATCH, 1, 5), 30),
    FISH_TREASURE_FIND(Item.ItemRarity.RARE, "Lucky", new EnchantTrinketData(Item.FishingAttributeType.TREASURE_FIND, 1, 1), 4),
    FISH_SCALER(Item.ItemRarity.UNCOMMON, null, new AbstractTrinketData("Cleans fish of any effects"), 10),
    FISH_DAY_SUCCESS(Item.ItemRarity.UNCOMMON, null, "the Sun", new AbstractTrinketData("Better Fishing during Daylight"), 10),
    FISH_NIGHT_SUCCESS(Item.ItemRarity.COMMON, null, "the Moon", new AbstractTrinketData("Better Fishing during the Night"), 20),
    FISH_HP_FISH(Item.ItemRarity.RARE, null, "Healing", new AbstractTrinketData("Increase HP fish caught by 10%"), 3),
    FISH_SPEED_FISH(Item.ItemRarity.RARE, null, "Haste", new AbstractTrinketData("Increase speed fish caught by 10%"), 3),
    //FISH_ONE_TYPE(Item.ItemRarity.RARE, null, "Scrupulous", new AbstractTrinketData("Only fish one type of fish"), 3),

    ORE_RESPAWN(Item.ItemRarity.RARE, "Magical", new AbstractTrinketData("Ore comes back 10% of the time"), 1),
    NO_MINING_FATIGUE(Item.ItemRarity.RARE, "Haste", new AbstractTrinketData("No Mining Fatigue"), 1),
    MINE_TREASURE_FIND(Item.ItemRarity.UNCOMMON, "Lucky", new EnchantTrinketData(Item.PickaxeAttributeType.TREASURE_FIND, 1, 1), 20),
    MINE_GEM_FIND(Item.ItemRarity.UNCOMMON, "Opulent", new EnchantTrinketData(Item.PickaxeAttributeType.GEM_FIND, 1, 5), 20),
    MINE_GEM_TELEPORT(Item.ItemRarity.RARE, "Magnetic", new AbstractTrinketData("Gem Find gems are automatically picked up"), 3),
    MINE_TRIPLE_ORE(Item.ItemRarity.UNCOMMON, "Bountiful", new EnchantTrinketData(Item.PickaxeAttributeType.TRIPLE_ORE, 1, 3), 15),
    MINE_DURABILITY(Item.ItemRarity.COMMON, "Sturdy", new EnchantTrinketData(Item.PickaxeAttributeType.DURABILITY, 5, 15), 25),
    MIN_DOUBLE_ORE(Item.ItemRarity.COMMON, "Ample", new EnchantTrinketData(Item.PickaxeAttributeType.DOUBLE_ORE, 1, 5), 25),

    //Rifts
    RIFT_LAVA_TRAIL(Item.ItemRarity.RARE, "Cooling", new AbstractTrinketData("Lava trail is now obsidian trail"), 20),
    INCREASED_RIFT(Item.ItemRarity.RARE, "Lucky", new AbstractTrinketData("2-4 Rift Shards per Rift Walker"), 10),
    UPCOMING_RIFT(Item.ItemRarity.RARE, null, "Clairvoyance", new AbstractTrinketData("Ability to detect upcoming Rift Locations"), 3),
    RIFT_DAMAGE_INCREASE(Item.ItemRarity.UNCOMMON, "Menacing", new AbstractTrinketData("Increased Damage to Rift Enemies"), 10),
    DUNGEON_TELEPORT(Item.ItemRarity.UNCOMMON, "Teleporting", "Forgiveness", new AbstractTrinketData("TP to Party Members in Boss Rooms with /djoin"), 4),

    //Special
    REDUCED_REPAIR(Item.ItemRarity.UNIQUE, "Forging", new AbstractTrinketData("Repairing items is now 50% cheaper"), 1),
    COMBAT_SPEED(Item.ItemRarity.UNIQUE, "Agile", new AbstractTrinketData("10% chance to get speed 2 on hit"), 1),
    COMBAT_SLOW_RESIST(Item.ItemRarity.UNIQUE, "Resistant", new AbstractTrinketData("Resistant to all incoming slows"), 1),
    COMBAT_LIFESTEAL(Item.ItemRarity.UNIQUE, "Vampyric", new AbstractTrinketData("Gain 20% lifesteal when below 25% of your max HP"), 1),
    COMBAT_DURABILITY(Item.ItemRarity.UNIQUE, "Durable", new AbstractTrinketData("Weapons now have double durability."), 1),
    COMBAT_RARE(Item.ItemRarity.RARE, null, new RandomEnchantTrinketDataRare(), 6),
    COMBAT(Item.ItemRarity.COMMON, null, new RandomEnchantTrinketData(), 260);

    @Getter
    private Item.ItemRarity itemRarity;

    private String prefix;

    private String suffix;

    private TrinketData data;

    private double chance;

    Trinket(Item.ItemRarity rarity, String prefix, double chance) {
        this(rarity, prefix, null, null, chance);
    }

    Trinket(Item.ItemRarity rarity, String prefix, TrinketData data, double chance) {
        this(rarity, prefix, null, data, chance);
    }


    public Integer getValue() {
        if (getData() instanceof EnchantTrinketData) {
            EnchantTrinketData data = (EnchantTrinketData) getData();
            int min = data.getMin(), max = data.getMax();

            int random = max - min;
            if (random <= 0) return min;
            return ThreadLocalRandom.current().nextInt(random) + min;
        }
        return null;
    }

    //Top left
    public static final int TRINKET_SLOT = 9;

    public static boolean hasActiveTrinket(Player player, Trinket trinket) {
        Trinket active = getActiveTrinket(player, false);
        return active != null && active == trinket;
    }

    public static boolean hasActiveTrinket(Player player, Trinket trinket, boolean checkCache) {
        Trinket active = getActiveTrinket(player, checkCache);
        return active != null && active == trinket;
    }

    /**
     * Get the active trinket the player has in the TRINKET_SLOT.
     * If the item found is not a valid instanceof of @{@link TrinketItem} then it will return null
     *
     * @param player
     * @return active trinket
     */
    public static Trinket getActiveTrinket(Player player, boolean checkCache) {
        if (checkCache) {
            TrinketItem item = TrinketMechanics.lastTrinketItem.get(player.getUniqueId());
            return item != null ? item.getTrinket() : null;
        }
        TrinketItem item = getActiveTrinketItem(player);
        return item != null ? item.getTrinket() : null;
    }

    public static TrinketItem getActiveTrinketItem(Player player) {
        ItemStack item = player.getInventory().getItem(TRINKET_SLOT);
        if (item != null && item.getType() != Material.AIR) {
            //Possible trinket?
            PersistentItem found = PersistentItem.constructItem(item);
            if (found != null && found instanceof TrinketItem) {
                return (TrinketItem) found;
            }
        }
        return null;
    }

    public static int getTrinketValue(Player player, Trinket trinket) {
        TrinketItem item = getActiveTrinketItem(player);
        if (item != null && item.getTrinket().equals(trinket)) {
            return item.getValue() != null ? item.getValue() : 0;
        }

        return 0;
    }

    public static Trinket getFromName(String name) {
        return Arrays.stream(values()).filter(t -> t.name().equals(name)).findFirst().orElse(null);
    }

    public TrinketType getType() {
        for(TrinketType type : TrinketType.values()) {
            if(type.getAccessory().contains(this)) return type;
        }

        return null;
    }
}
