package com.awakenedredstone.neoskies.gui.blockgen;

import com.awakenedredstone.neoskies.data.BlockGeneratorLoader;
import com.awakenedredstone.neoskies.util.Texts;
import com.awakenedredstone.neoskies.util.UIUtils;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockGenEditBlocksScreen extends SimpleGui {
    private final Identifier generatorId;
    private final BlockGeneratorLoader.BlockGenerator generator;
    private int page = 0;

    public BlockGenEditBlocksScreen(ServerPlayerEntity player, Identifier generatorId) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.generatorId = generatorId;
        this.generator = BlockGeneratorLoader.INSTANCE.getGenerators().get(generatorId);

        setTitle(Texts.translatable("gui.neoskies.block_gen.edit"));
        UIUtils.fillGui(this);

        List<BlockGeneratorLoader.BlockGenerator.GenSet> generators = generator.generates();

        int offset = page * 28;
        AtomicInteger slot = new AtomicInteger(10);

        for (int i = offset; i < Math.min(offset + 28, generators.size()); i++) {
            if ((slot.get() + 1) % 9 == 0 && slot.get() > 10) slot.addAndGet(2);
            BlockGeneratorLoader.BlockGenerator.GenSet set = generators.get(i);
        }
    }
}
