package me.zelha.bossfight.attacks;

import org.bukkit.scheduler.BukkitTask;

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

    public static BukkitTask randomAttack(int ticks) {
        Attacks attack = values()[ThreadLocalRandom.current().nextInt(values().length)];

        if (attack == SPECIAL) {
            return randomAttack(ticks);
        }

        return attack.getMethods().run(ticks);
    }

    public Attack getMethods() {
        return attack;
    }

    public static SpecialAttack getSpecialAttack() {
        return (SpecialAttack) SPECIAL.getMethods();
    }
}
