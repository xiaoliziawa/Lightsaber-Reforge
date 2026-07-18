package com.fiskmods.lightsabers.common.container;

import com.fiskmods.lightsabers.common.item.ILightsaberComponent;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class InventoryLightsaberForge extends SimpleContainer {
    private static final int INVENTORY_SIZE = 8;
    private static final int COLOR_CRYSTAL_SLOT = 5;
    private static final int FIRST_OPTIONAL_SLOT = 6;

    private LightsaberData result;

    public InventoryLightsaberForge() {
        super(INVENTORY_SIZE);
    }

    public LightsaberData updateResult() {
        long fingerprintHash = 0;

        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = getItem(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ILightsaberComponent component) {
                long fingerprint = component.getFingerprint(stack, slot);
                if (!component.isCompatibleSlot(stack, slot)
                        || fingerprint != 0 && fingerprintHash == (fingerprintHash |= fingerprint)) {
                    result = null;
                    return null;
                }
                continue;
            }
            if (stack.isEmpty() && slot >= FIRST_OPTIONAL_SLOT) {
                continue;
            }
            if (slot != COLOR_CRYSTAL_SLOT || stack.isEmpty() || !stack.is(ItemTags.FISHES)) {
                result = null;
                return null;
            }
        }

        LightsaberData candidate = new LightsaberData(fingerprintHash);
        if (!candidate.isAssemblyCompatible()) {
            result = null;
            return null;
        }

        result = candidate;
        return result;
    }

    public LightsaberData getResult() {
        return result;
    }
}
