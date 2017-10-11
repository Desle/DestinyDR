package net.dungeonrealms.game.quests;


import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.quests.QuestPlayerData.QuestProgress;
import net.dungeonrealms.game.quests.listeners.KillObjectiveListener;
import net.dungeonrealms.game.quests.listeners.NPCListener;
import net.dungeonrealms.game.quests.objectives.ObjectiveGoTo;
import net.dungeonrealms.game.quests.objectives.ObjectiveKill;
import net.dungeonrealms.game.quests.objectives.QuestObjective;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.teleportation.WorldRegion;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Quests implements GenericMechanic {
    public StoreBase<QuestNPC> npcStore = new StoreBase<QuestNPC>(QuestNPC.class, "npcs");
    public StoreBase<Quest> questStore = new StoreBase<Quest>(Quest.class, "quests");

    public HashMap<Player, QuestPlayerData> playerDataMap = new HashMap<Player, QuestPlayerData>();
    private static Quests INSTANCE = new Quests();

    private volatile Map<Player, CompassData> compassMap = new ConcurrentHashMap<>();
    @Override
    public void startInitialization() {
        Bukkit.getPluginManager().registerEvents(new NPCListener(), DungeonRealms.getInstance());
        Bukkit.getPluginManager().registerEvents(new KillObjectiveListener(), DungeonRealms.getInstance());
        npcStore.load();
        questStore.load();

        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> spawnQuestParticles(), 0, 10);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> checkQuestZones(), 0, 40);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> sendActionBar(), 0, 30);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> updateAllGlow(), 0, 30);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> updateCompass(), 0, 1);
    }


    @Override
    public void stopInvocation() {
        if (DungeonRealms.isMaster()) {
            questStore.save();
            npcStore.save();
        }
        this.playerDataMap.clear();
    }

    private void spawnQuestParticles() {
        npcStore.getList().forEach(npc ->
                ParticleAPI.spawnParticle(Particle.VILLAGER_HAPPY, npc.getLocation(), .5F, 1, .5F, 6, .01F));
    }

    public void updateGlow(Player player) {
        if (!this.playerDataMap.containsKey(player))
            return;
        QuestPlayerData data = this.playerDataMap.get(player);
        for (Quest q : data.getCurrentQuests()) {
            QuestProgress qp = this.playerDataMap.get(player).getQuestProgress(q);
            if (qp == null || qp.getCurrentStage() == null)
                continue;
            QuestStage stage = qp.getCurrentStage().getPrevious();
            if (stage != null && stage.getObjective() != null) {
                QuestObjective qo = stage.getObjective();
                if (qo instanceof ObjectiveKill)
                    ((ObjectiveKill) qo).updateGlow(player);
            }
        }
    }

    private void updateAllGlow() {
        Bukkit.getOnlinePlayers().forEach(this::updateGlow);
    }

    private void sendActionBar() {
        Bukkit.getOnlinePlayers().forEach(this::updateActionBar);
    }

    private void updateCompass(){

    }
    public void updateActionBar(Player player) {
        QuestPlayerData data = this.playerDataMap.get(player);
        if (data == null)
            return;
        List<Quest> quests = data.getCurrentQuests();
        if (quests.isEmpty())
            return;
        Quest current = quests.get(quests.size() - 1);
        QuestProgress qp = data.getQuestProgress(current);
        QuestStage stage = qp.getCurrentStage();
        if (stage == null || stage.getPrevious() == null)
            return;
        String description = stage.getPrevious().getObjective().getTaskDescription(player, stage);
        if (qp.shouldReceiveActionBar() && description != null)
            TitleAPI.sendActionBar(player, ChatColor.WHITE + description);
    }

    private void checkQuestZones() {
        //This is run async so we don't hang the main thread checking this.
        for (Player player : Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()])) {
            QuestPlayerData data = this.playerDataMap.get(player);
            if (data == null)
                continue;
            for (Quest quest : this.questStore.getList()) {
                if (data.isDoingQuest(quest)) {
                    QuestStage stage = data.getQuestProgress(quest).getCurrentStage();
                    if (stage.getStageTrigger() == Trigger.LOCATION)
                        if (((ObjectiveGoTo) stage.getPrevious().getObjective()).isCompleted(player, stage, stage.getNPC()))
                            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> quest.advanceQuest(player));
                }
            }
        }
    }

    public QuestNPC getNPCByName(String name) {
        for (QuestNPC npc : this.npcStore.getList())
            if (npc.getName().equals(name))
                return npc;
        return null;
    }

    private static Map<String, ItemStack> cachedSkulls = new HashMap<>();

    public static ItemStack createSkull(String username, String displayName, String[] lore) {
        ItemStack skullItem = cachedSkulls.get(username);

        ItemMeta im;
        if (skullItem == null) {
            skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skullItem.getItemMeta();
            meta.setOwner(username);
            skullItem.setItemMeta(meta);
            im = skullItem.getItemMeta();
            cachedSkulls.put(username, skullItem.clone());
        } else {
            skullItem = skullItem.clone();
            im = skullItem.getItemMeta();
        }

        List<String> list = Lists.newArrayList();
        for (String s : lore)
            list.add(ChatColor.GRAY + s);
        im.setDisplayName(displayName);
        im.setLore(list);
        skullItem.setItemMeta(im);
        return skullItem;
    }

    public static String getCoords(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    public static Quests getInstance() {
        return INSTANCE;
    }


    public void handleLogoutEvents(Player player) {
        this.playerDataMap.remove(player);
    }

    public void handleLogin(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player.getUniqueId());
        String data = wrapper.getQuestData();

        JsonArray object;
        if (data == null) {
            object = new JsonArray();
        } else {
            object = new JsonParser().parse(data).getAsJsonArray();
        }
        this.playerDataMap.put(player, new QuestPlayerData(player, object));
        for (Quest q : this.playerDataMap.get(player).getCurrentQuests()) {
            QuestProgress qp = this.playerDataMap.get(player).getQuestProgress(q);
            if (qp != null && qp.getCurrentStage() != null && qp.getCurrentStage().getObjective() != null)
                qp.getCurrentStage().getObjective().onStart(player);
        }
    }

    public static String getRegionDirections(Location loc) {
        WorldRegion region = WorldRegion.getByRegionName(GameAPI.getRegionName(loc));
        if (region != null)
            return "in " + region.getDisplayName();
        return "at [" + getCoords(loc) + "]";
    }

    public static boolean isEnabled() {
        return true;
    }

    public void triggerObjective(Player player, Class<? extends QuestObjective> cls) {
        QuestPlayerData pqd = this.playerDataMap.get(player);
        if (pqd != null)
            pqd.triggerObjectives(cls);
    }


    public boolean removeQuest(Player player, String questName) {
        QuestPlayerData data = this.playerDataMap.get(player);
        if (data == null) return false;
        Quest quest = data.getCurrentQuests().stream().filter(q -> q.getQuestName().equalsIgnoreCase(questName)).findFirst().orElse(null);
        return quest != null && data.removeQuest(quest);
    }

    public boolean isDoingQuest(Player player, String questName) {
        QuestPlayerData data = this.playerDataMap.get(player);
        if (data == null) return false;
        Quest quest = data.getCurrentQuests().stream().filter(q -> q.getQuestName().equalsIgnoreCase(questName)).findFirst().orElse(null);
        return quest != null && data.isDoingQuest(quest);
    }

    public boolean hasCurrentQuestObjective(Player player, String questName, Class<?> objective) {
        QuestPlayerData data = this.playerDataMap.get(player);
        if (data == null) return false;
        Quest quest = data.getCurrentQuests().stream().filter(q -> q.getQuestName().equalsIgnoreCase(questName)).findFirst().orElse(null);

        if (quest == null) return false;
        QuestProgress progress = data.getQuestProgress(quest);
        if (progress == null) return false;

        QuestStage stage = progress.getCurrentStage().getPrevious();
        if (stage == null) return false;
        return quest != null && data.isDoingQuest(quest) && stage != null && stage.getObjective().getClass().equals(objective);
    }
}
