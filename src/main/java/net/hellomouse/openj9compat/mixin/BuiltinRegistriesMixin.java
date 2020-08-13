package net.hellomouse.openj9compat.mixin;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(BuiltinRegistries.class)
public abstract class BuiltinRegistriesMixin {
    @Shadow @Final protected static Logger LOGGER;
    @Shadow @Final private static MutableRegistry<MutableRegistry<?>> ROOT;
    @Shadow @Mutable @Final private static Map<Identifier, Supplier<?>> DEFAULT_VALUE_SUPPLIERS;
    @Shadow @Mutable @Final public static Registry<StructurePool> STRUCTURE_POOL;
    @Shadow @Mutable @Final public static Registry<Biome> BIOME;
    @Shadow @Mutable @Final public static Registry<ChunkGeneratorSettings> CHUNK_GENERATOR_SETTINGS;
    @Shadow private static <T> Registry<T> addRegistry(RegistryKey<? extends Registry<T>> registryRef, Supplier<T> defaultValueSupplier) {
        return null;
    }

    // try to touch as little as possible
    @Inject(method = "<clinit>",
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/util/registry/Registry;TEMPLATE_POOL_WORLDGEN:Lnet/minecraft/util/registry/RegistryKey;"),
            cancellable = true)
    private static void staticInit(CallbackInfo ci) {
        LOGGER.info("Cancelling BuiltinRegistries class initializer");
        ci.cancel();

        // the rest of the stuff needed
        STRUCTURE_POOL = addRegistry(Registry.TEMPLATE_POOL_WORLDGEN, () -> null);
        BIOME = addRegistry(Registry.BIOME_KEY, () -> Biomes.PLAINS);
        CHUNK_GENERATOR_SETTINGS = addRegistry(Registry.NOISE_SETTINGS_WORLDGEN, () -> null);
        DEFAULT_VALUE_SUPPLIERS.forEach((identifier, supplier) -> {
            if (supplier.get() == null) {
                if (identifier != Registry.TEMPLATE_POOL_WORLDGEN.getValue() && identifier != Registry.NOISE_SETTINGS_WORLDGEN.getValue()) {
                    LOGGER.error("Unable to bootstrap registry '{}'", identifier);
                }
            }
        });
        StructurePools.initDefaultPools();
        ChunkGeneratorSettings.getInstance();
        Registry.validate(ROOT);
    }
}
