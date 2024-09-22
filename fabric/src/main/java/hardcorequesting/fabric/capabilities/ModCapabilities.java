package hardcorequesting.fabric.capabilities;

import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public final class ModCapabilities implements EntityComponentInitializer {
    public static final ComponentKey<CompoundTagComponent> PLAYER_EXTRA_DATA =
            ComponentRegistry.getOrCreate(new ResourceLocation(HardcoreQuestingCore.ID, "player_extra_data"), CompoundTagComponent.class);
    
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER_EXTRA_DATA, player -> new CompoundTagComponent(), RespawnCopyStrategy.ALWAYS_COPY);
    }
    
    public static class CompoundTagComponent implements Component {
        public CompoundTag tag = new CompoundTag();
        
        @Override
        public void readFromNbt(CompoundTag tag, HolderLookup.Provider provider) {
            this.tag = tag.getCompound("Tag");
        }
        
        @Override
        public void writeToNbt(CompoundTag tag, HolderLookup.Provider provider) {
            this.tag.put("Tag", this.tag);
        }
    }
}
