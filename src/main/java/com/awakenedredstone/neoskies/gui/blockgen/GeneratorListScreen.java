package com.awakenedredstone.neoskies.gui.blockgen;

import com.awakenedredstone.neoskies.data.BlockGeneratorLoader;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElement;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElementBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UIUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class GeneratorListScreen extends SimpleGui {
    private int page = 0;

    public GeneratorListScreen(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        setTitle(Texts.translatable("gui.neoskies.block_gen"));

        UIUtils.fillGui(this);

        AtomicInteger slot = new AtomicInteger(10);
        int offset = page * 28;

        GuiElementInterface createGenerator = new CBGuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
          .setName(Texts.translatable("gui.neoskies.block_gen.create"))
          .setCallback((index, type, action, gui) -> {
              ServerPlayerEntity guiPlayer = gui.getPlayer();
              new GeneratorNameScreen(guiPlayer, "").open();
          }).build();

        List<Map.Entry<Identifier, BlockGeneratorLoader.BlockGenerator>> generators = BlockGeneratorLoader.INSTANCE.getGenerators().entrySet().stream().toList();

        for (int i = offset; i < Math.min(offset + 28, generators.size()); i++) {
            if ((slot.get() + 1) % 9 == 0 && slot.get() > 10) slot.addAndGet(2);
            Map.Entry<Identifier, BlockGeneratorLoader.BlockGenerator> entry = generators.get(i);
            Identifier id = entry.getKey();
            BlockGeneratorLoader.BlockGenerator generator = entry.getValue();

            Optional<Block> blockOrEmpty = Registries.BLOCK.getOrEmpty(id);
            Item icon;
            if (blockOrEmpty.isPresent()) {
                Item item = blockOrEmpty.get().asItem();
                icon = Objects.requireNonNullElseGet(item, () -> generator.target().icon());
            } else {
                icon = generator.target().icon();
            }

            CBGuiElementBuilder builder = (CBGuiElementBuilder) new CBGuiElementBuilder()
              .setItem(icon)
              .setName(Texts.translatable("gui.neoskies.block_gen.generator"))
              .addLoreLine(Texts.loreBase("gui.neoskies.block_gen.generator.id", map -> {
                  map.put("id", id.toString());
              }))
              .addLoreLine(Texts.loreBase(Text.translatable("gui.neoskies.block_gen.generator.type"), map -> {
                  map.put("type", Texts.translatable("gui.neoskies.block_gen.generator.type." + generator.target().id()));
              }))
              .addLoreLine(Texts.loreBase("gui.neoskies.block_gen.generator.source", map -> {
                  map.put("source", generator.source().toString());
              }))
              .setCallback(() -> {
                  getPlayer().playSoundToPlayer(SoundEvents.BLOCK_VAULT_EJECT_ITEM, SoundCategory.MASTER, 0.5f, 1);
                  new GeneratorScreen(player, id).open();
              });

            String[] split = generator.target().description().split("\n");
            builder.addLoreLine(Texts.loreBase("gui.neoskies.block_gen.generator.target", map -> {
                map.put("target", split[0]);
            }));

            for (int j = 1; j < split.length; j++) {
                builder.addLoreLine(Texts.loreBase(Texts.literal("<dark_gray>" + split[j] + "</dark_gray>")));
            }

            setSlot(slot.getAndIncrement(), builder);
        }

        setSlot(slot.getAndIncrement(), createGenerator);
    }

    @Override
    public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
        Slot slot = this.getSlotRedirectOrPlayer(index);
        if (slot != null) {
            addSlot(slot.getStack());
        }

        return super.onClick(index, type, action, element);
    }
}
