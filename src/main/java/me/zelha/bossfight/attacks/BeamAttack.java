package me.zelha.bossfight.attacks;

import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleFirework;
import hm.zelha.particlesfx.particles.parents.TravellingParticle;
import hm.zelha.particlesfx.shapers.ParticleImage;
import hm.zelha.particlesfx.shapers.ParticleLine;
import hm.zelha.particlesfx.util.LocationSafe;
import hm.zelha.particlesfx.util.ParticleSFX;
import hm.zelha.particlesfx.util.Rotation;
import hm.zelha.particlesfx.util.ShapeDisplayMechanic;
import me.zelha.bossfight.Main;
import me.zelha.bossfight.listeners.ParryListener;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

public class BeamAttack extends Attack {

    private final ParticleLine beam = new ParticleLine(new ParticleFirework(new Vector(), 0.5, 0.5, 0.5, 5).setSpeed(0.5), 100, new LocationSafe(world, 0, 0, 0), new LocationSafe(world, 0, 0, 0));

    protected BeamAttack() {
        super(true);

        beam.addMechanic(ShapeDisplayMechanic.Phase.AFTER_DISPLAY, ((particle, current, addition, count) -> {
            EntityPlayer boss = Main.getBossfight().getEntity();

            Attacks.getSpecialAttack().handleCubeDamage(current, 1.5);

            if (boss == null) return;

            Location bossLoc = boss.getBukkitEntity().getLocation().add(0, 1, 0);

            if (current.distanceSquared(bossLoc) > 2.25) return;

            Main.getBossfight().handleDamage(5, false);

            if (shouldDeflect(current)) {
                addition.zero().add(bossLoc.subtract(current).toVector()).normalize().multiply(-0.75);
                beam.getLocation(1).zero().add(current).add(addition.clone().multiply(beam.getParticleFrequency() - count));
            }
        }));

        beam.stop();
    }

    @Override
    protected void attack() {
        if (counter % 30 != 0) return;

        new BukkitRunnable() {

            private final ParticleImage magicCircle = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 63, 0.5), new File("plugins/summoningcircle.png"), 0.01, 500);
            private final TravellingParticle beamParticle = ((TravellingParticle) beam.getParticle());
            private final ThreadLocalRandom rng = ThreadLocalRandom.current();
            private final Location center = new Location(world, 0.5, 27, 0.5);
            private final Location loc = new Location(world, 0, 0, 0);
            private final Rotation rot = new Rotation();
            private final Vector vec = new Vector();
            private final Player target = getTarget();
            private boolean parried = false;
            private int counter = 0;

            @Override
            public void run() {
                if (target == null) {
                    magicCircle.stop();
                    cancel();

                    return;
                }

                if (counter == 0) {
                    magicCircle.rotateAroundLocation(new Location(world, 0.5, 27, 0.5), rng.nextDouble(80), rng.nextDouble(360), 0);
                }

                counter++;

                magicCircle.setAxisRotation(0, counter * 10, 0);
                magicCircle.setParticleFrequency(Math.max(2, (int) (500 * (magicCircle.getXRadius() / 4))));

                if (counter <= 20) {
                    magicCircle.face(target.getLocation());
                    target.getLocation(loc).setY(loc.getY() + 0.5);
                    magicCircle.scale(1.35);
                }

                if (counter == 20) {
                    beam.getLocation(0).zero().add(magicCircle.getCenter());
                    beam.getLocation(1).zero().add(loc);
                    ParryListener.listenForParry(this, loc, 5);
                    beamParticle.setVelocity(new Vector());
                    beam.display();
                }

                if (!parried && ParryListener.getParryPlayer(this) != null) {
                    Location l = ParryListener.getParryPlayer(this).getLocation();
                    parried = true;

                    beam.getLocation(0).zero().add(loc);
                    loc.add(l.getDirection().multiply(75));
                    beam.getLocation(1).zero().add(loc);
                    beam.display();
                }

                if (counter == 25) {
                    loc.zero().add(beam.getLocation(1));
                    beamParticle.setVelocity(null);
                    beamParticle.setCount(500);
                    beamParticle.display(loc);
                    beamParticle.setCount(5);
                    world.playSound(loc, Sound.FIREWORK_LARGE_BLAST, 0.5f, 0.75f);
                    damageNearby(loc, 1.5, 5, null);
                    ParryListener.stopParryListening(this);

                    for (Player p : world.getPlayers()) {
                        if (loc.distanceSquared(p.getLocation()) > 2.25) continue;

                        vec.zero().setY(2);
                        rot.set(rng.nextDouble(40, 80), ParticleSFX.getDirection(p.getLocation(), center)[1], 0);
                        p.setVelocity(rot.apply(vec));
                    }
                }

                if (counter > 30) {
                    magicCircle.scale(0.8);
                }

                if (counter == 50) {
                    magicCircle.stop();
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }
}















