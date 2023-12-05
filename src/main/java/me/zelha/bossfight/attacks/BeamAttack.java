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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

public class BeamAttack extends Attack {

    private final ParticleLine beam = new ParticleLine(new ParticleFirework(new Vector(), 0.5, 0.5, 0.5, 5).setSpeed(0.5), 100, new LocationSafe(world, 0, 0, 0), new LocationSafe(world, 0, 0, 0));

    protected BeamAttack() {
        super(true);

        Location center = new Location(world, 0.5, 27, 0.5);
        Location target = new Location(world, 0, 0, 0);
        Rotation rot = new Rotation();
        Vector vec = new Vector();

        beam.addMechanic(ShapeDisplayMechanic.Phase.AFTER_DISPLAY, ((particle, current, addition, count) -> {
            damageNearby(current, 1, 5, null);

            for (Player p : world.getPlayers()) {
                if (current.distanceSquared(p.getLocation()) > 1) continue;

                vec.zero().setY(2);
                rot.set(ThreadLocalRandom.current().nextDouble(40, 80), ParticleSFX.getDirection(p.getLocation(target), center)[1], 0);
                p.setVelocity(rot.apply(vec).normalize().multiply(2));
            }
        }));

        beam.stop();
    }

    @Override
    protected void attack() {
        if (counter % 30 != 0) return;

        new BukkitRunnable() {

            private final ThreadLocalRandom rng = ThreadLocalRandom.current();
            private final Location loc = new Location(world, 0, 0, 0);
            private final Player target = getTarget();
            private ParticleImage magicCircle = null;
            private int counter = 0;

            @Override
            public void run() {
                if (target == null) {
                    cancel();

                    return;
                }

                if (magicCircle == null) {
                    magicCircle = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 63, 0.5), new File("plugins/summoningcircle.png"), 0.01, 500);

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

                if (counter == 25) {
                    TravellingParticle beamParticle = ((TravellingParticle) beam.getParticle());

                    beam.getLocation(0).zero().add(magicCircle.getCenter());
                    beam.getLocation(1).zero().add(loc);
                    beamParticle.setVelocity(new Vector());
                    beam.display();
                    beamParticle.setVelocity(null);
                    beamParticle.setCount(500);
                    beamParticle.display(loc);
                    beamParticle.setCount(5);
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















