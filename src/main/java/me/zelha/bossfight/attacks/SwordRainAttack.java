package me.zelha.bossfight.attacks;

import hm.zelha.particlesfx.particles.ParticleBlockBreak;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.shapers.ParticleCircle;
import hm.zelha.particlesfx.shapers.ParticleImage;
import hm.zelha.particlesfx.shapers.parents.Shape;
import hm.zelha.particlesfx.util.LocationSafe;
import hm.zelha.particlesfx.util.Rotation;
import me.zelha.bossfight.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

public class SwordRainAttack extends Attack {

    private final ParticleCircle circle = new ParticleCircle(new ParticleBlockBreak(new MaterialData(Material.GOLD_BLOCK), new Vector()), new LocationSafe(world, 0, 0, 0), 0.2, 0.2, 100);
    private Player target = null;

    protected SwordRainAttack() {
        super(false);
    }

    @Override
    public BukkitTask run(int ticks) {
        if (Main.getBossfight().getEntity().getHealth() > 250) {
            return Attacks.randomAttack(ticks);
        }

        target = getTarget();

        return super.run(ticks);
    }

    @Override
    protected void attack() {
        if (target == null) {
            counter = 0;

            return;
        }

        if (counter == 0) {
            target.getLocation(circle.getCenter()).setY(27.2);
            circle.start();
        }

        if (counter <= 20) {
            circle.scale(1.2);

            return;
        }

        new BukkitRunnable() {

            private final ParticleImage sword = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0, 0, 0), new File("plugins/goldsword.png"), 0.75, 25);
            private final ThreadLocalRandom rng = ThreadLocalRandom.current();
            private final Rotation rot = new Rotation();
            private final Vector vec = new Vector();
            private int counter = 0;

            @Override
            public void run() {
                if (counter == 0) {
                    vec.setX(rng.nextDouble(circle.getXRadius() - 1));
                    vec.setY(20);
                    rot.set(0, rng.nextDouble(360), 0);
                    rot.apply(vec);
                    sword.move(circle.getCenter());
                    sword.move(vec);
                    vec.zero().setY(-1.5);
                    rot.set(rng.nextDouble(20) - 10, rng.nextDouble(360), 0);
                    rot.apply(vec);
                    sword.rotate(rot.getPitch(), rot.getYaw(), rot.getRoll());
                    sword.setAxisRotation(90, 0, 45);
                }

                counter++;

                sword.move(vec);
                damageNearby(sword.getCenter(), 2, 4, null);

                if (counter == 15) {
                    sword.stop();
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    @Override
    protected void reset() {
        new BukkitRunnable() {

            private final Shape circleClone = circle.clone();
            private int counter = 0;

            @Override
            public void run() {
                counter++;

                circleClone.scale(0.8);

                if (counter == 20) {
                    circleClone.stop();
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);

        circle.stop();
        circle.setXRadius(0.2);
        circle.setZRadius(0.2);
        super.reset();
    }
}
