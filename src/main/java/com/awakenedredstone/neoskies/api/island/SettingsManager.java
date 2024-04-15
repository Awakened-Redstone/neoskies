package com.awakenedredstone.neoskies.api.island;

import com.awakenedredstone.neoskies.gui.polymer.CBGuiElement;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElementBuilder;
import com.awakenedredstone.neoskies.util.MapBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.SkylandsRegistries;
import com.awakenedredstone.neoskies.util.Texts;

import java.util.*;

@Deprecated //TODO: Replace with just registry
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
        int value = island.getSettings().get(identifier).permissionLevel.getLevel();
        List<Integer> levels = new ArrayList<>();
        for (Map.Entry<RegistryKey<PermissionLevel>, PermissionLevel> entry : SkylandsRegistries.PERMISSION_LEVELS.getEntrySet()) {
            levels.add(entry.getValue().getLevel());
        }
        if (!levels.contains(value)) levels.add(value);
        levels.sort(Integer::compareTo);
        Collections.reverse(levels);

        for (Integer level : levels) {

            Text levelText = Texts.of("text.neoskies.island_settings.level." + level);
            Map<String, Text> placeholders = new MapBuilder<String, Text>()
                .put("level", levelText)
                .build();

            if (value == level) {
                lore.add(Texts.of(Text.translatable("text.neoskies.island_settings.selected"), placeholders));
            } else {
                lore.add(Texts.of(Text.translatable("text.neoskies.island_settings.unselected"), placeholders));
            }
        }

        return lore;
    }

    private static void offsetPermission(IslandSettings settings, int offset) {
        int position = 0;
        List<PermissionLevel> levels = SkylandsRegistries.PERMISSION_LEVELS.streamEntries().map(RegistryEntry.Reference::value).toList();
        int length = levels.size();
        for (int i = 0; i < length; i++) {
            if (levels.get(i) == settings.permissionLevel) {
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

        settings.permissionLevel = levels.get(position);
    }
}
