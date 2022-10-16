package com.bgsoftware.ssbproxybridge.bukkit.utils;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.island.RemoteIsland;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Serializers {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

    private Serializers() {

    }

    @Nullable
    public static Location deserializeLocation(@Nullable String serialized) {
        if (Text.isBlank(serialized))
            return null;

        String[] sections = serialized.split(",");

        double x = Double.parseDouble(sections[1]);
        double y = Double.parseDouble(sections[2]);
        double z = Double.parseDouble(sections[3]);
        float yaw = sections.length > 5 ? Float.parseFloat(sections[4]) : 0;
        float pitch = sections.length > 4 ? Float.parseFloat(sections[5]) : 0;

        return new LazyWorldLocation(sections[0], x, y, z, yaw, pitch);
    }

    public static Island deserializeIsland(Bundle bundle) {
        Island.Builder builder = Island.newBuilder()
                .setUniqueId(bundle.getUUID(Consts.Island.UUID))
                .setOwner(module.getPlugin().getPlayers().getSuperiorPlayer(bundle.getUUID(Consts.Island.OWNER)))
                .setCenter(deserializeLocation(bundle.getExtra(Consts.Island.CENTER)))
                .setCreationTime(bundle.getLong(Consts.Island.CREATION_TIME))
                .setSchematicName(bundle.getString(Consts.Island.ISLAND_TYPE))
                .setDiscord(bundle.getString(Consts.Island.DISCORD))
                .setPaypal(bundle.getString(Consts.Island.PAYPAL))
                .setBonusWorth(bundle.getBigDecimal(Consts.Island.WORTH_BONUS))
                .setBonusLevel(bundle.getBigDecimal(Consts.Island.LEVELS_BONUS))
                .setLocked(bundle.getBoolean(Consts.Island.LOCKED))
                .setIgnored(bundle.getBoolean(Consts.Island.IGNORED))
                .setName(bundle.getString(Consts.Island.NAME))
                .setDescription(bundle.getString(Consts.Island.DESCRIPTION))
                .setGeneratedSchematics(bundle.getInt(Consts.Island.GENERATED_SCHEMATICS))
                .setUnlockedWorlds(bundle.getInt(Consts.Island.UNLOCKED_WORLDS))
                .setLastTimeUpdated(bundle.getLong(Consts.Island.LAST_TIME_UPDATED))
                .setIslandSize(bundle.getInt(Consts.Island.SIZE))
                .setBankLimit(bundle.getBigDecimal(Consts.Island.BANK_LIMIT))
                .setCoopLimit(bundle.getInt(Consts.Island.COOPS_LIMIT))
                .setTeamLimit(bundle.getInt(Consts.Island.MEMBERS_LIMIT))
                .setWarpsLimit(bundle.getInt(Consts.Island.WARPS_LIMIT))
                .setCropGrowth(bundle.getDouble(Consts.Island.CROP_GROWTH))
                .setSpawnerRates(bundle.getDouble(Consts.Island.SPAWNER_RATES))
                .setMobDrops(bundle.getDouble(Consts.Island.MOB_DROPS))
                .setBalance(bundle.getBigDecimal(Consts.Island.BALANCE))
                .setLastInterestTime(bundle.getLong(Consts.Island.LAST_INTEREST_TIME));
        deserialize(bundle, Consts.Island.BLOCK_COUNTS, blockCount -> {
            builder.setBlockLimit(Key.of(blockCount.getString(Consts.Island.BlockCount.BLOCK)),
                    blockCount.getInt(Consts.Island.BlockCount.COUNT));
        });
        deserialize(bundle, Consts.Island.HOMES, islandHome -> {
            builder.setIslandHome(deserializeLocation(islandHome.getExtra(Consts.Island.IslandHome.LOCATION)),
                    islandHome.getEnum(Consts.Island.IslandHome.ENVIRONMENT, World.Environment.class));
        });
        bundle.getList(Consts.Island.MEMBERS).forEach(memberUUID -> {
            SuperiorPlayer member = module.getPlugin().getPlayers().getSuperiorPlayer(UUID.fromString((String) memberUUID));
            builder.addIslandMember(member);
        });
        bundle.getList(Consts.Island.BANNED).forEach(memberUUID -> {
            SuperiorPlayer member = module.getPlugin().getPlayers().getSuperiorPlayer(UUID.fromString((String) memberUUID));
            builder.addBannedPlayer(member);
        });
        deserialize(bundle, Consts.Island.PLAYER_PERMISSIONS, playerPermission -> {
            SuperiorPlayer player = module.getPlugin().getPlayers().getSuperiorPlayer(
                    playerPermission.getUUID(Consts.Island.PlayerPermission.PLAYER));
            deserialize(playerPermission, Consts.Island.PlayerPermission.PERMISSIONS, permission -> builder.setPlayerPermission(player,
                    IslandPrivilege.getByName(permission.getString(Consts.Island.PlayerPermission.Privilege.NAME)),
                    permission.getBoolean(Consts.Island.PlayerPermission.Privilege.STATUS)));
        });
        deserialize(bundle, Consts.Island.ROLE_PERMISSIONS, rolePermission -> {
            builder.setRolePermission(
                    IslandPrivilege.getByName(rolePermission.getString(Consts.Island.RolePermission.PRIVILEGE)),
                    module.getPlugin().getRoles().getPlayerRole(rolePermission.getInt(Consts.Island.RolePermission.ROLE))
            );
        });
        deserialize(bundle, Consts.Island.UPGRADES, upgrade -> {
            builder.setUpgrade(
                    module.getPlugin().getUpgrades().getUpgrade(upgrade.getString(Consts.Island.Upgrade.NAME)),
                    upgrade.getInt(Consts.Island.Upgrade.LEVEL)
            );
        });
        deserialize(bundle, Consts.Island.BLOCK_LIMITS, blockLimit -> {
            builder.setBlockLimit(
                    Key.of(blockLimit.getString(Consts.Island.BlockLimit.BLOCK)),
                    blockLimit.getInt(Consts.Island.BlockLimit.LIMIT)
            );
        });
        deserialize(bundle, Consts.Island.RATINGS, rating -> {
            builder.setRating(
                    module.getPlugin().getPlayers().getSuperiorPlayer(rating.getUUID(Consts.Island.Rating.PLAYER)),
                    Rating.valueOf(rating.getInt(Consts.Island.Rating.RATING))
            );
        });
        deserialize(bundle, Consts.Island.COMPLETED_MISSIONS, rating -> {
            builder.setCompletedMission(
                    module.getPlugin().getMissions().getMission(rating.getString(Consts.Mission.NAME)),
                    rating.getInt(Consts.Mission.FINISH_COUNT)
            );
        });
        deserialize(bundle, Consts.Island.ISLAND_FLAGS, islandFlag -> {
            builder.setIslandFlag(
                    IslandFlag.getByName(islandFlag.getString(Consts.Island.IslandFlag.NAME)),
                    islandFlag.getBoolean(Consts.Island.IslandFlag.STATUS)
            );
        });
        deserialize(bundle, Consts.Island.GENERATOR_RATES, generatorRate -> {
            World.Environment environment = generatorRate.getEnum(Consts.Island.GeneratorRate.ENVIRONMENT, World.Environment.class);
            deserialize(generatorRate, Consts.Island.GeneratorRate.RATES, rate -> {
                builder.setGeneratorRate(
                        Key.of(rate.getString(Consts.Island.GeneratorRate.BlockRate.RATE)),
                        rate.getInt(Consts.Island.GeneratorRate.BlockRate.RATE),
                        environment
                );
            });
        });
        deserialize(bundle, Consts.Island.UNIQUE_VISITORS, uniqueVisitor -> {
            builder.addUniqueVisitor(
                    module.getPlugin().getPlayers().getSuperiorPlayer(uniqueVisitor.getUUID(Consts.Island.UniqueVisitor.PLAYER)),
                    uniqueVisitor.getLong(Consts.Island.UniqueVisitor.TIME)
            );
        });
        deserialize(bundle, Consts.Island.ENTITY_LIMITS, entityLimit -> {
            builder.setEntityLimit(
                    Key.of(entityLimit.getString(Consts.Island.EntityLimit.ENTITY)),
                    entityLimit.getInt(Consts.Island.UniqueVisitor.TIME)
            );
        });
        deserialize(bundle, Consts.Island.ISLAND_EFFECTS, islandEffect -> {
            builder.setIslandEffect(
                    PotionEffectType.getByName(islandEffect.getString(Consts.Island.IslandEffect.NAME)),
                    islandEffect.getInt(Consts.Island.IslandEffect.NAME)
            );
        });
        deserialize(bundle, Consts.Island.ROLE_LIMITS, roleLimit -> {
            builder.setRoleLimit(
                    module.getPlugin().getRoles().getPlayerRole(roleLimit.getInt(Consts.Island.RoleLimit.ROLE)),
                    roleLimit.getInt(Consts.Island.RoleLimit.LIMIT)
            );
        });
        deserialize(bundle, Consts.Island.VISITOR_HOMES, visitorHome -> {
            builder.setVisitorHome(
                    deserializeLocation(visitorHome.getExtra(Consts.Island.VisitorHome.LOCATION)),
                    visitorHome.getEnum(Consts.Island.VisitorHome.ENVIRONMENT, World.Environment.class)
            );
        });
        deserialize(bundle, Consts.Island.WARPS, islandWarp -> {
            builder.addWarp(
                    islandWarp.getString(Consts.Island.Warp.NAME),
                    islandWarp.getString(Consts.Island.Warp.CATEGORY),
                    deserializeLocation(islandWarp.getExtra(Consts.Island.Warp.LOCATION)),
                    islandWarp.getBoolean(Consts.Island.Warp.PRIVATE),
                    null // TODO
            );
        });
        deserialize(bundle, Consts.Island.WARP_CATEGORIES, warpCategory -> {
            builder.addWarpCategory(
                    warpCategory.getString(Consts.Island.WarpCategory.NAME),
                    warpCategory.getInt(Consts.Island.WarpCategory.SLOT),
                    null // TODO
            );
        });
        deserialize(bundle, Consts.Island.BANK_TRANSACTIONS, bankTransaction -> {
            builder.addBankTransaction(module.getPlugin().getFactory().createTransaction(
                    bankTransaction.getUUID(Consts.Island.BankTransaction.PLAYER),
                    bankTransaction.getEnum(Consts.Island.BankTransaction.BANK_ACTION, BankAction.class),
                    bankTransaction.getInt(Consts.Island.BankTransaction.POSITION),
                    bankTransaction.getLong(Consts.Island.BankTransaction.TIME),
                    bankTransaction.getString(Consts.Island.BankTransaction.FAILURE_REASON),
                    bankTransaction.getBigDecimal(Consts.Island.BankTransaction.AMOUNT)
            ));
        });
        if (bundle.contains(Consts.Island.PERSISTENT_DATA))
            builder.setPersistentData(bundle.getString(Consts.Island.PERSISTENT_DATA).getBytes(StandardCharsets.UTF_8));

        RemoteIsland remoteIsland = new RemoteIsland(bundle.getSender(), builder.build());

        DatabaseBridgeAccessor.runWithoutDataSave(remoteIsland, unused -> {
            bundle.getList(Consts.Island.INVITED).forEach(playerUUID -> {
                SuperiorPlayer player = module.getPlugin().getPlayers().getSuperiorPlayer(UUID.fromString((String) playerUUID));
                remoteIsland.inviteMember(player);
            });
            bundle.getList(Consts.Island.COOPS).forEach(playerUUID -> {
                SuperiorPlayer player = module.getPlugin().getPlayers().getSuperiorPlayer(UUID.fromString((String) playerUUID));
                remoteIsland.addCoop(player);
            });
        });

        return remoteIsland;
    }

    public static Bundle serializeIsland(Island island) {
        Bundle bundle = new Bundle();

        bundle.setUUID(Consts.Island.UUID, island.getUniqueId());
        bundle.setUUID(Consts.Island.OWNER, island.getOwner().getUniqueId());
        bundle.setExtra(Consts.Island.CENTER, serializeLocation(island.getCenter(module.getPlugin().getSettings().getWorlds().getDefaultWorld())));
        bundle.setLong(Consts.Island.CREATION_TIME, island.getCreationTime());
        bundle.setString(Consts.Island.ISLAND_TYPE, island.getSchematicName());
        bundle.setString(Consts.Island.DISCORD, island.getDiscord());
        bundle.setString(Consts.Island.PAYPAL, island.getPaypal());
        bundle.setBigDecimal(Consts.Island.WORTH_BONUS, island.getBonusWorth());
        bundle.setBigDecimal(Consts.Island.LEVELS_BONUS, island.getBonusLevel());
        bundle.setBoolean(Consts.Island.LOCKED, island.isLocked());
        bundle.setBoolean(Consts.Island.IGNORED, island.isIgnored());
        bundle.setString(Consts.Island.NAME, island.getName());
        bundle.setString(Consts.Island.DESCRIPTION, island.getDescription());
        bundle.setInt(Consts.Island.GENERATED_SCHEMATICS, island.getGeneratedSchematicsFlag());
        bundle.setInt(Consts.Island.UNLOCKED_WORLDS, island.getUnlockedWorldsFlag());
        bundle.setLong(Consts.Island.LAST_TIME_UPDATED, island.getLastTimeUpdate());

        serialize(bundle, Consts.Island.BLOCK_COUNTS, island.getBlockCountsAsBigInteger(), (block, count) -> {
            Bundle blockCount = new Bundle();
            blockCount.setString(Consts.Island.BlockCount.BLOCK, block.toString());
            blockCount.setBigInteger(Consts.Island.BlockCount.COUNT, count);
            return blockCount;
        });
        serialize(bundle, Consts.Island.HOMES, island.getIslandHomes(), (environment, location) -> {
            Bundle islandHome = new Bundle();
            islandHome.setString(Consts.Island.IslandHome.ENVIRONMENT, environment.name());
            islandHome.setExtra(Consts.Island.IslandHome.LOCATION, serializeLocation(location));
            return islandHome;
        });
        bundle.setList(Consts.Island.MEMBERS, island.getIslandMembers(false).stream()
                .map(player -> player.getUniqueId().toString()).collect(Collectors.toList()));
        bundle.setList(Consts.Island.BANNED, island.getBannedPlayers().stream()
                .map(player -> player.getUniqueId().toString()).collect(Collectors.toList()));
        bundle.setList(Consts.Island.INVITED, island.getInvitedPlayers().stream()
                .map(player -> player.getUniqueId().toString()).collect(Collectors.toList()));
        bundle.setList(Consts.Island.COOPS, island.getCoopPlayers().stream()
                .map(player -> player.getUniqueId().toString()).collect(Collectors.toList()));
        serialize(bundle, Consts.Island.PLAYER_PERMISSIONS, island.getPlayerPermissions(), (player, node) -> {
            Bundle permissions = new Bundle();
            permissions.setUUID(Consts.Island.PlayerPermission.PLAYER, player.getUniqueId());
            serialize(permissions, Consts.Island.PlayerPermission.PERMISSIONS, node.getCustomPermissions(), (privilege, status) -> {
                Bundle privilegeBundle = new Bundle();
                privilegeBundle.setString(Consts.Island.PlayerPermission.Privilege.NAME, privilege.getName());
                privilegeBundle.setBoolean(Consts.Island.PlayerPermission.Privilege.STATUS, status);
                return privilegeBundle;
            });
            return permissions;
        });
        serialize(bundle, Consts.Island.ROLE_PERMISSIONS, island.getRolePermissions(), (privilege, role) -> {
            Bundle permissions = new Bundle();
            permissions.setString(Consts.Island.RolePermission.PRIVILEGE, privilege.getName());
            permissions.setInt(Consts.Island.RolePermission.ROLE, role.getId());
            return permissions;
        });
        serialize(bundle, Consts.Island.UPGRADES, island.getUpgrades(), (upgradeName, level) -> {
            Bundle upgrade = new Bundle();
            upgrade.setString(Consts.Island.Upgrade.NAME, upgradeName);
            upgrade.setInt(Consts.Island.Upgrade.LEVEL, level);
            return upgrade;
        });
        serialize(bundle, Consts.Island.BLOCK_LIMITS, island.getCustomBlocksLimits(), (block, limit) -> {
            Bundle blockLimit = new Bundle();
            blockLimit.setString(Consts.Island.BlockLimit.BLOCK, block.toString());
            blockLimit.setInt(Consts.Island.BlockLimit.LIMIT, limit);
            return blockLimit;
        });
        serialize(bundle, Consts.Island.RATINGS, island.getRatings(), (player, rating) -> {
            Bundle ratingBundle = new Bundle();
            ratingBundle.setUUID(Consts.Island.Rating.PLAYER, player);
            ratingBundle.setInt(Consts.Island.Rating.RATING, rating.getValue());
            return ratingBundle;
        });
        serialize(bundle, Consts.Island.COMPLETED_MISSIONS, island.getCompletedMissionsWithAmounts(), (mission, finishCount) -> {
            Bundle missionBundle = new Bundle();
            missionBundle.setString(Consts.Mission.NAME, mission.getName());
            missionBundle.setInt(Consts.Mission.FINISH_COUNT, finishCount);
            return missionBundle;
        });
        serialize(bundle, Consts.Island.ISLAND_FLAGS, island.getAllSettings(), (islandFlag, status) -> {
            Bundle islandFlagBundle = new Bundle();
            islandFlagBundle.setString(Consts.Island.IslandFlag.NAME, islandFlag.getName());
            islandFlagBundle.setBoolean(Consts.Island.IslandFlag.STATUS, status == 1);
            return islandFlagBundle;
        });
        serializeGeneratorRates(bundle, Consts.Island.GENERATOR_RATES, island);
        serialize(bundle, Consts.Island.UNIQUE_VISITORS, island.getUniqueVisitorsWithTimes(), uniquePlayer -> {
            Bundle uniquePlayerBundle = new Bundle();
            uniquePlayerBundle.setUUID(Consts.Island.UniqueVisitor.PLAYER, uniquePlayer.getKey().getUniqueId());
            uniquePlayerBundle.setLong(Consts.Island.UniqueVisitor.TIME, uniquePlayer.getValue());
            return uniquePlayerBundle;
        });
        serialize(bundle, Consts.Island.ENTITY_LIMITS, island.getCustomEntitiesLimits(), (entity, limit) -> {
            Bundle entityLimit = new Bundle();
            entityLimit.setString(Consts.Island.EntityLimit.ENTITY, entity.toString());
            entityLimit.setInt(Consts.Island.EntityLimit.LIMIT, limit);
            return entityLimit;
        });
        serialize(bundle, Consts.Island.ISLAND_EFFECTS, island.getPotionEffects(), (effect, level) -> {
            Bundle islandEffect = new Bundle();
            islandEffect.setString(Consts.Island.IslandEffect.NAME, effect.getName());
            islandEffect.setInt(Consts.Island.IslandEffect.LEVEL, level);
            return islandEffect;
        });
        serialize(bundle, Consts.Island.ROLE_LIMITS, island.getCustomRoleLimits(), (role, limit) -> {
            Bundle roleLimit = new Bundle();
            roleLimit.setInt(Consts.Island.RoleLimit.ROLE, role.getId());
            roleLimit.setInt(Consts.Island.RoleLimit.LIMIT, limit);
            return roleLimit;
        });
        serializeVisitorHomes(bundle, Consts.Island.VISITOR_HOMES, island);

        bundle.setInt(Consts.Island.SIZE, island.getIslandSizeRaw());
        bundle.setBigDecimal(Consts.Island.BANK_LIMIT, island.getBankLimitRaw());
        bundle.setInt(Consts.Island.COOPS_LIMIT, island.getCoopLimitRaw());
        bundle.setInt(Consts.Island.MEMBERS_LIMIT, island.getTeamLimitRaw());
        bundle.setInt(Consts.Island.WARPS_LIMIT, island.getWarpsLimitRaw());
        bundle.setDouble(Consts.Island.CROP_GROWTH, island.getCropGrowthRaw());
        bundle.setDouble(Consts.Island.SPAWNER_RATES, island.getSpawnerRatesRaw());
        bundle.setDouble(Consts.Island.MOB_DROPS, island.getMobDropsRaw());

        bundle.setBigDecimal(Consts.Island.BALANCE, island.getIslandBank().getBalance());
        bundle.setLong(Consts.Island.LAST_INTEREST_TIME, island.getLastInterestTime());

        serialize(bundle, Consts.Island.WARPS, island.getIslandWarps().values(), islandWarp -> {
            Bundle islandWarpBundle = new Bundle();
            islandWarpBundle.setString(Consts.Island.Warp.NAME, islandWarp.getName());
            islandWarpBundle.setExtra(Consts.Island.Warp.LOCATION, serializeLocation(islandWarp.getLocation()));
            islandWarpBundle.setBoolean(Consts.Island.Warp.PRIVATE, islandWarp.hasPrivateFlag());
            // TODO
//            islandWarpBundle.setBoolean("icon", islandWarp.hasPrivateFlag());
            islandWarpBundle.setString(Consts.Island.Warp.CATEGORY, islandWarp.getCategory().getName());
            return islandWarpBundle;
        });
        serialize(bundle, Consts.Island.WARP_CATEGORIES, island.getWarpCategories().values(), warpCategory -> {
            Bundle warpCategoryBundle = new Bundle();
            warpCategoryBundle.setString(Consts.Island.WarpCategory.NAME, warpCategory.getName());
            warpCategoryBundle.setInt(Consts.Island.WarpCategory.SLOT, warpCategory.getSlot());
            // TODO
//            warpCategoryBundle.setString("icon", );
            return warpCategoryBundle;
        });
        serialize(bundle, Consts.Island.BANK_TRANSACTIONS, island.getIslandBank().getAllTransactions(), bankTransaction -> {
            Bundle bankTransactionBundle = new Bundle();
            UUID player = bankTransaction.getPlayer();
            if (player != null)
                bankTransactionBundle.setUUID(Consts.Island.BankTransaction.PLAYER, player);
            bankTransactionBundle.setEnum(Consts.Island.BankTransaction.BANK_ACTION, bankTransaction.getAction());
            bankTransactionBundle.setInt(Consts.Island.BankTransaction.POSITION, bankTransaction.getPosition());
            bankTransactionBundle.setLong(Consts.Island.BankTransaction.TIME, bankTransaction.getTime());
            bankTransactionBundle.setString(Consts.Island.BankTransaction.FAILURE_REASON, bankTransaction.getFailureReason());
            bankTransactionBundle.setBigDecimal(Consts.Island.BankTransaction.AMOUNT, bankTransaction.getAmount());
            return bankTransactionBundle;
        });

        if (!island.isPersistentDataContainerEmpty())
            bundle.setString(Consts.Island.PERSISTENT_DATA, new String(island.getPersistentDataContainer().serialize()));

        return bundle;
    }

    public static Bundle serializePlayer(SuperiorPlayer superiorPlayer) {
        Bundle bundle = new Bundle();

        bundle.setUUID(Consts.Player.UUID, superiorPlayer.getUniqueId());
        bundle.setString(Consts.Player.LAST_USED_NAME, superiorPlayer.getName());
        bundle.setInt(Consts.Player.ROLE, superiorPlayer.getPlayerRole().getId());
        bundle.setInt(Consts.Player.DISBANDS, superiorPlayer.getDisbands());
        bundle.setString(Consts.Player.LAST_USED_SKIN, superiorPlayer.getTextureValue());
        bundle.setLong(Consts.Player.LAST_TIME_UPDATED, superiorPlayer.getLastTimeStatus());
        Locale language = superiorPlayer.getUserLocale();
        bundle.setString(Consts.Player.LANGUAGE, language.getLanguage() + "-" + language.getCountry());
        bundle.setEnum(Consts.Player.BORDER_COLOR, superiorPlayer.getBorderColor());
        bundle.setBoolean(Consts.Player.TOGGLED_PANEL, superiorPlayer.hasToggledPanel());
        bundle.setBoolean(Consts.Player.TOGGLED_BORDER, superiorPlayer.hasWorldBorderEnabled());
        bundle.setBoolean(Consts.Player.ISLAND_FLY, superiorPlayer.hasIslandFlyEnabled());
        bundle.setBoolean(Consts.Player.BLOCKS_STACKER, superiorPlayer.hasBlocksStackerEnabled());
        bundle.setBoolean(Consts.Player.TEAM_CHAT, superiorPlayer.hasTeamChatEnabled());
        bundle.setBoolean(Consts.Player.ADMIN_BYPASS, superiorPlayer.hasBypassModeEnabled());
        bundle.setBoolean(Consts.Player.ADMIN_SPY, superiorPlayer.hasAdminSpyEnabled());
        serialize(bundle, Consts.Player.COMPLETED_MISSIONS, superiorPlayer.getCompletedMissionsWithAmounts(), (mission, finishCount) -> {
            Bundle missionBundle = new Bundle();
            missionBundle.setString(Consts.Mission.NAME, mission.getName());
            missionBundle.setInt(Consts.Mission.FINISH_COUNT, finishCount);
            return missionBundle;
        });
        if (!superiorPlayer.isPersistentDataContainerEmpty())
            bundle.setString(Consts.Player.PERSISTENT_DATA, new String(superiorPlayer.getPersistentDataContainer().serialize()));

        return bundle;
    }

    public static SuperiorPlayer deserializePlayer(Bundle bundle) {
        SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(bundle.getUUID(Consts.Player.UUID));
        DatabaseBridgeAccessor.runWithoutDataSave(superiorPlayer, unused -> {
            superiorPlayer.setName(bundle.getString(Consts.Player.LAST_USED_NAME));
            superiorPlayer.setPlayerRole(module.getPlugin().getRoles().getPlayerRole(bundle.getInt(Consts.Player.ROLE)));
            superiorPlayer.setDisbands(bundle.getInt(Consts.Player.DISBANDS));
            superiorPlayer.setDisbands(bundle.getInt(Consts.Player.DISBANDS));
            superiorPlayer.setTextureValue(bundle.getString(Consts.Player.LAST_USED_SKIN));
            superiorPlayer.setLastTimeStatus(bundle.getLong(Consts.Player.LAST_TIME_UPDATED));
            String[] language = bundle.getString(Consts.Player.LANGUAGE).split("-");
            superiorPlayer.setUserLocale(new Locale(language[0], language[1]));
            superiorPlayer.setBorderColor(bundle.getEnum(Consts.Player.BORDER_COLOR, BorderColor.class));
            superiorPlayer.setToggledPanel(bundle.getBoolean(Consts.Player.TOGGLED_PANEL));
            superiorPlayer.setWorldBorderEnabled(bundle.getBoolean(Consts.Player.TOGGLED_BORDER));
            superiorPlayer.setIslandFly(bundle.getBoolean(Consts.Player.ISLAND_FLY));
            superiorPlayer.setBlocksStacker(bundle.getBoolean(Consts.Player.BLOCKS_STACKER));
            superiorPlayer.setTeamChat(bundle.getBoolean(Consts.Player.TEAM_CHAT));
            superiorPlayer.setBypassMode(bundle.getBoolean(Consts.Player.ADMIN_BYPASS));
            superiorPlayer.setAdminSpy(bundle.getBoolean(Consts.Player.ADMIN_SPY));
            if (bundle.contains(Consts.Player.COMPLETED_MISSIONS)) {
                bundle.getList(Consts.Player.COMPLETED_MISSIONS).forEach(completedMission -> {
                    Bundle completedMissionBundle = (Bundle) completedMission;
                    Mission<?> mission = module.getPlugin().getMissions().getMission(completedMissionBundle.getString(Consts.Mission.NAME));
                    superiorPlayer.setAmountMissionCompleted(mission, completedMissionBundle.getInt(Consts.Mission.FINISH_COUNT));
                });
            }
            if (bundle.contains(Consts.Player.PERSISTENT_DATA))
                superiorPlayer.getPersistentDataContainer().load(bundle.getString(Consts.Player.PERSISTENT_DATA).getBytes(StandardCharsets.UTF_8));
        });
        return superiorPlayer;
    }

    private static void serializeGeneratorRates(Bundle bundle, String key, Island island) {
        List<Bundle> bundles = new LinkedList<>();
        for (World.Environment environment : World.Environment.values()) {
            Bundle environmentBundle = new Bundle();
            environmentBundle.setString(Consts.Island.GeneratorRate.ENVIRONMENT, environment.name());
            serialize(environmentBundle, Consts.Island.GeneratorRate.RATES, island.getCustomGeneratorAmounts(environment), (block, rate) -> {
                Bundle rateBundle = new Bundle();
                rateBundle.setString(Consts.Island.GeneratorRate.BlockRate.BLOCK, block.toString());
                rateBundle.setInt(Consts.Island.GeneratorRate.BlockRate.RATE, rate);
                return rateBundle;
            });
            bundles.add(environmentBundle);
        }
        if (!bundles.isEmpty())
            bundle.setList(key, bundles);
    }

    private static void serializeVisitorHomes(Bundle bundle, String key, Island island) {
        List<Bundle> bundles = new LinkedList<>();
        for (World.Environment environment : World.Environment.values()) {
            Location visitorHome = island.getVisitorsLocation(environment);
            if (visitorHome != null) {
                Bundle environmentBundle = new Bundle();
                environmentBundle.setString(Consts.Island.VisitorHome.ENVIRONMENT, environment.name());
                environmentBundle.setExtra(Consts.Island.VisitorHome.LOCATION, serializeLocation(visitorHome));
                bundles.add(environmentBundle);
            }
        }
        if (!bundles.isEmpty())
            bundle.setList(key, bundles);
    }

    private static Bundle serializeLocation(Location location) {
        Bundle bundle = new Bundle();
        bundle.setString("world", Objects.requireNonNull(LazyWorldLocation.getWorldName(location)));
        bundle.setDouble("x", location.getX());
        bundle.setDouble("y", location.getY());
        bundle.setDouble("z", location.getZ());
        bundle.setFloat("yaw", location.getYaw());
        bundle.setFloat("pitch", location.getPitch());
        return bundle;
    }

    private static Location deserializeLocation(Bundle bundle) {
        return new LazyWorldLocation(bundle.getString("world"),
                bundle.getDouble("x"), bundle.getDouble("y"), bundle.getDouble("z"),
                bundle.getFloat("yaw"), bundle.getFloat("pitch"));
    }

    private static <E> void serialize(Bundle bundle, String key, Collection<E> collection, Function<E, Bundle> function) {
        List<Bundle> bundles = new LinkedList<>();
        collection.forEach(element -> bundles.add(function.apply(element)));
        if (!bundles.isEmpty())
            bundle.setList(key, bundles);
    }

    private static <K, V> void serialize(Bundle bundle, String key, Map<K, V> map, BiFunction<K, V, Bundle> function) {
        List<Bundle> bundles = new LinkedList<>();
        map.forEach((mapKey, value) -> bundles.add(function.apply(mapKey, value)));
        if (!bundles.isEmpty())
            bundle.setList(key, bundles);
    }

    private static void deserialize(Bundle bundle, String key, Consumer<Bundle> consumer) {
        if (bundle.contains(key))
            bundle.getList(key).forEach(element -> consumer.accept((Bundle) element));
    }

}
