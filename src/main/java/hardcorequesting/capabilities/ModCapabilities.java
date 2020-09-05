package hardcorequesting.capabilities;

import hardcorequesting.HardcoreQuesting;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ModCapabilities {
    public static final ComponentType<CompoundTagComponent> PLAYER_EXTRA_DATA;
    
    static {
        PLAYER_EXTRA_DATA = ComponentRegistry.INSTANCE
                .registerIfAbsent(new ResourceLocation(HardcoreQuesting.ID, "player_extra_data"), CompoundTagComponent.class)
                .attach(EntityComponentCallback.event(Player.class), player -> new CompoundTagComponent());
        EntityComponents.setRespawnCopyStrategy(PLAYER_EXTRA_DATA, RespawnCopyStrategy.ALWAYS_COPY);
    }
    
    public static void init() {}
    
    public static class CompoundTagComponent implements Component {
        public CompoundTag tag = new CompoundTag();
        
        @Override
        public void fromTag(CompoundTag tag) {
            this.tag = tag.getCompound("Tag");
        }
        
        @NotNull
        @Override
        public CompoundTag toTag(CompoundTag tag) {
            this.tag.put("Tag", this.tag);
            return tag;
        }
    }
}
