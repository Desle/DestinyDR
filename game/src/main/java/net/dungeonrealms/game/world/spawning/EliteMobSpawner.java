package net.dungeonrealms.game.world.spawning;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Location;

/**
 * Handles spawning of elites.
 * <p>
 * Redone on April 20th, 2017.
 *
 * @author Kneesnap
 */
public class EliteMobSpawner extends MobSpawner {

    @Setter
    @Getter
    private EnumNamedElite eliteType;

    public EliteMobSpawner(EnumNamedElite elite) {
        this(elite.getLocation(), elite.getDisplayName(), elite.getMonster(), elite.getTier(), elite.getRespawnDelay(), elite.getSpread());
        setEliteType(elite);
        if (getEliteType() != null)
            setMonsterType(getEliteType().getMonster());
    }

    public EliteMobSpawner(Location l, String displayName, EnumMonster m, int tier, int respawnDelay, int spread) {
        super(l, m, displayName, tier, 1, "low", respawnDelay, 1, spread);
        if (this.customName != null && !this.customName.isEmpty())
            this.customName = Item.ItemTier.getByTier(tier).getColor() + ChatColor.BOLD.toString() + displayName;
    }

    @Override
    public void init() {
        setTimer(this::spawnIn, 20);
    }

    @Override
    public int[] getDelays() {
        return new int[]{300, 500, 750, 1000, 1500};
    }
}
