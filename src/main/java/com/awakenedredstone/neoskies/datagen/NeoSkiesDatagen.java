package com.awakenedredstone.neoskies.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class NeoSkiesDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(NeoSkiesItemTagProvider::new);
        pack.addProvider(NeoSkiesBlockTagProvider::new);
        pack.addProvider(NeoSkiesEntityTagProvider::new);
        pack.addProvider(NeoSkiesBlockGenProvider::new);
    }
}
