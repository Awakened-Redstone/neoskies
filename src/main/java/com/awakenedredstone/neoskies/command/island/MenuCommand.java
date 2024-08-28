package com.awakenedredstone.neoskies.command.island;

import com.awakenedredstone.neoskies.NeoSkies;
import com.awakenedredstone.neoskies.api.NeoSkiesAPI;
import com.awakenedredstone.neoskies.gui.IslandSettingsGui;
import com.awakenedredstone.neoskies.gui.polymer.CBGuiElementBuilder;
import com.awakenedredstone.neoskies.gui.polymer.CBSimpleGuiBuilder;
import com.awakenedredstone.neoskies.logic.Island;
import com.awakenedredstone.neoskies.logic.IslandLogic;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UIUtils;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.sgui.api.SlotHolder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static com.awakenedredstone.neoskies.command.utils.CommandUtils.node;
import static com.awakenedredstone.neoskies.command.utils.CommandUtils.register;
import static net.minecraft.server.command.CommandManager.literal;

public class MenuCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node().then(literal("menu")
                .requires(Permissions.require("neoskies.command.menu", true))
                .executes(context -> MenuCommand.execute(context.getSource()))
            )
        );

        for (String alias : IslandLogic.getConfig().commands.commandAliases) {
            dispatcher.register(CommandManager.literal(alias).executes(context -> MenuCommand.execute(context.getSource())));
        }
    }

    private static int execute(ServerCommandSource source) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Texts.prefixed("message.neoskies.error.player_only"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();

        int permissionLevel = player.getPermissionLevel();

        CBSimpleGuiBuilder guiBuilder = new CBSimpleGuiBuilder(ScreenHandlerType.GENERIC_9X3, false);
        guiBuilder.setTitle(Texts.translatable("gui.neoskies.menu"));

        var ref = new Object() {
            boolean dirty = false;
        };
        final Consumer<SlotHolder> consumer = slotHolder -> {
            Optional<Island> islandOptional = NeoSkiesAPI.getIslandByPlayer(player);
            UIUtils.fillGui(slotHolder, new CBGuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.empty()).hideTooltip().build());
            if (Permissions.check(player, "neoskies.teleport.hub", true)) {
                slotHolder.setSlot(10, new CBGuiElementBuilder(Items.BEACON).setName(Texts.translatable("item_name.neoskies.hub"))
                    .setCallback((index, type, action, gui) -> {
                        //gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.3f, 1);
                        gui.close();
                        IslandLogic.getInstance().hub.visit(player);
                    })
                    .build());
            }
            if (islandOptional.isPresent()) {
                if (Permissions.check(player, "neoskies.teleport.home", true)) {
                    slotHolder.setSlot(11, new CBGuiElementBuilder(Items.GRASS_BLOCK).setName(Texts.translatable("item_name.neoskies.home"))
                        .setCallback((index, type, action, gui) -> {
                            //gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.3f, 1);
                            gui.close();
                            HomeCommand.run(player);
                        })
                        .build());
                }
                if (Permissions.check(player, "neoskies.island.settings", true)) {
                    slotHolder.setSlot(12, new CBGuiElementBuilder(Items.REDSTONE).setName(Texts.translatable("item_name.neoskies.island_settings"))
                        .setCallback((index, type, action, gui) -> {
                            gui.getPlayer().playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                            new IslandSettingsGui(islandOptional.get(), gui).openGui(player);
                        })
                        .build());
                }
            } else if (Permissions.check(player, "neoskies.island.create", true)) {
                slotHolder.setSlot(11, new CBGuiElementBuilder(Items.OAK_SAPLING).setName(Texts.translatable("item_name.neoskies.create"))
                    .setCallback((index, type, action, gui) -> {
                        gui.getPlayer().playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                        CreateCommand.run(player);
                        ref.dirty = true;
                    })
                    .build());
            }
            if (Permissions.check(player, "neoskies.admin.protection.bypass", 4)) {
                Set<PlayerEntity> protectionBypass = NeoSkies.PROTECTION_BYPASS;
                boolean overrideMode = protectionBypass.contains(player);
                Item item = overrideMode ? Items.OAK_CHEST_BOAT : Items.OAK_BOAT;
                slotHolder.setSlot(slotHolder.getSize() - 2, new CBGuiElementBuilder(item).setName(Texts.translatable("item_name.neoskies.protection_bypass"))
                    .addLoreLine(Texts.loreBase(Texts.translatable("text.neoskies.protection_bypass", map -> map.put("value", String.valueOf(overrideMode)))))
                    .setCallback((index, type, action, gui) -> {
                        gui.getPlayer().playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                        if (overrideMode) {
                            protectionBypass.remove(player);
                        } else {
                            protectionBypass.add(player);
                        }
                        ref.dirty = true;
                    }).build());
            }
            /*if (Permissions.check(player, "neoskies.admin.settings", 4)) {
                slotHolder.setSlot(slotHolder.getSize() - 1, new CBGuiElementBuilder(Items.COMMAND_BLOCK_MINECART).setName(Texts.of("item_name.neoskies.mod_settings"))
                    .setCallback((index, type, action, gui) -> {
                        gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                        new ConfigScreen(source.getPlayer(), NeoSkies.MAIN_CONFIG, null, null);
                    })
                    .build());
            }*/
        };

        consumer.accept(guiBuilder);
        guiBuilder.setOnTick(gui -> {
            if (gui.getPlayer().getPermissionLevel() != permissionLevel || ref.dirty) {
                ref.dirty = false;
                consumer.accept(gui);
                if (!Permissions.check(player, "neoskies.admin.protection.bypass", 4)) {
                    NeoSkies.PROTECTION_BYPASS.remove(player);
                }
            }
        });

        guiBuilder.build(player).open();
        player.playSoundToPlayer(SoundEvents.ENTITY_HORSE_SADDLE, SoundCategory.MASTER, 0.4f, 1.2f);
        return 1;
    }
}
