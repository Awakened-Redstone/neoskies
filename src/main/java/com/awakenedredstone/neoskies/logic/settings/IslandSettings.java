package com.awakenedredstone.neoskies.logic.settings;

import com.awakenedredstone.neoskies.api.island.PermissionLevel;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElement;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElementBuilder;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesPermissionLevels;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesRegistries;
import com.awakenedredstone.neoskies.util.MapBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IslandSettings {
    protected final PermissionLevel defaultLevel;
    private final Identifier identifier;
    private final CBGuiElement icon;

    public IslandSettings(Identifier identifier, CBGuiElement icon, PermissionLevel defaultLevel) {
        this.defaultLevel = defaultLevel;
        this.identifier = identifier;
        this.icon = icon;
    }

    public IslandSettings(Identifier identifier, CBGuiElement icon) {
        this(identifier, icon, NeoSkiesPermissionLevels.MEMBER);
    }

    public IslandSettings(Identifier identifier, Item icon) {
        this(identifier, new CBGuiElementBuilder(icon).build());
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public CBGuiElement getIcon() {
        return icon;
    }

    public PermissionLevel getDefaultLevel() {
        return defaultLevel;
    }

    public CBGuiElement buildIcon(Island island) {
        CBGuiElementBuilder builder = icon.getBuilder();
        builder.hideDefaultTooltip()
          .setLore(buildLore(island))
          .setName(Texts.translatable("island_settings." + identifier.toTranslationKey()))
          .setCallback((index, type, action, gui) -> {
              switch (type) {
                  case MOUSE_LEFT -> {
                      gui.getPlayer().playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                      IslandSettingsUtil.offsetPermission(island.getSettings().get(identifier), 1);
                  }
                  case MOUSE_RIGHT -> {
                      gui.getPlayer().playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                      IslandSettingsUtil.offsetPermission(island.getSettings().get(identifier), -1);
                  }
              }
          });
        return builder.build();
    }

    public List<Text> buildLore(Island island) {
        List<Text> lore = new ArrayList<>();
        lore.add(Texts.loreBase("island_settings/description." + identifier.toTranslationKey()));
        lore.add(Text.empty());
        int value = island.getSettings().get(identifier).getPermissionLevel().getLevel();
        List<Integer> levels = new ArrayList<>();
        for (Map.Entry<RegistryKey<PermissionLevel>, PermissionLevel> entry : NeoSkiesRegistries.PERMISSION_LEVELS.getEntrySet()) {
            levels.add(entry.getValue().getLevel());
        }
        if (!levels.contains(value)) levels.add(value);
        levels.sort(Integer::compareTo);
        Collections.reverse(levels);

        for (Integer level : levels) {
            Text levelText = Texts.translatable("island_settings.level." + level);
            Map<String, Text> placeholders = new MapBuilder<String, Text>()
              .put("level", levelText)
              .build();

            if (value == level) {
                lore.add(Text.empty().setStyle(Style.EMPTY.withItalic(false)).append(Texts.of(Text.translatable("island_settings.selected"), placeholders)));
            } else {
                lore.add(Text.empty().setStyle(Style.EMPTY.withItalic(false)).append(Texts.of(Text.translatable("island_settings.unselected"), placeholders)));
            }
        }

        return lore;
    }

    public String getTranslationKey() {
        return identifier.toTranslationKey();
    }
}
