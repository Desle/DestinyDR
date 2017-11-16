package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.*;
import net.dungeonrealms.game.item.items.functional.*;
import net.dungeonrealms.game.item.items.functional.ItemHealingFood.EnumHealingFood;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketItem;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketType;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueScrollItem;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueScrollType;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.PouchTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.player.inventory.menus.staff.GUIItemBank;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.BuffMechanics;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandAdd extends BaseCommand {

    public CommandAdd(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        if (!Rank.isGM(player))
            return false;

        // Extended Permission Check
        if (!Rank.isHeadGM(player) && !DungeonRealms.getInstance().isGMExtendedPermissions) {
            player.sendMessage(ChatColor.RED + "You don't have permission to execute this command.");
            return false;
        }


        if (args.length > 0) {
            int tier;
            LivingEntity target;
            ItemStack held = player.getEquipment().getItemInMainHand();

            if (args[0].equalsIgnoreCase("itemtype")) {

                ItemType type = ItemType.getByName(args[1]);
                if(type == null){
                    player.sendMessage(ChatColor.RED + "Invalid Item Type!");
                    return true;
                }

                try {
                    PersistentItem item = type.getItemClass().newInstance();
                    player.getInventory().addItem(item.generateItem());
                    player.sendMessage(ChatColor.RED + "Generated!");
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                return true;
            }
            switch (args[0]) {
                case "save":
                    if (held == null || held.getType() == Material.AIR) {
                        player.sendMessage(ChatColor.RED + "You must be holding an item");
                        return true;
                    }

                    if (args.length == 1) {
                        player.sendMessage(ChatColor.RED + "Syntax: /ad save <name>");
                        return true;
                    }

                    ItemGenerator.saveItem(held, args[1]);
                    player.sendMessage(ChatColor.GREEN + "Saved " + args[1] + ".");
                    break;
                case "trinket":
                    if (args.length >= 2 && args[1].equalsIgnoreCase("all")) {
                        for (TrinketType type : TrinketType.values()) {
                            //Give all trinkets?
                            for (Trinket trink : Trinket.values()) {
                                if (type.getAccessory().contains(trink)) { //Allowed for this type.
                                    player.getInventory().addItem(new TrinketItem(type, trink).generateItem());
                                }
                            }
                        }
                    } else if (args.length == 3) {
                        TrinketType type = TrinketType.getFromName(args[1]);

                        Trinket trinket = Trinket.getFromName(args[2]);

                        if (type == null) {
                            player.sendMessage(ChatColor.RED + "Invalid trinket! LURE, MINING_GLOVE, RIFT_RING, COMBAT");
                            return true;
                        }

                        if (trinket != null) {
                            player.getInventory().addItem(new TrinketItem(type, trinket).generateItem());
                        } else {
                            player.getInventory().addItem(new TrinketItem(type).generateItem());
                        }

                        player.sendMessage("Trinket added to inventory.");
                    } else if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("combat") || args[1].equalsIgnoreCase("jewel")) {
                            player.getInventory().addItem(new TrinketItem(TrinketType.COMBAT, Trinket.COMBAT).generateItem());
                        }
                    }
                    return true;
                case "dryloot":
                    PlayerWrapper wra = PlayerWrapper.getPlayerWrapper(player);

                    ItemTier tr = ItemTier.getByTier(Integer.parseInt(args[1]));

                    int am = Integer.parseInt(args[2]);
                    Integer current = wra.getDryLoot().get(tr);
                    wra.getDryLoot().put(tr, current == null ? am : current + am);
                    player.sendMessage(ChatColor.RED + "Loot added to tier " + tr.name());
                    return true;
                case "clue":
                    player.getInventory().addItem(new ClueScrollItem(ClueScrollType.COMBAT).generateItem());
                    break;
                case "discount":
                    ItemDiscountScroll scroll = new ItemDiscountScroll();
                    scroll.setDiscountPercent(10);
                    player.getInventory().addItem(scroll.generateItem());
                    break;
                case "load":
                case "nameditem":
                    if (args.length > 1) {
                        String namedItem = args[1];
                        ItemStack itemStack = ItemGenerator.getNamedItem(namedItem);
                        player.getInventory().addItem(itemStack);
                        if (itemStack == null)
                            player.sendMessage(ChatColor.RED + "Item not found.");
                    } else {
                        player.sendMessage(ChatColor.RED + "/ad " + args[0] + " <name>");
                    }
                    break;
                case "riftcrystal":
                case "riftfragment":
                    int t = Integer.parseInt(args[1]);
                    int amount = args.length == 3 ? Integer.parseInt(args[2]) : 1;
                    if (args[0].equals("riftcrystal")) {
                        player.getInventory().addItem(new ItemRiftCrystal(ItemTier.getByTier(t), amount).generateItem());
                    } else if (args[1].equals("riftfragment")) {
                        player.getInventory().addItem(new ItemRiftFragment(ItemTier.getByTier(t), amount).generateItem());
                    }
                    break;
                case "attributes":
                    player.sendMessage(ChatColor.GREEN + "Player Attributes:");
                    PlayerWrapper pw = PlayerWrapper.getWrapper(player);
                    for (AttributeType at : pw.getAttributes().getAttributes())
                        player.sendMessage(ChatColor.YELLOW + at.getNBTName() + ": " + ChatColor.RED + pw.getAttributes().getAttribute(at).toString());
                    break;
                case "armor":
                case "weapon":
                    //TODO: Attribute editor.
                    try {
                        CombatItem gear = args[0].equals("armor") ? new ItemArmor() : new ItemWeapon();

                        if (args.length >= 2)
                            gear.setTier(ItemTier.getByTier(Integer.parseInt(args[1])));

                        if (args.length >= 3)
                            gear.setType(ItemType.valueOf(args[2].toUpperCase()));

                        if (args.length >= 4)
                            gear.setRarity(ItemRarity.valueOf(args[3].toUpperCase()));

                        player.getInventory().addItem(gear.generateItem());

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        player.sendMessage("Format: /ad weapon [tier] [type] [rarity]. Leave parameter blank to generate a random value.");
                    }
                    break;
                case "reloadModifiers":
                    ItemGenerator.loadModifiers();
                    break;
                case "item":
                    new GUIItemBank(player);
                    break;
                case "pick":
                    int level = args.length == 2 ? Integer.parseInt(args[1]) : 1;
                    player.getInventory().addItem(new ItemPickaxe().setLevel(level).generateItem());
                    break;
                case "rod":
                    level = args.length == 2 ? Integer.parseInt(args[1]) : 1;
                    player.getInventory().addItem(new ItemFishingPole().setLevel(level).generateItem());
                    break;
                case "scrap":
                    for (int i = 1; i <= 5; i++)
                        player.getInventory().addItem(new ItemScrap(ScrapTier.getScrapTier(i)).generateItem());
                    break;
                case "potion":
                    for (PotionTier p : PotionTier.values()) {
                        PotionItem item = new PotionItem(p);
                        player.getInventory().addItem(item.generateItem());
                        item.setSplash(true);
                        player.getInventory().addItem(item.generateItem());
                    }
                    break;
                case "food":
                    player.setFoodLevel(1);
                    for (EnumHealingFood food : EnumHealingFood.values())
                        player.getInventory().addItem(new ItemHealingFood(food).generateItem());
                    break;
                case "buff":
                    BuffMechanics.spawnBuff(player);
                    break;
                case "armorench":
                case "armorenchant":
                    player.getInventory().addItem(new ItemEnchantArmor(ItemTier.getByTier(Integer.parseInt(args[1]))).generateItem());
                    break;
                case "orb":
                    player.getInventory().addItem(new ItemOrb().generateItem());
                    break;
                case "weaponench":
                case "enchant":
                case "weaponenchant":
                    player.getInventory().addItem(new ItemEnchantWeapon(ItemTier.getByTier(Integer.parseInt(args[1]))).generateItem());
                    break;
                case "prot":
                case "protect":
                case "scroll":
                case "protectscroll":
                    player.getInventory().addItem(new ItemProtectionScroll(ItemTier.getByTier(Integer.parseInt(args[1]))).generateItem());
                    break;
                case "pouch":
                    tier = Integer.parseInt(args[1]);
                    player.getInventory().addItem(new ItemGemPouch(PouchTier.getById(tier)).generateItem());
                    break;
                case "votemessage":
                    GameAPI.announceVote(player);
                    break;
                case "banknote":
                    int quantity = 1000;
                    if (args.length >= 2) {
                        try {
                            quantity = Integer.parseInt(args[1]);
                            if (quantity <= 0) {
                                player.sendMessage(ChatColor.RED + "Failed to create bank note because " + quantity + " is too small.");
                                break;
                            }
                        } catch (NumberFormatException ex) {
                            player.sendMessage(ChatColor.RED + "Failed to create bank note because " + args[1] + " isn't a valid number.");
                            break;
                        }
                    }
                    player.getInventory().addItem(new ItemGemNote(player.getName(), quantity).generateItem());
                    player.sendMessage(ChatColor.GREEN + "Successfully created a bank note worth " + NumberFormat.getIntegerInstance().format(quantity) + " gems.");
                    break;
                case "teleport":
                case "teleports":
                    if (args.length == 1) {
                        for (TeleportLocation tl : TeleportLocation.values())
                            player.getInventory().addItem(new ItemTeleportBook(tl).generateItem());
                        player.sendMessage(ChatColor.GREEN + "Spawned all teleport books.");
                    } else if (args.length >= 2) {
                        if (args[1].equalsIgnoreCase("random")) {
                            player.getInventory().addItem(new ItemTeleportBook().generateItem());
                            player.sendMessage(ChatColor.GREEN + "Spawned random teleport book.");
                        } else {
                            TeleportLocation tl = TeleportLocation.valueOf(args[1].toUpperCase());
                            if (tl == null) {
                                player.sendMessage(ChatColor.RED + "Location not found.");
                                return true;
                            }
                            player.getInventory().addItem(new ItemTeleportBook(tl).generateItem());
                            player.sendMessage(ChatColor.GREEN + "Spawned " + tl.getDisplayName() + " teleport book.");
                        }
                    }
                    break;
                case "xplamp":
                    if (args.length == 3) {
                        ItemEXPLamp.ExpType type;
                        try {
                            type = ItemEXPLamp.ExpType.valueOf(args[1]);
                        } catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "Invalid type! Valid types: PLAYER, PROFESSION");
                            return true;
                        }

                        int exp = StringUtils.isNumeric(args[2]) ? Integer.parseInt(args[2]) : 1;
                        ItemEXPLamp lamp = new ItemEXPLamp(type, exp);
                        player.getInventory().addItem(lamp.generateItem());
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid args! /ad xplamp <xpType> <amount>");
                        return true;
                    }
                    break;
                /*case "ecash_buff":
                    if (args.length >= 2) {
                        int buffDuration = 1800;
                        int buffBonus = 20;

                        if (args.length >= 3) {
                            buffDuration = Integer.parseInt(args[2]) * 60;
                        }
                        if (args.length >= 4) {
                            buffBonus = Integer.parseInt(args[3]);
                        }

                        player.getInventory().addItem(new ItemBuff(EnumBuff.valueOf(args[1].toUpperCase()), buffDuration, buffBonus,false).generateItem());
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid usage! /add ecash_buff <LOOT|PROFESSION|LEVEL>");
                    }
                    break;*/
                case "stats":
                    PlayerWrapper.getWrapper(player).calculateAllAttributes();
                    player.sendMessage(ChatColor.GREEN + "Recalculated.");
                    break;
                case "einfo":
                    target = getEntity(player, args);
                    boolean monster = EntityAPI.isMonster(target);
                    player.sendMessage("Name: " + target.getCustomName());
                    player.sendMessage("HP: " + HealthHandler.getHP(target));
                    player.sendMessage("Monster: " + monster);
                    if (monster) {
                        player.sendMessage("Tier: " + EntityAPI.getTier(target));
                        if (EntityAPI.isElemental(target))
                            player.sendMessage("Element: " + EntityAPI.getElement(target).getPrefix());
                        player.sendMessage("Attributes: " + EntityAPI.getAttributes(target));
                    }
                    break;
                case "invsee":
                    target = getEntity(player, args);

                    Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, target.getCustomName());
                    EntityEquipment e = target.getEquipment();
                    inventory.addItem(e.getItemInMainHand());
                    inventory.addItem(e.getArmorContents());
                    player.sendMessage(ChatColor.YELLOW + "Opening inventory of " + target.getCustomName() + ".");
                    player.openInventory(inventory);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Invalid usage! '" + args[0] + "' is not a valid variable.");
                    break;
            }
        }

        return true;
    }

    private LivingEntity getEntity(Player player, String[] args) {
        return getEntity(player, args, 1);
    }

    private LivingEntity getEntity(Player player, String[] args, int startIndex) {
        String search = "";
        for (int i = startIndex; i < args.length; i++)
            search += (i == startIndex ? "" : " ") + args[i];

        final String finalSearch = search;
        LivingEntity target = (LivingEntity) player.getNearbyEntities(10, 10, 10).stream().filter(e -> e instanceof LivingEntity
                && getName(e).contains(finalSearch)).findAny().orElse(null);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Entity not found.");
            return player;
        }
        return target;
    }

    private String getName(Entity e) {
        return e.getName() != null && e.getName().length() > 0 ? ChatColor.stripColor(e.getCustomName()) : "";
    }
}