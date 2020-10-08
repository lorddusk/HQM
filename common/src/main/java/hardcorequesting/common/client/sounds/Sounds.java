package hardcorequesting.common.client.sounds;


import com.google.common.collect.Sets;
import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum Sounds {
    COMPLETE("complete"),
    LIFE("heart"),
    BAG("reward"),
    DEATH("ban"),
    ROTTEN("rotten");
    
    private static Map<Sounds, SoundEvent> sounds = new HashMap<>();
    private String sound;
    
    static Set<Tuple<SoundEvent, ResourceLocation>> registeredSounds = Sets.newHashSet();
    
    Sounds(String sound) {
        this.sound = sound;
    }
    
    public static void initSounds() {
        for (Sounds sound : Sounds.values()) {
            ResourceLocation identifier = new ResourceLocation(HardcoreQuestingCore.ID, sound.getSoundName());
            SoundEvent event = registerSound(identifier);
            sounds.put(sound, event);
        }
    }
    
    private static SoundEvent registerSound(ResourceLocation identifier) {
        SoundEvent event = new SoundEvent(identifier);
        registeredSounds.add(new Tuple<>(event, identifier));
        return event;
    }
    
    public String getSoundName() {
        return sound;
    }
    
    public SoundEvent getSound() {
        return sounds.get(this);
    }
    
    public static void registerSounds() {
        for (Tuple<SoundEvent, ResourceLocation> pair : registeredSounds) {
            Registry.register(Registry.SOUND_EVENT, pair.getB(), pair.getA());
        }
    }
}
