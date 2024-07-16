package com.awakenedredstone.neoskies.gui;

import com.awakenedredstone.neoskies.gui.polymer.CBGuiElement;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElementBuilder;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UIUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.atomic.AtomicInteger;

public class CobbleGenGui extends SimpleGui {
    private int page = 0;

    public CobbleGenGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        setTitle(Texts.of("Cobblestone generator config"));

        UIUtils.fillGui(this);

        AtomicInteger slot = new AtomicInteger(10);
        int offset = page * 28;

        var ref = new Object() {
            CBGuiElement item = null;
        };
        ref.item = CBGuiElementBuilder.from(UIUtils.FILLER).setCallback((index, type, action, gui) -> {
            ServerPlayerEntity guiPlayer = gui.getPlayer();
            var stack = gui.getPlayer().currentScreenHandler.getCursorStack();
            if (!stack.isEmpty()) {
                setSlot(index, stack.copy());
                if ((slot.get() + 1) % 9 == 0 && slot.get() > 10) slot.addAndGet(2);
                setSlot(slot.getAndIncrement(), ref.item);
            }
        }).setItem(Items.LIME_STAINED_GLASS_PANE).build();

        AtomicInteger column = new AtomicInteger(1);
        AtomicInteger row = new AtomicInteger(1);

        /*List<CobbleGenConfig.CacheData> cache = IslandLogic.getCobbleGenConfig().getCache();
        for (int i = offset; i < Math.min(offset + 28, cache.size()); i++) {
            if ((slot.get() + 1) % 9 == 0 && slot.get() > 10) slot.addAndGet(2);
            CobbleGenConfig.CacheData cacheData = cache.get(i);
            setSlot(slot.getAndIncrement(), cacheData.blockStateArgument().getBlockState().getBlock().asItem().getDefaultStack());
        }*/

        setSlot(slot.getAndIncrement(), ref.item);
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
