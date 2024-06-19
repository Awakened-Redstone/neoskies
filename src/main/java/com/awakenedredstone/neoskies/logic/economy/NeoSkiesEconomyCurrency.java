package com.awakenedredstone.neoskies.logic.economy;

import com.awakenedredstone.neoskies.logic.IslandLogic;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@SuppressWarnings("ClassCanBeRecord")
public class NeoSkiesEconomyCurrency implements EconomyCurrency {
    private final Identifier id;

    public NeoSkiesEconomyCurrency(Identifier id) {
        this.id = id;
    }

    @Override
    public Text name() {
        return Text.translatable("neoskies.economy.name");
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public String formatValue(long value, boolean precise) {
        return String.format("$%d", value);
    }

    @Override
    public long parseValue(String value) throws NumberFormatException {
        String parse = value.replaceFirst("^\\$", "");
        return Long.parseLong(parse);
    }

    @Override
    public EconomyProvider provider() {
        return IslandLogic.getInstance().ECONOMY.PROVIDER;
    }
}
