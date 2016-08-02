package net.dungeonrealms.game.world.entity.type.monster.StaffMobs;

import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRBlaze;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class BasicEntityBlaze extends DRBlaze {

	/**
	 * @param world
	 * @param mons
	 * @param tier
	 */
	public BasicEntityBlaze(World world, EnumMonster mons, int tier) {
		super(world, mons, tier, EnumEntityType.HOSTILE_MOB, true);
	}

	public BasicEntityBlaze(World world) {
		super(world);
	}

	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}


	@Override
	protected void setStats() {

	}

}
