package com.awakenedredstone.neoskies.gui.polymer;

import net.minecraft.item.ItemStack;

public class CBGuiElement extends eu.pb4.sgui.api.elements.GuiElement {
    public CBGuiElement(ItemStack item, ClickCallback callback) {
        super(item, callback);
    }

    public CBGuiElement(ItemStack item, ItemClickCallback callback) {
        super(item, callback);
    }

    public CBGuiElementBuilder getBuilder() {
        return CBGuiElementBuilder.from(this);
    }

    public CBGuiElementBuilder getBuilderSimple() {
        return CBGuiElementBuilder.fromStack(this);
    }

    public CBGuiElement copy() {
        return CBGuiElementBuilder.from(this).build();
    }
}
