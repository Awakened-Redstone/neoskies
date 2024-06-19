package com.awakenedredstone.neoskies.logic.economy;

import com.awakenedredstone.neoskies.logic.IslandLogic;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.math.BigInteger;
import java.util.UUID;

public class NeoSkiesEconomyAccount implements EconomyAccount {
    private final UUID islandUuid;
    private final Identifier id;
    private long balance;

    public NeoSkiesEconomyAccount(UUID islandUuid, Identifier id) {
        this(islandUuid, id, 0);
    }

    public NeoSkiesEconomyAccount(UUID islandUuid, Identifier id, long balance) {
        this.islandUuid = islandUuid;
        this.id = id;
        this.balance = balance;
    }

    @Override
    public Text name() {
        return Text.translatable("neoskies.economy.name");
    }

    @Override
    public UUID owner() {
        return islandUuid;
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public long balance() {
        return balance;
    }

    @Override
    public EconomyTransaction canIncreaseBalance(long value) {
        //Avoid overflow without risking precision errors, at the cost of some RAM and CPU
        return BigInteger.valueOf(balance).add(BigInteger.valueOf(value)).compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? failure(value) : success(value);
    }

    @Override
    public EconomyTransaction canDecreaseBalance(long value) {
        return balance >= value ? success(value) : failure(value);
    }

    @Override
    public void setBalance(long value) {
        balance = value;
    }

    @Override
    public EconomyProvider provider() {
        return IslandLogic.ECONOMY.PROVIDER;
    }

    @Override
    public EconomyCurrency currency() {
        return IslandLogic.ECONOMY.CURRENCY;
    }

    private EconomyTransaction failure(long transactionAmount) {
        return new EconomyTransaction.Simple(false, Text.translatable("neoskies.economy.transaction.fail"), balance(), balance(), transactionAmount, this);
    }

    private EconomyTransaction success(long transactionAmount) {
        return new EconomyTransaction.Simple(true, Text.translatable("neoskies.economy.transaction.success"), balance() - transactionAmount, balance(), transactionAmount, this);
    }
}
