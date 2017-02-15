package hardcorequesting.client.sounds;


import hardcorequesting.ModInformation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import java.util.HashMap;
import java.util.Map;

public enum Sounds {
    COMPLETE("complete"),
    LIFE("heart"),
    BAG("reward"),
    DEATH("ban"),
    ROTTEN("rotten");

    private static Map<Sounds, SoundEvent> sounds = new HashMap<>();
    private String sound;

    Sounds(String sound) {
        this.sound = sound;
    }

    public static void initSounds() {
        for (Sounds sound : Sounds.values()) {
            SoundEvent event = registerSound(new ResourceLocation(ModInformation.ID, sound.getSoundName()));
            sounds.put(sound, event);
        }
    }

    private static SoundEvent registerSound(ResourceLocation sound) {
        SoundEvent event = new SoundEvent(sound).setRegistryName(sound);
        SoundEvent.REGISTRY.register(-1, sound, event);
        return event;
    }

    public String getSoundName() {
        return sound;
    }

    public SoundEvent getSound() {
        return sounds.get(this);
    }
}
