package com.awakenedredstone.neoskies.gui.blockgen;

import com.awakenedredstone.neoskies.data.BlockGeneratorLoader;
import com.awakenedredstone.neoskies.gui.PagedGui;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElement;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElementBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UIUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GenerationGroupScreen extends PagedGui {
    private final BlockGeneratorLoader.BlockGenerator.GenerationGroup generationGroup;
    private final List<BlockGeneratorLoader.BlockGenerator.Generation> generators;
    private final @Nullable GuiInterface parent;
    private int page = 0;
    private int selectedSlot = -1;
    private GuiElementInterface selectedElement = null;

    public GenerationGroupScreen(ServerPlayerEntity player, BlockGeneratorLoader.BlockGenerator.GenerationGroup generationGroup, @Nullable GuiInterface parent) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.generationGroup = generationGroup;
        if (parent != null) {
            this.parent = parent;
        } else if (player.currentScreenHandler instanceof VirtualScreenHandler virtualScreenHandler) {
            this.parent = virtualScreenHandler.getGui();
        } else {
            this.parent = null;
        }

        setTitle(Texts.translatable("gui.neoskies.block_gen.edit"));
        UIUtils.fillGui(this);

        this.generators = generationGroup.blocks();

        /*int offset = page * 28;
        AtomicInteger slot = new AtomicInteger(10);

        for (int i = offset; i < Math.min(offset + 28, generators.size()); i++) {
            if ((slot.get() + 1) % 9 == 0 && slot.get() > 10) slot.addAndGet(2);
            BlockGeneratorLoader.BlockGenerator.Generation generation = generators.get(i);
            Item item = generation.state().getBlock().asItem();

            CBGuiElement element = new CBGuiElementBuilder(item)
              .hideDefaultTooltip()
              .addLoreLine(Texts.loreBase("gui.neoskies.block_gen.block.weight", map -> map.put("weight", String.valueOf(generation.weight()))))
              .addLoreLine(Text.empty())
              .addLoreLine(Texts.loreBase("gui.neoskies.block_gen.block.delete.tip"))
              .setCallback((index, type, action, gui) -> {
                  if (type == ClickType.MOUSE_RIGHT) {
                      gui.getPlayer().playSoundToPlayer(SoundEvents.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER, SoundCategory.MASTER, 1f, 1);

                      setSlot(selectedSlot, selectedElement);
                      setSlot(index, deleteConfirmation(gui.getSlot(index)));
                      selectedSlot = index;
                  }
              }).build();

            setSlot(slot.getAndIncrement(), element);
        }*/

        /*CBGuiElementBuilder close = new CBGuiElementBuilder(Items.BARRIER)
          .setName(Texts.translatable("gui.neoskies.close"))
          .setCallback((index, type, action, gui) -> {
              gui.getPlayer().playSoundToPlayer(SoundEvents.BLOCK_VAULT_INSERT_ITEM_FAIL, SoundCategory.MASTER, 0.5f, 1);
              if (this.parent != null) {
                  this.parent.close();
                  this.parent.open();
              } else {
                  gui.close();
              }
          });*/

        /*setSlot(getSize() - 1, close);*/

        this.updateDisplay();
    }

    private GuiElementInterface deleteConfirmation(GuiElementInterface item) {
        selectedElement = item;
        return new CBGuiElementBuilder(Items.TNT_MINECART)
          .addLoreLine(Texts.loreBase("gui.neoskies.block_gen.block.delete.tooltip"))
          .enchant(Enchantments.UNBREAKING, 1)
          .applyComponent(DataComponentTypes.ENCHANTMENTS, comp -> comp.withShowInTooltip(false))
          .setName(Texts.translatable("gui.neoskies.block_gen.block.delete"))
          .setCallback((index, type, action, gui) -> {
              if (type == ClickType.MOUSE_RIGHT_SHIFT) {
                  gui.getPlayer().playSoundToPlayer(SoundEvents.BLOCK_TRIAL_SPAWNER_SPAWN_MOB, SoundCategory.MASTER, 1f, 1);
                  gui.getPlayer().sendMessage(Texts.literal("Delete item"));
              } else {
                  gui.getPlayer().playSoundToPlayer(SoundEvents.BLOCK_VAULT_INSERT_ITEM_FAIL, SoundCategory.MASTER, 0.5f, 1);
                  setSlot(index, item);
              }
          }).build();
    }

    @Override
    protected int getPageAmount() {
        return generators.size() / 28 + 1;
    }

    @Override
    protected DisplayElement getElement(int id) {
        if (id < generators.size()) {
            return DisplayElement.of(new CBGuiElementBuilder(generators.get(id).state().getBlock().asItem())
              .hideDefaultTooltip()
              .addLoreLine(Texts.loreBase("gui.neoskies.block_gen.block.weight", map -> map.put("weight", String.valueOf(generators.get(id).weight()))))
              .addLoreLine(Text.empty())
              .addLoreLine(Texts.loreBase("gui.neoskies.block_gen.block.delete.tip"))
              .setCallback((index, type, action, gui) -> {
                  if (type == ClickType.MOUSE_RIGHT) {
                      gui.getPlayer().playSoundToPlayer(SoundEvents.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER, SoundCategory.MASTER, 1f, 1);

                      setSlot(selectedSlot, selectedElement);
                      setSlot(index, deleteConfirmation(gui.getSlot(index)));
                      selectedSlot = index;
                  }
              }).build());
        }

        return DisplayElement.empty();
    }
}
