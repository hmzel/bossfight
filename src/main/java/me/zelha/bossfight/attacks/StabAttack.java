package me.zelha.bossfight.attacks;

import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleWitchMagic;
import hm.zelha.particlesfx.shapers.ParticleCircle;
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
import org.bukkit.util.Vector;

import java.io.File;

public class StabAttack extends Attack {
    protected StabAttack() {
        super(true);
    }

    @Override
    public boolean run(int ticks) {
        if (Main.getBossfight().getEntity().getHealth() > 375) {
            return false;
        }

        return super.run(ticks);
    }

    @Override
    protected void attack() {
        if (counter % 90 != 0) return;

        new BukkitRunnable() {

            private final ParticleImage sword = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0, 0, 0), new File("plugins/swordempty.png"), 3, 100);
            private final ParticleCircle circle = new ParticleCircle(new ParticleWitchMagic(), new LocationSafe(world, 0, 0, 0), 0.35, 0.35, 50);
            private final Location loc = new Location(world, 0, 0, 0);
            private final Rotation rot = new Rotation();
            private final Vector vec = new Vector();
            private final Player target = getTarget();
            private boolean inGround = false;
            private boolean parried = false;
            private int counter = 0;

            @Override
            public void run() {
                if (target == null) {
                    circle.stop();
                    sword.stop();
                    cancel();

                    return;
                }

                if (counter == 0) {
                    target.getLocation(circle.getCenter()).setY(27);
                    target.getLocation(sword.getCenter()).setY(23.5);
                    sword.setAxisRotation(90, 0, 45);
                    sword.rotate(180, 270, 0);
                    sword.stop();
                }

                counter++;

                if (counter == 180) {
                    ParryListener.stopParryListening(this);
                    sword.stop();
                    cancel();
                }

                rot.set(sword.getPitch(), sword.getYaw(), sword.getRoll());
                rot.apply(vec.zero().setY(-3));

                if (!inGround && counter >= 45 && world.getBlockAt(loc.zero().add(sword.getCenter()).add(vec)).getType() != Material.AIR) {
                    world.playSound(sword.getCenter(), Sound.ZOMBIE_METAL, 5, 0.7f);
                    ParryListener.stopParryListening(this);

                    inGround = true;
                }

                if (inGround) return;

                if (!parried && ParryListener.getParryPlayer(this) != null) {
                    Location l = ParryListener.getParryPlayer(this).getLocation();

                    sword.setRotation(l.getPitch() - 90, l.getYaw(), 0);

                    parried = true;
                }

                if (counter <= 10) {
                    circle.scale(1.2);
                }

                if (counter == 15) {
                    sword.start();
                }

                if (counter >= 15) {
                    sword.move(vec.multiply(0.5));
                    damageNearby(loc.zero().add(sword.getCenter()).add(vec.multiply(2)), 1.5, 5, null);
                }

                if (counter == 25) {
                    circle.stop();
                }

                if (counter == 45) {
                    ParryListener.listenForParry(this, loc, 1.5);
                }

                if (counter >= 45 && counter <= 90) {
                    Utils.faceSlowly(sword, target.getLocation(), 5);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }
}
