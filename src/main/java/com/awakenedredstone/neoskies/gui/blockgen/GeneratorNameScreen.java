package com.awakenedredstone.neoskies.gui.blockgen;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.data.BlockGeneratorLoader;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElementBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GeneratorNameScreen extends AnvilInputGui {
    private static final GuiElementInterface CONFLICT = new CBGuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
      .setName(Texts.literal("<red>Conflicting ID"))
      .setCallback((index, type1, action, gui) -> {
          gui.getPlayer().playSoundToPlayer(SoundEvents.BLOCK_VAULT_CLOSE_SHUTTER, SoundCategory.MASTER, 0.5f, 1);
          GuiHelpers.sendPlayerScreenHandler(gui.getPlayer());
      })
      .build();
    private static final GuiElementInterface INVALID = new CBGuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
      .setName(Texts.literal("<red>Invalid ID"))
      .setCallback((index, type1, action, gui) -> {
          gui.getPlayer().playSoundToPlayer(SoundEvents.BLOCK_VAULT_CLOSE_SHUTTER, SoundCategory.MASTER, 0.5f, 1);
          GuiHelpers.sendPlayerScreenHandler(gui.getPlayer());
      })
      .build();
    private static final GuiElementInterface CREATE = new CBGuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
      .setName(Texts.literal("<green>Create"))
      .build();

    private final List<Identifier> identifiers;
    private final @Nullable GuiInterface parent;

    /**
     * Constructs a new input gui for the provided player.
     *
     * @param player                the player to serve this gui to
     */
    public GeneratorNameScreen(ServerPlayerEntity player, String defaultId) {
        super(player, false);
        this.identifiers = BlockGeneratorLoader.INSTANCE.getGenerators().keySet().stream().toList();
        if (player.currentScreenHandler instanceof VirtualScreenHandler virtualScreenHandler) {
            this.parent = virtualScreenHandler.getGui();
        } else {
            this.parent = null;
        }

        this.setTitle(Texts.translatable("gui.neoskies.block_gen.set_id"));
        setDefaultInputValue(defaultId);
    }

    @Override
    public void onInput(String input) {
        GuiElementInterface itemOut;
        if (!Identifier.isPathValid(input) || StringUtils.isBlank(input)) {
            itemOut = CBGuiElementBuilder.from(INVALID)
              .addLoreLine(Texts.loreBase(Texts.literal("<dark_gray>\"%s:%s\" is not a valid ID".formatted(NeoSkies.MOD_ID + "_generated", input))))
              .build();
        } else if (identifiers.contains(NeoSkies.generatedId(input))) {
            itemOut = CBGuiElementBuilder.from(CONFLICT)
              .addLoreLine(Texts.loreBase(Texts.literal("<dark_gray>%s:%s is already in use".formatted(NeoSkies.MOD_ID + "_generated", input))))
              .build();
        } else {
            itemOut = CBGuiElementBuilder.from(CREATE)
              .addLoreLine(Texts.loreBase(Texts.literal("<dark_gray>Creating %s:%s".formatted(NeoSkies.MOD_ID + "_generated", input))))
              .setCallback(() -> {
                  getPlayer().playSoundToPlayer(SoundEvents.BLOCK_VAULT_OPEN_SHUTTER, SoundCategory.MASTER, 0.3f, 1);
                  new GeneratorScreen(player, NeoSkies.generatedId(input), parent).open();
              }).build();
        }

        setSlot(2, itemOut);
    }

    @Override
    public void onClose() {
        if (parent != null) {
            parent.close();
            parent.open();
        }
    }
}
