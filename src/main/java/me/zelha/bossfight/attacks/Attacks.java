package me.zelha.bossfight.attacks;

import java.util.concurrent.ThreadLocalRandom;

public enum Attacks {
    BOW(new BowAttack()),
    SWORD(new SwordAttack()),
    STAB(new StabAttack()),
    SWORD_RAIN(new SwordRainAttack()),
    BEAM(new BeamAttack()),
    PUPPET(new PuppetAttack()),
    HAND(new HandAttack()),
    SPECIAL(new SpecialAttack());

    private final Attack attack;

    Attacks(Attack attack) {
        this.attack = attack;
    }

    public static Attacks randomAttack(int ticks, Attacks... exclusions) {
        Attacks attack = values()[ThreadLocalRandom.current().nextInt(values().length)];

        for (Attacks exclusion : exclusions) {
            if (attack == exclusion) {
                return randomAttack(ticks, exclusions);
            }
        }

        if (!attack.getMethods().run(ticks)) {
            return randomAttack(ticks, exclusions);
        }

        return attack;
    }

    public static boolean hasNoTarget() {
        return getSpecialAttack().getTarget() == null;
    }

    public static SpecialAttack getSpecialAttack() {
        return (SpecialAttack) SPECIAL.getMethods();
    }

    public Attack getMethods() {
        return attack;
    }
}
