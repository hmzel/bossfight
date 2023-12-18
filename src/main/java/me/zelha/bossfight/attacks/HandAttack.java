package me.zelha.bossfight.attacks;

import hm.zelha.particlesfx.particles.ParticleBlockBreak;
import hm.zelha.particlesfx.particles.ParticleNull;
import hm.zelha.particlesfx.particles.parents.Particle;
import hm.zelha.particlesfx.shapers.ParticleLine;
import hm.zelha.particlesfx.shapers.parents.Shape;
import hm.zelha.particlesfx.util.LocationSafe;
import hm.zelha.particlesfx.util.ParticleShapeCompound;
import hm.zelha.particlesfx.util.ShapeDisplayMechanic;
import me.zelha.bossfight.Main;
import me.zelha.bossfight.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class HandAttack extends Attack {

    private final Particle slamParticle = new ParticleBlockBreak(new MaterialData(Material.ENDER_STONE), (Vector) null, 3, 1, 3, 200).setSpeed(0.25);
    private final ParticleShapeCompound hand = new ParticleShapeCompound();
    private final Map<Shape, List<ArmorStand>> headMap = new HashMap<>();

    protected HandAttack() {
        super(false);

        hand.addShape(new ParticleLine(new ParticleNull(), 5, new LocationSafe(world, 0, 34.85, 0.12), new LocationSafe(world, 0, 34.85, 2.25)));
        hand.addShape(new ParticleLine(new ParticleNull(), 5, new LocationSafe(world, 0.3, 35, 0), new LocationSafe(world, 0.3, 35, 2.5)));
        hand.addShape(new ParticleLine(new ParticleNull(), 5, new LocationSafe(world, 0.75, 35, 0), new LocationSafe(world, 0.75, 35, 2.5)));
        hand.addShape(new ParticleLine(new ParticleNull(), 5, new LocationSafe(world, 1.25, 35, 0), new LocationSafe(world, 1.25, 35, 2.5)));
        hand.addShape(new ParticleLine(new ParticleNull(), 5, new LocationSafe(world, 1.65, 34.85, 0.05), new LocationSafe(world, 1.65, 34.85, 2.15)));
        hand.addShape(new ParticleLine(new ParticleNull(), 6, new LocationSafe(world, -0.27, 34.75, 1.8), new LocationSafe(world, -0.27, 34.5, 2.8), new LocationSafe(world, -0.27, 33.25, 2.3)));
        hand.addShape(new ParticleLine(new ParticleNull(), 8, new LocationSafe(world, 0.85, 34.75, 2), new LocationSafe(world, 0.85, 34.60, 3.25), new LocationSafe(world, 0.85, 33, 3)));
        hand.addShape(new ParticleLine(new ParticleNull(), 8, new LocationSafe(world, 2.1, 34.7, 1.5), new LocationSafe(world, 2.1, 34.55, 2.75), new LocationSafe(world, 2.1, 32.95, 2)));
        hand.addShape(new ParticleLine(new ParticleNull(), 6, new LocationSafe(world, 2, 34.7, -0.425), new LocationSafe(world, 2, 34.55, 0.475), new LocationSafe(world, 2, 33.2, 0.075)));
        hand.getShape(4).rotate(0, -5, 0);
        hand.getShape(5).rotate(20, 45, 0);
        hand.getShape(6).rotate(20, 0, 0);
        hand.getShape(7).rotate(20, -40, 0);
        hand.getShape(8).rotate(20, -105, 0);
        hand.move(0, 1000, 0);
        hand.stop();

        for (int i = 0; i < hand.getShapeAmount(); i++) {
            Shape shape = hand.getShape(i);

            headMap.put(shape, new ArrayList<>());

            for (int k = 0; k < shape.getParticleFrequency(); k++) {
                ArmorStand stand = (ArmorStand) world.spawnEntity(new Location(world, 0, 1000, 0), EntityType.ARMOR_STAND);

                stand.setGravity(false);
                stand.setVisible(false);
                stand.setBasePlate(false);
                stand.setMetadata("bossfight-entity", new FixedMetadataValue(Main.getInstance(), true));
                stand.setHelmet(Utils.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vZWR1Y2F0aW9uLm1pbmVjcmFmdC5uZXQvd3AtY29udGVudC91cGxvYWRzL25vdGUucG5nIn19fQ=="));
                headMap.get(shape).add(stand);
            }

            shape.addMechanic(ShapeDisplayMechanic.Phase.BEFORE_ROTATION, ((particle, location, vector, k) -> {
                if (headMap.get(shape).size() <= k) return;

                headMap.get(shape).get(k).teleport(new LocationSafe(location).subtract(0, 1.8, 0));
                headMap.get(shape).get(k).setHeadPose(new EulerAngle(Math.toRadians(shape.getPitch() + hand.getPitch()), Math.toRadians(shape.getYaw() + hand.getYaw()), -Math.toRadians(shape.getRoll() + hand.getRoll())));
            }));
        }

        headMap.get(hand.getShape(5)).get(5).remove();
        headMap.get(hand.getShape(6)).get(7).remove();
        headMap.get(hand.getShape(7)).get(7).remove();
        headMap.get(hand.getShape(8)).get(5).remove();

        new BukkitRunnable() {
            @Override
            public void run() {
                hand.start();
            }
        }.runTaskLater(Main.getInstance(), 1);
    }

    @Override
    protected void attack() {
        if (counter % 60 != 0) return;

        Player target = getTarget();

        if (target == null) return;

        if (ThreadLocalRandom.current().nextBoolean()) {
            boolean inverse = ThreadLocalRandom.current().nextBoolean();

            new BukkitRunnable() {

                private final Location loc = new Location(world, 0, 0, 0);
                private final Vector vec = new Vector();
                private int counter = 0;

                @Override
                public void run() {
                    if (counter == 0) {
                        target.getLocation(loc).add(0, 22, 0);
                        hand.move(target.getLocation().add(-20, 22, 0).subtract(hand.getClonedCenter()));

                        if (inverse) {
                            hand.move(40, 0, 0);
                            hand.rotate(0, 180, 0);
                        }
                    }

                    counter++;

                    if (inverse) {
                        hand.rotateAroundLocation(loc, 0, 0, -5);
                        hand.rotate(0, 0, -5);
                    } else {
                        hand.rotateAroundLocation(loc, 0, 0, 5);
                        hand.rotate(0, 0, 5);
                    }

                    for (Player p : world.getPlayers()) {
                        if (hand.getClonedCenter().distanceSquared(p.getLocation()) > 9) continue;

                        vec.zero().setX(2.5).setY(1.5);

                        if (inverse) {
                            vec.setX(-2.5);
                        }

                        //looks weird without a delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                p.setVelocity(vec);
                            }
                        }.runTaskLater(Main.getInstance(), 4);
                    }

                    damageNearby(hand.getClonedCenter(), 3, 8, null);

                    if (counter == 27) {
                        hand.setAroundRotation(hand.getClonedCenter(), 0, 0, 0);
                        hand.setRotation(0, 0, 0);
                        hand.move(0, 1000, 0);
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0, 1);
        } else {
            new BukkitRunnable() {

                private boolean inGround = false;
                private int cancel = 60;
                private int counter = 0;

                @Override
                public void run() {
                    counter++;

                    if (counter <= 20) {
                        hand.move(target.getLocation().add(0, 15, 0).subtract(hand.getClonedCenter()));
                    }

                    if (counter >= 20 && !inGround) {
                        hand.move(0, -1.5, 0);
                    }

                    if (!inGround) {
                        damageNearby(hand.getClonedCenter(), 2.5, 8, null);
                    }

                    if (!inGround && hand.getClonedCenter().subtract(0, 1.5, 0).getBlock().getType() != Material.AIR) {
                        cancel = Math.min(60, counter + 7);
                        inGround = true;

                        world.playSound(hand.getClonedCenter(), Sound.ZOMBIE_WOODBREAK, 1, 1.5f);
                        slamParticle.display(hand.getClonedCenter());
                    }

                    if (counter == cancel) {
                        hand.move(0, 1000, 0);
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0, 1);
        }
    }

    @Override
    protected void reset() {
        hand.setAroundRotation(hand.getClonedCenter(), 0, 0, 0);
        hand.setRotation(0, 0, 0);
        hand.move(0, 1000, 0);
        super.reset();
    }
}


























