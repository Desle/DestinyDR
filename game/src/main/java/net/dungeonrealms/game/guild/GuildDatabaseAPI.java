package net.dungeonrealms.game.guild;

import net.dungeonrealms.game.guild.database.GuildDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;


public interface GuildDatabaseAPI {

    static GuildDatabaseAPI get() {
        return GuildDatabase.getAPI();
    }


    /**
     * [DATA STRUCTURE]
     * <p>
     * {
     * -> info:
     * -> name
     * -> display name
     * -> owner
     * -> tag
     * -> motd
     * -> officers
     * -> members
     * -> netLevel
     * -> experience
     * }
     *
     * @return Returns default document template
     */
    static Document getDocumentTemplate(String owner, String guildName, String displayName, String tag, String banner) {
        return new Document("info",
                new Document("owner", owner)
                        .append("name", guildName)
                        .append("displayName", displayName)
                        .append("tag", tag)
                        .append("motd", "")
                        .append("banner", banner)
                        .append("officers", new ArrayList<String>())
                        .append("members", new ArrayList<String>())
                        .append("netLevel", 1)
                        .append("experience", 0));
    }

    /**
     * Returns guild document
     *
     * @param guildName
     * @return
     */
    Document getDocument(String guildName);

    /**
     * @param guildName Guild Name.
     * @param tag       Clan Tag.
     * @param owner     owner UUID
     * @param callback  Call back type
     */

    void createGuild(String guildName, String displayName, String tag, UUID owner, String banner, Consumer<Boolean> callback);

    /**
     * @param uuid Player
     * @return If guild is considered null
     */
    boolean isGuildNull(UUID uuid);


    /**
     * Updates cached guild
     *
     * @param guildName Guild name
     */
    boolean updateCache(String guildName);

    /**
     * Remove from guild
     *
     * @param guildName Guild name
     */
    void removeFromCache(String guildName);


    /**
     * Checks if guild is cached
     *
     * @param guildName Guild name
     */
    boolean isGuildCached(String guildName);


    /**
     * Gets the guild of a player.
     *
     * @param uuid Player
     * @return Gets guild that player is in
     */
    String getGuildOf(UUID uuid);

    /**
     * Remove guild
     *
     * @param guildName Guild name
     */

    void deleteGuild(String guildName);


    /**
     * Demotes a player from [OFFICER] to [MEMBER]
     *
     * @param guildName Name of guild
     * @param uuid      Player
     */
    void demotePlayer(String guildName, UUID uuid);

    /**
     * Adds player to guild
     *
     * @param guildName Name of guild
     * @param uuid      Player
     */
    void addPlayer(String guildName, UUID uuid);


    /**
     * Promotes a player, like [MEMBER] to [OFFICER]
     *
     * @param guildName Name of guild
     * @param uuid      Player
     */
    void promotePlayer(String guildName, UUID uuid);

    /**
     * @param guildName Name of guild
     * @param uuid      Player
     */
    void removeFromGuild(String guildName, UUID uuid);

    /**
     * @param uuid      Player
     * @param guildName Name of guild
     */
    boolean isOwner(UUID uuid, String guildName);

    /**
     * @param uuid      Player
     * @param guildName Name of guild
     */
    boolean isMember(UUID uuid, String guildName);

    /**
     * @param uuid      Player
     * @param guildName Name of guild
     */
    boolean isOfficer(UUID uuid, String guildName);

    /**
     * @param uuid      Player
     * @param guildName Name of guild
     */
    boolean isInGuild(UUID uuid, String guildName);

    /**
     * @param guildName Name of guild.
     * @param motd      the motd
     */
    void setMotdOf(String guildName, String motd);

    /**
     * @param guildName Name of guild.
     */
    String getMotdOf(String guildName);


    /**
     * @param guildName Name of guild.
     */
    String getOwnerOf(String guildName);


    /**
     * @param guildName Name of guild.
     */
    void setOwner(String guildName, UUID uuid);


    /**
     * @param player target player
     * @return boolean
     */
    boolean isOwnerOfGuild(UUID player);

    /**
     * @param guildName targeted guild.
     * @return The tag
     */
    String getTagOf(String guildName);

    /**
     * @param guildName targeted guild.
     * @return The display name
     */
    String getDisplayNameOf(String guildName);

    /**
     * @param guildName targeted guild.
     * @return The banner of guild
     */
    String getBannerOf(String guildName);

    /**
     * @param guildName name wanting the players.
     * @return The online players of a guild.
     */
    List<UUID> getAllGuildMembers(String guildName);

    /**
     * @param guildName name wanting the players.
     * @return The online players of a guild.
     */
    List<UUID> getGuildOfficers(String guildName);

    /**
     * @param guildName name wanting the players.
     * @return The online players of a guild.
     */
    List<UUID> getAllOfGuild(String guildName);

    /**
     * Sets the players guild.
     *
     * @param uuid      Player
     * @param guildName Name of Guild
     */
    void setGuild(UUID uuid, String guildName);


    /**
     * @param clanTag Name of ClanTag.
     * @param action  ASync Callback.
     * @return boolean.
     */
    boolean doesTagExist(String clanTag, Consumer<Boolean> action);

    /**
     * @param guildName Name of the Guild.
     * @param action    Async Callback.
     * @return boolean.
     */
    boolean doesGuildNameExist(String guildName, Consumer<Boolean> action);

    /**
     * @param uuid1 UUID of first player.
     * @param uuid2 UUID of second player.
     * @return boolean.
     */
    boolean areInSameGuild(UUID uuid1, UUID uuid2);

}
