package me.zelha.bossfight.attacks;

import java.util.concurrent.ThreadLocalRandom;

public enum Attacks {
    BOW(new BowAttack()),
    BEAM(new BeamAttack()),
    PUPPET(new PuppetAttack()),
    HAND(new HandAttack()),
    SPECIAL(new SpecialAttack());

    private final Attack attack;

    Attacks(Attack attack) {
        this.attack = attack;
    }

    public static void randomAttack(int ticks) {
        Attacks attack = values()[ThreadLocalRandom.current().nextInt(values().length)];

        if (attack == SPECIAL) {
            randomAttack(ticks);

            return;
        }

        attack.getMethods().run(ticks);
    }

    public Attack getMethods() {
        return attack;
    }
}
