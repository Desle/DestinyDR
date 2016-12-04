package net.dungeonrealms.frontend.vgame.world.entity.generic.handle;

import lombok.Getter;
import net.dungeonrealms.api.creature.EnumCreatureState;
import net.dungeonrealms.api.creature.lib.move.EnumPowerMove;
import net.dungeonrealms.api.creature.lib.move.type.ItemPowerMove;
import net.dungeonrealms.api.event.creature.CreatureDamageEntityEvent;
import net.dungeonrealms.api.event.creature.CreatureStateChangeEvent;
import net.dungeonrealms.api.event.creature.EntityDamageCreatureEvent;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import net.dungeonrealms.common.frontend.lib.message.CenteredMessage;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.player.GamePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Random;
import java.util.UUID;

/**
 * Created by Giovanni on 28-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GenericEntityHandler implements Handler.ListeningHandler {

    @Getter
    private boolean prepared;

    @Getter
    private UUID uniqueId;

    @Override
    public void prepare() {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
        this.uniqueId = UUID.randomUUID();
        this.prepared = true;
    }

    @Override
    public void disable() {
        this.uniqueId = null;
        this.prepared = false;
    }

    @EventHandler
    public void onCreatureDamageEntity(CreatureDamageEntityEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(Game.getGame().getRegistryRegistry().getPlayerRegistry().isAccepted(player.getUniqueId())) {
                GamePlayer gamePlayer = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(player.getUniqueId());
                gamePlayer.setCombat(true);
                gamePlayer.setLastDamageCause(event.getEntity().getBukkitEntity());
                if(event.getItemStack() != null) {
                    int damage = 0;
                    int newHealth = 0;
                } else {
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageCreature(EntityDamageCreatureEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (Game.getGame().getRegistryRegistry().getPlayerRegistry().isAccepted(player.getUniqueId())) {
                GamePlayer gamePlayer = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(player.getUniqueId());
                gamePlayer.setCombat(true); // Set their combat log enabled
                gamePlayer.setLastDamaged(event.getGameEntity().getEntity().getBukkitEntity());
                if (event.getItemStack() != null) {
                    // TODO Calculate item damage
                    int damage = 0;
                    int newHealth = (int) event.getGameEntity().getEntity().getHealth() - damage;

                    final boolean[] powerMoved = {false};

                    // Blocking & dodging the damage
                    EnumPowerMove powerMove = EnumPowerMove.randomCombatMove();
                    if (powerMove != EnumPowerMove.EMPTY) {
                        ItemPowerMove itemPowerMove = new ItemPowerMove(powerMove,
                                ((LivingEntity) event.getGameEntity().getEntity()).getEquipment().getArmorContents()) {
                            @Override
                            public void perform() {
                                int moveChance = new Random().nextInt(150);
                                if (this.getChance() >= moveChance) {
                                    powerMoved[0] = true;
                                    switch (this.getPowerMove()) {
                                        case DODGE:
                                            CenteredMessage.sendCenteredMessage(player, "&c&l*OPPONENT DODGED* &c(" + event.getGameEntity().getEntityData().getName() + "&c)");
                                            player.playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 1f, 0.3f);
                                            break;
                                        case BLOCK:
                                            CenteredMessage.sendCenteredMessage(player, "&c&l*OPPONENT BLOCKED* &c(" + event.getGameEntity().getEntityData().getName() + "&c)");
                                            player.playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, 1f, 0.3f);
                                            break;
                                    }
                                }
                            }
                        };
                        itemPowerMove.perform();
                    }
                    if (!powerMoved[0]) {
                        event.getGameEntity().displayHealth();
                        // Damage the creature
                        event.getGameEntity().getCreature().getLivingMeta().damage(gamePlayer.getPlayer().getUniqueId(), damage);
                        // 5 DMG -> Atlas__ [99999]
                        CenteredMessage.sendCenteredMessage(player, "&c" + damage + " &c&lDMG &c-> " + event.getGameEntity().getEntityData().getName() + "[" + newHealth + "]");
                    }
                } else {
                    // 1 Damage, player (most likely) used their hand to damage
                    event.getGameEntity().getCreature().getLivingMeta().damage(gamePlayer.getPlayer().getUniqueId(), 1);
                }
            }
        }
    }


    @EventHandler
    public void onCreatureStateChange(CreatureStateChangeEvent event) {
        event.getGameEntity().getEntityData().setCreatureState(event.getNewState());
        if (event.getGameEntity().getEntityData().getCreatureState() == EnumCreatureState.DAMAGED) {
            double axis[] = {10, 10, 10};
            if (!event.getGameEntity().hasPlayersNearby(axis)) {
                // When the creature state changes but there are no players nearby we will display the name again
                event.getGameEntity().displayName();
            }
        }
    }
}
