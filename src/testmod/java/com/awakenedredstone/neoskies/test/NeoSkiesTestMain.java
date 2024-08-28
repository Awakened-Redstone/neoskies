package com.awakenedredstone.neoskies.test;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.command.utils.CommandUtils;
import com.awakenedredstone.neoskies.logic.registry.NeoSkiesRegistries;
import com.awakenedredstone.neoskies.logic.settings.IslandSettings;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.AbstractBlock;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class NeoSkiesTestMain implements ModInitializer {
    @Override
    public void onInitialize() {
        TestBlock testBlock = Registry.register(Registries.BLOCK, NeoSkies.id("test_block"), new TestBlock(AbstractBlock.Settings.create().dropsNothing().solid().strength(999, 999)));
        Registry.register(Registries.ITEM, NeoSkies.id("test_block"), new PolymerBlockItem(testBlock, new Item.Settings(), Items.RED_CONCRETE) {
            @Override
            public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
                BlockStateComponent stateComponent = itemStack.getComponents().get(DataComponentTypes.BLOCK_STATE);
                if (stateComponent == null) {
                    return Items.RED_CONCRETE;
                }
                return switch (stateComponent.getValue(TestBlock.TEST_STATE)) {
                    case NOT_WORKING -> Items.RED_CONCRETE;
                    case PARTIAL -> Items.YELLOW_CONCRETE;
                    case WORKING -> Items.LIME_CONCRETE;
                    case null -> Items.RED_CONCRETE;
                };
            }
        });


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("discard").requires(source -> source.hasPermissionLevel(2))
              .then(argument("targets", EntityArgumentType.entities())
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");

                    for (Entity entity : targets) {
                        entity.discard();
                    }
                    if (targets.size() == 1) {
                        source.sendFeedback(() -> Text.translatable("commands.kill.success.single", targets.iterator().next().getDisplayName()), true);
                    } else {
                        source.sendFeedback(() -> Text.translatable("commands.kill.success.multiple", targets.size()), true);
                    }
                    return targets.size();
                })
              )
            );

            CommandUtils.registerAdmin(dispatcher, literal("test")
              .then(literal("protection_translations")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    MutableText text = Text.empty();
                    for (IslandSettings settings : NeoSkiesRegistries.ISLAND_SETTINGS) {
                        Language lang = Language.getInstance();
                        String translationKey = settings.getIdentifier().toTranslationKey();
                        boolean hasTranslation = lang.hasTranslation("island_protection." + translationKey);
                        text.append(Text.literal(translationKey));
                        text.append(Text.literal(": "));
                        Text text1 = Text.literal(hasTranslation ? "Translated" : "Untranslated").formatted(hasTranslation ? Formatting.GREEN : Formatting.RED);
                        text.append(text1);
                        text.append(Text.literal("\n"));
                    }

                    source.sendFeedback(() -> text, false);
                    return 0;
                })
              ).then(literal("settings_translations")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    MutableText text = Text.empty();
                    for (IslandSettings settings : NeoSkiesRegistries.ISLAND_SETTINGS) {
                        Language lang = Language.getInstance();
                        String translationKey = settings.getIdentifier().toTranslationKey();
                        boolean hasTranslation = lang.hasTranslation("island_settings." + translationKey);
                        text.append(Text.literal(translationKey));
                        text.append(Text.literal(": "));
                        Text text1 = Text.literal(hasTranslation ? "Translated" : "Untranslated").formatted(hasTranslation ? Formatting.GREEN : Formatting.RED);
                        text.append(text1);
                        text.append(Text.literal("\n"));
                    }

                    source.sendFeedback(() -> text, false);
                    return 0;
                })
              ).then(literal("settings_description_translations")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    MutableText text = Text.empty();
                    for (IslandSettings settings : NeoSkiesRegistries.ISLAND_SETTINGS) {
                        Language lang = Language.getInstance();
                        String translationKey = settings.getIdentifier().toTranslationKey();
                        boolean hasTranslation = lang.hasTranslation("island_settings/description." + translationKey);
                        text.append(Text.literal(translationKey));
                        text.append(Text.literal(": "));
                        Text text1 = Text.literal(hasTranslation ? "Translated" : "Untranslated").formatted(hasTranslation ? Formatting.GREEN : Formatting.RED);
                        text.append(text1);
                        text.append(Text.literal("\n"));
                    }

                    source.sendFeedback(() -> text, false);
                    return 0;
                })
              )
            );
        });
    }
}
