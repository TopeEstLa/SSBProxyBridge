package com.bgsoftware.ssbproxybridge.bukkit.island;

import com.bgsoftware.ssbproxybridge.bukkit.database.ProxyDatabaseBridgeFactory;
import com.bgsoftware.ssbproxybridge.bukkit.proxy.ProxyPlayerBridge;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.factory.BanksFactory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class RemoteIsland implements Island {

    private static final SuperiorSkyblock plugin = SuperiorSkyblockAPI.getSuperiorSkyblock();

    private final DatabaseBridge databaseBridge = ProxyDatabaseBridgeFactory.getInstance().createIslandsDatabaseBridge(this, null);
    private final IslandBank islandBank = createIslandBank();

    private final SuperiorPlayer islandLeader;
    private final UUID uuid;
    private final BlockPosition center;
    private final long creationTime;
    private final String islandType;

    /* islands table */
    private String discord;
    private String paypal;
    private BigDecimal bonusWorth;
    private BigDecimal bonusLevel;
    private boolean isLocked;
    private boolean isIgnored;
    private String name;
    private String description;
    private byte generatedSchematic;
    private byte unlockedWorlds;
    private long lastTimeUpdated;
    private KeyMap<BigInteger> blockCounts;

    public RemoteIsland(SuperiorPlayer islandLeader, UUID uuid, BlockPosition center, long creationTime,
                        String islandType, String discord, String paypal, BigDecimal bonusWorth, BigDecimal bonusLevel,
                        boolean isLocked, boolean isIgnored, String name, String description, byte generatedSchematic,
                        byte unlockedWorlds, long lastTimeUpdated, KeyMap<BigInteger> blockCounts) {
        this.islandLeader = islandLeader;
        this.uuid = uuid;
        this.center = center;
        this.creationTime = creationTime;
        this.islandType = islandType;
        this.discord = discord;
        this.paypal = paypal;
        this.bonusWorth = bonusWorth;
        this.bonusLevel = bonusLevel;
        this.isLocked = isLocked;
        this.isIgnored = isIgnored;
        this.name = name;
        this.description = description;
        this.generatedSchematic = generatedSchematic;
        this.unlockedWorlds = unlockedWorlds;
        this.lastTimeUpdated = lastTimeUpdated;
        this.blockCounts = blockCounts;

        // Update player with him being the leader
        try {
            this.islandLeader.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);
            this.islandLeader.setIsland(this);
            this.islandLeader.setPlayerRole(plugin.getRoles().getLastRole());
        } finally {
            this.islandLeader.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
        }
    }

    @Override
    public SuperiorPlayer getOwner() {
        return this.islandLeader;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getCreationTimeDate() {
        return "";
    }

    @Override
    public void updateDatesFormatter() {
        // TODO
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(boolean includeOwner) {
        // TODO
        // TODO: When implemented, check #removeIsland
        return Collections.emptyList();
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(PlayerRole... playerRoles) {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public List<SuperiorPlayer> getBannedPlayers() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors(boolean checkCoopStatus) {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public List<SuperiorPlayer> getAllPlayersInside() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public List<SuperiorPlayer> getUniqueVisitors() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public List<Pair<SuperiorPlayer, Long>> getUniqueVisitorsWithTimes() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public void inviteMember(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public void revokeInvite(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public boolean isInvited(SuperiorPlayer superiorPlayer) {
        // TODO
        return false;
    }

    @Override
    public List<SuperiorPlayer> getInvitedPlayers() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        // TODO
    }

    @Override
    public void kickMember(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public boolean isMember(SuperiorPlayer superiorPlayer) {
        // TODO
        return false;
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer, @Nullable SuperiorPlayer whoBanned) {
        // TODO
    }

    @Override
    public void unbanMember(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public boolean isBanned(SuperiorPlayer superiorPlayer) {
        // TODO
        return false;
    }

    @Override
    public void addCoop(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public void removeCoop(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public boolean isCoop(SuperiorPlayer superiorPlayer) {
        // TODO
        return false;
    }

    @Override
    public List<SuperiorPlayer> getCoopPlayers() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public int getCoopLimit() {
        // TODO
        return 0;
    }

    @Override
    public int getCoopLimitRaw() {
        // TODO
        return 0;
    }

    @Override
    public void setCoopLimit(int coopLimit) {
        // TODO
    }

    @Override
    public void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside) {
        // TODO
    }

    @Override
    public boolean isVisitor(SuperiorPlayer superiorPlayer, boolean includeCoopStatus) {
        // TODO
        return false;
    }

    @Override
    public Location getCenter(World.Environment environment) {
        World world = plugin.getGrid().getIslandsWorld(this, environment);
        return this.center.parse(world).add(0.5, 0, 0.5);
    }

    @Nullable
    @Override
    public Location getTeleportLocation(World.Environment environment) {
        return this.getIslandHome(environment);
    }

    @Override
    public Map<World.Environment, Location> getTeleportLocations() {
        return this.getIslandHomes();
    }

    @Override
    public void setTeleportLocation(Location location) {
        this.setIslandHome(location);
    }

    @Override
    public void setTeleportLocation(World.Environment environment, @Nullable Location location) {
        this.setIslandHome(environment, location);
    }

    @Nullable
    @Override
    public Location getIslandHome(World.Environment environment) {
        // TODO
        return this.getCenter(environment);
    }

    @Override
    public Map<World.Environment, Location> getIslandHomes() {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public void setIslandHome(Location location) {
        // TODO
    }

    @Override
    public void setIslandHome(World.Environment environment, @Nullable Location location) {
        // TODO
    }

    @Nullable
    @Override
    public Location getVisitorsLocation() {
        // TODO
        return this.getCenter(plugin.getSettings().getWorlds().getDefaultWorld());
    }

    @Override
    public void setVisitorsLocation(@Nullable Location location) {
        // TODO
    }

    @Override
    public Location getMinimum() {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).subtract(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMinimumProtected() {
        int islandSize = getIslandSize();
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).subtract(islandSize, 0, islandSize);
    }

    @Override
    public Location getMaximum() {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).add(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMaximumProtected() {
        int islandSize = getIslandSize();
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).add(islandSize, 0, islandSize);
    }

    @Override
    public List<Chunk> getAllChunks() {
        return getAllChunks(false);
    }

    @Override
    public List<Chunk> getAllChunks(boolean onlyProtected) {
        List<Chunk> chunks = new LinkedList<>();

        for (World.Environment environment : World.Environment.values()) {
            try {
                chunks.addAll(getAllChunks(environment, onlyProtected));
            } catch (NullPointerException ignored) {
            }
        }

        return Collections.unmodifiableList(chunks);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment) {
        return getAllChunks(environment, false);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected) {
        return getAllChunks(environment, onlyProtected, false);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(environment, onlyProtected, false, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                            boolean noEmptyChunks, @Nullable Consumer<Chunk> onChunkLoad) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public void resetChunks(World.Environment environment, boolean onlyProtected) {
        resetChunks(environment, onlyProtected, null);
    }

    @Override
    public void resetChunks(World.Environment environment, boolean onlyProtected, @Nullable Runnable onFinish) {
        // On another server, therefore nothing to do.
    }

    @Override
    public void resetChunks(boolean onlyProtected) {
        resetChunks(onlyProtected, null);
    }

    @Override
    public void resetChunks(boolean onlyProtected, @Nullable Runnable onFinish) {
        // On another server, therefore nothing to do.
    }

    @Override
    public boolean isInside(Location location) {
        if (location.getWorld() == null || !plugin.getGrid().isIslandsWorld(location.getWorld()))
            return false;

        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        IslandArea islandArea = new IslandArea(this.center, islandDistance);

        return islandArea.intercepts(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public boolean isInsideRange(Location location) {
        return isInsideRange(location, 0);
    }

    @Override
    public boolean isInsideRange(Location location, int extra) {
        if (location.getWorld() == null || !plugin.getGrid().isIslandsWorld(location.getWorld()))
            return false;

        IslandArea islandArea = new IslandArea(center, getIslandSize());
        islandArea.expand(extra);

        return islandArea.intercepts(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public boolean isInsideRange(Chunk chunk) {
        if (chunk.getWorld() == null || !plugin.getGrid().isIslandsWorld(chunk.getWorld()))
            return false;

        IslandArea islandArea = new IslandArea(center, getIslandSize());
        islandArea.rshift(4);

        return islandArea.intercepts(chunk.getX(), chunk.getZ());
    }

    @Override
    public boolean isNormalEnabled() {
        return plugin.getProviders().getWorldsProvider().isNormalUnlocked() || (unlockedWorlds & 4) == 4;
    }

    @Override
    public void setNormalEnabled(boolean enabled) {
        // TODO
    }

    @Override
    public boolean isNetherEnabled() {
        return plugin.getProviders().getWorldsProvider().isNetherUnlocked() || (unlockedWorlds & 1) == 1;
    }

    @Override
    public void setNetherEnabled(boolean enabled) {
        // TODO
    }

    @Override
    public boolean isEndEnabled() {
        return plugin.getProviders().getWorldsProvider().isEndUnlocked() || (unlockedWorlds & 2) == 2;
    }

    @Override
    public void setEndEnabled(boolean b) {
        // TODO
    }

    @Override
    public int getUnlockedWorldsFlag() {
        return this.unlockedWorlds;
    }

    @Override
    public boolean hasPermission(CommandSender sender, IslandPrivilege islandPrivilege) {
        return sender instanceof ConsoleCommandSender ||
                hasPermission(plugin.getPlayers().getSuperiorPlayer((Player) sender), islandPrivilege);
    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege) {
        PermissionNode playerNode = getPermissionNode(superiorPlayer);
        return superiorPlayer.hasBypassModeEnabled() || superiorPlayer.hasPermissionWithoutOP("superior.admin.bypass.*") ||
                superiorPlayer.hasPermissionWithoutOP("superior.admin.bypass." + islandPrivilege.getName()) ||
                (playerNode != null && playerNode.hasPermission(islandPrivilege));
    }

    @Override
    public boolean hasPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege) {
        return getRequiredPlayerRole(islandPrivilege).getWeight() <= playerRole.getWeight();
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege, boolean value) {
        if (value)
            this.setPermission(playerRole, islandPrivilege);
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege) {
        // TODO
    }

    @Override
    public void resetPermissions() {
        // TODO
    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value) {
        // TODO
    }

    @Override
    public void resetPermissions(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public PermissionNode getPermissionNode(SuperiorPlayer superiorPlayer) {
        // TODO
        return null;
    }

    @Override
    public PlayerRole getRequiredPlayerRole(IslandPrivilege islandPrivilege) {
        // TODO
        return islandLeader.getPlayerRole();
    }

    @Override
    public Map<SuperiorPlayer, PermissionNode> getPlayerPermissions() {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public Map<IslandPrivilege, PlayerRole> getRolePermissions() {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public boolean isSpawn() {
        return false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        // TODO
    }

    @Override
    public String getRawName() {
        // TODO
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String name) {
        // TODO
    }

    @Override
    public void disbandIsland() {
        // TODO
    }

    public void removeIsland() {
        // Remove roles and island status from leader and members

        // TODO: REMOVE when #getIslandMembers functional
        try {
            this.islandLeader.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);
            this.islandLeader.setIsland(null);
            this.islandLeader.setPlayerRole(plugin.getRoles().getGuestRole());
        } finally {
            this.islandLeader.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
        }

        getIslandMembers(true).forEach(islandMember -> {
            try {
                islandMember.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);
                islandMember.setIsland(null);
                islandMember.setPlayerRole(plugin.getRoles().getGuestRole());
            } finally {
                islandMember.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
            }
        });

        plugin.getGrid().getIslandsContainer().removeIsland(this);
    }

    @Override
    public boolean transferIsland(SuperiorPlayer superiorPlayer) {
        // TODO
        return false;
    }

    @Override
    public void replacePlayers(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer) {
        // TODO
    }

    @Override
    public void calcIslandWorth(@Nullable SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public void calcIslandWorth(@Nullable SuperiorPlayer superiorPlayer, @Nullable Runnable runnable) {
        // TODO
    }

    @Override
    public IslandCalculationAlgorithm getCalculationAlgorithm() {
        return RemoteIslandCalculationAlgorithm.getInstance();
    }

    @Override
    public void updateBorder() {
        // TODO
    }

    @Override
    public void updateIslandFly(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public int getIslandSize() {
        // TODO
        return 0;
    }

    @Override
    public void setIslandSize(int islandSize) {
        // TODO
    }

    @Override
    public int getIslandSizeRaw() {
        // TODO
        return 0;
    }

    @Override
    public String getDiscord() {
        return this.discord;
    }

    @Override
    public void setDiscord(String discord) {
        // TODO
    }

    @Override
    public String getPaypal() {
        return this.paypal;
    }

    @Override
    public void setPaypal(String paypal) {
        // TODO
    }

    @Override
    public Biome getBiome() {
        // TODO
        return Biome.PLAINS;
    }

    @Override
    public void setBiome(Biome biome) {
        // TODO
    }

    @Override
    public void setBiome(Biome biome, boolean updateBlocks) {
        // TODO
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override
    public void setLocked(boolean locked) {
        // TODO
    }

    @Override
    public boolean isIgnored() {
        return this.isIgnored;
    }

    @Override
    public void setIgnored(boolean ignored) {
        // TODO
    }

    @Override
    public void sendMessage(String message, UUID... ignoredMembers) {
        // TODO: null recipient
        List<UUID> ignoredList = Arrays.asList(ignoredMembers);
        getIslandMembers(true).stream()
                .filter(superiorPlayer -> !ignoredList.contains(superiorPlayer.getUniqueId()))
                .forEach(superiorPlayer -> ProxyPlayerBridge.sendMessage(null, superiorPlayer.getName(), message));
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, Object... args) {
        this.sendMessage(messageComponent, Collections.emptyList(), args);
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, List<UUID> ignoredMembers, Object... args) {
        // TODO: null recipient
        getIslandMembers(true).stream()
                .filter(superiorPlayer -> !ignoredMembers.contains(superiorPlayer.getUniqueId()))
                .forEach(superiorPlayer -> ProxyPlayerBridge.sendMessage(null, superiorPlayer.getName(), messageComponent.getMessage()));
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int duration,
                          int fadeOut, UUID... ignoredMembers) {
        // TODO
    }

    @Override
    public void executeCommand(String command, boolean onlyOnlineMembers, UUID... ignoredMembers) {
        // TODO: other server command execution
    }

    @Override
    public boolean isBeingRecalculated() {
        return false;
    }

    @Override
    public void updateLastTime() {
        // TODO
    }

    @Override
    public void setCurrentlyActive() {
        this.lastTimeUpdated = -1L;
    }

    @Override
    public long getLastTimeUpdate() {
        return this.lastTimeUpdated;
    }

    @Override
    public void setLastTimeUpdate(long lastTimeUpdated) {
        // TODO
    }

    @Override
    public IslandBank getIslandBank() {
        // TODO
        return this.islandBank;
    }

    @Override
    public BigDecimal getBankLimit() {
        return BigDecimal.ZERO;
    }

    @Override
    public void setBankLimit(BigDecimal bankLimit) {
        // TODO
    }

    @Override
    public BigDecimal getBankLimitRaw() {
        // TODO
        return BigDecimal.ZERO;
    }

    @Override
    public boolean giveInterest(boolean checkOnlineStatus) {
        // TODO
        return false;
    }

    @Override
    public long getLastInterestTime() {
        // TODO
        return 0;
    }

    @Override
    public void setLastInterestTime(long lastInterestTime) {
        // TODO
    }

    @Override
    public long getNextInterest() {
        // TODO
        return 0;
    }

    @Override
    public void handleBlockPlace(Block block) {
        handleBlockPlace(Key.of(block), 1);
    }

    @Override
    public void handleBlockPlace(Block block, int amount) {
        handleBlockPlace(Key.of(block), amount, true);
    }

    @Override
    public void handleBlockPlace(Block block, int amount, boolean save) {
        handleBlockPlace(Key.of(block), amount, save);
    }

    @Override
    public void handleBlockPlace(Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        handleBlockPlace(key, amount, true);
    }

    @Override
    public void handleBlockPlace(Key key, int amount, boolean save) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        handleBlockPlace(key, BigInteger.valueOf(amount), save);
    }

    @Override
    public void handleBlockPlace(Key key, BigInteger amount, boolean save) {
        handleBlockPlace(key, amount, save, true);
    }

    @Override
    public void handleBlockPlace(Key key, BigInteger amount, boolean save, boolean updateLastTimeStatus) {
        // TODO
    }

    @Override
    public void handleBlocksPlace(Map<Key, Integer> map) {
        // TODO
    }

    @Override
    public void handleBlockBreak(Block block) {
        handleBlockBreak(Key.of(block), 1);
    }

    @Override
    public void handleBlockBreak(Block block, int amount) {
        handleBlockBreak(block, amount, true);
    }

    @Override
    public void handleBlockBreak(Block block, int amount, boolean save) {
        handleBlockBreak(Key.of(block), amount, save);
    }

    @Override
    public void handleBlockBreak(Key key, int amount) {
        handleBlockBreak(key, amount, true);
    }

    @Override
    public void handleBlockBreak(Key key, int amount, boolean save) {
        handleBlockBreak(key, BigInteger.valueOf(amount), save);
    }

    @Override
    public void handleBlockBreak(Key key, BigInteger amount, boolean save) {
        // TODO
    }

    @Override
    public BigInteger getBlockCountAsBigInteger(Key key) {
        return blockCounts.getOrDefault(key, BigInteger.ZERO);
    }

    @Override
    public Map<Key, BigInteger> getBlockCountsAsBigInteger() {
        return Collections.unmodifiableMap(blockCounts);
    }

    @Override
    public BigInteger getExactBlockCountAsBigInteger(Key key) {
        return blockCounts.getRaw(key, BigInteger.ZERO);
    }

    @Override
    public void clearBlockCounts() {
        // TODO
    }

    @Override
    public IslandBlocksTrackerAlgorithm getBlocksTracker() {
        return null;
    }

    @Override
    public BigDecimal getWorth() {
        // TODO
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getRawWorth() {
        // TODO
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getBonusWorth() {
        return this.bonusWorth;
    }

    @Override
    public void setBonusWorth(BigDecimal bonusWorth) {
        // TODO
    }

    @Override
    public BigDecimal getBonusLevel() {
        return this.bonusLevel;
    }

    @Override
    public void setBonusLevel(BigDecimal bonusLevel) {
        // TODO
    }

    @Override
    public BigDecimal getIslandLevel() {
        // TODO
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getRawLevel() {
        // TODO
        return BigDecimal.ZERO;
    }

    @Override
    public UpgradeLevel getUpgradeLevel(Upgrade upgrade) {
        return upgrade.getUpgradeLevel(getUpgrades().getOrDefault(upgrade.getName(), 1));
    }

    @Override
    public void setUpgradeLevel(Upgrade upgrade, int level) {
        // TODO
    }

    @Override
    public Map<String, Integer> getUpgrades() {
        return Collections.emptyMap();
    }

    @Override
    public void syncUpgrades() {
        // TODO
    }

    @Override
    public void updateUpgrades() {
        // TODO
    }

    @Override
    public long getLastTimeUpgrade() {
        // TODO
        return 0;
    }

    @Override
    public boolean hasActiveUpgradeCooldown() {
        long lastTimeUpgrade = getLastTimeUpgrade();
        long currentTime = System.currentTimeMillis();
        long upgradeCooldown = plugin.getSettings().getUpgradeCooldown();
        return upgradeCooldown > 0 && lastTimeUpgrade > 0 && currentTime - lastTimeUpgrade <= upgradeCooldown;
    }

    @Override
    public double getCropGrowthMultiplier() {
        // TODO
        return 0;
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {
        // TODO
    }

    @Override
    public double getCropGrowthRaw() {
        // TODO
        return 0;
    }

    @Override
    public double getSpawnerRatesMultiplier() {
        // TODO
        return 0;
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        // TODO
    }

    @Override
    public double getSpawnerRatesRaw() {
        // TODO
        return 0;
    }

    @Override
    public double getMobDropsMultiplier() {
        // TODO
        return 0;
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        // TODO
    }

    @Override
    public double getMobDropsRaw() {
        // TODO
        return 0;
    }

    @Override
    public int getBlockLimit(Key key) {
        // TODO
        return 0;
    }

    @Override
    public int getExactBlockLimit(Key key) {
        // TODO
        return 0;
    }

    @Override
    public Key getBlockLimitKey(Key key) {
        // TODO
        return key;
    }

    @Override
    public Map<Key, Integer> getBlocksLimits() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Key, Integer> getCustomBlocksLimits() {
        return Collections.emptyMap();
    }

    @Override
    public void clearBlockLimits() {
        // TODO
    }

    @Override
    public void setBlockLimit(Key key, int limit) {
        // TODO
    }

    @Override
    public void removeBlockLimit(Key key) {
        // TODO
    }

    @Override
    public boolean hasReachedBlockLimit(Key key) {
        return hasReachedBlockLimit(key, 1);
    }

    @Override
    public boolean hasReachedBlockLimit(Key key, int amount) {
        // TODO
        return false;
    }

    @Override
    public int getEntityLimit(EntityType entityType) {
        // TODO
        return 0;
    }

    @Override
    public int getEntityLimit(Key key) {
        // TODO
        return 0;
    }

    @Override
    public Map<Key, Integer> getEntitiesLimitsAsKeys() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Key, Integer> getCustomEntitiesLimits() {
        return Collections.emptyMap();
    }

    @Override
    public void clearEntitiesLimits() {
        // TODO
    }

    @Override
    public void setEntityLimit(EntityType entityType, int limit) {
        // TODO
    }

    @Override
    public void setEntityLimit(Key key, int limit) {
        // TODO
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType) {
        return hasReachedEntityLimit(Key.of(entityType));
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key) {
        return hasReachedEntityLimit(key, 1);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, int amount) {
        return hasReachedEntityLimit(Key.of(entityType), amount);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key, int amount) {
        // TODO
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public IslandEntitiesTrackerAlgorithm getEntitiesTracker() {
        return null;
    }

    @Override
    public int getTeamLimit() {
        // TODO
        return 0;
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        // TODO
    }

    @Override
    public int getTeamLimitRaw() {
        // TODO
        return 0;
    }

    @Override
    public int getWarpsLimit() {
        // TODO
        return 0;
    }

    @Override
    public void setWarpsLimit(int warpsLimit) {
        // TODO
    }

    @Override
    public int getWarpsLimitRaw() {
        // TODO
        return 0;
    }

    @Override
    public void setPotionEffect(PotionEffectType islandEffect, int level) {
        // TODO
    }

    @Override
    public void removePotionEffect(PotionEffectType islandEffect) {
        // TODO
    }

    @Override
    public int getPotionEffectLevel(PotionEffectType islandEffect) {
        // TODO
        return 0;
    }

    @Override
    public Map<PotionEffectType, Integer> getPotionEffects() {
        return Collections.emptyMap();
    }

    @Override
    public void applyEffects(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public void removeEffects(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public void removeEffects() {
        // TODO
    }

    @Override
    public void clearEffects() {
        // TODO
    }

    @Override
    public void setRoleLimit(PlayerRole playerRole, int limit) {
        // TODO
    }

    @Override
    public void removeRoleLimit(PlayerRole playerRole) {
        // TODO
    }

    @Override
    public int getRoleLimit(PlayerRole playerRole) {
        // TODO
        return 0;
    }

    @Override
    public int getRoleLimitRaw(PlayerRole playerRole) {
        // TODO
        return 0;
    }

    @Override
    public Map<PlayerRole, Integer> getRoleLimits() {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public Map<PlayerRole, Integer> getCustomRoleLimits() {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public WarpCategory createWarpCategory(String name) {
        // TODO
        return null;
    }

    @Nullable
    @Override
    public WarpCategory getWarpCategory(String name) {
        // TODO
        return null;
    }

    @Nullable
    @Override
    public WarpCategory getWarpCategory(int slot) {
        // TODO
        return null;
    }

    @Override
    public void renameCategory(WarpCategory warpCategory, String name) {
        // TODO
    }

    @Override
    public void deleteCategory(WarpCategory warpCategory) {
        // TODO
    }

    @Override
    public Map<String, WarpCategory> getWarpCategories() {
        return Collections.emptyMap();
    }

    @Override
    public IslandWarp createWarp(String name, Location location, @Nullable WarpCategory warpCategory) {
        // TODO
        return null;
    }

    @Override
    public void renameWarp(IslandWarp islandWarp, String name) {
        // TODO
    }

    @Nullable
    @Override
    public IslandWarp getWarp(Location location) {
        // TODO
        return null;
    }

    @Nullable
    @Override
    public IslandWarp getWarp(String name) {
        // TODO
        return null;
    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String name) {
        // TODO
    }

    @Override
    public void deleteWarp(@Nullable SuperiorPlayer superiorPlayer, Location location) {
        // TODO
    }

    @Override
    public void deleteWarp(String name) {
        // TODO
    }

    @Override
    public Map<String, IslandWarp> getIslandWarps() {
        return Collections.emptyMap();
    }

    @Override
    public Rating getRating(SuperiorPlayer superiorPlayer) {
        // TODO
        return Rating.UNKNOWN;
    }

    @Override
    public void setRating(SuperiorPlayer superiorPlayer, Rating rating) {
        // TODO
    }

    @Override
    public void removeRating(SuperiorPlayer superiorPlayer) {
        // TODO
    }

    @Override
    public double getTotalRating() {
        // TODO
        return 0;
    }

    @Override
    public int getRatingAmount() {
        // TODO
        return 0;
    }

    @Override
    public Map<UUID, Rating> getRatings() {
        return Collections.emptyMap();
    }

    @Override
    public void removeRatings() {
        // TODO
    }

    @Override
    public boolean hasSettingsEnabled(IslandFlag islandFlag) {
        // TODO
        return false;
    }

    @Override
    public Map<IslandFlag, Byte> getAllSettings() {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public void enableSettings(IslandFlag islandFlag) {
        // TODO
    }

    @Override
    public void disableSettings(IslandFlag islandFlag) {
        // TODO
    }

    @Override
    public void setGeneratorPercentage(Key key, int rate, World.Environment environment) {
        // TODO
    }

    @Override
    public boolean setGeneratorPercentage(Key key, int rate, World.Environment environment,
                                          @Nullable SuperiorPlayer superiorPlayer, boolean save) {
        // TODO
        return false;
    }

    @Override
    public int getGeneratorPercentage(Key key, World.Environment environment) {
        // TODO
        return 0;
    }

    @Override
    public Map<String, Integer> getGeneratorPercentages(World.Environment environment) {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public void setGeneratorAmount(Key key, int amount, World.Environment environment) {
        // TODO
    }

    @Override
    public void removeGeneratorAmount(Key key, World.Environment environment) {
        // TODO
    }

    @Override
    public int getGeneratorAmount(Key key, World.Environment environment) {
        // TODO
        return 0;
    }

    @Override
    public int getGeneratorTotalAmount(World.Environment environment) {
        // TODO
        return 0;
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts(World.Environment environment) {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public Map<Key, Integer> getCustomGeneratorAmounts(World.Environment environment) {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public void clearGeneratorAmounts(World.Environment environment) {
        // TODO
    }

    @Nullable
    @Override
    public Key generateBlock(Location location, boolean optimizeCobblestone) {
        return generateBlock(location, location.getWorld().getEnvironment(), optimizeCobblestone);
    }

    @Override
    public Key generateBlock(Location location, World.Environment environment, boolean optimizeCobblestone) {
        // TODO
        return null;
    }

    @Override
    public boolean wasSchematicGenerated(World.Environment environment) {
        int generateBitChange = getGeneratedSchematicBitMask(environment);

        if (generateBitChange == 0)
            return false;

        return (this.generatedSchematic & generateBitChange) != 0;
    }

    @Override
    public void setSchematicGenerate(World.Environment environment) {
        setSchematicGenerate(environment, true);
    }

    @Override
    public void setSchematicGenerate(World.Environment environment, boolean generated) {
        // TODO
    }

    @Override
    public int getGeneratedSchematicsFlag() {
        return this.generatedSchematic;
    }

    @Override
    public String getSchematicName() {
        return this.islandType;
    }

    @Override
    public int getPosition(SortingType sortingType) {
        // TODO
        return 0;
    }

    @Override
    public IslandChest[] getChest() {
        // TODO
        return new IslandChest[0];
    }

    @Override
    public int getChestSize() {
        // TODO
        return 0;
    }

    @Override
    public void setChestRows(int index, int rows) {
        // TODO
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return this.databaseBridge;
    }

    @Override
    public void completeMission(Mission<?> mission) {
        // TODO
    }

    @Override
    public void resetMission(Mission<?> mission) {
        // TODO
    }

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        // TODO
        return false;
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        // TODO
        return false;
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        // TODO
        return 0;
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts() {
        // TODO
        return Collections.emptyMap();
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        // TODO
        return null;
    }

    @Override
    @SuppressWarnings("all")
    public int compareTo(Island other) {
        if (other == null)
            return -1;

        if (plugin.getSettings().getIslandTopOrder().equals("WORTH")) {
            int compare = getWorth().compareTo(other.getWorth());
            if (compare != 0) return compare;
        } else {
            int compare = getIslandLevel().compareTo(other.getIslandLevel());
            if (compare != 0) return compare;
        }

        return getOwner().getName().compareTo(other.getOwner().getName());
    }

    private IslandBank createIslandBank() {
        RemoteIslandBank islandBank = new RemoteIslandBank(this);
        BanksFactory banksFactory = plugin.getFactory().getBanksFactory();
        return banksFactory == null ? islandBank : banksFactory.createIslandBank(this, islandBank);
    }

    private static int getGeneratedSchematicBitMask(World.Environment environment) {
        switch (environment) {
            case NORMAL:
                return 8;
            case NETHER:
                return 4;
            case THE_END:
                return 3;
            default:
                return 0;
        }
    }

}
