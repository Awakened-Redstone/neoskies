package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.gui.polymer.CBGuiElement;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElementBuilder;
import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.NotNull;

public class UIUtils {
    public static final CBGuiElement FILLER = new CBGuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.empty()).hideTooltip().build();

    public static @NotNull CrashException createCrashException(Throwable throwable, String message) {
        return new CrashException(CrashReport.create(throwable, message));
    }

    public static void fillGui(@NotNull SlotHolder gui) {
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setSlot(i, FILLER);
        }
    }

    public static void fillGui(@NotNull SlotHolder gui, GuiElementInterface item) {
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setSlot(i, item);
        }
    }

    public static void quickFillGui(@NotNull SlotHolder gui, GuiElementInterface item) {
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getSlot(i) == null || gui.getSlot(i).getItemStack().isEmpty()) gui.setSlot(i, item);
        }
    }

    public static @NotNull Identifier addToPath(@NotNull Identifier identifier, String toAdd) {
        return new Identifier(identifier + "/" + toAdd);
    }
}
