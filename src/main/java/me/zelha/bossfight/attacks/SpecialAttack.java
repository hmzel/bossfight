package me.zelha.bossfight.attacks;

import hm.zelha.particlesfx.particles.ParticleBlockBreak;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleExplosion;
import hm.zelha.particlesfx.particles.ParticleNull;
import hm.zelha.particlesfx.shapers.ParticleImage;
import hm.zelha.particlesfx.shapers.ParticleLine;
import hm.zelha.particlesfx.shapers.ParticlePolygon;
import hm.zelha.particlesfx.shapers.ParticleSphere;
import hm.zelha.particlesfx.shapers.parents.ParticleShaper;
import hm.zelha.particlesfx.util.*;
import me.zelha.bossfight.Main;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SpecialAttack extends Attack {

    private final ParticleImage magicCircle = new ParticleImage(new ParticleDustColored(), new LocationSafe(Bukkit.getWorld("zelha"), 0.5, 200, 0.5), new File("plugins/magiccircle.png"), 1, 1000);
    private final ParticleExplosion magicCircleParticle = new ParticleExplosion(1D);
    private final ParticleShapeCompound centerBeam = new ParticleShapeCompound();
    private final List<ParticleShapeCompound> cubes = new ArrayList<>();
    private final List<List<Entity>> structureBlocksList = new ArrayList<>();
    private final List<String> structureData;
    private final Location loc = new Location(world, 0, 0, 0);
    private final Rotation rot = new Rotation();
    private final Vector vec = new Vector();
    private BukkitTask attackTask = null;
    private boolean deactivating = false;
    private boolean activating = false;

    protected SpecialAttack() {
        super(false);

        BufferedReader reader = new BufferedReader(new InputStreamReader(SpecialAttack.class.getClassLoader().getResourceAsStream("SpecialAttackStructureData")));
        structureData = reader.lines().collect(Collectors.toList());
        LocationSafe l = new LocationSafe(world, 0.5, 60, 0.5);
        ParticleSphere sphere = new ParticleSphere(new ParticleBlockBreak(l), l, 3.5, 100);
        ParticleLine beam = new ParticleLine(new ParticleExplosion(1.5D), 50, l, l.clone().add(0, 140, 0));

        magicCircle.addPlayer(UUID.randomUUID());
        magicCircle.addMechanic(ShapeDisplayMechanic.Phase.AFTER_DISPLAY, ((particle, current, addition, count) -> magicCircleParticle.display(current)));
        magicCircle.stop();
        centerBeam.addShape(sphere);
        centerBeam.addShape(beam);
        centerBeam.stop();

        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 8; i++) {
            structureBlocksList.add(new ArrayList<>());
        }
    }

    @Override
    public BukkitTask run(int ticks) {
        attackTask = super.run(Integer.MAX_VALUE);

        return attackTask;
    }

    @Override
    protected void attack() {
        if (counter % 20 == 0 && counter <= 140) {
            ParticlePolygon cubeFilled = ParticleSFX.cubeFilled(new ParticleNull(), new LocationSafe(world, 0.5, 0.5, 0.5), 0.5, 100);
            ParticlePolygon cube = ParticleSFX.cube(new ParticleNull(), new LocationSafe(world, 0.5, 0.5, 0.5), 1.5, 100);

            cubes.add(new ParticleShapeCompound(cubeFilled, cube));
            rot.set(0, 45D * (counter / 20), 0);
            rot.apply(vec.setX(0).setY(30).setZ(-35));
            vec.setX((int) vec.getX()).setY((int) vec.getY()).setZ((int) vec.getZ());
            cubes.get(counter / 20).move(vec);

            for (int k = 0; k < 8; k++) {
                cube.getCorner(k).connect(cubeFilled.getCorner(k));
            }

            for (String data : structureData) {
                String[] splitData = data.split(", ");

                createBlock(
                        (int) (vec.getX() + Integer.parseInt(splitData[0])),
                        Integer.parseInt(splitData[1]),
                        (int) (vec.getZ() + Integer.parseInt(splitData[2])),
                        Material.getMaterial(splitData[3]),
                        Byte.parseByte(splitData[4])
                );
            }

            new BukkitRunnable() {

                private final int i = SpecialAttack.this.counter / 20;
                private int counter = 0;

                @Override
                public void run() {
                    counter++;

                    if (counter <= 20) {
                        for (Entity e : structureBlocksList.get(i)) {
                            e.setPosition(e.locX, e.locY + 0.7, e.locZ);
                        }
                    }

                    if (counter == 40) {
                        cubes.get(i).setParticle(new ParticleBlockBreak(new Vector()));
                        cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 1, 1);
        }

        for (ParticleShapeCompound cube : cubes) {
            cube.getShape(0).rotate(5, 5, 1);
            cube.getShape(1).rotate(5, 5, 1);
        }

        if (counter == 200) {
            LocationSafe l = new LocationSafe(cubes.get(0).getClonedCenter());

            for (int i = 0; i < 8; i++) {
                ParticleLine line = new ParticleLine(new ParticleBlockBreak(new Vector(), 0.5, 0.5, 0.5), 100, l.clone(), l.clone().add(0, 25, -20), l.clone().add(0, 30, 32));

                line.rotateAroundLocation(new Location(world, 0.5, 0, 0.5), 0, 45 * i, 0);
                line.rotate(0, 45 * i, 0);
                cubes.get(i).addShape(line);
            }

            centerBeam.start();
            magicCircle.start();
        }

        if (counter >= 200 && !activating && !deactivating) {
            magicCircleParticle.setSize(magicCircleParticle.getSize() + 0.00455 * (cubes.size() / 8D));
            magicCircle.setXRadius(magicCircle.getXRadius() + 0.138 * (cubes.size() / 8D));
            magicCircle.setZRadius(magicCircle.getZRadius() + 0.138 * (cubes.size() / 8D));
            magicCircle.rotate(0, cubes.size() / 8D, 0);
            centerBeam.getShape(0).rotate(5, 5, 1);
            loc.zero().add(0.5, 0, 0.5);
            rot.set(0, 0.5, 0);

            for (ParticleShapeCompound compound : cubes) {
                for (int i = 0; i < compound.getShapeAmount(); i++) {
                    compound.getShape(i).rotateAroundLocation(loc, 0, 0.5, 0);
                }

                compound.getShape(2).rotate(0, 0.5, 0);
            }

            for (List<Entity> entities : structureBlocksList) {
                loc.zero();

                for (Entity e : entities) {
                    loc.add(e.locX, e.locY, e.locZ);
                }

                loc.multiply(1D / entities.size());
                vec.setX(loc.getX() - 0.5);
                vec.setY(loc.getY());
                vec.setZ(loc.getZ() - 0.5);
                rot.apply(vec);

                for (Entity e : entities) {
                    e.setPosition(e.locX + vec.getX() + 0.5 - loc.getX(), e.locY, e.locZ + vec.getZ() + 0.5 - loc.getZ());
                }
            }
        }

        if (magicCircle.getXRadius() >= 500) {
            activating = true;

            for (ParticleShapeCompound compound : cubes) {
                compound.stop();
            }

            centerBeam.stop();
            structureBlocksList.clear();
        }

        if (cubes.isEmpty()) {
            deactivating = true;

            centerBeam.stop();
            structureBlocksList.clear();
        }

        if (activating || deactivating) {
            magicCircle.scale(0.99);
            magicCircle.rotate(0, (magicCircle.getXRadius() - 500) / 10, 0);

            if (activating) {
                magicCircle.getCenter().subtract(0, 0.75, 0);
            } else {
                magicCircle.scale(0.95);
            }

            if (magicCircle.getXRadius() <= 1 || (activating && magicCircle.getXRadius() <= 50)) {
                if (activating) {
                    for (Player p : world.getPlayers()) {
                        p.damage(9999);
                    }
                }

                attackTask.cancel();
                magicCircle.stop();
                cubes.clear();
                magicCircle.setXRadius(1);
                magicCircle.setZRadius(1);
                magicCircleParticle.setSize(1D);

                deactivating = false;
                activating = false;
                counter = 0;
            }
        }
    }

    public void handleCubeDamage(Location l, double distance) {
        ParticleShapeCompound cube = null;

        for (ParticleShapeCompound compound : cubes) {
            if (l.distanceSquared(compound.getShape(0).getClonedCenter()) <= Math.pow(distance, 2)) {
                cube = compound;

                break;
            }
        }

        if (cube == null) return;

        ParticleBlockBreak particle = (ParticleBlockBreak) cube.getShape(0).getParticle();
        ParticleShapeCompound finalCube = cube;

        particle.setLocationToGo(cube.getLocations()[0]);
        cube.getShape(1).setParticle(particle);
        cube.getShape(2).stop();

        new BukkitRunnable() {

            private int counter = 0;

            @Override
            public void run() {
                Material randomMaterial;

                counter++;

                finalCube.getShape(0).rotate(5, 5, 1);
                finalCube.getShape(1).rotate(5, 5, 1);

                do {
                    randomMaterial = Material.values()[ThreadLocalRandom.current().nextInt(Material.values().length)];
                } while (!randomMaterial.isBlock());

                particle.setMaterialData(new MaterialData(randomMaterial));

                if (counter == 25) {
                    world.createExplosion(finalCube.getClonedCenter().subtract(0, 3, 0), 5);
                    cubes.remove(finalCube);
                    finalCube.stop();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    //as far as i can tell this is the best way to get falling blocks to not fall pre-1.10, its annoying and janky but oh well i guess
    private void createBlock(int x, int y, int z, Material material, int data) {
        EntityFallingBlock block = new EntityFallingBlock(((CraftWorld) this.world).getHandle(), x + 0.5, y, z + 0.5, Block.getById(material.getId()).fromLegacyData(data)) {

            private final ParticleShaper cube = (ParticleShaper) cubes.get(counter / 20).getShape(0);

            @Override
            public void t_() {
                ticksLived = 500;

                super.t_();

                if (!cube.isRunning()) return;

                motY = 0.04;
                velocityChanged = true;

                for (EntityHuman p : world.players) {
                    ((EntityPlayer) p).playerConnection.sendPacket(new PacketPlayOutEntityTeleport(this));
                }
            }
        };

        block.dropItem = false;

        block.getWorld().addEntity(block, CreatureSpawnEvent.SpawnReason.CUSTOM);
        block.getBukkitEntity().setMetadata("bossfight-entity", new FixedMetadataValue(Main.getInstance(), true));
        structureBlocksList.get(counter / 20).add(block);
    }
}