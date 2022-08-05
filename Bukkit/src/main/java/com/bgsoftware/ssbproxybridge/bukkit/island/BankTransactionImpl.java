package com.bgsoftware.ssbproxybridge.bukkit.island;

import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.UUID;

public class BankTransactionImpl implements BankTransaction {

    private final UUID player;
    private final BankAction action;
    private final int position;
    private final long time;
    private final String failureReason;
    private final BigDecimal amount;

    public BankTransactionImpl(UUID player, BankAction action, int position, long time, String failureReason, BigDecimal amount) {
        this.player = player;
        this.action = action;
        this.position = position;
        this.time = time;
        this.failureReason = failureReason;
        this.amount = amount;
    }

    @Nullable
    @Override
    public UUID getPlayer() {
        return this.player;
    }

    @Override
    public BankAction getAction() {
        return this.action;
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public String getDate() {
        return ""; // TODO
    }

    @Override
    public String getFailureReason() {
        return this.failureReason;
    }

    @Override
    public BigDecimal getAmount() {
        return this.amount;
    }

}
