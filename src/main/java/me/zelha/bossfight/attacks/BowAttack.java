package me.zelha.bossfight.attacks;

import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.shapers.ParticleImage;
import hm.zelha.particlesfx.util.LocationSafe;
import hm.zelha.particlesfx.util.ParticleSFX;
import hm.zelha.particlesfx.util.Rotation;
import me.zelha.bossfight.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;

public class BowAttack extends Attack {

    private final ParticleImage bow = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 43, -36.5), new File("plugins/bow.gif"), 5, 750).setRadius(3);
    private int running = 0;
    private int frame = 0;

    protected BowAttack() {
        super(true);

        bow.setAxisRotation(-90, -90, 0);
        bow.rotate(-10, 0, 0);
        bow.setFrameDelay(Integer.MAX_VALUE);
        bow.stop();
    }

    @Override
    public BukkitTask run(int ticks) {
        Main.getBossfight().getEye().stop();
        bow.start();

        running++;

        return super.run(ticks);
    }

    @Override
    protected void attack() {
        if (counter % 20 == 0) {
            frame++;
        }

        if (frame == 4) {
            frame = 0;

            new BukkitRunnable() {

                private final Location loc = new Location(world, 0, 0, 0);
                private final Rotation rot = new Rotation();
                private final Vector vec = new Vector();
                private final Player target = getTarget();
                private ParticleImage arrow = null;
                private int counter = 0;

                @Override
                public void run() {
                    if (target == null) {
                        cancel();

                        return;
                    }

                    if (arrow == null) {
                        arrow = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 43, -36.5), new File("plugins/arrow.png"), 5, 200).setRadius(3);

                        arrow.setAxisRotation(0, 135, 90);
                        arrow.setRotation(-145, 0, 0);
                    }

                    counter++;

                    if (counter == 160) {
                        arrow.stop();
                        cancel();
                    }

                    rot.set(arrow.getPitch(), arrow.getYaw(), arrow.getRoll());
                    rot.apply(vec.zero().setY(-3));

                    if (world.getBlockAt(loc.zero().add(arrow.getCenter()).add(vec)).getType() != Material.AIR) return;

                    double[] direction = ParticleSFX.getDirection(target.getLocation(loc), arrow.getCenter());
                    double pitchInc, yawInc;
                    double pitch = arrow.getPitch();
                    double yaw = arrow.getYaw();
                    double wantedPitch = direction[0];
                    double wantedYaw = direction[1];
                    double speed = Math.min(20, counter);

                    if (pitch + speed <= wantedPitch) {
                        pitchInc = speed;
                    } else if (pitch - speed >= wantedPitch) {
                        pitchInc = -speed;
                    } else {
                        pitchInc = wantedPitch - pitch;
                    }

                    if (yaw + (360 - wantedYaw) < Math.abs(yaw - wantedYaw)) {
                        yawInc = -speed;
                    } else if (wantedYaw + (360 - yaw) < Math.abs(yaw - wantedYaw)) {
                        yawInc = speed;
                    } else if (yaw + speed <= wantedYaw) {
                        yawInc = speed;
                    } else if (yaw - speed >= wantedYaw) {
                        yawInc = -speed;
                    } else {
                        yawInc = wantedYaw - yaw;
                    }

                    if (yaw + yawInc > 360) {
                        yawInc = yawInc - 360;
                    }

                    if (yaw + yawInc < 0) {
                        yawInc = yawInc + 360;
                    }

                    arrow.rotate(pitchInc, yawInc, 0);
                    arrow.move(rot.apply(vec.zero().setY(-1.5)));
                    damageNearby(loc.zero().add(arrow.getCenter()).add(rot.apply(vec.zero().setY(-3))), 0.75, 1, null);
                }
            }.runTaskTimer(Main.getInstance(), 0, 1);
        }

        bow.setCurrentFrame(frame);
    }

    @Override
    protected void reset() {
        running--;

        if (running != 0) return;

        Main.getBossfight().getEye().start();
        bow.stop();
        super.reset();
    }
}
