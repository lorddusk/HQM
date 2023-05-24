package hardcorequesting.common.client.sounds;


import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public enum Sounds {
    COMPLETE("complete"),
    LIFE("heart"),
    BAG("reward"),
    DEATH("ban"),
    ROTTEN("rotten");
    
    private final ResourceLocation soundId;
    private Supplier<SoundEvent> supplier;
    
    Sounds(String soundName) {
        this.soundId = new ResourceLocation(HardcoreQuestingCore.ID, soundName);
    }
    
    public ResourceLocation getSoundId() {
        return soundId;
    }
    
    public SoundEvent getSound() {
        return supplier.get();
    }
    
    public static void registerSounds() {
        for (Sounds sound : Sounds.values()) {
            sound.supplier = HardcoreQuestingCore.platform.registerSound(sound.soundId.getPath(), () -> SoundEvent.createVariableRangeEvent(sound.soundId));
        }
    }
}
