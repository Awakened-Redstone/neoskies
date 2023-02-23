package skylands.logic;

import net.minecraft.util.Identifier;
import skylands.SkylandsMain;
import skylands.api.island.IslandSettingsList;

public enum SkylandsIslandSettings implements IslandSettingsList {
    PLACE("place"),
    BREAK("break"),
    REDSTONE("redstone"),
    BEACON("beacon"),
    COMPOSTER("composter"),
    LODESTONE("lodestone"),
    ANVIL("anvil"),
    BREWING_STAND("brewing_stand"),
    CONTAINERS("containers"),
    RESPAWN_ANCHOR("respawn_anchor"),
    HURT_HOSTILE("hurt_hostile"),
    HURT_PASSIVE("hurt_passive"),
    DRIPLEAF("dripleaf");

    private final Identifier identifier;

    SkylandsIslandSettings(String identifier) {
        this.identifier = SkylandsMain.id(identifier);
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }
}
