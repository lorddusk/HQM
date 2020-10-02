package hardcorequesting.fabric;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.util.Fraction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

public class FabricFluidStack implements FluidStack {
    public FluidVolume _volume;
    
    public FabricFluidStack(FluidVolume volume) {
        this._volume = volume;
    }
    
    @Override
    public Component getName() {
        return _volume.getName();
    }
    
    @Override
    public FluidStack copy() {
        return new FabricFluidStack(_volume.copy());
    }
    
    @Override
    public Fluid getFluid() {
        return _volume.getRawFluid();
    }
    
    @Override
    public Fraction getAmount() {
        FluidAmount amount = _volume.getAmount_F();
        return Fraction.of(amount.whole, amount.numerator, amount.denominator);
    }
    
    @Override
    public boolean isEmpty() {
        return _volume.isEmpty();
    }
    
    @Override
    public void setAmount(Fraction amount) {
        _volume = _volume.fluidKey.withAmount(FluidAmount.of(amount.getNumerator(), amount.getDenominator()));
    }
}
