package skylands.command.island;

import com.awakenedredstone.cbserverconfig.polymer.CBGuiElementBuilder;
import com.awakenedredstone.cbserverconfig.polymer.CBSimpleGuiBuilder;
import com.awakenedredstone.cbserverconfig.ui.ConfigScreen;
import com.awakenedredstone.cbserverconfig.util.Utils;
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
import skylands.SkylandsMain;
import skylands.api.SkylandsAPI;
import skylands.gui.IslandSettingsGui;
import skylands.logic.Island;
import skylands.logic.Skylands;
import skylands.util.Texts;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.literal;
import static skylands.command.utils.CommandUtils.node;
import static skylands.command.utils.CommandUtils.register;

public class MenuCommand {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        register(dispatcher, node().then(literal("menu").executes(context -> MenuCommand.execute(context.getSource()))));

        for (String alias : SkylandsMain.MAIN_CONFIG.commandAliases()) {
            dispatcher.register(CommandManager.literal(alias).executes(context -> MenuCommand.execute(context.getSource())));
        }
    }

    private static int execute(ServerCommandSource source) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Texts.prefixed("message.skylands.error.player_only"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        assert player != null;

        int permissionLevel = player.getPermissionLevel();

        CBSimpleGuiBuilder guiBuilder = new CBSimpleGuiBuilder(ScreenHandlerType.GENERIC_9X3, false);
        guiBuilder.setTitle(Texts.of("gui.skylands.menu"));

        var ref = new Object() {
            boolean dirty = false;
        };
        final Consumer<SlotHolder> consumer = slotHolder -> {
            Optional<Island> islandOptional = SkylandsAPI.getIslandByPlayer(player);
            Utils.fillGui(slotHolder, new CBGuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.empty()).build());
            if (Permissions.check(player, "skylands.teleport.hub", true)) {
                slotHolder.setSlot(10, new CBGuiElementBuilder(Items.BEACON).setName(Texts.of("item_name.skylands.hub"))
                    .setCallback((index, type, action, gui) -> {
                        //gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.3f, 1);
                        Skylands.getInstance().hub.visit(player);
                        gui.close();
                    })
                    .build());
            }
            if (islandOptional.isPresent()) {
                if (Permissions.check(player, "skylands.teleport.home", true)) {
                    slotHolder.setSlot(11, new CBGuiElementBuilder(Items.GRASS_BLOCK).setName(Texts.of("item_name.skylands.home"))
                        .setCallback((index, type, action, gui) -> {
                            //gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.3f, 1);
                            HomeCommand.run(player);
                            gui.close();
                        })
                        .build());
                }
                if (Permissions.check(player, "skylands.island.settings", true)) {
                    slotHolder.setSlot(12, new CBGuiElementBuilder(Items.REDSTONE).setName(Texts.of("item_name.skylands.island_settings"))
                        .setCallback((index, type, action, gui) -> {
                            gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                            new IslandSettingsGui(islandOptional.get(), gui).openGui(player);
                        })
                        .build());
                }
            } else if (Permissions.check(player, "skylands.island.create", true)) {
                slotHolder.setSlot(11, new CBGuiElementBuilder(Items.OAK_SAPLING).setName(Texts.of("item_name.skylands.create"))
                    .setCallback((index, type, action, gui) -> {
                        gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                        CreateCommand.run(player);
                    })
                    .build());
            }
            if (Permissions.check(player, "skylands.admin.protection.bypass", 4)) {
                Set<PlayerEntity> protectionBypass = SkylandsMain.PROTECTION_BYPASS;
                boolean overrideMode = protectionBypass.contains(player);
                Item item = overrideMode ? Items.OAK_CHEST_BOAT : Items.OAK_BOAT;
                slotHolder.setSlot(slotHolder.getSize() - 2, new CBGuiElementBuilder(item).setName(Texts.of("item_name.skylands.protection_bypass"))
                    .addLoreLine(Texts.of("text.skylands.protection_bypass", map -> map.put("value", String.valueOf(overrideMode))))
                    .setCallback((index, type, action, gui) -> {
                        gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                        if (overrideMode) protectionBypass.remove(player);
                        else protectionBypass.add(player);
                        ref.dirty = true;
                    }).build());
            }
            if (Permissions.check(player, "skylands.admin.settings", 4)) {
                slotHolder.setSlot(slotHolder.getSize() - 1, new CBGuiElementBuilder(Items.COMMAND_BLOCK_MINECART).setName(Texts.of("item_name.skylands.mod_settings"))
                    .setCallback((index, type, action, gui) -> {
                        gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                        new ConfigScreen(source.getPlayer(), SkylandsMain.MAIN_CONFIG, null, null);
                    })
                    .build());
            }
        };

        consumer.accept(guiBuilder);
        guiBuilder.setOnTick(gui -> {
            if (gui.getPlayer().getPermissionLevel() != permissionLevel || ref.dirty) {
                ref.dirty = false;
                consumer.accept(gui);
                if (!Permissions.check(player, "skylands.admin.protection.bypass", 4)) {
                    SkylandsMain.PROTECTION_BYPASS.remove(player);
                }
            }
        });

        guiBuilder.build(player).open();
        player.playSound(SoundEvents.ENTITY_HORSE_SADDLE, SoundCategory.MASTER, 0.4f, 1.2f);
        return 1;
    }
}
