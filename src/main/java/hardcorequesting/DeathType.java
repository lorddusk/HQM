package hardcorequesting;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;


public enum  DeathType {
    LAVA("Lava") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getDamageType().equals("lava");
        }
    },
    FIRE("Fire") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.isFireDamage();
        }
    },
    SUFFOCATION("Suffocation") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getDamageType().equals("inWall");
        }
    },
    THORNS("Thorns") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getDamageType().equals("thorns") || source.getDamageType().equals("cactus");
        }
    },
    DROWNING("Drowning") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getDamageType().equals("drown");
        }
    },
    STARVATION("Starvation") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getDamageType().equals("starve");
        }
    },
    FALL("Fall") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getDamageType().equals("fall");
        }
    },
    VOID("Void") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getDamageType().equals("outOfWorld");
        }
    },
    CRUSHED("Crushed") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getDamageType().equals("anvil") || source.getDamageType().equals("fallingBlock");
        }
    },
    EXPLOSION("Explosions") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.isExplosion();
        }
    },
    MONSTER("Monsters") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getDamageType().equals("mob") || source.getEntity() instanceof EntityLiving;
        }
    },
    PLAYER("Other players") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.getDamageType().equals("player") || source.getEntity() instanceof EntityPlayer;
        }
    },
    MAGIC("Magic") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return source.isMagicDamage();
        }
    },
    HQM("Rotten Hearts") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return false; //handled elsewhere
        }
    },
    OTHER("Other / Unknown") {
        @Override
        boolean isSourceValid(DamageSource source) {
            return true; //fallback
        }
    };

    private String name;

    DeathType(String name) {
        this.name = name;
    }

    public void onDeath(EntityPlayer player) {
        QuestingData.getQuestingData(player).getDeathStat().increaseDeath(ordinal());
    }

    public static void onDeath(EntityPlayer player, DamageSource source) {
        if (source != null && source.getDamageType() != null) {
            for (DeathType deathType : values()) {
                if (deathType.isSourceValid(source)) {
                    deathType.onDeath(player);
                    break;
                }
            }
        }else{
            OTHER.onDeath(player);
        }

    }

    //is only accurate if called in the values() order
    abstract boolean isSourceValid(DamageSource source);

    public String getName() {
        return name;
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
