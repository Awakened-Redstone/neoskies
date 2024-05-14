package com.awakenedredstone.neoskies.logic.economy;

import com.awakenedredstone.neoskies.SkylandsMain;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import net.minecraft.util.Identifier;

public class Economy {
    public Economy() {
        CommonEconomy.register("skycoin", this.PROVIDER);
    }

    public final SkylandsEconomyProvider PROVIDER = new SkylandsEconomyProvider();
    public final EconomyCurrency CURRENCY = new SkylandsEconomyCurrency(new Identifier(SkylandsMain.MOD_ID, "sky_coin"));
}
