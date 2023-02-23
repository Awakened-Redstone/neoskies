package skylands.logic;

import net.minecraft.entity.player.PlayerEntity;
import skylands.api.BalanceHandler;

import java.util.ArrayList;
import java.util.UUID;

//TODO: Make it per island instead of per player, maybe?
public class SkylandsBalanceHandler implements BalanceHandler {
    public ArrayList<Wallet> wallets = new ArrayList<>();

    public Wallet getWallet(PlayerEntity player) {
        for (var wallet : this.wallets) {
            if (wallet.playerUuid.equals(player.getUuid())) return wallet;
        }
        Wallet wallet = new Wallet(player);
        this.wallets.add(wallet);
        return wallet;
    }

    @Override
    public double getBalance(PlayerEntity player) {
        return getWallet(player).balance;
    }

    @Override
    public void setBalance(PlayerEntity player, double balance) {
        Wallet wallet = this.getWallet(player);
        wallet.balance = balance;
    }

    public static class Wallet {
        public UUID playerUuid;
        public double balance = 0;

        public Wallet(PlayerEntity player) {
            this.playerUuid = player.getUuid();
        }
    }
}
