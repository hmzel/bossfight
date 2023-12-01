package me.zelha.bossfight.attacks;

import java.util.concurrent.ThreadLocalRandom;

public enum Attacks {
    ;

    private Attack attack;

    Attacks(Attack attack) {
        this.attack = attack;
    }

    public static void randomAttack(int ticks) {
        values()[ThreadLocalRandom.current().nextInt(values().length)].attack.run(ticks);
    }
}
