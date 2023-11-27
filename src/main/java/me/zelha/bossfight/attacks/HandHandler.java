package me.zelha.bossfight.attacks;

import hm.zelha.particlesfx.particles.ParticleNull;
import hm.zelha.particlesfx.shapers.ParticleLine;
import hm.zelha.particlesfx.shapers.parents.Shape;
import hm.zelha.particlesfx.util.LocationSafe;
import hm.zelha.particlesfx.util.ParticleShapeCompound;
import hm.zelha.particlesfx.util.ShapeDisplayMechanic;
import me.zelha.bossfight.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandHandler {//this is just a prototype, ill probably rename the class and put it in a package and other junk later

    private final ParticleShapeCompound compound = new ParticleShapeCompound();
    private final Map<Shape, List<ArmorStand>> headMap = new HashMap<>();
    private final World world = Bukkit.getWorld("zelha");

    public HandHandler() {
        compound.addShape(new ParticleLine(new ParticleNull(), 5, new LocationSafe(world, 0, 34.85, 0.12), new LocationSafe(world, 0, 34.85, 2.25)));
        compound.addShape(new ParticleLine(new ParticleNull(), 5, new LocationSafe(world, 0.3, 35, 0), new LocationSafe(world, 0.3, 35, 2.5)));
        compound.addShape(new ParticleLine(new ParticleNull(), 5, new LocationSafe(world, 0.75, 35, 0), new LocationSafe(world, 0.75, 35, 2.5)));
        compound.addShape(new ParticleLine(new ParticleNull(), 5, new LocationSafe(world, 1.25, 35, 0), new LocationSafe(world, 1.25, 35, 2.5)));
        compound.addShape(new ParticleLine(new ParticleNull(), 5, new LocationSafe(world, 1.65, 34.85, 0.05), new LocationSafe(world, 1.65, 34.85, 2.15)));
        compound.getShape(4).rotate(0, -5, 0);

        compound.addShape(new ParticleLine(new ParticleNull(), 6, new LocationSafe(world, 0, 35, 0), new LocationSafe(world, 0, 34.75, 1), new LocationSafe(world, 0, 33.5, 0.5)));
        compound.getShape(5).rotate(0, 35, 0);
        compound.getShape(5).move(-0.57, 0, 2.2);


        for (int i = 0; i < compound.getShapeAmount(); i++) {
            Shape shape = compound.getShape(i);

            headMap.put(shape, new ArrayList<>());

            for (int k = 0; k < shape.getParticleFrequency(); k++) {
                ArmorStand stand = (ArmorStand) world.spawnEntity(new Location(world, 0, 0, 0), EntityType.ARMOR_STAND);

                stand.setGravity(false);
                stand.setVisible(false);
                stand.setBasePlate(false);
                stand.setHelmet(Utils.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vZWR1Y2F0aW9uLm1pbmVjcmFmdC5uZXQvd3AtY29udGVudC91cGxvYWRzL2N1c3RvbWhlYWR2aWJyYW50LnBuZyJ9fX0="));
                headMap.get(shape).add(stand);
            }

            shape.addMechanic(ShapeDisplayMechanic.Phase.BEFORE_ROTATION, ((particle, location, vector, k) -> {
                headMap.get(shape).get(k).teleport(new Location(world, 0, 0, 0).add(location).subtract(0, 2, 0));
                headMap.get(shape).get(k).setHeadPose(new EulerAngle(Math.toRadians(shape.getPitch() + compound.getPitch()), Math.toRadians(shape.getYaw() + compound.getYaw()), 0));
            }));
        }
    }

    public ParticleShapeCompound getCompound() {
        return compound;
    }
}














