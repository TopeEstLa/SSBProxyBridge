package com.bgsoftware.ssbproxybridge.bukkit.player;

import com.bgsoftware.superiorskyblock.api.player.DelegateSuperiorPlayer;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class RemoteSuperiorPlayer extends DelegateSuperiorPlayer {

    private boolean onlineStatus = false;
    private Player fakePlayer;

    public RemoteSuperiorPlayer(SuperiorPlayer handle) {
        super(handle);
    }

    public void setName(String name) {
        try {
            this.fakePlayer = new FakeBukkitPlayer(name, this.getUniqueId());
            super.updateName(); // Will trigger name changes for the player.
        } finally {
            this.fakePlayer = null;
        }
    }

    @Nullable
    @Override
    public Player asPlayer() {
        return this.fakePlayer != null ? this.fakePlayer : super.asPlayer();
    }

    @Override
    public boolean isOnline() {
        return onlineStatus || super.isOnline();
    }

    @Override
    public boolean isShownAsOnline() {
        return onlineStatus || super.isShownAsOnline();
    }

    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

}
