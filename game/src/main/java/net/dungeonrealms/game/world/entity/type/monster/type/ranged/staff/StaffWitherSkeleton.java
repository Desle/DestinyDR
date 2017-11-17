package net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff;

import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.entity.LivingEntity;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class StaffWitherSkeleton extends DRWitherSkeleton implements IRangedEntity {

    public StaffWitherSkeleton(World world) {
        super(world, EnumMonster.Skeleton);
        
        this.goalSelector.a(0, new PathfinderGoalArrowAttack(this, 1.0D, 15, 35, 20.0F));
        this.goalSelector.a(1, new PathfinderGoalRandomStroll(this, .6F));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new PathfinderGoalHurtByTarget(this, false, EntityHuman.class));
        this.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public org.bukkit.inventory.ItemStack getWeapon() {
    	return makeItem(new ItemWeaponBow());
    }

    @Override
    protected void r() {
    }

    @Override
    public void o() {
    }

    @Override
    public void a(EntityLiving entity, float f) {
    	DamageAPI.fireStaffProjectile((LivingEntity)getBukkitEntity(), new ItemWeaponBow(getHeld()));
    }
}
