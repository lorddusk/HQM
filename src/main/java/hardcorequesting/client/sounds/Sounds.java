package hardcorequesting.client.sounds;


import com.google.common.collect.Sets;
import hardcorequesting.HardcoreQuesting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;

import java.util.HashMap;
import java.util.Iterator;
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
    
    static Set<SoundEvent> registeredSounds = Sets.newHashSet();

    Sounds(String sound) {
        this.sound = sound;
    }

    public static void initSounds() {
        for (Sounds sound : Sounds.values()) {
            SoundEvent event = registerSound(new ResourceLocation(HardcoreQuesting.ID, sound.getSoundName()));
            sounds.put(sound, event);
        }
    }

    private static SoundEvent registerSound(ResourceLocation sound) {
        SoundEvent event = new SoundEvent(sound).setRegistryName(sound);
        registeredSounds.add(event);
        return event;
    }

    public String getSoundName() {
        return sound;
    }

    public SoundEvent getSound() {
        return sounds.get(this);
    }
    
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        Iterator<SoundEvent> s = registeredSounds.iterator();

        while (s.hasNext()) {
            event.getRegistry().register(s.next());
        }
    }
}
