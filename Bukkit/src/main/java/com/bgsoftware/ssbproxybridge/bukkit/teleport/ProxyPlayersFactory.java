package com.bgsoftware.ssbproxybridge.bukkit.teleport;

import com.bgsoftware.superiorskyblock.api.factory.PlayersFactory;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class ProxyPlayersFactory implements PlayersFactory {

    private static final ProxyPlayersFactory INSTANCE = new ProxyPlayersFactory();

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
        return ProxyPlayerTeleportAlgorithm.get(playerTeleportAlgorithm);
    }

}
