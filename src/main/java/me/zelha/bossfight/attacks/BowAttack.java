package me.zelha.bossfight.attacks;

import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.shapers.ParticleImage;
import hm.zelha.particlesfx.util.LocationSafe;
import hm.zelha.particlesfx.util.Rotation;
import me.zelha.bossfight.Main;
import me.zelha.bossfight.Utils;
import me.zelha.bossfight.listeners.ParryListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;

public class BowAttack extends Attack {

    private final ParticleImage bow = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 43, -36.5), new File("plugins/bow.gif"), 5, 750).setRadius(3);
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

                private final ParticleImage arrow = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 43, -36.5), new File("plugins/arrow.png"), 5, 200).setRadius(3);
                private final Location loc = new Location(world, 0, 0, 0);
                private final Rotation rot = new Rotation();
                private final Vector vec = new Vector();
                private final Player target = getTarget();
                private boolean parried = false;
                private boolean inGround = false;
                private double damageRadius = 0.75;
                private int counter = 0;

                @Override
                public void run() {
                    if (target == null) {
                        arrow.stop();
                        cancel();

                        return;
                    }

                    if (counter == 0) {
                        arrow.setAxisRotation(0, 135, 90);
                        arrow.setRotation(-145, 0, 0);
                        world.playSound(arrow.getCenter(), Sound.SHOOT_ARROW, 100, 0.75f);
                        ParryListener.listenForParry(this, loc, 1.5);
                    }

                    counter++;

                    if (counter == 160) {
                        ParryListener.stopParryListening(this);
                        arrow.stop();
                        cancel();
                    }

                    rot.set(arrow.getPitch(), arrow.getYaw(), arrow.getRoll());
                    rot.apply(vec.zero().setY(-3));

                    if (!inGround && world.getBlockAt(loc.zero().add(arrow.getCenter()).add(vec)).getType() != Material.AIR) {
                        world.playSound(arrow.getCenter(), Sound.ARROW_HIT, 5, 0.75f);
                        ParryListener.stopParryListening(this);

                        inGround = true;
                    }

                    if (inGround) return;

                    if (!parried && ParryListener.getParryPlayer(this) != null) {
                        Location l = ParryListener.getParryPlayer(this).getLocation();

                        arrow.setRotation(l.getPitch() - 90, l.getYaw(), 0);

                        damageRadius = 1.5;
                        parried = true;
                    }

                    arrow.move(vec.multiply(0.5));
                    damageNearby(loc.zero().add(arrow.getCenter()).add(vec.multiply(2)), damageRadius, 5, arrow.getCenter());

                    if (parried) return;

                    Utils.faceSlowly(arrow, target.getLocation(), Math.min(20, counter));
                }
            }.runTaskTimer(Main.getInstance(), 0, 1);
        }

        bow.setCurrentFrame(frame);
    }

    @Override
    protected void reset() {
        Main.getBossfight().getEye().start();
        bow.stop();
        super.reset();
    }
}
