package hardcorequesting.common.death;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


public enum DeathType {
    LAVA("lava") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.msgId.equals("lava");
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
            return source.msgId.equals("inWall");
        }
    },
    THORNS("thorns") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.msgId.equals("thorns") || source.msgId.equals("cactus");
        }
    },
    DROWNING("drowning") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.msgId.equals("drown");
        }
    },
    STARVATION("starvation") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.msgId.equals("starve");
        }
    },
    FALL("fall") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.msgId.equals("fall");
        }
    },
    VOID("void") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.msgId.equals("outOfWorld");
        }
    },
    CRUSHED("crushed") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.msgId.equals("anvil") || source.msgId.equals("fallingBlock");
        }
    },
    EXPLOSION("explosions") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.isExplosion();
        }
    },
    MONSTER("monsters") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.msgId.equals("mob") || source.getEntity() instanceof LivingEntity;
        }
    },
    PLAYER("otherPlayers") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.msgId.equals("player") || source.getEntity() instanceof Player;
        }
    },
    MAGIC("magic") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.isMagic();
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
    
    public static void onDeath(Player player, DamageSource source) {
        if (source != null && source.msgId != null) {
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
    
    public void onDeath(Player player) {
        DeathStatsManager.getInstance().getDeathStat(player).increaseDeath(ordinal());
    }
    
    //is only accurate if called in the values() order
    abstract boolean isSourceValid(DamageSource source);
    
    public String getName() {
        return I18n.get("hqm.deathType." + name);
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
