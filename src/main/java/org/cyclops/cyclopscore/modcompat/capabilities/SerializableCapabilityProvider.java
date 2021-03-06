package org.cyclops.cyclopscore.modcompat.capabilities;

import net.minecraft.nbt.INBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A serializable implementation of the capability provider.
 * @author rubensworks
 */
public class SerializableCapabilityProvider<T> extends DefaultCapabilityProvider<T> implements INBTSerializable<INBT> {

    public SerializableCapabilityProvider(ICapabilityTypeGetter<T> capabilityGetter, T capability) {
        super(capabilityGetter, capability);
    }

    @Deprecated
    public SerializableCapabilityProvider(Capability<T> capabilityType, T capability) {
        super(capabilityType, capability);
    }

    @Override
    public INBT serializeNBT() {
        return this.getCapabilityType().writeNBT(capability.orElse(null), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        this.getCapabilityType().readNBT(capability.orElse(null), null, nbt);
    }
}
