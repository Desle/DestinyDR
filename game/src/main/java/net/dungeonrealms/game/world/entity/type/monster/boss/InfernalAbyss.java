package net.dungeonrealms.game.world.entity.type.monster.boss;

import lombok.Getter;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedWitherSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

/**
 * InfernalAbyss Boss
 * <p>
 * Redone on April 28th, 2017.
 *
 * @author Kneesnap
 */
public class InfernalAbyss extends RangedWitherSkeleton implements DungeonBoss {

    @Getter
    private InfernalGhast ghast;
    @Getter
    private boolean finalForm;

    public InfernalAbyss(World world) {
        super(world);
        this.fireProof = true;

        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(40);
    }

    @Override
    public BossType getBossType() {
        return BossType.InfernalAbyss;
    }

    public void doFinalForm(int hp) {
//        getBukkit().teleport(getDungeon().getBoss().get)
        HealthHandler.initHP(getBukkit(), hp);

        getBukkit().setMaximumNoDamageTicks(0);
        getBukkit().setNoDamageTicks(0);

        finalForm = true;

        DamageAPI.setDamageBonus(getBukkit(), 50);
        say("You... cannot... kill me IN MY OWN DOMAIN, FOOLISH MORTALS!");
        getDungeon().announce(ChatColor.GRAY + "The Infernal Abyss has become enraged! " + ChatColor.UNDERLINE + "+50% DMG!");
        playSound(Sound.ENTITY_ENDERDRAGON_GROWL, 2F, 0.85F);
        playSound(Sound.ENTITY_GHAST_DEATH, 2F, 0.85F);

        for (int i = 0; i < 4; i++)
            spawnMinion(EnumMonster.MagmaCube, "Demonic Spawn of Inferno", 3, false);
    }

    @Override
    public void n() {
        super.n();

        if(ghast != null && ghast.isAlive() && getBukkit().getVehicle() == null) {
            getBukkit().teleport(ghast.getBukkit());
            ghast.getBukkit().eject();
            ghast.getBukkit().setPassenger(getBukkit());
        }
    }

    @Override
    public void onBossDeath(Player player) {
        getDungeon().getPlayers().forEach(p -> pushAwayPlayer(p, 3.5F));
        playSound(Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
        playSound(Sound.ENTITY_ENDERDRAGON_DEATH, 2F, 2F);
    }

//    @Override
//    public void a(EntityLiving entity, float f) {
//        if(ghast != null && ghast.isAlive())
//            return; //ghast should be firing instead.
//
//        ItemWeapon wep = (ItemWeapon) PersistentItem.constructItem(getHeld());
//
//        LivingEntity target = getGoalTarget() != null ? (LivingEntity) getGoalTarget().getBukkitEntity() : null;
//        Projectile proj = DamageAPI.fireStaffProjectile((LivingEntity) this.getBukkitEntity(), wep.getAttributes(), target, wep);
//        if (proj != null)
//            proj.setVelocity(proj.getVelocity().multiply(1.4));
//    }

    private void pushAwayPlayer(Player p, double speed) {
        org.bukkit.util.Vector unitVector = p.getLocation().toVector().subtract(getBukkit().getLocation().toVector()).normalize();
        Material m = p.getLocation().subtract(0, 1, 0).getBlock().getType();

        if (p.getLocation().getY() - 1 <= getBukkit().getLocation().getY() || m == Material.AIR)
            EntityMechanics.setVelocity(p, unitVector.multiply(speed));
    }

    @Override
    public void onBossAttacked(Player attacker) {
        //if player.
        if (attacker.getLocation().distanceSquared(getBukkit().getLocation()) <= 16) {
            pushAwayPlayer(attacker, 2F);
            attacker.setFireTicks(80);
            attacker.playSound(getBukkit().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1F, 1F);
        }

        boolean spawnedGhast = getDungeon().hasSpawned(BossType.InfernalGhast);

        if (spawnedGhast && !this.ghast.isAlive())
            setVulnerable(true);

        if (getPercentHP() <= 0.5D && !spawnedGhast) {
            // Summons the ghast.
            say("Behond, the powers of the inferno!");
            ghast = (InfernalGhast) getDungeon().spawnBoss(BossType.InfernalGhast, getBukkit().getLocation().clone().add(0, 7, 0));
            ghast.init();
            say("The inferno will devour you!");
            //playSound(Sound.ENTITY_GHAST_WARN, 2F, 0.35F);
            setVulnerable(false);
        }

        if (spawnedGhast && !ghast.isAlive())
            return;

        if (random.nextInt(15) == 0) {
            Location loc = getBukkit().getLocation();
            ParticleAPI.spawnParticle(Particle.SMOKE_LARGE, loc.add(0, 0.5, 0), 100, 1F);

            // Spawn minions
            int minionType = random.nextInt(2);
            EnumMonster monsterType = finalForm ? EnumMonster.Silverfish : EnumMonster.MagmaCube;
            String name = finalForm ? "Abyssal Demon" : "Demonic Spawn of Inferno";
            if (minionType == 1)
                name = (finalForm ? "Greater" : "Demonic") + " " + name;
            spawnMinion(monsterType, name, 3 + minionType, false);
        }
    }
}