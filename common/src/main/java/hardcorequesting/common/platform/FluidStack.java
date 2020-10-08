package hardcorequesting.common.platform;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.util.Fraction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import java.math.RoundingMode;

public interface FluidStack {
    Component getName();
    
    FluidStack copy();
    
    Fluid getFluid();
    
    Fraction getAmount();
    
    boolean isEmpty();
    
    default FluidStack split(Fraction toRemove) {
        if (toRemove.isLessThan(Fraction.empty())) {
            throw new IllegalArgumentException("Cannot split off a negative amount!");
        }
        if (toRemove.equals(Fraction.empty()) || isEmpty()) {
            return HardcoreQuestingCore.platform.createEmptyFluidStack();
        }
        if (toRemove.isGreaterThan(getAmount())) {
            toRemove = getAmount();
        }
        return _split(toRemove);
    }
    
    default FluidStack _split(Fraction toTake) {
        setAmount(getAmount().minus(toTake));
        return HardcoreQuestingCore.platform.createFluidStack(getFluid(), toTake);
    }
    
    void setAmount(Fraction amount);
}
