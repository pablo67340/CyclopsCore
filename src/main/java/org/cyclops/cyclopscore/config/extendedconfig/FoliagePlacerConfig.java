package org.cyclops.cyclopscore.config.extendedconfig;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.foliageplacer.FoliagePlacer;
import net.minecraft.world.gen.foliageplacer.FoliagePlacerType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.cyclops.cyclopscore.config.ConfigurableType;
import org.cyclops.cyclopscore.init.ModBase;

import java.util.function.Function;

/**
 * Config for foliage placer types.
 * @author rubensworks
 * @see ExtendedConfig
 */
public class FoliagePlacerConfig<T extends FoliagePlacer> extends ExtendedConfigForge<FoliagePlacerConfig<T>, FoliagePlacerType<T>> {

    public FoliagePlacerConfig(ModBase mod, String namedId, Function<FoliagePlacerConfig<T>, Codec<T>> codec) {
        super(mod, namedId, (eConfig) -> {
            FoliagePlacerType<T> type = new FoliagePlacerType<>(codec.apply(eConfig));
            type.setRegistryName(mod.getModId(), namedId);
            return type;
        });
    }

    @Override
    public String getTranslationKey() {
        return "foliageplacer." + getMod().getModId() + "." + getNamedId();
	}

    // Needed for config gui
    @Override
    public String getFullTranslationKey() {
        return getTranslationKey();
    }
    
    @Override
	public ConfigurableType getConfigurableType() {
		return ConfigurableType.FOLIAGE_PLACER;
	}

    @Override
    public IForgeRegistry<? super FoliagePlacerType<T>> getRegistry() {
        return ForgeRegistries.FOLIAGE_PLACER_TYPES;
    }
}
