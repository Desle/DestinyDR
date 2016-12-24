package net.dungeonrealms.game.world.item.itemgenerator.modifiers;

import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierCondition;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierType;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WeaponModifiers {

	public static List<Item.ItemType> weapons = Arrays.asList(Item.ItemType.AXE, Item.ItemType.BOW, Item.ItemType.POLEARM, Item.ItemType.STAFF, Item.ItemType.SWORD);
	
	public static List<String> elements = Arrays.asList("FIRE", "ICE", "POISON");
	public static List<Double> elementMultipliers = Arrays.asList(0.15D, 1D, 0.10D);

	public static List<String> versus = Arrays.asList("Monsters", "Players");
	
	public static ChatColor r = ChatColor.RED;

	public class SwordDamage extends ItemModifier {

		public SwordDamage() {
			super(Collections.singletonList(Item.ItemType.SWORD), 100, "damage", r + "DMG: ", null, false);
			
			setOrderPriority(0);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 5, 8)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 6, 9, 23)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 9, 15, 24)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 10, 12, 17)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 16, 18, 24)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 20, 30, 65)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 22, 35, 70)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 25, 30, 45)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 30, 35, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 50, 90, 150)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 60, 100, 160)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 80, 125)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 70, 85, 155)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 110, 220)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 110, 150, 240)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 130, 140, 210)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 150, 160, 260)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 160, 230, 407)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 190, 250, 450)));
		}
		
	}
	
	public class AxeDamage extends ItemModifier {

        public AxeDamage() {
            super(Collections.singletonList(Item.ItemType.AXE), 100, "damage", r + "DMG: ", null, false);
            
            setOrderPriority(0);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 5, 8)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 6, 9, 23)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 9, 15, 24)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 10, 12, 17)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 16, 18, 24)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 20, 30, 65)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 22, 35, 70)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 25, 30, 45)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 30, 35, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 50, 90, 150)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 60, 100, 160)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 80, 125)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 70, 85, 155)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 110, 220)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 110, 150, 240)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 130, 140, 210)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 150, 160, 260)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 160, 230, 407)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 200, 300, 550)));
        }
        
    }
	
	public class StaffDamage extends ItemModifier {

        public StaffDamage() {
            super(Collections.singletonList(Item.ItemType.STAFF), 100, "damage", r + "DMG: ", null, false);
            
            setOrderPriority(0);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 3)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 3, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 5, 7)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 2, 7, 11)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 9)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 8, 9, 12)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 10, 15, 30)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 11, 25, 35)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 12, 15, 23)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 15, 12, 35)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 25, 45, 60)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 30, 50, 80)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 32, 40, 63)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 35, 45, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 45, 60, 110)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 55, 76, 120)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 70, 115)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 75, 80, 130)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 115, 200)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 100, 140, 270)));
		}
        
	}

	public class PolearmDamage extends ItemModifier {

	    public PolearmDamage() {
	        super(Collections.singletonList(Item.ItemType.POLEARM), 100, "damage", r + "DMG: ", null, false);

	        setOrderPriority(0);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 1, 3)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 1, 3, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 1, 5, 7)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 2, 7, 11)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 5, 6, 9)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 8, 9, 12)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 10, 15, 30)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 11, 25, 35)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 12, 15, 23)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 15, 12, 35)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 25, 45, 60)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 30, 50, 80)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 32, 40, 63)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 35, 45, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 45, 60, 110)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 55, 76, 120)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 70, 115)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 75, 80, 130)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 115, 200)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 100, 140, 270)));
	    }

	}

	public class BowDamage extends ItemModifier {

	    public BowDamage() {
	        super(Collections.singletonList(Item.ItemType.BOW), 100, "damage", r + "DMG: ", null, false);

			setOrderPriority(0);

			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 1, 2, 5)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 3, 5, 8)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 6, 9, 23)));
			addCondition(new ModifierCondition(ItemTier.TIER_1, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 9, 15, 24)));

			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 10, 12, 17)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 16, 18, 24)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 20, 30, 65)));
			addCondition(new ModifierCondition(ItemTier.TIER_2, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 22, 35, 70)));

			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 25, 30, 45)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 30, 35, 70)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 50, 90, 150)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 60, 100, 160)));

			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 65, 80, 125)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 70, 85, 155)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 90, 110, 220)));
			addCondition(new ModifierCondition(ItemTier.TIER_4, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 110, 150, 240)));

			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.COMMON, new ModifierRange(ModifierType.TRIPLE, 130, 140, 210)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNCOMMON, new ModifierRange(ModifierType.TRIPLE, 150, 160, 260)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.RARE, new ModifierRange(ModifierType.TRIPLE, 160, 230, 407)));
			addCondition(new ModifierCondition(ItemTier.TIER_5, ItemRarity.UNIQUE, new ModifierRange(ModifierType.TRIPLE, 190, 250, 450)));
	    }

	}

	/* disabled as of patch 1.9 by Mayley's request
	/*public class StrDexVitInt extends ItemModifier {

		public StrDexVitInt() {
			super(weapons, -1, null, null, null);
			setOrderPriority(11);
			addCondition(new ModifierCondition(ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 15), 25));
			addCondition(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 35), 20).setBonus(new ModifierCondition(ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 35), 5).setReplacement(StrDexVitInt.class)));
			addCondition(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 75), 15).setBonus(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 75), 5).setReplacement(StrDexVitInt.class).setBonus(new ModifierCondition(ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 75), 1).setReplacement(StrDexVitInt.class))));
			addCondition(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 115), 15).setBonus(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 115), 9).setReplacement(StrDexVitInt.class).setBonus(new ModifierCondition(ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 115), 4).setReplacement(StrDexVitInt.class))));
			addCondition(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 315), 20).setBonus(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 315), 10).setReplacement(StrDexVitInt.class).setBonus(new ModifierCondition(ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 315), 5).setReplacement(StrDexVitInt.class))));
		}
		
		@Override
        public void chooseStat() {
            List<String> allStats = Arrays.asList("strength", "vitality", "intellect", "dexterity");
            chosenStat = allStats.get(new Random().nextInt(allStats.size()));
        }
        
        @Override
        public String getPrefix(ItemMeta meta){
            if (chosenStat == null || chosenStat.equals("")) {
                chooseStat();
            }
            return r + chosenStat.substring(0, 3).toUpperCase() + ": +";
        }
        
        @Override
        public String getNBTName() {
            if (chosenStat == null || chosenStat.equals("")) {
                chooseStat();
            }
            return chosenStat;
        }
	}*/

	
	public class Critical extends ItemModifier {

		public Critical() {
			super(weapons, -1, "criticalHit", r + "CRITICAL HIT: ", "%");
			setOrderPriority(4);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 2), 2));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 4), 5));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 5), 8));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 6), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 10), 7));
		}
		
	}
	
	public class LifeSteal extends ItemModifier {

		public LifeSteal() {
			super(weapons, -1, "lifesteal", r + "LIFE STEAL: ", "%");
			setOrderPriority(2);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 30), 2));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 15), 4));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 12), 5));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 7), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 8), 8));
		}
		
	}
	
	public class Knockback extends ItemModifier {

		public Knockback() {
			super(Arrays.asList(Item.ItemType.AXE, Item.ItemType.POLEARM, Item.ItemType.STAFF, Item.ItemType.SWORD), -1, "knockback", r + "KNOCKBACK: ", "%");
			setOrderPriority(7);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 3), 3));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 6), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 12), 13));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 15), 16));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 20), 20));
		}
		
	}
	
	public class Blind extends ItemModifier {

		public Blind() {
			super(weapons, -1, "blind", r + "BLIND: ", "%");
			setOrderPriority(8);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5), 3));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 7), 5));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 9), 8));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 9), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 11), 11));
		}
		
	}
	
	public class Slow extends ItemModifier {

		public Slow() {
			super(Collections.singletonList(Item.ItemType.BOW), -1, "slow", r + "SLOW: ", "%");
			setOrderPriority(6);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 3), 3));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 4), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 5), 13));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 7), 16));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 10), 20));
		}
		
	}

	public class Elemental extends ItemModifier {

		public Elemental() {
			super(Arrays.asList(Item.ItemType.AXE, Item.ItemType.POLEARM, Item.ItemType.STAFF, Item.ItemType.SWORD), -1, null, null, null);
			setOrderPriority(9);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 4), 6));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 9), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 15), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 25), 15));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 55), 20));
		}
		
		@Override
        public void chooseStat() {
            chosenStat = elements.get(new Random().nextInt(elements.size()));
        }
        
        @Override
        public String getPrefix(ItemMeta meta){
            chooseStat();
            return r + chosenStat + " DMG: +";
        }
        
        @Override
        public String getNBTName() {
            return chosenStat.toLowerCase() + "Damage";
        }
	}

	public class ElementalBow extends ItemModifier {

		public ElementalBow() {
			super(Collections.singletonList(Item.ItemType.BOW), -1, null, null, null);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 8), 6));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 15), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 25), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 45), 15));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 75), 20));
		}
		
		@Override
        public void chooseStat() {
            chosenStat = elements.get(new Random().nextInt(elements.size()));
        }
        
        @Override
        public String getPrefix(ItemMeta meta){
            chooseStat();
            return r + chosenStat + " DMG: +";
        }
        
        @Override
        public String getNBTName() {
            return chosenStat.toLowerCase() + "Damage";
        }
	}
	
	public class Versus extends ItemModifier {

		public Versus() {
			super(weapons, -1, null, null, "% DMG");
			setOrderPriority(1);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 10), 6));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 12), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 15), 10));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 20), 12));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 15), 15));
		}
		
		@Override
		public void chooseStat() {
		    chosenStat = versus.get(new Random().nextInt(versus.size()));
		}
		
		@Override
		public String getPrefix(ItemMeta meta){
			if (chosenStat == null || chosenStat.equals("")) {
			    chooseStat();
			}
		    return r + "vs. " + chosenStat.toUpperCase() + ": +";
		}
		
		@Override
		public String getNBTName() {
		    return "vs" + chosenStat;
		}
	}
	
	public class Pure extends ItemModifier {

		public Pure() {
			super(Collections.singletonList(Item.ItemType.AXE), -1, "pureDamage", r + "PURE DMG: +", null);
			setOrderPriority(10);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 5), 6));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 8), 9));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 15), 5));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 25), 5));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 45), 10));
		}
		
	}
	
	public class Accuracy extends ItemModifier {

		public Accuracy() {
			super(Collections.singletonList(Item.ItemType.SWORD), -1, "accuracy", r + "ACCURACY: ", "%");
			setOrderPriority(3);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 10), 8));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 12), 12));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 25), 15));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 28), 20));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 35), 15));
		}
		
	}
	
	public class ArmorPenetration extends ItemModifier {

		public ArmorPenetration() {
			super(Collections.singletonList(Item.ItemType.AXE), -1, "armorPenetration", r + "ARMOR PENETRATION: ", "%");
			setOrderPriority(5);
			addCondition(new ModifierCondition(Item.ItemTier.TIER_1, null, new ModifierRange(ModifierType.STATIC, 1, 1), 20));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_2, null, new ModifierRange(ModifierType.STATIC, 1, 3), 20));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_3, null, new ModifierRange(ModifierType.STATIC, 1, 5), 25));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_4, null, new ModifierRange(ModifierType.STATIC, 1, 8), 20));
			addCondition(new ModifierCondition(Item.ItemTier.TIER_5, null, new ModifierRange(ModifierType.STATIC, 1, 10), 15));
		}
		
	}
	
}
