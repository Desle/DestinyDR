package net.dungeonrealms.game.world.entity.util;

import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * Created by Kieran on 9/19/2015.
 */
public class EntityAPI {

    public static boolean hasPetOut(UUID uuid) {
        return EntityMechanics.PLAYER_PETS.containsKey(uuid);
    }

    public static boolean hasMountOut(UUID uuid) {
        return EntityMechanics.PLAYER_MOUNTS.containsKey(uuid);
    }

    public static Entity getPlayerPet(UUID uuid) {
        return EntityMechanics.PLAYER_PETS.get(uuid);
    }

    public static Entity getPlayerMount(UUID uuid) {
        return EntityMechanics.PLAYER_MOUNTS.get(uuid);
    }

    public static void removePlayerPetList(UUID uuid) {
        if (EntityMechanics.PLAYER_PETS.containsKey(uuid)) {
            EntityMechanics.PLAYER_PETS.remove(uuid);
        }
    }

    public static void removePlayerMountList(UUID uuid) {
        if (EntityMechanics.PLAYER_MOUNTS.containsKey(uuid)) {
            EntityMechanics.PLAYER_MOUNTS.remove(uuid);
        }
    }

    public static void addPlayerPetList(UUID uuid, Entity entity) {
        EntityMechanics.PLAYER_PETS.put(uuid, entity);
    }

    public static void addPlayerMountList(UUID uuid, Entity entity) {
        EntityMechanics.PLAYER_MOUNTS.put(uuid, entity);
    }

    /**
     * Get all nearby entities within a certain radius to untarget another entity.
     *
     * @param entToUntarget
     * @param radius
     */
    public static void untargetEntity(LivingEntity entToUntarget, int radius) {
        entToUntarget.getNearbyEntities(radius, radius, radius).stream().forEach(ent -> {
            if (!(ent instanceof Creature)) return;
            if (((Creature) ent).getTarget() == null || !((Creature) ent).getTarget().equals(entToUntarget)) return;
            ((Creature) ent).setTarget(null);
        });
    }

}
