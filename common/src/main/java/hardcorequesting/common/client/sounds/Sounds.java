package hardcorequesting.common.client.sounds;


import com.google.common.collect.Sets;
import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public enum Sounds {
    COMPLETE("complete"),
    LIFE("heart"),
    BAG("reward"),
    DEATH("ban"),
    ROTTEN("rotten");
    
    private static final Map<Sounds, Supplier<SoundEvent>> sounds = new HashMap<>();
    private final String sound;
    
    static Set<Tuple<Supplier<SoundEvent>, ResourceLocation>> registeredSounds = Sets.newHashSet();
    
    Sounds(String sound) {
        this.sound = sound;
    }
    
    public static void initSounds() {
        for (Sounds sound : Sounds.values()) {
            ResourceLocation identifier = new ResourceLocation(HardcoreQuestingCore.ID, sound.getSoundName());
            Supplier<SoundEvent> event = registerSound(identifier);
            sounds.put(sound, event);
        }
    }
    
    private static Supplier<SoundEvent> registerSound(ResourceLocation identifier) {
        registeredSounds.add(new Tuple<>(() -> new SoundEvent(identifier), identifier));
        return () -> HardcoreQuestingCore.platform.getSoundEvent(identifier);
    }
    
    public String getSoundName() {
        return sound;
    }
    
    public Supplier<SoundEvent> getSound() {
        return sounds.get(this);
    }
    
    public static void registerSounds() {
        for (Tuple<Supplier<SoundEvent>, ResourceLocation> pair : registeredSounds) {
            HardcoreQuestingCore.platform.registerSound(pair.getB(), pair.getA());
        }
    }
}
