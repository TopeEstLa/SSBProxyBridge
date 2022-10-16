package com.bgsoftware.ssbproxybridge.bukkit.utils;

public interface Consts {

    interface Island {

        String UUID = "uuid";
        String OWNER = "owner";
        String CENTER = "center";
        String CREATION_TIME = "creation_time";
        String ISLAND_TYPE = "island_type";
        String DISCORD = "discord";
        String PAYPAL = "paypal";
        String WORTH_BONUS = "worth_bonus";
        String LEVELS_BONUS = "levels_bonus";
        String LOCKED = "locked";
        String IGNORED = "ignored";
        String NAME = "name";
        String DESCRIPTION = "description";
        String GENERATED_SCHEMATICS = "generated_schematics";
        String UNLOCKED_WORLDS = "unlocked_worlds";
        String LAST_TIME_UPDATED = "last_time_updated";
        String BLOCK_COUNTS = "block_counts";
        String HOMES = "island_homes";
        String MEMBERS = "island_members";
        String BANNED = "banned_players";
        String INVITED = "invited_players";
        String COOPS = "coop_players";
        String PLAYER_PERMISSIONS = "player_permissions";
        String ROLE_PERMISSIONS = "role_permissions";
        String UPGRADES = "upgrades";
        String BLOCK_LIMITS = "block_limits";
        String RATINGS = "ratings";
        String COMPLETED_MISSIONS = "completed_missions";
        String ISLAND_FLAGS = "island_flags";
        String GENERATOR_RATES = "generator_rates";
        String UNIQUE_VISITORS = "unique_visitors";
        String ENTITY_LIMITS = "entity_limits";
        String ISLAND_EFFECTS = "island_effects";
        String ROLE_LIMITS = "role_limits";
        String VISITOR_HOMES = "visitor_homes";
        String SIZE = "size";
        String BANK_LIMIT = "bank_limit";
        String COOPS_LIMIT = "coops_limit";
        String MEMBERS_LIMIT = "members_limit";
        String WARPS_LIMIT = "warps_limit";
        String CROP_GROWTH = "crop_growth_multiplier";
        String SPAWNER_RATES = "spawner_rates_multiplier";
        String MOB_DROPS = "mob_drops_multiplier";
        String BALANCE = "balance";
        String LAST_INTEREST_TIME = "last_interest_time";
        String WARPS = "warps";
        String WARP_CATEGORIES = "warp_categories";
        String BANK_TRANSACTIONS = "bank_transactions";
        String PERSISTENT_DATA = "data";

        interface BlockCount {

            String BLOCK = "block";
            String COUNT = "count";

        }

        interface IslandHome {

            String ENVIRONMENT = "environment";
            String LOCATION = "location";

        }

        interface Member {

            String PLAYER = "player";
            String ROLE = "role";

        }

        interface Banned {

            String PLAYER = "player";
            String BANNED_BY = "banned_by";

        }

        interface PlayerPermission {

            String PLAYER = "player";
            String PERMISSIONS = "permissions";

            interface Privilege {

                String NAME = "permission";
                String STATUS = "status";

            }

        }

        interface RolePermission {

            String PRIVILEGE = "permission";
            String ROLE = "role";

        }

        interface Upgrade {

            String NAME = "upgrade";
            String LEVEL = "level";

        }

        interface BlockLimit {

            String BLOCK = "block";
            String LIMIT = "limit";

        }

        interface Rating {

            String PLAYER = "player";
            String RATING = "rating";

        }

        interface IslandFlag {

            String NAME = "name";
            String STATUS = "status";

        }

        interface GeneratorRate {

            String ENVIRONMENT = "environment";
            String RATES = "rates";

            interface BlockRate {

                String BLOCK = "block";
                String RATE = "rate";

            }

        }

        interface UniqueVisitor {

            String PLAYER = "player";
            String TIME = "time";

        }

        interface EntityLimit {

            String ENTITY = "entity";
            String LIMIT = "limit";

        }

        interface IslandEffect {

            String NAME = "effect_type";
            String LEVEL = "level";

        }

        interface RoleLimit {

            String ROLE = "role";
            String LIMIT = "limit";

        }

        interface VisitorHome {

            String ENVIRONMENT = "environment";
            String LOCATION = "location";

        }

        interface Warp {

            String NAME = "name";
            String LOCATION = "location";
            String PRIVATE = "private";
            String CATEGORY = "category";

        }

        interface WarpCategory {

            String NAME = "name";
            String SLOT = "slot";

        }

        interface BankTransaction {

            String PLAYER = "player";
            String BANK_ACTION = "bank_action";
            String POSITION = "position";
            String TIME = "time";
            String FAILURE_REASON = "failure_reason";
            String AMOUNT = "amount";

        }

        interface Visitors {

            String PLAYER = "player";

        }

        interface Coop {

            String UUID = "uuid";

        }

        interface Invited {

            String UUID = "uuid";

        }

    }

    interface Player {

        String UUID = "uuid";
        String LAST_USED_NAME = "last_used_name";
        String ROLE = "role";
        String DISBANDS = "disbands";
        String LAST_USED_SKIN = "last_used_skin";
        String LAST_TIME_UPDATED = "last_time_updated";
        String LANGUAGE = "language";
        String TOGGLED_PANEL = "toggled_panel";
        String BORDER_COLOR = "border_color";
        String TOGGLED_BORDER = "toggled_border";
        String ISLAND_FLY = "island_fly";
        String BLOCKS_STACKER = "blocks_stacker";
        String TEAM_CHAT = "team_chat";
        String ADMIN_BYPASS = "admin_bypass";
        String ADMIN_SPY = "admin_spy";
        String COMPLETED_MISSIONS = "completed_missions";
        String PERSISTENT_DATA = "data";

    }

    interface Mission {

        String NAME = "name";
        String FINISH_COUNT = "finish_count";

    }

    interface DataSync {

        interface DataSyncRequest {

            String INCLUDE_PLAYERS = "include_players";

        }

        interface ForceDataSync {

            String ISLANDS = "islands";
            String PLAYERS = "players";

        }

    }

    interface Action {

        String ACTION = "action";
        String PLAYER = "player";
        String RESPONSE_ID = "response-id";

        interface Teleport {

            String ISLAND = "island";
            String LOCATION = "location";

        }

        interface CreateIsland {

            String UUID = "uuid";
            String LEADER = "leader";
            String POSITION = "position";
            String NAME = "name";
            String SCHEMATIC = "schematic";
            String WORTH_BONUS = "worth_bonus";
            String LEVELS_BONUS = "level_bonus";

            interface Response {

                String ERROR = "error";
                String RESULT = "result";

            }

        }

        interface SendMessage {

            String PLAYER = "player";
            String CONSOLE = "console";
            String TYPE = "type";
            String ARGS = "args";

        }

        interface WarpPlayer {

            String ISLAND = "island";
            String WARP_NAME = "warp_name";

        }

        interface CalculateIsland {

            String ISLAND = "island";

            interface Response {

                String ERROR = "error";
                String RESULT = "block_counts";

            }

            interface BlockCount {

                String BLOCK = "block";
                String COUNT = "count";

            }

        }

        interface SetBiome {

            String ISLAND = "island";
            String BIOME = "biome";
            String UPDATE_BLOCKS = "update_blocks";

        }

    }

}
