package skylands.gui;

import com.awakenedredstone.cbserverconfig.gui.Icons;
import com.awakenedredstone.cbserverconfig.polymer.CBGuiElement;
import com.awakenedredstone.cbserverconfig.polymer.CBGuiElementBuilder;
import com.awakenedredstone.cbserverconfig.polymer.CBSimpleGuiBuilder;
import com.awakenedredstone.cbserverconfig.util.Utils;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import skylands.api.island.IslandSettings;
import skylands.api.island.SettingsManager;
import skylands.logic.Island;
import skylands.util.Texts;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class IslandSettingsGui {
    private final Island island;
    private final GuiInterface parent;
    private final List<Map.Entry<Identifier, IslandSettings>> entries;
    private final Consumer<SlotGuiInterface> updateGui;
    private final Consumer<SlotGuiInterface> simpleUpdateGui;
    private int page = 0;

    private final CBGuiElement filler = new CBGuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.empty()).build();
    //TODO: Update to use translations
    private final CBGuiElement nextPage = CBGuiElementBuilder.from(Icons.INCREASE).setName(Texts.of("Next page")).setCallback((index, type, action, gui) -> offsetPage(1, gui)).build();
    private final CBGuiElement prevPage = CBGuiElementBuilder.from(Icons.DECREASE).setName(Texts.of("Previous page")).setCallback((index, type, action, gui) -> offsetPage(-1, gui)).build();


    public IslandSettingsGui(Island island, @Nullable GuiInterface parent) {
        this.island = island;
        this.parent = parent;
        this.entries = island.getSettings().entrySet().stream().toList();

        updateGui = gui -> {
            Utils.fillGui(gui, filler);

            gui.setTitle(Texts.of("gui.skylands.island_settings"));

            int slot = 10;
            int offset = page * 24;
            for (int i = offset; i < Math.min(offset + 24, island.getSettings().size()); i++) {
                if ((slot + 1) % 9 == 0 && slot > 10) slot += 2;
                Map.Entry<Identifier, IslandSettings> pageEntry = entries.get(i);
                gui.setSlot(slot++, SettingsManager.getIcon(pageEntry.getKey(), island));
            }

            if (page < getPageMax()) gui.setSlot(gui.getSize() - 8, nextPage);
            if (page > 0) gui.setSlot(gui.getSize() - 9, prevPage);

            CBGuiElementBuilder close = new CBGuiElementBuilder(Items.BARRIER)
                    .setName(Texts.of("<red>Close"))
                    .setCallback((index, type, action, gui1) -> {
                        gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
                        if (parent != null) {
                            parent.close();
                            parent.open();
                        } else gui.close();
                    });
            gui.setSlot(gui.getSize() - 1, close);
        };

        simpleUpdateGui = gui -> {
            int slot = 10;
            int offset = page * 24;
            for (int i = offset; i < Math.min(offset + 24, island.getSettings().size()); i++) {
                if ((slot + 1) % 9 == 0 && slot > 10) slot += 2;
                Map.Entry<Identifier, IslandSettings> pageEntry = entries.get(i);
                gui.setSlot(slot++, SettingsManager.getIcon(pageEntry.getKey(), island));
            }
        };
    }

    public SimpleGui buildGui(ServerPlayerEntity player) {
        CBSimpleGuiBuilder builder = new CBSimpleGuiBuilder(ScreenHandlerType.GENERIC_9X6, false);
        Utils.fillGui(builder, filler);

        builder.setOnOpen(updateGui::accept);
        builder.setOnClick(simpleUpdateGui::accept);

        return builder.build(player);
    }

    public void openGui(ServerPlayerEntity player) {
        buildGui(player).open();
    }

    public void offsetPage(int offset, SlotGuiInterface gui) {
        gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.3f, 1);
        this.page = (int) MathHelper.clamp(this.page + offset, 0, Math.floor(island.getSettings().size() / 24f));
        updateGui.accept(gui);
    }

    private int getPageMax() {
        return (int) Math.ceil(island.getSettings().size() / 24f) - 1;
    }
}
