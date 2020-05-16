package hardcorequesting.client.sounds;


import com.google.common.collect.Sets;
import hardcorequesting.HardcoreQuesting;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

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
    
    static Set<Pair<SoundEvent, Identifier>> registeredSounds = Sets.newHashSet();
    
    Sounds(String sound) {
        this.sound = sound;
    }
    
    public static void initSounds() {
        for (Sounds sound : Sounds.values()) {
            Identifier identifier = new Identifier(HardcoreQuesting.ID, sound.getSoundName());
            SoundEvent event = registerSound(identifier);
            sounds.put(sound, event);
        }
    }
    
    private static SoundEvent registerSound(Identifier identifier) {
        SoundEvent event = new SoundEvent(identifier);
        registeredSounds.add(new Pair<>(event, identifier));
        return event;
    }
    
    public String getSoundName() {
        return sound;
    }
    
    public SoundEvent getSound() {
        return sounds.get(this);
    }
    
    public static void registerSounds() {
        for (Pair<SoundEvent, Identifier> pair : registeredSounds) {
            Registry.register(Registry.SOUND_EVENT, pair.getRight(), pair.getLeft());
        }
    }
}
