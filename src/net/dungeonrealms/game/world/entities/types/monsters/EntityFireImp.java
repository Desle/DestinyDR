package net.dungeonrealms.game.world.entities.types.monsters;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.API;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRSkeleton;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Sep 21, 2015
 */
public class EntityFireImp extends DRSkeleton {

    /**
     * @param world
     * @param mobName
     * @param mobHead
     * @param tier
     * @param entityType
     */

    private int tier;

    public EntityFireImp(World world){
    	super(world);
    }
    
    @SuppressWarnings("unchecked")
    public EntityFireImp(World world, int tier, EnumEntityType entityType) {
        super(world, EnumMonster.FireImp, tier, entityType);
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0D, 20, 60, 15.0F));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
        this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.tier = tier;
        this.setEquipment(0, CraftItemStack.asNMSCopy(new ItemGenerator().setType(ItemType.STAFF)
                .setTier(ItemTier.getByTier(tier)).setRarity(API.getItemRarity()).getItem()));
    }

    @Override
    public void setArmor(int tier) {
        ItemStack leggings = new ItemGenerator().setType(ItemType.LEGGINGS).setTier(ItemTier.getByTier(tier)).setRarity(API.getItemRarity()).getItem();
        ItemStack chestplate = new ItemGenerator().setType(ItemType.CHESTPLATE).setTier(ItemTier.getByTier(tier)).setRarity(API.getItemRarity()).getItem();
        ItemStack boots = new ItemGenerator().setType(ItemType.BOOTS).setTier(ItemTier.getByTier(tier)).setRarity(API.getItemRarity()).getItem();
        this.setEquipment(1, CraftItemStack.asNMSCopy(boots));
        this.setEquipment(2, CraftItemStack.asNMSCopy(leggings));
        this.setEquipment(3, CraftItemStack.asNMSCopy(chestplate));
        this.setEquipment(4, getHead());
    }

    @Override
    protected void getRareDrop() {

    }
    
	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}

    @Override
    public void setStats() {

    }

    @Override
    public void a(EntityLiving entity, float f) {
        /*double d0 = entity.locX - this.locX;
        float f1 = MathHelper.c(f) * 0.5F;
        double d1 = entity.getBoundingBox().b + entity.length / 2.0F - (this.locY + this.length / 2.0F);
        double d2 = entity.locZ - this.locZ;
        EntityWitherSkull entityWitherSkull = new EntityWitherSkull(this.world, this, d0 + this.random.nextGaussian() * f1, d1, d2 + this.random.nextGaussian() * f1);
        entityWitherSkull.locY = this.locY + this.length / 2.0F + 0.5D;
        Projectile projectileWitherSkull = (Projectile) entityWitherSkull.getBukkitEntity();
        projectileWitherSkull.setVelocity(projectileWitherSkull.getVelocity().multiply(1.35));
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
        NBTTagCompound tag = nmsItem.getTag();
        MetadataUtils.registerProjectileMetadata(tag, projectileWitherSkull, tier);
        this.makeSound("random.bow", 1.0F, 1.0F / (0.8F));
        this.world.addEntity(entityWitherSkull);*/

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
        NBTTagCompound tag = nmsItem.getTag();
        DamageAPI.fireStaffProjectileMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entity.getBukkitEntity());
    }

}
