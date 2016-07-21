package hardcorequesting.client.sounds;


import hardcorequesting.ModInformation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.SoundEvent;

import java.util.HashMap;
import java.util.Map;

public enum Sounds {
    COMPLETE("complete"),
    LIFE("heart"),
    BAG("reward"),
    DEATH("ban"),
    ROTTEN("rotten");

    private String sound;

    private static Map<Sounds, SoundEvent> sounds = new HashMap<>();

    Sounds(String sound) {
        this.sound = sound;
    }

    public String getSoundName() {
        return sound;
    }

    public SoundEvent getSound() {
        return sounds.get(this);
    }
}
