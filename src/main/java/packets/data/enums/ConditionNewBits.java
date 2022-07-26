package packets.data.enums;

import java.util.ArrayList;
import java.util.Arrays;

public enum ConditionNewBits {
    WEAK(0x8),
    CURSED(0x40),
    EXPOSED(0x20000);

    private final int bitMask;

    ConditionNewBits(int i) {
        bitMask = i;
    }

    public static ConditionNewBits[] getEffects(int effects) {
        ArrayList<ConditionNewBits> list = new ArrayList<>();
        for (ConditionNewBits e : ConditionNewBits.values()) {
            if ((effects & e.bitMask) != 0) {
                list.add(e);
            }
        }

        return list.toArray(new ConditionNewBits[0]);
    }

    public static String effectsToString(int effects) {
        return Arrays.toString(getEffects(effects));
    }

    public boolean effect(int mask) {
        return (bitMask & mask) != 0;
    }

    public int value() {
        return bitMask;
    }
}

//    /**
//     * The ID values of all condition effects in the game
//     */
//    export declare enum ConditionEffect {
//    NOTHING = 0,
//    DEAD = 1,
//    QUIET = 2,
//    WEAK = 3,
//    SLOWED = 4,
//    SICK = 5,
//    DAZED = 6,
//    STUNNED = 7,
//    BLIND = 8,
//    HALLUCINATING = 9,
//    DRUNK = 10,
//    CONFUSED = 11,
//    STUN_IMMUNE = 12,
//    INVISIBLE = 13,
//    PARALYZED = 14,
//    SPEEDY = 15,
//    BLEEDING = 16,
//    ARMOR_BROKEN_IMMUNE = 17,
//    HEALING = 18,
//    DAMAGING = 19,
//    BERSERK = 20,
//    PAUSED = 21,
//    STASIS = 22,
//    STASIS_IMMUNE = 23,
//    INVINCIBLE = 24,
//    INVULNERABLE = 25,
//    ARMORED = 26,
//    ARMORBROKEN = 27,
//    HEXED = 28,
//    NINJA_SPEEDY = 29,
//    UNSTABLE = 30,
//    DARKNESS = 31,
//    SLOWED_IMMUNE = 32,
//    DAZED_IMMUNE = 33,
//    PARALYZED_IMMUNE = 34,
//    PETRIFIED = 35,
//    PETRIFIED_IMMUNE = 36,
//    PET_EFFECT_ICON = 37,
//    CURSE = 38,
//    CURSE_IMMUNE = 39,
//    HP_BOOST = 40,
//    MP_BOOST = 41,
//    ATT_BOOST = 42,
//    DEF_BOOST = 43,
//    SPD_BOOST = 44,
//    VIT_BOOST = 45,
//    WIS_BOOST = 46,
//    DEX_BOOST = 47,
//    SILENCED = 48,
//    EXPOSED = 49,
//    ENERGIZED = 50,
//    HP_DEBUFF = 51,
//    MP_DEBUFF = 52,
//    ATT_DEBUFF = 53,
//    DEF_DEBUFF = 54,
//    SPD_DEBUFF = 55,
//    VIT_DEBUFF = 56,
//    WIS_DEBUFF = 57,
//    DEX_DEBUFF = 58,
//    INSPIRED = 59,
//    GROUND_DAMAGE = 99
//}
//
///**
// * The bitmask value for each condition effect
// */
//export enum ConditionEffectBits {
//    DEAD = 0x10,
//    QUIET = 0x20,
//    WEAK = 0x40,
//    SLOWED = 0x80,
//    SICK = 0x10,
//    DAZED = 0x20,
//    STUNNED = 0x40,
//    BLIND = 0x80,
//    HALLUCINATING = 0x100,
//    DRUNK = 0x200,
//    CONFUSED = 0x400,
//    STUN_IMMUNE = 0x800,
//    INVISIBLE = 0x1000,
//    PARALYZED = 0x2000,
//    SPEEDY = 0x4000,
//    BLEEDING = 0x8000,
//    ARMOR_BROKEN_IMMUNE = 0x10000,
//    HEALING = 0x20000,
//    DAMAGING = 0x40000,
//    BERSERK = 0x80000,
//    PAUSED = 0x100000,
//    STASIS = 0x200000,
//    STASIS_IMMUNE = 0x400000,
//    INVINCIBLE = 0x800000,
//    INVULNERABLE = 0x1000000,
//    ARMORED = 0x2000000,
//    ARMOR_BROKEN = 0x4000000,
//    HEXED = 0x8000000,
//    NINJA_SPEEDY = 0x10000000,
//    UNSTABLE = 0x20000000,
//    DARKNESS = 0x40000000
//}
//
///**
// * The ID values of all visual/particle effects in the game
// */
//export enum VisualEffect {
//    UNKNOWN = 0,
//    HEAL = 1,
//    TELEPORT = 2,
//    STREAM = 3,
//    THROW = 4,
//    NOVA = 5,
//    POISON = 6,
//    LINE = 7,
//    BURST = 8,
//    FLOW = 9,
//    RING = 10,
//    LIGHTNING = 11,
//    COLLAPSE = 12,
//    CONEBLAST = 13,
//    JITTER = 14,
//    FLASH = 15,
//    THROW_PROJECTILE = 16,
//    SHOCKER = 17,
//    SHOCKEE = 18,
//    RISING_FURY = 19,
//    NOVA_NO_AOE = 20,
//    INSPIRED = 21,
//    HOLY_BEAM = 22,
//    CIRCLE_TELEGRAPH = 23,
//    CHAOS_BEAM = 24,
//    TELEPORT_MONSTER = 25,
//    METEOR = 26,
//    GILDED_BUFF = 27,
//    JADE_BUFF = 28,
//    CHAOS_BUFF = 29,
//    THUNDER_BUFF = 30,
//    STATUS_FLASH = 31,
//    FIRE_ORB_BUFF = 32,
//    OVERLAY = 33
//}
//
///**
// * The types of particle effects that exist in the game
// */
//export enum ParticleEffect {
//    CircleParticle = 0,
//    CustomParticle = 1,
//    ExplosionComplexParticle = 2,
//    FountainParticle = 3,
//    FountainSnowyParticle = 4,
//    HealParticle = 5,
//    HeartParticle = 6,
//    HitParticle = 7,
//    LevelUpParticle = 8,
//    RisingFuryParticle = 9,
//    ShockParticle = 10,
//    SkyBeamParticle = 11,
//    SnowflakeParticle = 12,
//    SparkerParticle = 13,
//    SparkParticle = 14,
//    StreamParticle = 15,
//    TeleportParticle = 16,
//    ThrownProjectile = 17,
//    ThrowParticle = 18,
//    VentParticle = 19,
//    VortexParticle = 20,
//    XmlParticle = 21,
//    FlowParticle = 22,
//    GildedParticle = 23,
//    AnimatedParticle = 24,
//    SkullParticle = 25,
//    MeteorParticle = 26,
//    HolyBeamParticle = 27,
//    CircleTelegraphParticle = 28,
//    SmokeCloudParticle = 29,
//    NoteParticle = 30,
//    LaserParticle = 31,
//    SummonerRingParticle = 32
//}
