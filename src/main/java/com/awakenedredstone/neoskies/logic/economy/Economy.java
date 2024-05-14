package com.awakenedredstone.neoskies.logic.economy;

import com.awakenedredstone.neoskies.NeoSkies;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.util.Identifier;

public class Economy {
    public Economy() {
        CommonEconomy.register("skycoin", this.PROVIDER);
    }

    public final NeoSkiesEconomyProvider PROVIDER = new NeoSkiesEconomyProvider();
    public final EconomyCurrency CURRENCY = new NeoSkiesEconomyCurrency(new Identifier(NeoSkies.MOD_ID, "sky_coin"));
}
