package me.zelha.bossfight.attacks;

import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleFirework;
import hm.zelha.particlesfx.shapers.ParticleCircle;
import hm.zelha.particlesfx.shapers.ParticleImage;
import hm.zelha.particlesfx.util.LocationSafe;
import hm.zelha.particlesfx.util.ParticleSFX;
import me.zelha.bossfight.Main;
import me.zelha.bossfight.listeners.ParryListener;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;

public class SwordAttack extends Attack {

    private final ParticleImage sword = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 43, -36.5), new File("plugins/sword.png"), 3, 300);
    private final Location bossLoc = new Location(world, 0.5, 37, -36.5);
    private Player target = null;
    private double pitch = -46;
    private double yaw = 46;

    protected SwordAttack() {
        super(true);

        sword.setAxisRotation(0, 315, -40);
        sword.stop();
    }

    @Override
    public boolean run(int ticks) {
        if (Main.getBossfight().getEntity().getHealth() > 300) {
            return false;
        }

        sword.start();

        return super.run(ticks);
    }

    @Override
    protected void attack() {
        if (counter <= 1) {
            target = getTarget();
        }

        if (target == null) return;

        double[] direction = ParticleSFX.getDirection(target.getLocation().add(0, 1, 0), bossLoc);
        direction[0] += 180;

        sword.setAroundRotation(bossLoc, direction[0] + pitch, direction[1] + yaw, 0);
        sword.setRotation(direction[0] - 90 + pitch, direction[1] + yaw, 0);

        if (counter == 6 || counter == 36) {
            new BukkitRunnable() {

                private final ParticleCircle slash = new ParticleCircle(new ParticleFirework(new Vector()), new LocationSafe(world, 0.5, 43, -36.5), 3, 3, 50);
                private final Location loc = new Location(world, 0, 0, 0);
                private Vector vec = null;
                private int counter = 0;
                private boolean parried = false;

                @Override
                public void run() {
                    if (counter == 0) {
                        slash.setLimit(50);
                        slash.setLimitInverse(true);
                        slash.setAxisRoll(sword.getAxisRoll());
                        slash.setAroundRotation(bossLoc, direction[0] + pitch, direction[1] + yaw, 0);
                        slash.setRotation(-direction[0] - 90 + pitch, direction[1] + yaw + 180, 0);
                        world.playSound(slash.getCenter(), Sound.ENDERDRAGON_WINGS, 3, 1.25f);
                        ParryListener.listenForParry(getTaskId(), loc, 2);

                        vec = target.getLocation(loc).add(0, 1, 0).subtract(slash.getCenter()).toVector().normalize().multiply(1.5);
                    }

                    counter++;

                    if (ParryListener.getParryPlayer(getTaskId()) != null && !parried) {
                        Location l = ParryListener.getParryPlayer(getTaskId()).getLocation();
                        parried = true;

                        slash.setRotation(l.getPitch(), l.getYaw(), 0);
                        slash.setAxisRoll(-slash.getAxisRoll());
                        vec.zero().add(l.getDirection()).multiply(1.5);
                    }

                    if (shouldDeflect(loc)) {
                        double[] direction = ParticleSFX.getDirection(loc, bossLoc);

                        slash.setRotation(direction[0] + 90, direction[1], 0);
                        slash.setAxisRoll(-slash.getAxisRoll());
                        vec.zero().add(slash.getClonedCenter().subtract(bossLoc).toVector()).normalize().multiply(1.5);
                    }

                    slash.move(vec);
                    loc.zero().add(slash.getCenter()).add(vec);
                    damageNearby(loc, 2, 5, null);

                    if (counter == 80) {
                        ParryListener.stopParryListening(getTaskId());
                        slash.stop();
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0, 1);
        }

        if (counter <= 10) {
            pitch += 6;
            yaw -= 6;
        }

        if (counter >= 11 && counter <= 30) {
            sword.setAxisRoll(sword.getAxisRoll() + 4);

            pitch -= 3;
            yaw -= 1;
        }

        if (counter >= 31 && counter <= 40) {
            pitch += 6;
            yaw += 6;
        }

        if (counter >= 41 && counter <= 60) {
            sword.setAxisRoll(sword.getAxisRoll() - 4);

            pitch -= 3;
            yaw += 1;
        }

        if (counter == 61) {
            counter = 0;
        }
    }

    @Override
    protected void reset() {
        pitch = -46;
        yaw = 46;

        sword.setAxisRoll(-40);
        sword.stop();
        super.reset();
    }
}
