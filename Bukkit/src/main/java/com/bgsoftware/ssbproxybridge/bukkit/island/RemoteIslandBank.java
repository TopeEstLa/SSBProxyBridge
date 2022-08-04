package com.bgsoftware.ssbproxybridge.bukkit.island;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class RemoteIslandBank implements IslandBank {

    private final RemoteIsland island;

    public RemoteIslandBank(RemoteIsland island) {
        this.island = island;
    }

    @Override
    public BigDecimal getBalance() {
        // TODO
        return BigDecimal.ZERO;
    }

    @Override
    public void setBalance(BigDecimal balance) {
        // TODO
    }

    @Override
    public BankTransaction depositMoney(SuperiorPlayer superiorPlayer, BigDecimal money) {
        // TODO
        return null;
    }

    @Override
    public BankTransaction depositAdminMoney(CommandSender commandSender, BigDecimal money) {
        // TODO
        return null;
    }

    @Override
    public boolean canDepositMoney(BigDecimal money) {
        // TODO
        return false;
    }

    @Override
    public BankTransaction withdrawMoney(SuperiorPlayer superiorPlayer, BigDecimal bigDecimal, @Nullable List<String> list) {
        // TODO
        return null;
    }

    @Override
    public BankTransaction withdrawAdminMoney(CommandSender commandSender, BigDecimal bigDecimal) {
        // TODO
        return null;
    }

    @Override
    public List<BankTransaction> getAllTransactions() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public List<BankTransaction> getTransactions(SuperiorPlayer superiorPlayer) {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public List<BankTransaction> getConsoleTransactions() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public void loadTransaction(BankTransaction bankTransaction) {
        // TODO
    }

}
