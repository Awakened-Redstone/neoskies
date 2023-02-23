package skylands.api.island;

import com.awakenedredstone.cbserverconfig.polymer.CBGuiElement;
import com.awakenedredstone.cbserverconfig.polymer.CBGuiElementBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import skylands.SkylandsMain;
import skylands.logic.Island;
import skylands.util.Texts;

import java.util.*;

public class SettingsManager {
    private static final Map<Identifier, IslandSettings> defaultSettings = new HashMap<>();
    private static final Map<Identifier, CBGuiElement> icons = new HashMap<>();

    public static boolean registerIcon(Identifier identifier, CBGuiElement icon) {
        if (icons.containsKey(identifier)) return false;
        icons.put(identifier, icon);
        return true;
    }

    public static boolean register(Identifier identifier, IslandSettings settings, CBGuiElement icon) {
        registerIcon(identifier, icon);
        return register(identifier, settings);
    }

    public static boolean register(Identifier identifier, IslandSettings settings) {
        if (defaultSettings.containsKey(identifier)) return false;
        defaultSettings.put(identifier, settings);
        return true;
    }

    public static void update(Map<Identifier, IslandSettings> toUpdate) {
        defaultSettings.forEach((identifier, settings) -> {
            if (!toUpdate.containsKey(identifier)) toUpdate.put(identifier, settings);
        });
    }

    public static Map<Identifier, IslandSettings> getDefaultSettings() {
        return defaultSettings;
    }

    public static Map<Identifier, CBGuiElement> getIcons() {
        return icons;
    }

    public static CBGuiElement getIcon(Identifier identifier, Island island) {
        CBGuiElementBuilder builder = icons.getOrDefault(identifier, new CBGuiElementBuilder(Items.PAPER).build()).getBuilder();
        builder.hideFlag(ItemStack.TooltipSection.ADDITIONAL)
                .hideFlag(ItemStack.TooltipSection.DYE)
                .hideFlag(ItemStack.TooltipSection.CAN_DESTROY)
                .hideFlag(ItemStack.TooltipSection.CAN_PLACE)
                .hideFlag(ItemStack.TooltipSection.ENCHANTMENTS)
                .hideFlag(ItemStack.TooltipSection.MODIFIERS)
                .hideFlag(ItemStack.TooltipSection.UNBREAKABLE)
                .setLore(buildLore(island, identifier))
                .setName(Texts.of(identifier.getNamespace() + ".island_settings." + identifier.getPath()))
                .setCallback((index, type, action, gui) -> {
                    switch (type) {
                        case MOUSE_LEFT -> {
                            gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                            offsetPermission(island.getSettings().get(identifier), 1);
                        }
                        case MOUSE_RIGHT -> {
                            gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                            offsetPermission(island.getSettings().get(identifier), -1);
                        }
                    }
                });
        return builder.build();
    }

    private static List<Text> buildLore(Island island, Identifier identifier) {
        List<Text> lore = new ArrayList<>();
        lore.add(Texts.of(identifier.getNamespace() + ".island_settings." + identifier.getPath() + ".description"));
        lore.add(Text.empty());
        int value = island.getSettings().get(identifier).level.getLevel();
        List<Integer> levels = new ArrayList<>();
        for (PermissionLevel level : PermissionLevel.values()) {
            levels.add((int) level.getLevel());
        }
        if (!levels.contains(value)) levels.add(value);
        levels.sort(Integer::compareTo);
        Collections.reverse(levels);

        for (Integer level : levels) {
            if (value == level) {
                lore.add(Texts.prefixed("text.skylands.island_settings.selected", "text.skylands.island_settings.level." + level));
            } else {
                lore.add(Texts.prefixed("text.skylands.island_settings.unselected", "text.skylands.island_settings.level." + level));
            }
        }

        return lore;
    }

    private static void offsetPermission(IslandSettings settings, int offset) {
        int position = 0;
        PermissionLevel[] levels = PermissionLevel.values();
        int length = levels.length;
        for (int i = 0; i < length; i++) {
            if (levels[i] == settings.level) {
                position = i;
                break;
            }
        }

        position += offset;
        while (position < 0) {
            position += length;
        }

        while (position >= length) {
            position -= length;
        }

        settings.level = levels[position];
    }

    static {
        register(SkylandsMain.id("place"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.STONE).build());
        register(SkylandsMain.id("break"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.WOODEN_PICKAXE).build());
        register(SkylandsMain.id("redstone"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.REDSTONE).build());
        register(SkylandsMain.id("beacon"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.BEACON).build());
        register(SkylandsMain.id("composter"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.COMPOSTER).build());
        register(SkylandsMain.id("lodestone"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.LODESTONE).build());
        register(SkylandsMain.id("anvil"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.ANVIL).build());
        register(SkylandsMain.id("brewing_stand"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.BREWING_STAND).build());
        register(SkylandsMain.id("containers"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.CHEST).build());
        register(SkylandsMain.id("respawn_anchor"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.RESPAWN_ANCHOR).build());
        register(SkylandsMain.id("hurt_hostile"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.DIAMOND_SWORD).build());
        register(SkylandsMain.id("hurt_passive"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.WOODEN_SWORD).build());
        register(SkylandsMain.id("dripleaf"), new IslandSettings(PermissionLevel.MEMBER), new CBGuiElementBuilder(Items.BIG_DRIPLEAF).build());
    }
}
