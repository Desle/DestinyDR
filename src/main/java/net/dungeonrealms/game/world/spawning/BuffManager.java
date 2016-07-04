package net.dungeonrealms.game.world.spawning;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.entities.types.EnderCrystal;
import net.dungeonrealms.game.world.entities.utils.BuffUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Kieran on 11/5/2015.
 */
public class BuffManager implements GenericMechanic {

    private static BuffManager instance = null;
    public CopyOnWriteArrayList<EnderCrystal> CURRENT_BUFFS = new CopyOnWriteArrayList<>();

    public static BuffManager getInstance() {
        if (instance == null)
            instance = new BuffManager();
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
	public void startInitialization() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::spawnSomeBuffs, 40L, 1800L);
    }

    @Override
    public void stopInvocation() {

    }

    private void spawnSomeBuffs() {
        int MAX_BUFFS = (Bukkit.getOnlinePlayers().size() / 4) + 1;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!API.isPlayer(player)) {
                continue;
            }
            if (API.isInSafeRegion(player.getLocation())) {
                continue;
            }
            if (!player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                continue;
            }
            if (API._hiddenPlayers.contains(player)) {
                continue;
            }
            if (player.getEquipment().getItemInMainHand().getType() != Material.AIR && player.getEquipment().getItemInMainHand() != null) {
                if (Mining.isDRPickaxe(player.getEquipment().getItemInMainHand()) || Fishing.isDRFishingPole(player.getEquipment().getItemInMainHand())) {
                    continue;
                }
            }
            if (getNearbyBuffs(player, 10).size() >= 1) {
                continue;
            }
            if (new Random().nextInt(21) < 4) {
                if (!CURRENT_BUFFS.isEmpty()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        EnderCrystal enderCrystal = CURRENT_BUFFS.get(new Random().nextInt(CURRENT_BUFFS.size()));
                        CURRENT_BUFFS.remove(enderCrystal);
                        enderCrystal.dead = true;
                    }, 0L);
                }
            }
            if (CURRENT_BUFFS.size() >= MAX_BUFFS) {
                continue;
            }
            if (new Random().nextInt(21) < 6) {
                if (CURRENT_BUFFS.size() < MAX_BUFFS) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> CURRENT_BUFFS.add(BuffUtils.spawnBuff(player.getUniqueId())), 0L);
                }
            }
        }
    }

    private static Set<Entity> getNearbyBuffs(Player player, int radius) {
        Set<Entity> buffsNearby = new HashSet<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity.hasMetadata("type")) {
                if (entity.getMetadata("type").get(0).asString().equalsIgnoreCase("buff")) {
                    buffsNearby.add(entity);
                }
            }
        }
        return buffsNearby;
    }
}
