package net.dungeonrealms.game.world.teleportation;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public enum TeleportLocation {

	STARTER("Tutorial", null, -1, 817.5, 47, -101.818, -179.7F, 6.2F, false),
	EVENT_AREA("Event Area", null, -1, -378, 85, 341, false),
	CYRENNICA("Cyrennica", WorldRegion.CYRENNICA, 1000, -378, 85, 357),
	HARRISON_FIELD("Harrison Field", WorldRegion.HARRISON, 1500, -594, 59, 687, 92.0F, 1F),
	DARK_OAK("Dark Oak Tavern", WorldRegion.DARK_OAK, 3500, 280, 59, 1132, 2.0F, 1F),
	GLOOMY_HOLLOWS("Gloomy Hollows", WorldRegion.GLOOMY, 3500, -590, 44, 0, 144F, 1F),
	TROLLSBANE("Trollsbane Tavern", WorldRegion.TROLLSBANE, 7500, 962, 95, 1069, -153.0F, 1F),
	TRIPOLI("Tripoli", WorldRegion.TRIPOLI, 7500, -1320, 91, 370, 153F, 1F),
	CRESTGUARD("Crestguard Keep", WorldRegion.CRESTGUARD, 15000, -1428, 116, -489, 95F, 1F),
	DEADPEAKS("Deadpeaks Mountain Camp", WorldRegion.DEADPEAKS, 35000, -1173, 106, 1030, -88.0F, 1F, true, true);
	
	private String displayName;
	private WorldRegion region;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	private int price;
	private boolean allowBooks;
	@Getter private boolean chaotic;
	
	TeleportLocation(String displayName, WorldRegion region, int price, double x, double y, double z) {
		this(displayName, region, price, x, y, z, 0, 0);
	}
	
	TeleportLocation(String displayName, WorldRegion region, int price, double x, double y, double z, boolean allowBooks) {
		this(displayName, region, price, x, y, z, 0, 0, allowBooks);
	}
	
	TeleportLocation(String displayName, WorldRegion region, int price, double x, double y, double z, float yaw, float pitch) {
		this(displayName, region, price, x, y, z, yaw, pitch, true);
	}
	
	TeleportLocation(String displayName, WorldRegion region, int price, double x, double y, double z, float yaw, float pitch, boolean allowBooks){
		this(displayName, region, price, x, y, z, yaw, pitch, allowBooks, false);
	}
	
	TeleportLocation(String displayName, WorldRegion region, int price, double x, double y, double z, float yaw, float pitch, boolean allowBooks, boolean chaotic){
		this.displayName = displayName;
		this.region = region;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.price = price;
		this.allowBooks = allowBooks;
		this.chaotic = chaotic;
	}
	
	public boolean canSetHearthstone(Player player){
		if(this == CYRENNICA)
			return true;
		if(this.region == null || this.region.getAchievement() == null)
			return false;
		return Achievements.hasAchievement(player, this.region.getAchievement());
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
	
	public Location getLocation(){
		return new Location(Bukkit.getWorlds().get(0), this.x, this.y, this.z, this.yaw, this.pitch);
	}
	
	public boolean canTeleportTo(Player player){
		PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
		if (wrapper == null)
            return false;
		
        return wrapper.getAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC;
	}
	
	public static TeleportLocation getTeleportLocation(NBTTagCompound tag){
		if(tag == null || !tag.hasKey("usage"))
			return null;
		return TeleportLocation.valueOf(tag.getString("usage").toUpperCase());
	}
	
	public int getPrice(){
		return this.price;
	}
	
	public boolean canBeABook() {
		return this.allowBooks;
	}

	public static TeleportLocation getRandomBookTP() {
		List<TeleportLocation> teleportable = new ArrayList<TeleportLocation>();
    	for(TeleportLocation tl : TeleportLocation.values())
    		if(tl.canBeABook())
    			teleportable.add(tl);
    	return teleportable.get(Utils.randInt(0, teleportable.size() - 1));
	}
	
	public String getDBString() {
		return x + "," + y + "," + z + "," + yaw + "," + pitch;
	}
}
