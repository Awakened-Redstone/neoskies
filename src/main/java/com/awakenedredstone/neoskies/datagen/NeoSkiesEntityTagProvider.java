package com.awakenedredstone.neoskies.datagen;

import com.awakenedredstone.neoskies.logic.tags.NeoSkiesEntityTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class NeoSkiesEntityTagProvider extends FabricTagProvider.EntityTypeTagProvider {
    public NeoSkiesEntityTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(NeoSkiesEntityTags.ARMOR_STAND)
          .add(EntityType.ARMOR_STAND);

        getOrCreateTagBuilder(NeoSkiesEntityTags.LEASH_KNOT)
          .add(EntityType.LEASH_KNOT);
    }
}
