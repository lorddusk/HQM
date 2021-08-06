package hardcorequesting.forge;

import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.util.Fraction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class ForgeFluidStack implements FluidStack {
    public net.minecraftforge.fluids.FluidStack _stack;
    
    public ForgeFluidStack() {
        this(new net.minecraftforge.fluids.FluidStack(Fluids.EMPTY, 0));
    }
    
    public ForgeFluidStack(net.minecraftforge.fluids.FluidStack _stack) {
        this._stack = _stack;
    }
    
    @Override
    public Component getName() {
        return _stack.getDisplayName();
    }
    
    @Override
    public FluidStack copy() {
        return new ForgeFluidStack(_stack.copy());
    }
    
    @Override
    public Fluid getFluid() {
        return _stack.getFluid();
    }
    
    @Override
    public Fraction getAmount() {
        return Fraction.ofWhole(_stack.getAmount());
    }
    
    @Override
    public boolean isEmpty() {
        return _stack.isEmpty();
    }
    
    @Override
    public void setAmount(Fraction fraction) {
        _stack.setAmount(fraction.intValue());
    }
}
