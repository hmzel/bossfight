package me.zelha.bossfight;

import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleExplosion;
import hm.zelha.particlesfx.shapers.ParticleImage;
import hm.zelha.particlesfx.shapers.ParticleSphere;
import hm.zelha.particlesfx.util.LocationSafe;
import hm.zelha.particlesfx.util.ParticleShapeCompound;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class Bossfight extends BukkitRunnable {

    private final World world = Bukkit.getWorld("zelha");
    private final ParticleShapeCompound watcher = new ParticleShapeCompound();
    private final ParticleImage summoningCircle = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0, 27.2, 0), new File("plugins/summoningcircle.png"), 5, 5, 1000);
    private final ParticleSphere sphere = (ParticleSphere) new ParticleSphere(new ParticleExplosion(10D), new LocationSafe(world, 0, 28, 0), 500, 500, 500, 4).setLimit(25).setLimitInverse(true).stop();
    private boolean started = false;
    private int counter = 0;

    public Bossfight() {
        summoningCircle.rotate(180, 0, 0);
        watcher.addShape(new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0, 33, 0), new File("plugins/outereye.png"), 9, 500));
        watcher.addShape(new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0, 33, 0), new File("plugins/innereye.gif"), 2.5, 500).setRadius(3));
    }

    @Override
    public void run() {
        if (!started && !world.getNearbyEntities(summoningCircle.getCenter(), 5, 5, 5).isEmpty()) {
            started = true;

            summoningCircle.setParticleFrequency(2000);
            sphere.start();
            watcher.stop();
        }

        if (!started) {
            Player p = null;
            double distance = Double.MAX_VALUE;

            for (Player player : world.getPlayers()) {
                if (player.getLocation().distanceSquared(summoningCircle.getCenter()) < distance) {
                    p = player;
                    distance = player.getLocation().distanceSquared(summoningCircle.getCenter());
                }
            }

            if (p != null) {
                watcher.face(p.getLocation());
            }

            return;
        }

        counter++;

        summoningCircle.rotate(0, (counter / 10D), 0);
        summoningCircle.setXRadius(summoningCircle.getXRadius() + 0.04);
        summoningCircle.setZRadius(summoningCircle.getZRadius() + 0.04);
        sphere.setParticleFrequency(sphere.getParticleFrequency() + 4);
        sphere.rotate(0, (counter / 10D), 0);
        sphere.setXRadius(sphere.getXRadius() - 1);
        sphere.setYRadius(sphere.getYRadius() - 1);
        sphere.setZRadius(sphere.getZRadius() - 1);

        if (counter == 500) {
            sphere.stop();
            summoningCircle.stop();
        }
    }
}













