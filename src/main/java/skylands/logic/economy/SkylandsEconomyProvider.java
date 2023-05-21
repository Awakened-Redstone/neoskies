package skylands.logic.economy;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import skylands.api.SkylandsAPI;
import skylands.logic.Island;
import skylands.logic.Skylands;

import java.util.*;

public class SkylandsEconomyProvider implements EconomyProvider {
    @Getter
    private final Map<UUID, EconomyAccount> accounts = new HashMap<>();

    @Override
    public Text name() {
        return Text.translatable("skylands.economy.name");
    }

    @Override
    public @Nullable EconomyAccount getAccount(MinecraftServer server, GameProfile profile, String accountId) {
        Optional<Island> islandOptional = SkylandsAPI.getIsland(profile.getId());
        if (islandOptional.isPresent()) {
            Island island = islandOptional.get();
            return getAccountFromIsland(island);
        } else {
            return null;
        }
    }

    @Override
    public Collection<EconomyAccount> getAccounts(MinecraftServer server, GameProfile profile) {
        Optional<Island> islandOptional = SkylandsAPI.getIslandByPlayer(profile.getId());
        if (islandOptional.isPresent()) {
            Island island = islandOptional.get();
            return Collections.singleton(getAccountFromIsland(island));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public @Nullable EconomyCurrency getCurrency(MinecraftServer server, String currencyId) {
        return Skylands.getInstance().economy.CURRENCY;
    }

    @Override
    public Collection<EconomyCurrency> getCurrencies(MinecraftServer server) {
        return Collections.singleton(Skylands.getInstance().economy.CURRENCY);
    }

    @Override
    public @Nullable String defaultAccount(MinecraftServer server, GameProfile profile, EconomyCurrency currency) {
        Optional<Island> islandOptional = SkylandsAPI.getIsland(profile.getId());
        if (islandOptional.isPresent()) {
            Island island = islandOptional.get();
            return island.getIslandIdentifier().toString();
        } else {
            return null;
        }
    }

    private EconomyAccount getAccountFromIsland(Island island) {
        return accounts.computeIfAbsent(island.getIslandId(), pair -> new SkylandsEconomyAccount(island.getIslandId(), island.getIslandIdentifier()));
    }
}
