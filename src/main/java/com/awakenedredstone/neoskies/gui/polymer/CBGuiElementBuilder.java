package com.awakenedredstone.neoskies.gui.polymer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class CBGuiElementBuilder extends GuiElementBuilder {
    /**
     * Constructs a CBGuiElementBuilder with the default options.
     */
    public CBGuiElementBuilder() { }

    /**
     * Constructs a CBGuiElementBuilder with the specified Item.
     *
     * @param item the item to use
     */
    public CBGuiElementBuilder(Item item) {
        this.itemStack = new ItemStack(item);
    }

    /**
     * Constructs a CBGuiElementBuilder with the specified Item
     * and number of items.
     *
     * @param item  the item to use
     * @param count the number of items
     */
    public CBGuiElementBuilder(Item item, int count) {
        this.itemStack = new ItemStack(item, count);
    }

    /**
     * Constructs a CBGuiElementBuilder with the specified ItemStack.
     *
     * @param stack  the item stack to use
     */
    public CBGuiElementBuilder(ItemStack stack) {
        this.itemStack = stack.copy();
    }

    /**
     * Constructs a CBGuiElementBuilder based on the supplied stack.
     *
     * @param stack the stack to base the builder of
     * @return the constructed builder
     */
    public static CBGuiElementBuilder from(ItemStack stack) {
        return new CBGuiElementBuilder(stack);
    }

    @NotNull
    public static CBGuiElementBuilder from(@NotNull GuiElementInterface element) {
        CBGuiElementBuilder builder = from(element.getItemStack());
        builder.callback = element.getGuiCallback();

        return builder;
    }

    @NotNull
    public static CBGuiElementBuilder fromStack(@NotNull GuiElementInterface element) {
        return from(element.getItemStack());
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Deprecated
    public static List<Text> getLore(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines();
    }

    /**
     * Sets the type of Item of the element.
     *
     * @param item the item to use
     * @return this element builder
     */
    public CBGuiElementBuilder setItem(Item item) {
        this.itemStack = new ItemStack(item.getRegistryEntry(), this.itemStack.getCount(), this.itemStack.getComponentChanges());
        return this;
    }

    /**
     * Sets the name of the element.
     *
     * @param name the name to use
     * @return this element builder
     */
    public CBGuiElementBuilder setName(Text name) {
        this.itemStack.set(DataComponentTypes.ITEM_NAME, name.copy());
        return this;
    }

    /**
     * Sets the rarity of the element.
     *
     * @param rarity to use
     * @return this element builder
     */
    public CBGuiElementBuilder setRarity(Rarity rarity) {
        this.itemStack.set(DataComponentTypes.RARITY, rarity);
        return this;
    }

    /**
     * Sets the number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public CBGuiElementBuilder setCount(int count) {
        this.itemStack.setCount(count);
        return this;
    }


    /**
     * Sets the max number of items in the element.
     *
     * @param count the number of items
     * @return this element builder
     */
    public CBGuiElementBuilder setMaxCount(int count) {
        this.itemStack.set(DataComponentTypes.MAX_STACK_SIZE, count);
        return this;
    }

    /**
     * Sets the lore lines of the element.
     *
     * @param lore a list of all the lore lines
     * @return this element builder
     */
    public CBGuiElementBuilder setLore(List<Text> lore) {
        this.itemStack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return this;
    }

    /**
     * Adds a line of lore to the element.
     *
     * @param lore the line to add
     * @return this element builder
     */
    public CBGuiElementBuilder addLoreLine(Text lore) {
        this.itemStack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, lore, LoreComponent::with);
        return this;
    }

    /**
     * Set the damage of the element. This will only be
     * visible if the item supports has durability.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public CBGuiElementBuilder setDamage(int damage) {
        this.itemStack.set(DataComponentTypes.DAMAGE, damage);
        return this;
    }

    /**
     * Set the max damage of the element.
     *
     * @param damage the amount of durability the item is missing
     * @return this element builder
     */
    public CBGuiElementBuilder setMaxDamage(int damage) {
        this.itemStack.set(DataComponentTypes.MAX_DAMAGE, damage);
        return this;
    }

    /**
     * Disables all default components on an item.
     * @return this element builder
     */
    public CBGuiElementBuilder noDefaults() {
        for (var x : this.itemStack.getItem().getComponents()) {
            if (this.itemStack.get(x.type()) == x.value()) {
                this.itemStack.set(x.type(), null);
            }
        }
        return this;
    }

    public <T> CBGuiElementBuilder setComponent(DataComponentType<T> type, @Nullable T value) {
        this.itemStack.set(type, value);
        return this;
    }

    public <T> CBGuiElementBuilder applyComponent(DataComponentType<T> type, UnaryOperator<T> applier) {
        this.itemStack.apply(type, null, comp -> comp != null ? applier.apply(comp) : null);
        return this;
    }

    /**
     * Hides all component-item related tooltip added by item's or non name/lore components.
     *
     * @return this element builder
     */
    public CBGuiElementBuilder hideDefaultTooltip() {
        this.itemStack.apply(DataComponentTypes.TRIM, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.UNBREAKABLE, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.ENCHANTMENTS, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.STORED_ENCHANTMENTS, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.ATTRIBUTE_MODIFIERS, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.DYED_COLOR, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.CAN_BREAK, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.apply(DataComponentTypes.CAN_PLACE_ON, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        this.itemStack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        return this;
    }

    /**
     * Hides tooltip completely, making it never show.
     * @return this element builder
     */
    public CBGuiElementBuilder hideTooltip() {
        this.itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
        return this;
    }

    /**
     * Give the element the specified enchantment.
     *
     * @param enchantment the enchantment to apply
     * @param level       the level of the specified enchantment
     * @return this element builder
     */
    public CBGuiElementBuilder enchant(Enchantment enchantment, int level) {
        this.itemStack.addEnchantment(enchantment, level);
        return this;
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public CBGuiElementBuilder glow() {
        this.itemStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return this;
    }

    /**
     * Sets the element to have an enchantment glint.
     *
     * @return this element builder
     */
    public CBGuiElementBuilder glow(boolean value) {
        this.itemStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, value);
        return this;
    }

    /**
     * Sets the custom model data of the element.
     *
     * @param value the value used for custom model data
     * @return this element builder
     */
    public CBGuiElementBuilder setCustomModelData(int value) {
        this.itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(value));
        return this;
    }

    /**
     * Sets the element to be unbreakable, also hides the durability bar.
     *
     * @return this element builder
     */
    public CBGuiElementBuilder unbreakable() {
        this.itemStack.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
        return this;
    }

    /**
     * Sets the skull owner tag of a player head.
     * If the server parameter is not supplied it may lag the client while it loads the texture,
     * otherwise if the server is provided and the {@link GameProfile} contains a UUID then the
     * textures will be loaded by the server. This can take some time the first load,
     * however the skins are cached for later uses so its often less noticeable to let the
     * server load the textures.
     *
     * @param profile the {@link GameProfile} of the owner
     * @param server  the server instance, used to get the textures
     * @return this element builder
     */
    public CBGuiElementBuilder setSkullOwner(GameProfile profile, @Nullable MinecraftServer server) {
        if (profile.getId() != null && server != null) {
            if (server.getSessionService().getTextures(profile) == MinecraftProfileTextures.EMPTY) {
                var tmp = server.getSessionService().fetchProfile(profile.getId(), false);
                if (tmp != null) {
                    profile = tmp.profile();
                }
            }
        }
        this.itemStack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
        return this;
    }

    /**
     * Sets the skull owner tag of a player head.
     * This method uses raw values required by client to display the skin
     * Ideal for textures generated with 3rd party websites like mineskin.org
     *
     * @param value     texture value used by client
     * @return this element builder
     */
    public CBGuiElementBuilder setSkullOwner(String value) {
        return this.setSkullOwner(value, null, null);
    }

    /**
     * Sets the skull owner tag of a player head.
     * This method uses raw values required by client to display the skin
     * Ideal for textures generated with 3rd party websites like mineskin.org
     *
     * @param value     texture value used by client
     * @param signature optional signature, will be ignored when set to null
     * @param uuid      UUID of skin owner, if null default will be used
     * @return this element builder
     */
    public CBGuiElementBuilder setSkullOwner(String value, @Nullable String signature, @Nullable UUID uuid) {
        PropertyMap map = new PropertyMap();
        map.put("textures", new Property("textures", value, signature));
        this.itemStack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.ofNullable(uuid), map));
        return this;
    }

    @Override
    public CBGuiElementBuilder setCallback(GuiElement.ClickCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public CBGuiElementBuilder setCallback(GuiElementInterface.ItemClickCallback callback) {
        this.callback = callback;
        return this;
    }

    public CBGuiElement build() {
        return new CBGuiElement(asStack(), this.callback);
    }
}
