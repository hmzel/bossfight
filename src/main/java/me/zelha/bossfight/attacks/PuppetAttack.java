package me.zelha.bossfight.attacks;

import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleExplosion;
import hm.zelha.particlesfx.shapers.ParticleLine;
import hm.zelha.particlesfx.util.*;
import me.zelha.bossfight.Main;
import me.zelha.bossfight.listeners.ParryListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PuppetAttack extends Attack {

    private final Vector vec = new Vector();

    protected PuppetAttack() {
        super(false);
    }

    @Override
    protected void attack() {
        if (counter % 100 != 0) return;

        new BukkitRunnable() {

            private final ParticleShapeCompound controllerBar = new ParticleShapeCompound();
            private final ParticleShapeCompound controller = new ParticleShapeCompound();
            private final List<LocationSafe> limbs = new ArrayList<>();
            private final Location parryLoc = new Location(world, 0, 0, 0);
            private final Location loc = new Location(world, 0, 0, 0);
            private final Rotation rot = new Rotation();
            private final Vector vec = new Vector();
            private final Player target = getTarget();
            private final double randomYaw = ThreadLocalRandom.current().nextDouble(360);
            private EulerAngle rightArmInc;
            private EulerAngle leftArmInc;
            private EulerAngle rightLegInc;
            private EulerAngle leftLegInc;
            private ArmorStand puppet = null;
            private boolean hasParried = false;
            private double barPitchInc = -2.1;
            private int animationStart = 0;
            private int animationEnd = 26;
            private int counter = 0;

            @Override
            public void run() {
                if (target == null) {
                    cancel();

                    return;
                }

                //i dont like having this bit before everything else but the client does some wacky things
                //if the puppet is spawned and immediately teleported far away
                if (counter <= animationEnd + 10) {
                    rot.set(0, randomYaw, 0);
                    rot.apply(vec.zero().setZ(-1.5));
                    target.getLocation(loc).add(vec);
                    loc.setYaw((float) randomYaw);
                }

                if (puppet == null) {
                    puppet = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
                    rightArmInc = getIncrement(-108.6, -33.6, -27.8, puppet.getRightArmPose(), 20);
                    leftArmInc = getIncrement(-118.4, 46.2, -20.4, puppet.getLeftArmPose(), 20);
                    rightLegInc = getIncrement(18.8, -4.2, 5.2, puppet.getRightLegPose(), 20);
                    leftLegInc = getIncrement(-23, -4.2, -6.2, puppet.getLeftLegPose(), 20);

                    controllerBar.addShape(new ParticleLine(new ParticleDustColored(Color.WHITE).setPureColor(true), 20, new LocationSafe(world, 0.75, 30.5, 0.75), new LocationSafe(world, 0, 30.5, 0), new LocationSafe(world, -0.75, 30.5, -0.75)));
                    controllerBar.addShape(new ParticleLine(new ParticleDustColored(Color.WHITE).setPureColor(true), 20, new LocationSafe(world, -0.75, 30.5, 0.75), new LocationSafe(world, 0.75, 30.5, -0.75)));
                    controllerBar.move(puppet.getLocation().subtract(0, 27, 0));
                    controllerBar.setYaw(randomYaw);

                    for (int i = 0; i < 5; i++) {
                        limbs.add(new LocationSafe(world, 0, 0, 0));
                        controller.addShape(new ParticleLine(new ParticleExplosion(0.035D), 50, (LocationSafe) controllerBar.getLocations()[i], limbs.get(i)));
                    }

                    controller.addShape(controllerBar);
                    puppet.setMetadata("bossfight-entity", new FixedMetadataValue(Main.getInstance(), true));
                    puppet.setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
                    puppet.setHelmet(new ItemStack(Material.GOLD_HELMET));
                    puppet.setBasePlate(false);
                    puppet.setGravity(false);
                    puppet.setArms(true);
                }

                Location puppetLoc = puppet.getLocation();
                counter++;

                if (counter >= animationStart && counter <= animationEnd) {
                    puppet.setRightArmPose(puppet.getRightArmPose().add(rightArmInc.getX(), rightArmInc.getY(), rightArmInc.getZ()));
                    puppet.setLeftArmPose(puppet.getLeftArmPose().add(leftArmInc.getX(), leftArmInc.getY(), leftArmInc.getZ()));
                    puppet.setRightLegPose(puppet.getRightLegPose().add(rightLegInc.getX(), rightLegInc.getY(), rightLegInc.getZ()));
                    puppet.setLeftLegPose(puppet.getLeftLegPose().add(leftLegInc.getX(), leftLegInc.getY(), leftLegInc.getZ()));
                    controllerBar.rotate(barPitchInc, 0, 0);
                }

                if (counter == 20) {
                    rightArmInc = getIncrement(-11, -27, -36, puppet.getRightArmPose(), 6);
                    leftArmInc = getIncrement(-21, 74, 7, puppet.getLeftArmPose(), 6);
                    rightLegInc = new EulerAngle(0, 0, 0);
                    leftLegInc = new EulerAngle(0, 0, 0);
                    barPitchInc = 8;

                    ParryListener.listenForParry(this, parryLoc, 1);
                }

                if (counter >= 20) {
                    puppet.getLocation(parryLoc).add(0, 1.25, 0).add(puppetLoc.getDirection().multiply(0.5));
                }

                if (ParryListener.getParryPlayer(this) != null) {
                    rightArmInc = getIncrement(-201, 0, -20, puppet.getRightArmPose(), 6);
                    leftArmInc = getIncrement(-204, 0, 20, puppet.getLeftArmPose(), 6);
                    barPitchInc = (-40 - controllerBar.getPitch()) / 6;
                    animationStart = counter;
                    animationEnd = counter + 5;
                    hasParried = true;

                    ParryListener.stopParryListening(this);
                }

                if (!hasParried && counter == 26) {
                    damageNearby(parryLoc, 2, 8, null);
                    ParryListener.stopParryListening(this);
                }

                if (counter <= animationEnd + 10) {
                    controllerBar.move(loc.getX() - puppetLoc.getX(), loc.getY() - puppetLoc.getY(), loc.getZ() - puppetLoc.getZ());
                    puppet.teleport(loc);
                }

                if (counter >= animationEnd + 10) {
                    Vector direction = puppetLoc.getDirection().multiply(-1).setY(1);

                    puppet.teleport(puppetLoc.add(direction));
                    controllerBar.move(direction);
                }

                if (counter == 60) {
                    controller.stop();
                    puppet.remove();
                    cancel();

                    return;
                }

                //this is all to connect the marionette strings to the right place on the armor stand
                //i kinda hate this but i dont think theres a better way to do it
                rot.set(Math.toDegrees(puppet.getLeftArmPose().getX()), Math.toDegrees(puppet.getLeftArmPose().getY()), -Math.toDegrees(puppet.getLeftArmPose().getZ()));
                puppet.getLocation(limbs.get(0)).add(0.35, 1.35, 0).add(rot.apply(vec.zero().setY(-0.66)));
                puppet.getLocation(limbs.get(1)).add(0, 1.8, 0);
                rot.set(Math.toDegrees(puppet.getRightLegPose().getX()), Math.toDegrees(puppet.getRightLegPose().getY()), -Math.toDegrees(puppet.getRightLegPose().getZ()));
                puppet.getLocation(limbs.get(2)).add(-0.125, 0.7, -0.025).add(rot.apply(vec.zero().setY(-0.66)));
                rot.set(Math.toDegrees(puppet.getRightArmPose().getX()), Math.toDegrees(puppet.getRightArmPose().getY()), -Math.toDegrees(puppet.getRightArmPose().getZ()));
                puppet.getLocation(limbs.get(3)).add(-0.35, 1.35, 0.025).add(rot.apply(vec.zero().setY(-0.66)));
                rot.set(Math.toDegrees(puppet.getLeftLegPose().getX()), Math.toDegrees(puppet.getLeftLegPose().getY()), -Math.toDegrees(puppet.getLeftLegPose().getZ()));
                puppet.getLocation(limbs.get(4)).add(0.125, 0.7, 0).add(rot.apply(vec.zero().setY(-0.66)));
                rot.set(0, randomYaw, 0);
                loc.zero();

                for (int i = 0; i < 5; i++) {
                    loc.add(limbs.get(i));
                }

                loc.multiply(1D / 5);

                for (int i = 0; i < 5; i++) {
                    LVMath.additionToLocation(limbs.get(i), loc, rot.apply(LVMath.subtractToVector(vec, limbs.get(i), loc)));
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    private EulerAngle getIncrement(double wantedPitch, double wantedYaw, double wantedRoll, EulerAngle current, int steps) {
        vec.zero();
        vec.setX(Math.toRadians(wantedPitch) - current.getX());
        vec.setY(Math.toRadians(wantedYaw) - current.getY());
        vec.setZ(Math.toRadians(wantedRoll) - current.getZ());
        vec.multiply(1D / steps);

        return new EulerAngle(vec.getX(), vec.getY(), vec.getZ());
    }
}
