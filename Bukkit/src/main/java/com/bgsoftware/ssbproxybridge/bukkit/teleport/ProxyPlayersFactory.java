package com.bgsoftware.ssbproxybridge.bukkit.teleport;

import com.bgsoftware.superiorskyblock.api.factory.PlayersFactory;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProxyPlayersFactory implements PlayersFactory {

    private static final ProxyPlayersFactory INSTANCE = new ProxyPlayersFactory();

    private static final Map<UUID, ProxyPlayerTeleportAlgorithm> teleportAlgorithms = new HashMap<>();

    public static ProxyPlayersFactory getInstance() {
        return INSTANCE;
    }

    private ProxyPlayersFactory() {

    }

    public SuperiorPlayer createPlayer(SuperiorPlayer superiorPlayer) {
        return superiorPlayer;
    }

    public PersistentDataContainer createPersistentDataContainer(SuperiorPlayer superiorPlayer, PersistentDataContainer persistentDataContainer) {
        return persistentDataContainer;
    }

    @Override
    public PlayerTeleportAlgorithm createPlayerTeleportAlgorithm(SuperiorPlayer superiorPlayer, PlayerTeleportAlgorithm playerTeleportAlgorithm) {
        return teleportAlgorithms.computeIfAbsent(superiorPlayer.getUniqueId(), u -> new ProxyPlayerTeleportAlgorithm(playerTeleportAlgorithm));
    }

    @Nullable
    public ProxyPlayerTeleportAlgorithm getPlayerTeleportAlgorithm(UUID playerUUID) {
        return teleportAlgorithms.get(playerUUID);
    }

}
