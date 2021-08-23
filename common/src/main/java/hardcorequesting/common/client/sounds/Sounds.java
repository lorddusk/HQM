package hardcorequesting.common.client.sounds;


import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public enum Sounds {
    COMPLETE("complete"),
    LIFE("heart"),
    BAG("reward"),
    DEATH("ban"),
    ROTTEN("rotten");
    
    private final ResourceLocation soundId;
    
    Sounds(String soundName) {
        this.soundId = new ResourceLocation(HardcoreQuestingCore.ID, soundName);
    }
    
    public ResourceLocation getSoundId() {
        return soundId;
    }
    
    public SoundEvent getSound() {
        return HardcoreQuestingCore.platform.getSoundEvent(soundId);
    }
    
    public static void registerSounds() {
        for (Sounds sound : Sounds.values()) {
            HardcoreQuestingCore.platform.registerSound(sound.soundId, () -> new SoundEvent(sound.soundId));
        }
    }
}
