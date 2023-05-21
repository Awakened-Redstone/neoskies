package skylands.logic.economy;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import skylands.logic.Skylands;

@SuppressWarnings("ClassCanBeRecord")
public class SkylandsEconomyCurrency implements EconomyCurrency {
    private final Identifier id;

    public SkylandsEconomyCurrency(Identifier id) {
        this.id = id;
    }

    @Override
    public Text name() {
        return Text.translatable("skylands.economy.name");
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
        String parse = value.replace("$", "");
        return Long.parseLong(parse);
    }

    @Override
    public EconomyProvider provider() {
        return Skylands.getInstance().economy.PROVIDER;
    }
}
