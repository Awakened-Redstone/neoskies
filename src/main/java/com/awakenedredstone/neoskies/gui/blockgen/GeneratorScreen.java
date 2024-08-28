package com.awakenedredstone.neoskies.gui.blockgen;

import com.awakenedredstone.neoskies.data.BlockGeneratorLoader;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElement;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElementBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UIUtils;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GeneratorScreen extends SimpleGui {
    private final Identifier generatorId;
    private final BlockGeneratorLoader.BlockGenerator generator;
    private final @Nullable GuiInterface parent;
    private int page = 0;

    public GeneratorScreen(ServerPlayerEntity player, Identifier generatorId) {
        this(player, generatorId, null);
    }

    public GeneratorScreen(ServerPlayerEntity player, Identifier generatorId, @Nullable GuiInterface parent) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.generatorId = generatorId;
        if (parent != null) {
            this.parent = parent;
        } else if (player.currentScreenHandler instanceof VirtualScreenHandler virtualScreenHandler) {
            this.parent = virtualScreenHandler.getGui();
        } else {
            this.parent = null;
        }

        BlockGeneratorLoader.BlockGenerator generator = BlockGeneratorLoader.INSTANCE.getGenerators().get(generatorId);
        if (generator == null) {
            generator = new BlockGeneratorLoader.BlockGenerator(null, null, List.of());
        }

        this.generator = generator;

        setTitle(Texts.translatable("gui.neoskies.block_gen.edit"));
        UIUtils.fillGui(this);

        List<BlockGeneratorLoader.BlockGenerator.GenerationGroup> groups = this.generator.generates();

        int offset = page * 28;
        AtomicInteger slot = new AtomicInteger(10);

        GuiElementInterface createGenerator = new CBGuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
          .setName(Texts.translatable("gui.neoskies.block_gen.create"))
          .setCallback((index, type, action, gui) -> {
              ServerPlayerEntity guiPlayer = gui.getPlayer();
              //new GeneratorNameScreen(guiPlayer, "").open();
          }).build();

        GuiElementInterface sourceItem = new CBGuiElementBuilder(Items.WATER_BUCKET)
          .setName(Texts.literal("Generator source"))
          .setCallback(() -> {
              getPlayer().playSoundToPlayer(SoundEvents.BLOCK_VAULT_EJECT_ITEM, SoundCategory.MASTER, 0.5f, 1);
          }).build();

        GuiElementInterface targetItem = new CBGuiElementBuilder(Items.LAVA_BUCKET)
          .setName(Texts.literal("Generator target"))
          .setCallback(() -> {
              getPlayer().playSoundToPlayer(SoundEvents.BLOCK_VAULT_EJECT_ITEM, SoundCategory.MASTER, 0.5f, 1);
          }).build();

        setSlot(slot.getAndIncrement(), sourceItem);
        setSlot(slot.getAndIncrement(), targetItem);

        for (int i = offset; i < Math.min(offset + 28, groups.size()); i++) {
            if ((slot.get() + 1) % 9 == 0 && slot.get() > 10) slot.addAndGet(2);
            BlockGeneratorLoader.BlockGenerator.GenerationGroup group = groups.get(i);
            GuiElementInterface groupItem = new CBGuiElementBuilder(Items.CHEST)
              .setName(Texts.literal("Generator group"))
              .setCallback(() -> {
                  getPlayer().playSoundToPlayer(SoundEvents.BLOCK_VAULT_EJECT_ITEM, SoundCategory.MASTER, 0.5f, 1);
                  new GenerationGroupScreen(player, group.mutable(), null).open();
              }).build();

            setSlot(slot.getAndIncrement(), groupItem);
        }

        setSlot(slot.getAndIncrement(), createGenerator);

        CBGuiElementBuilder close = new CBGuiElementBuilder(Items.BARRIER)
          .setName(Texts.translatable("gui.neoskies.close"))
          .setCallback((index, type, action, gui) -> {
              gui.getPlayer().playSoundToPlayer(SoundEvents.BLOCK_VAULT_INSERT_ITEM_FAIL, SoundCategory.MASTER, 0.5f, 1);
              if (this.parent != null) {
                  this.parent.close();
                  this.parent.open();
              } else {
                  gui.close();
              }
          });

        setSlot(getSize() - 1, close);
    }
}
