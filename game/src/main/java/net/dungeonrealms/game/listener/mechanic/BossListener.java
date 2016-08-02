package net.dungeonrealms.game.listener.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.DungeonManager.DungeonObject;
import net.dungeonrealms.game.world.entity.type.monster.EnumBoss;
import net.dungeonrealms.game.world.entity.type.monster.boss.Boss;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Created by Chase on Oct 18, 2015
 */
public class BossListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBossDeath(EntityDeathEvent event) {
		if (event.getEntity().hasMetadata("boss")) {
			event.getEntity().removeMetadata("boss", DungeonRealms.getInstance());
			if (event.getEntity() instanceof CraftLivingEntity) {
				Boss b = (Boss) ((CraftLivingEntity) event.getEntity()).getHandle();
				if (b.getEnumBoss() != EnumBoss.Pyromancer && b.getEnumBoss() != EnumBoss.InfernalGhast && b.getEnumBoss()!= EnumBoss.LordsGuard)
				if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()) != null) {
					DungeonObject dungeon = DungeonManager.getInstance().getDungeon(event.getEntity().getWorld());
					dungeon.teleportPlayersOut(false);
					dungeon.giveShards();
				}
				b.onBossDeath();
			}
		}
	}

}
