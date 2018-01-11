package net.dungeonrealms.game.world.entity;

import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.NMSUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster.CustomEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.customprojectiles.*;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.potion.CraftPotionUtil;
import org.bukkit.entity.*;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Kieran on 9/18/2015.
 */
public class EntityMechanics implements GenericMechanic {

    @Getter
    private static EntityMechanics instance = new EntityMechanics();

    public static ConcurrentHashMap<LivingEntity, Integer> MONSTER_LAST_ATTACK = new ConcurrentHashMap<>();
    public static ConcurrentSet<LivingEntity> MONSTERS_LEASHED = new ConcurrentSet<>();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {

        //  REGISTER MONSTERS  //
        for (CustomEntityType type : CustomEntityType.values())
            type.register();

        //  REGISTER PETS  //
        for (EnumPets pet : EnumPets.values())
            if (!pet.isFrame())
                NMSUtils.registerEntity(pet.getClazz().getSimpleName(), pet.getEggShortData(), pet.getClazz());

        //  REGISTER MOUNTS  //
        for (EnumMounts m : EnumMounts.values())
            if (m.shouldRegister())
                NMSUtils.registerEntity(m.getClazz().getSimpleName(), m.getEntityId(), m.getClazz());

        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::checkForLeashedMobs, 0, 20L);
    }

    @Override
    public void stopInvocation() {

    }

    public static Projectile spawnFireballProjectile(World world, CraftLivingEntity shooter, Vector velocity, Class<? extends Projectile> projectile, double accuracy) {
        Location location = shooter.getEyeLocation();
        Vector direction = velocity == null ? location.getDirection().multiply(10) : velocity;

        Entity launch = null;
        double accurate = .4D - .4D * accuracy / 100D;
        if (Fireball.class.isAssignableFrom(projectile)) {
            if (SmallFireball.class.isAssignableFrom(projectile)) {
                launch = new CustomSmallFireball(world, shooter, direction.getX(), direction.getY(), direction.getZ(), accurate);
            } else if (WitherSkull.class.isAssignableFrom(projectile)) {
                //Pending
                launch = new CustomWitherSkull(world, direction.getX(), direction.getY(), direction.getZ(), shooter, accurate);
            } else if (DragonFireball.class.isAssignableFrom(projectile)) {
                launch = new CustomDragonFireball(world, shooter, direction.getX(), direction.getY(), direction.getZ(), accurate);
            } else {
                launch = new CustomLargeFireball(world, shooter, direction.getX(), direction.getY(), direction.getZ(), accurate);
            }
        } else if (projectile.isAssignableFrom(Arrow.class)) {
            if (TippedArrow.class.isAssignableFrom(projectile)) {
                launch = new CustomEntityTippedArrow(world, shooter.getHandle());
                ((EntityTippedArrow) launch).setType(CraftPotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
            } else {
                launch = new CustomEntityTippedArrow(world, shooter.getHandle());
            }

            if (shooter.getHandle() instanceof DRMonster)
                ((EntityArrow) launch).setKnockbackStrength(0);

            ((EntityArrow) launch).a(shooter.getHandle(), shooter.getHandle().pitch, shooter.getHandle().yaw, 0.0F, 3.0F, 1.0F);
        } else if(projectile.isAssignableFrom(SpectralArrow.class)) {
            if(SpectralArrow.class.isAssignableFrom(projectile)) {
                launch = new CustomEntitySpectralArrow(world, shooter.getHandle());
            }

            if (shooter.getHandle() instanceof DRMonster)
                ((EntitySpectralArrow) launch).setKnockbackStrength(0);

            ((EntitySpectralArrow) launch).a(shooter.getHandle(), shooter.getHandle().pitch, shooter.getHandle().yaw, 0.0F, 3.0F, 1.0F);
        }

        if (launch == null)
            return null;

        launch.setPosition(location.getX(), location.getY() - (shooter instanceof Skeleton && ((Skeleton) shooter).getSkeletonType() == Skeleton.SkeletonType.WITHER ? .45D : 0), location.getZ());
        if (velocity != null)
            launch.getBukkitEntity().setVelocity(velocity);

        world.addEntity(launch);
        return (Projectile) launch.getBukkitEntity();
    }

    public static void setVelocity(org.bukkit.entity.Entity player, Vector velocity) {

        if (Double.isNaN(velocity.getX()) || Double.isNaN(velocity.getY()) || Double.isNaN(velocity.getZ())) {
            Bukkit.getLogger().info("Prevented Crash due to velocity: " + velocity + " bound for " + player.getName() + " at " + player.getLocation().toString());
            //Get the source of the problem.
            try {
                Thread.dumpStack();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        player.setVelocity(velocity);
    }

    /**
     * Handles general restrictions on entities.
     * Could be named better.
     */
    private void checkForLeashedMobs() {
        for (LivingEntity entity : MONSTERS_LEASHED) {
            if (entity == null) {
                Utils.log.warning("[ENTITIES] [ASYNC] Mob is somehow leashed but null, safety removing!");
                continue;
            }
            if (entity.isDead() || (Metadata.DUNGEON.get(entity).asBoolean() && EntityAPI.isElite(entity)) || Metadata.BOSS.get(entity).asBoolean()) {
                MONSTERS_LEASHED.remove(entity);
                MONSTER_LAST_ATTACK.remove(entity);
                if (entity.isDead()) //Remove the entity if it's dead...
                    entity.remove();
                continue;
            }

            if (!MONSTER_LAST_ATTACK.containsKey(entity)) {
                MONSTER_LAST_ATTACK.put(entity, 15);
                continue;
            }

            int lastAttack = MONSTER_LAST_ATTACK.get(entity);
            EntityInsentient ei = (EntityInsentient) ((CraftEntity) entity).getHandle();

            if (entity.isInvulnerable()) continue;
            if (lastAttack == 11) {
                // Teleport back to spawnpoint if too far away.
                Location target = ei.getGoalTarget() != null ? ei.getGoalTarget().getBukkitEntity().getLocation() : null;
                if (target != null && target.getWorld().equals(entity.getWorld())) {
                    //Distance squared
                    double distance = target.distanceSquared(entity.getLocation());

                    // If they're a certain range away from the player and on a different Y level, they could be safe-spotting.
                    if ((distance >= (DungeonManager.isDungeon(target.getWorld()) ? 1 : 3)) && distance <= 6 * 6 && (DungeonManager.isDungeon(target.getWorld()) || target.getBlockY() > entity.getLocation().getBlockY() + 1)) {
                        entity.teleport(target.clone().add(0, 1, 0));
                        MONSTER_LAST_ATTACK.put(entity, 15);
                        Bukkit.getLogger().info("Teleporting " + entity + " to " + ei.getGoalTarget().getName() + " to prevent leashing!");
                    }
                }
            } else if (lastAttack == 10) {
                // Update entity name.
                EntityAPI.updateName(entity);
            } else if (lastAttack <= 0) {
                // Remove.
                MONSTERS_LEASHED.remove(entity);
                MONSTER_LAST_ATTACK.remove(entity);
                tryToReturnMobToBase(((CraftEntity) entity).getHandle());
                continue;
                // Reset goal.
//                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ei.setGoalTarget(null, TargetReason.CUSTOM, true), 220L);
            }

            MONSTER_LAST_ATTACK.put(entity, lastAttack - 1);
        }
    }

    private void tryToReturnMobToBase(Entity entity) {
        MetadataValue val = Metadata.SPAWN_LOCATION.get(entity.getBukkitEntity());
        if (val == null || val.value() == null) {
            return;
        }
        EntityInsentient entityInsentient = (EntityInsentient) entity;
        Location spawn = (Location) val.value();

        if(entity.getWorld().equals(spawn.getWorld()) && entity.getBukkitEntity().getLocation().distanceSquared(spawn) <= 20 * 20)
            return;

        entityInsentient.setGoalTarget(null);
        PathEntity path = entityInsentient.getNavigation().a(spawn.getX(), spawn.getY(), spawn.getZ());
        if (path == null || path.c() == null) {
            //Cant walk back? Just teleport.
            entity.getBukkitEntity().teleport(spawn);
            entityInsentient.setGoalTarget(null);
            if (entity.getBukkitEntity().hasMetadata("startVelocity"))
                EntityMechanics.setVelocity(entity.getBukkitEntity(), ((Vector) entity.getBukkitEntity().getMetadata("startVelocity").get(0).value()).clone());

        } else {
            entityInsentient.getNavigation().a(path, 1.56);
            entityInsentient.setGoalTarget(null);
        }
//            SpawningMechanics.getSpawners().stream().filter(mobSpawner -> mobSpawner.getSpawnedMonsters().contains(entity))
//                    .forEach(mobSpawner -> {
//                        Bukkit.getLogger().info("Calling walk back! found in spawner!");
//                        EntityArmorStand eas = (EntityArmorStand) ((CraftEntity) mobSpawner.getArmorStand()).getHandle();
//                        EntityInsentient entityInsentient = (EntityInsentient) entity;
//                        entityInsentient.setGoalTarget(eas, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
//                        Location l = mobSpawner.getLocation();
//                        PathEntity path = entityInsentient.getNavigation().a(l.getX(), l.getY(), l.getZ());
//                        entityInsentient.getNavigation().a(path, 2);
//                        double distance = mobSpawner.getArmorStand().getLocation().distance(entity.getBukkitEntity().getLocation());
//                        if ((distance > 30 || path == null) && !entity.dead) {
//                            Bukkit.getLogger().info("Path null!");
//                            entity.getBukkitEntity().teleport(mobSpawner.getArmorStand().getLocation());
//                            entityInsentient.setGoalTarget(eas, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
//                        }
//                    });
    }
}
