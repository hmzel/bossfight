package me.zelha.bossfight.attacks;

import java.util.concurrent.ThreadLocalRandom;

public enum Attacks {
    BOW(new BowAttack()),
    BEAM(new BeamAttack()),
    PUPPET(new PuppetAttack()),
    HAND(new HandAttack());

    private final Attack attack;

    Attacks(Attack attack) {
        this.attack = attack;
    }

    public static void randomAttack(int ticks) {
        values()[ThreadLocalRandom.current().nextInt(values().length)].attack.run(ticks);
    }

    public Attack getMethods() {
        return attack;
    }
}
