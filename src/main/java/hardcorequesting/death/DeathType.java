package hardcorequesting.death;

import hardcorequesting.quests.QuestingData;
import hardcorequesting.util.Translator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;


public enum DeathType {
    LAVA("lava") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.name.equals("lava");
        }
    },
    FIRE("fire") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.isFire();
        }
    },
    SUFFOCATION("suffocation") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.name.equals("inWall");
        }
    },
    THORNS("thorns") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.name.equals("thorns") || source.name.equals("cactus");
        }
    },
    DROWNING("drowning") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.name.equals("drown");
        }
    },
    STARVATION("starvation") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.name.equals("starve");
        }
    },
    FALL("fall") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.name.equals("fall");
        }
    },
    VOID("void") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.name.equals("outOfWorld");
        }
    },
    CRUSHED("crushed") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.name.equals("anvil") || source.name.equals("fallingBlock");
        }
    },
    EXPLOSION("explosions") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.isExplosive();
        }
    },
    MONSTER("monsters") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.name.equals("mob") || source.getAttacker() instanceof LivingEntity;
        }
    },
    PLAYER("otherPlayers") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.name.equals("player") || source.getAttacker() instanceof PlayerEntity;
        }
    },
    MAGIC("magic") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getMagic();
        }
    },
    HQM("rottenHearts") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return false; //handled elsewhere
        }
    },
    OTHER("other") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return true; //fallback
        }
    };
    
    private String name;
    
    DeathType(String name) {
        this.name = name;
    }
    
    public static void onDeath(PlayerEntity player, DamageSource source) {
        if (source != null && source.name != null) {
            for (DeathType deathType : values()) {
                if (deathType.isSourceValid(source)) {
                    deathType.onDeath(player);
                    break;
                }
            }
        } else {
            OTHER.onDeath(player);
        }
        
    }
    
    public void onDeath(PlayerEntity player) {
        QuestingData.getQuestingData(player).getDeathStat().increaseDeath(ordinal());
    }
    
    //is only accurate if called in the values() order
    abstract boolean isSourceValid(DamageSource source);
    
    public String getName() {
        return Translator.translate("hqm.deathType." + name);
    }

    /*Fire: inFire, onFire, fireball
    Lava: lava
    Suffocation: inWall
    Thorns: cactus, thorns
    Drown: drown
    Starvation: starve
    Fall: fall
    Void: outOfWorld
    Crushed: anvil, fallingBlock
    Explosion: explosion, explosion.player
    Monster: mob, arrow(if shot by mob), thrown(if shot by mob)
    Player: player, arrow(if shot by player), thrown(if shot by player)
    Magic: magic, wither, indirectMagic
    Rotten Hearts: consuming rotten heart
    Other: generic, arrow(if shot by no one), thrown(if shot by no one), invalid sources, mod sources*/
}
