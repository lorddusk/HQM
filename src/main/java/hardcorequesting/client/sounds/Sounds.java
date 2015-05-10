package hardcorequesting.client.sounds;


import hardcorequesting.ModInformation;

public enum Sounds {
    COMPLETE("complete"),
    LIFE("heart"),
    BAG("reward"),
    DEATH("ban"),
    ROTTEN("rotten");

    private String sound;

    Sounds(String sound) {
        this.sound = sound;
    }

    public String getSound() {
        return sound;
    }
}
