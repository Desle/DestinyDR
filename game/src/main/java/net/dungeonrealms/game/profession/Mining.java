package net.dungeonrealms.game.profession;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.Buff;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.donation.overrides.CosmeticOverrides;
import net.dungeonrealms.game.item.items.core.AuraType;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.item.items.functional.*;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueScrollItem;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueScrollType;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueUtils;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveMineOre;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.PickaxeAttributeType;
import net.dungeonrealms.game.mastery.RandomCollection;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.TreeMap;
import java.util.NavigableMap;

/**
 * Mining - Core listeners for the mining profession.
 * <p>
 * Redone by Kneesnap on April 7th, 2017.
 */
public class Mining implements GenericMechanic, Listener {

    private static HashMap<Location, MiningTier> oreLocations = new HashMap<>();

    public static Set<Location> treasureChests = new HashSet<>();
    private static Map<Item, Consumer<ArmorStand>> treasureItems = new ConcurrentHashMap<>();
    private static final int[] GEM_FIND_MIN = new int[]{01, 20, 40, 70, 90};
    private static final int[] GEM_FIND_MAX = new int[]{20, 40, 60, 90, 110};

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleMiningFatigue(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Player p = event.getPlayer();
        ItemStack stackInHand = p.getEquipment().getItemInMainHand();
        Block block = event.getClickedBlock();

        if (!ItemPickaxe.isPickaxe(stackInHand))
            return;

        ItemPickaxe pickaxe = new ItemPickaxe(stackInHand);
        MiningTier oreTier = MiningTier.getTierFromOre(block.getType());

        if (oreTier == null) return;

        p.removePotionEffect(PotionEffectType.SLOW_DIGGING);

        if (pickaxe.getTier() == ItemTier.TIER_1)
            return;

        if (pickaxe.getTier() == ItemTier.TIER_2) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 80, 0));
        } else if (pickaxe.getTier().getId() == oreTier.getTier()) {
            //Ignore this.
            if (Trinket.hasActiveTrinket(p, Trinket.NO_MINING_FATIGUE, true)) return;

            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 80, 0));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void breakOre(BlockBreakEvent e) {
        Block block = e.getBlock();
        Random rand = ThreadLocalRandom.current();
        ItemStack item = e.getPlayer().getEquipment().getItemInMainHand();

        // Verify main world.
        if (!GameAPI.isMainWorld(block.getLocation()))
            return;

        // Verify this is a pickaxe.
        if (!ItemPickaxe.isPickaxe(item))
            return;

        //Verify we're breaking ore.
        MiningTier oreTier = MiningTier.getTierFromOre(block.getType());
        if (oreTier == null) {
            if (isMineable(block.getLocation()) && block.getType() == Material.STONE)
                e.setCancelled(true);
            return;
        }

        e.setCancelled(true);

        if (!isMineable(block.getLocation())) return;
        ItemPickaxe pickaxe = new ItemPickaxe(item);
        Player p = e.getPlayer();

        //  WRONG TIER  //
        if (pickaxe.getTier().getId() < oreTier.getTier()) {
            p.sendMessage(ChatColor.RED + "Your pick is not strong enough to mine this ore!");
            return;
        }

        //  ADD PLAYER XP  //
        int xpGain = oreTier.getXP();

        double mult = ItemLootAura.getDropMultiplier(p.getLocation(), AuraType.PROFESSION);

        int xpToAdd = 0;
        if (mult > 0)
            xpToAdd = (int) (xpGain * mult * .01);

        PlayerWrapper pw = PlayerWrapper.getWrapper(p);
        pw.addExperience(xpGain / 12, false, true, true);

        Material type = e.getBlock().getType();
        int oreToAdd = 0;
        p.playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 1F, 0.75F);
        e.getBlock().setType(Material.STONE);

        //  REPLACE ORE  //
        if (Trinket.hasActiveTrinket(p, Trinket.ORE_RESPAWN, true) && rand.nextInt(10) == 0) {
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> e.getBlock().setType(oreTier.getOre()), 0);
            p.playSound(block.getLocation(), Sound.BLOCK_NOTE_PLING, 1F, 0.75F);
            pw.sendDebug(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "       The ore magically respawned!");
        } else {
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> e.getBlock().setType(oreTier.getOre()), oreTier.getOreRespawnTime() * 15);
        }
        double chance = rand.nextInt(100);
        if (TutorialIsland.onTutorialIsland(p.getLocation()) && Quests.getInstance().hasCurrentQuestObjective(p, "Tutorial Island", ObjectiveMineOre.class)) {
            chance = 1;
        }
        //  SUCCESS  //
        if (chance < pickaxe.getSuccessChance() || pickaxe.getTier().getId() > oreTier.getTier()) {
            oreToAdd = 1;
            pw.getPlayerGameStats().addStat(StatColumn.ORE_MINED);
        }

        //  DAMAGE ITEM  //
        if (rand.nextInt(100) > pickaxe.getAttributes().getAttribute(PickaxeAttributeType.DURABILITY).getValue() + Trinket.getTrinketValue(p, Trinket.MINE_DURABILITY))
            pickaxe.damageItem(p, oreToAdd > 0 ? 2 : 1);

        //  FAILED  //
        if (oreToAdd == 0) {
            pickaxe.updateItem(p, false);
//            p.getEquipment().setItemInMainHand(pickaxe.generateItem());
            p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "You fail to gather any ore.");
            return;
        }

        pickaxe.addExperience(p, xpGain);

        if (xpToAdd > 0) {
            pickaxe.addExperience(p, xpToAdd, false);
            pw.sendDebug(ChatColor.YELLOW.toString() + ChatColor.BOLD + "    " + ChatColor.GOLD
                    .toString() + ChatColor.BOLD + "PROF. AURA >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD
                    + "+" + ChatColor.YELLOW + Math.round(xpToAdd) + ChatColor.BOLD + " EXP " +
                    ChatColor.GRAY + "[" + pickaxe.getXP() + ChatColor.BOLD + "/" + ChatColor.GRAY + pickaxe.getNeededXP() + " EXP]");
        }

        if (xpGain > 0) {
            Buff active = DonationEffects.getInstance().getWeekendBuff();
            if (active != null && active.getType() == EnumBuff.PROFESSION) {
                double toGive = xpGain * (active.getBonusAmount() * 0.01);
                if (toGive > 0) {
                    pickaxe.addExperience(p, (int) toGive, false);
                    pw.sendDebug(ChatColor.YELLOW.toString() + ChatColor.BOLD + "    " + ChatColor.GOLD
                            .toString() + ChatColor.BOLD + (int) active.getBonusAmount() + "% XP WEEKEND >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD
                            + "+" + ChatColor.YELLOW + Math.round(toGive) + ChatColor.BOLD + " EXP " +
                            ChatColor.GRAY + "[" + pickaxe.getXP() + ChatColor.BOLD + "/" + ChatColor.GRAY + pickaxe.getNeededXP() + " EXP]");
                }
            }
        }

        boolean hasPickaxe = pickaxe.updateItem(p, false);
        if (!hasPickaxe) {
            p.sendMessage(ChatColor.RED + "It seems your Pickaxe has disappeared?");
            return;
        }
        p.updateInventory();

        Location l = block.getLocation();
        if (rand.nextInt(100) < pickaxe.getAttributes().getAttribute(PickaxeAttributeType.TREASURE_FIND).getValue() + Trinket.getTrinketValue(p, Trinket.MINE_TREASURE_FIND)) {
            //Spawn treasure?
            block.setType(Material.CHEST);
            Byte direction = direction(p);
            block.setData(direction);
            treasureChests.add(e.getBlock().getLocation());
            l.getWorld().playSound(l, Sound.ENTITY_WITHER_BREAK_BLOCK, 1, 1.8F);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                ((CraftWorld) l.getWorld()).getHandle().playBlockAction(new BlockPosition(l.getX(), l.getY(), l.getZ()), net.minecraft.server.v1_9_R2.Block.getById(e.getBlock().getTypeId()), 1, 1);
                l.getWorld().playSound(l, Sound.BLOCK_CHEST_OPEN, 1, 1.1F);
                Quest.spawnFirework(l.clone().add(0.5, 0.7, 0.5), FireworkEffect.builder().withColor(Color.GREEN).build());
            }, 10);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                //Give loot.
                ItemStack loot = createTreasureFindItem(oreTier, pickaxe);

                ArmorStand rise = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().clone().add(0.5, -1.3, 0.5), EntityType.ARMOR_STAND);
                rise.setVisible(false);
                rise.setGravity(false);
                rise.setInvulnerable(true);
                rise.setCollidable(false);
                rise.setCustomName(Utils.getItemName(loot));
                rise.setCustomNameVisible(false);
                Item itemEnt = ItemManager.whitelistItemDrop(p, block.getWorld().dropItem(rise.getLocation(), new ItemStack(loot.getType(), loot.getAmount(), loot.getDurability())));
                MetadataUtils.Metadata.NO_PICKUP.set(itemEnt, true);
                itemEnt.setPickupDelay(Integer.MAX_VALUE);
                rise.setPassenger(itemEnt);
                //Called after its done floating or after 6 seconds.
                treasureItems.put(itemEnt, stand -> {
                    stand.remove();
                    giveLoot(p, loot);
                    ClueUtils.handleTreasureFindMining(p, loot, oreTier);
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_TOUCH, 1, 1.2F);
                });
            }, 20);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                //5 seconds.
                if (block.getType() == Material.CHEST) {
                    block.getWorld().playEffect(block.getLocation().add(0.5, .5, .5), Effect.STEP_SOUND, Material.CHEST);
                    block.setType(Material.STONE);
                    treasureChests.add(block.getLocation());
                }
            }, 20 * 5);
        }


        //  DOUBLE ORE  //
        if (pickaxe.getAttributes().getAttribute(PickaxeAttributeType.DOUBLE_ORE).getValue() + Trinket.getTrinketValue(p, Trinket.MIN_DOUBLE_ORE) >= rand.nextInt(100) + 1) {
            oreToAdd *= 2;
            pw.sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE ORE DROP" + ChatColor.YELLOW + " (2x)");
        }

        //  TRIPLE ORE  //
        if (pickaxe.getAttributes().getAttribute(PickaxeAttributeType.TRIPLE_ORE).getValue() + Trinket.getTrinketValue(p, Trinket.MINE_TRIPLE_ORE) >= rand.nextInt(100) + 1) {
            oreToAdd *= 3;
            pw.sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE ORE DROP" + ChatColor.YELLOW + " (3x)");
        }

        //  GIVE ORE  //
        ItemStack ore = oreTier.createOreItem();
        ore.setAmount(oreToAdd);
        GameAPI.giveOrDropItem(p, ore);


        if (oreToAdd > 0) {
            Quests.getInstance().triggerObjective(p, ObjectiveMineOre.class);
            ClueUtils.handleOreMined(p, oreTier);
        }

        if (pickaxe.getAttributes().getAttribute(PickaxeAttributeType.GEM_FIND).getValue() + Trinket.getTrinketValue(p, Trinket.MINE_GEM_FIND) >= rand.nextInt(100) + 1) {
            int tier = oreTier.getTier() - 1;
            int amount = (int) (Utils.randInt(GEM_FIND_MIN[tier], GEM_FIND_MAX[tier]) * 0.8);

            //  DROP GEMS  //
            if (amount > 0) {
                if (Trinket.hasActiveTrinket(p, Trinket.MINE_GEM_TELEPORT)) {
                    GameAPI.giveOrDropItem(p, new ItemGem(amount).generateItem());
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1.4F);
                } else {
                    Item is = p.getWorld().dropItemNaturally(block.getLocation().add(.5, 1.2, .5), new ItemGem(amount).generateItem());
                    is.setVelocity(new Vector(0, -.3D, 0));
                }
                pw.sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "          FOUND " + amount + " GEM(s)");
            }
        }

        if (rand.nextInt(2000) == 3) {
            ClueScrollItem clue = new ClueScrollItem(ClueScrollType.MINING);
            GameAPI.giveOrDropItem(p, clue.generateItem());
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, .5F);
        }

    }

    public Byte direction(Player player) {
        double rotation = (player.getLocation().getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (rotation >= 45 && rotation <= 135) return 3;
        else if (rotation >= 135 && rotation <= 225) return 4;
        else if (rotation <= 45 || rotation >= 315) return 5;
        else return 1;
    }

    public void giveLoot(Player player, ItemStack toGive) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        String name = Utils.getItemName(toGive);
        if (toGive.getType().equals(Material.GOLD_ORE)) {
            //wrapper.getPurchaseablesUnlocked().put(Purchaseables.GOLD_ORE_HAT, 1);
            Purchaseables.GOLD_ORE_HAT.setNumberOwned(wrapper, 1);
            wrapper.setActiveHatOverride(CosmeticOverrides.GOLD_ORE_HAT);
        } else if (toGive.getType().equals(Material.DIAMOND_ORE)) {
            //wrapper.getPurchaseablesUnlocked().put(Purchaseables.DIAMOND_ORE_HAT, 1);
            wrapper.setActiveHatOverride(CosmeticOverrides.DIAMOND_ORE_HAT);
            Purchaseables.DIAMOND_ORE_HAT.setNumberOwned(wrapper, 1);
        } else if (toGive.getType().equals(Material.IRON_ORE)) {
            //wrapper.getPurchaseablesUnlocked().put(Purchaseables.IRON_ORE_HAT, 1);
            wrapper.setActiveHatOverride(CosmeticOverrides.IRON_ORE_HAT);
            Purchaseables.IRON_ORE_HAT.setNumberOwned(wrapper, 1);
        } else if (toGive.getType().equals(Material.EMERALD_ORE)) {
            //wrapper.getPurchaseablesUnlocked().put(Purchaseables.EMERALD_ORE_HAT, 1);
            wrapper.setActiveHatOverride(CosmeticOverrides.EMERALD_ORE_HAT);
            Purchaseables.EMERALD_ORE_HAT.setNumberOwned(wrapper, 1);
        } else if (toGive.getType().equals(Material.COAL_ORE)) {
            //wrapper.getPurchaseablesUnlocked().put(Purchaseables.COAL_ORE_HAT, 1);
            wrapper.setActiveHatOverride(CosmeticOverrides.COAL_ORE_HAT);
            Purchaseables.COAL_ORE_HAT.setNumberOwned(wrapper, 1);
        } else {
            GameAPI.giveOrDropItem(player, toGive);
        }

        player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "** " + ChatColor.GREEN + "You discovered " + (toGive.getAmount() > 1 ? ChatColor.GREEN.toString() + ChatColor.BOLD + toGive.getAmount() + "x " + ChatColor.GREEN : "a(n) ") + name + ChatColor.GREEN + " inside a Treasure Chest! " + ChatColor.GREEN + ChatColor.BOLD + "**");
        if (toGive.getType().name().endsWith("_ORE"))
            player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.UNDERLINE + "/hats" + ChatColor.GRAY + " to view all available hats!");
    }

    public ItemStack createTreasureFindItem(MiningTier tier, ItemPickaxe pick) {
        /*
        Random r = ThreadLocalRandom.current();
        //FOOD, ORE, POTIONS, ENCHANT SCROLLS, ORB OF FLIGHT, ORB OF ALT .001%, PROF_EXP LAMP?
        if (r.nextInt(500) == 5) {
            //Hat
            ItemStack oreHelm = new ItemStack(tier.getOre());

            ItemMeta im = oreHelm.getItemMeta();
            im.setDisplayName(tier.getColor() + Utils.getItemName(oreHelm) + " Hat");
            oreHelm.setItemMeta(im);
            return oreHelm;
        } else if (r.nextInt(250) == 5) {
            return new ItemOrb().generateItem();
        } else if (r.nextInt(75) == 5) {
            return new ItemEXPLamp(ItemEXPLamp.ExpType.PROFESSION, Utils.randInt(tier.getMinXPBottle(), tier.getMaxXPBottle())).generateItem();
        } else if (r.nextInt(750) == 5) {
            ItemEnchantPickaxe newPickEnchant = new ItemEnchantPickaxe();
            newPickEnchant.setTier(ItemTier.getByTier(tier.getTier()));
            newPickEnchant.addEnchant(PickaxeAttributeType.values()[ThreadLocalRandom.current().nextInt(PickaxeAttributeType.values().length)]);
            return newPickEnchant.generateItem();
        } else {
            List<Material> junks = Lists.newArrayList(Material.COOKED_BEEF, Material.BAKED_POTATO, Material.APPLE, Material.BREAD, Material.PUMPKIN_PIE);
            return new ItemStack(junks.get(r.nextInt(junks.size())), r.nextInt(6) + 3);
        }
*/
//        return new ItemEXPLamp(ItemEXPLamp.ExpType.PROFESSION, 100 + r.nextInt(1000)).generateItem();
        Random r = ThreadLocalRandom.current();
        //GENERAL ROLL
        int roll = r.nextInt(1000);
        if (roll >= 980) { // HAT CHECK
            ItemStack oreHelm = new ItemStack(tier.getOre());
            ItemMeta im = oreHelm.getItemMeta();
            im.setDisplayName(tier.getColor() + Utils.getItemName(oreHelm) + " Hat");
            oreHelm.setItemMeta(im);
            return oreHelm;
        } else if (roll >= 200) { // PROF ROLL
            return new ItemEXPLamp(ItemEXPLamp.ExpType.PROFESSION, Utils.randInt(tier.getMinXPBottle(), tier.getMaxXPBottle())).generateItem();
        } else { // LOOT ROLL
            if (tier.getTier() == 1) {
                RandomCollection<String> rc = new RandomCollection<String>()
                        .add(27, "enchantArmor").add(27, "enchantWeapon")
                        .add(26, "scrap").add(1, "parchment");
                String item = rc.next();
                if (item.equals("enchantArmor")) {
                    return new ItemEnchantArmor(ItemTier.TIER_1).generateItem();
                } else if (item.equals("enchantWeapon")) {
                    return new ItemEnchantWeapon(ItemTier.TIER_1).generateItem();
                } else if (item.equals("parchment")) {
                    return new ClueScrollItem(ClueScrollType.MINING).generateItem();
                } else {
                    ItemStack scrap = new ItemScrap(ScrapTier.TIER1).generateItem();
                    scrap.setAmount(Utils.randInt(10, 20));
                    return scrap;
                }
            } else if (tier.getTier() == 2) {
                RandomCollection<String> rc = new RandomCollection<String>()
                        .add(27, "enchantArmor").add(27, "enchantWeapon")
                        .add(26, "scrap").add(2, "parchment");
                String item = rc.next();
                if (item.equals("enchantArmor")) {
                    return new ItemEnchantArmor(ItemTier.TIER_2).generateItem();
                } else if (item.equals("enchantWeapon")) {
                    return new ItemEnchantWeapon(ItemTier.TIER_2).generateItem();
                } else if (item.equals("parchment")) {
                    return new ClueScrollItem(ClueScrollType.MINING).generateItem();
                } else {
                    ItemStack scrap = new ItemScrap(ScrapTier.TIER2).generateItem();
                    scrap.setAmount(Utils.randInt(10, 20));
                    return scrap;
                }
            } else if (tier.getTier() == 3) {
                RandomCollection<String> rc = new RandomCollection<String>()
                        .add(25, "enchantArmor").add(25, "enchantWeapon")
                        .add(25, "scrap").add(5, "orb").add(3, "parchment");
                String item = rc.next();
                if (item.equals("enchantArmor")) {
                    return new ItemEnchantArmor(ItemTier.TIER_3).generateItem();
                } else if (item.equals("enchantWeapon")) {
                    return new ItemEnchantWeapon(ItemTier.TIER_3).generateItem();
                } else if (item.equals("parchment")) {
                    return new ClueScrollItem(ClueScrollType.MINING).generateItem();
                } else if (item.equals("scrap")) {
                    ItemStack scrap = new ItemScrap(ScrapTier.TIER3).generateItem();
                    scrap.setAmount(Utils.randInt(10, 20));
                    return scrap;
                } else {
                    return new ItemOrb().generateItem();
                }
            } else if (tier.getTier() == 4) {
                RandomCollection<String> rc = new RandomCollection<String>()
                        .add(23, "enchantArmor").add(23, "enchantWeapon")
                        .add(24, "scrap").add(10, "orb").add(4, "parchment");
                String item = rc.next();
                if (item.equals("enchantArmor")) {
                    return new ItemEnchantArmor(ItemTier.TIER_4).generateItem();
                } else if (item.equals("enchantWeapon")) {
                    return new ItemEnchantWeapon(ItemTier.TIER_4).generateItem();
                } else if (item.equals("scrap")) {
                    ItemStack scrap = new ItemScrap(ScrapTier.TIER4).generateItem();
                    scrap.setAmount(Utils.randInt(10, 20));
                    return scrap;
                } else if (item.equals("parchment")) {
                    return new ClueScrollItem(ClueScrollType.MINING).generateItem();
                } else {
                    return new ItemOrb().generateItem();
                }
            } else {
                RandomCollection<String> rc = new RandomCollection<String>()
                        .add(20, "enchantArmor").add(20, "enchantWeapon")
                        .add(20, "scrap").add(20, "orb").add(5, "parchment");
                String item = rc.next();
                if (item.equals("enchantArmor")) {
                    return new ItemEnchantArmor(ItemTier.TIER_5).generateItem();
                } else if (item.equals("enchantWeapon")) {
                    return new ItemEnchantWeapon(ItemTier.TIER_5).generateItem();
                } else if (item.equals("scrap")) {
                    ItemStack scrap = new ItemScrap(ScrapTier.TIER5).generateItem();
                    scrap.setAmount(Utils.randInt(10, 20));
                    return scrap;
                } else if (item.equals("parchment")) {
                    return new ClueScrollItem(ClueScrollType.MINING).generateItem();
                } else {
                    return new ItemOrb().generateItem();
                }
            }
        }
    }


    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        loadOreLocations();
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), this::placeOre);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            treasureItems.forEach((is, callback) -> {

                ArmorStand vehicle = is.getVehicle() != null ? (ArmorStand) is.getVehicle() : null;

                int ticksLived = is.getTicksLived();
                if (ticksLived >= 20 * 4) {
                    is.remove();
                    callback.accept(vehicle);
                    treasureItems.remove(is);
                } else if (vehicle != null) {
                    if (ticksLived < 12) {
                        EntityArmorStand stand = (EntityArmorStand) ((CraftLivingEntity) vehicle).getHandle();
                        //riseeeee up.
                        stand.locY = stand.locY + .1D;
                    } else if (ticksLived > 12 && !vehicle.isCustomNameVisible()) {
                        vehicle.setCustomNameVisible(true);
                    }
                }
            });
        }, 2, 1);
    }

    private void loadOreLocations() {
        int count = 0;
        ArrayList<String> CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("orespawns");
        for (String line : CONFIG) {
            if (line.contains("=")) {
                try {
                    String[] cords = line.split("=")[0].split(",");
                    Location loc;
                    if(cords.length >= 4) {
                        loc = new Location(Bukkit.getWorld(cords[0]), Double.parseDouble(cords[1]),
                                Double.parseDouble(cords[2]), Double.parseDouble(cords[3]));
                    } else {
                        loc = new Location(GameAPI.getMainWorld(), Double.parseDouble(cords[0]),
                                Double.parseDouble(cords[1]), Double.parseDouble(cords[2]));
                    }

                    int tier = Integer.parseInt(line.split("=")[1]);
                    oreLocations.put(loc, MiningTier.values()[tier - 1]);

                    count++;
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
        }
        Utils.log.info("[Mining] Loaded " + count + " ore spawns.");
    }

    /**
     * Add an ore spawn location.
     */
    public static void addOre(Location loc, Material m) {
        assert isPossibleOre(m);
        oreLocations.put(loc, MiningTier.getTierFromOre(m));
        updateConfig();
    }

    /**
     * Remove an ore for the spawn db.
     */
    public static void removeOre(Block ore) {
        oreLocations.remove(ore.getLocation());
        updateConfig();
    }

    /**
     * Save the config. Called whenever a change is made.
     */
    private static void updateConfig() {
        List<String> save = new ArrayList<>();
        for (Location l : oreLocations.keySet())
            save.add(l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "=" + oreLocations.get(l).getTier());
        DungeonRealms.getInstance().getConfig().set("orespawns", save);
        DungeonRealms.getInstance().saveConfig();
    }

    /**
     * Places every ore in the map. (Called on startup.)
     */
    private void placeOre() {
        oreLocations.keySet().forEach(loc -> loc.getWorld().getBlockAt(loc).setType(oreLocations.get(loc).getOre()));
    }

    /**
     * Is this material a correct type of ore?
     */
    public static boolean isPossibleOre(Material mat) {
        return MiningTier.getTierFromOre(mat) != null;
    }

    /**
     * Is this block a registered ore mining spot?
     */
    public static boolean isMineable(Block block) {
        return isMineable(block.getLocation());
    }

    /**
     * Is this location a registered ore mining spot?
     *
     * @param loc
     * @return
     */
    public static boolean isMineable(Location loc) {
        return oreLocations.keySet().stream().anyMatch(l -> l.equals(loc));
    }

    @Override
    public void stopInvocation() {

    }
}