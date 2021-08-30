package hardcorequesting.fabric.capabilities;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

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
        public void readFromNbt(CompoundTag tag) {
            this.tag = tag.getCompound("Tag");
        }
        
        @Override
        public void writeToNbt(CompoundTag tag) {
            this.tag.put("Tag", this.tag);
        }
    }
}
