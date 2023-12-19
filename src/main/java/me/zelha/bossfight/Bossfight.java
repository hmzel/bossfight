package me.zelha.bossfight;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleExplosion;
import hm.zelha.particlesfx.particles.ParticleExplosionEmitter;
import hm.zelha.particlesfx.particles.ParticleSwirlTransparent;
import hm.zelha.particlesfx.particles.parents.ColorableParticle;
import hm.zelha.particlesfx.shapers.ParticleImage;
import hm.zelha.particlesfx.shapers.ParticleLine;
import hm.zelha.particlesfx.shapers.ParticleSphere;
import hm.zelha.particlesfx.util.Color;
import hm.zelha.particlesfx.util.Rotation;
import hm.zelha.particlesfx.util.*;
import me.zelha.bossfight.attacks.Attacks;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftWither;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Bossfight extends BukkitRunnable {

    private final ThreadLocalRandom rng = ThreadLocalRandom.current();
    private final World world = Bukkit.getWorld("zelha");
    private final List<Location> deathAnimLocs = new ArrayList<>();
    private final List<Location> modifiedDeathAnimLocs = new ArrayList<>();
    private final ParticleShapeCompound watcher = new ParticleShapeCompound();
    private final ParticleShapeCompound wings = new ParticleShapeCompound();
    private final ParticleImage summoningCircle = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 27.2, 0.5), new File("plugins/summoningcircle.png"), 5, 5, 1000);
    private final ParticleSphere sphere = (ParticleSphere) new ParticleSphere(new ParticleExplosion(10D), new LocationSafe(world, 0.5, 28, 0.5), 500, 500, 500, 4).setLimit(25).setLimitInverse(true).stop();
    private final ParticleLine dyingShape = (ParticleLine) new ParticleLine(new ParticleExplosionEmitter(), 100, new LocationSafe(world, 0.5, 36, -36.5), new LocationSafe(world, 0.5, 36, -36.5)).stop();
    private final Vector vec = new Vector();
    private EntityPlayer boss;
    private Wither bossbar = null;
    private Attacks lastAttack2 = null;
    private Attacks lastAttack = null;
    private boolean wingFlapping = true;
    private boolean isEscaping = false;
    private boolean isDying = false;
    private boolean started = false;
    private int startedEscaping = 0;
    private int startedDying = 0;
    private long lastHit = 0;
    private int counter = 0;

    public Bossfight() {
        Rotation rot = new Rotation();

        summoningCircle.rotate(180, 0, 0);
        watcher.addShape(new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 33, 0.5), new File("plugins/outereye.png"), 9, 500));
        watcher.addShape(new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 33, 0.5), new File("plugins/innereye.gif"), 2.5, 500).setRadius(3));
        wings.addShape(new ParticleImage(new ParticleDustColored(), new LocationSafe(world, -3.25, 39.5, -36.6), new File("plugins/wing.png"), 1, 500).setRadius(4));
        wings.addShape(wings.getShape(0).clone());
        wings.getShape(1).move(7.5, 0, 0);
        watcher.addPlayer(UUID.randomUUID());
        dyingShape.stop();
        wings.stop();

        for (int i = 0; i <= 1; i++) {
            wings.getShape(i).setAxisRotation(-90, (180 * i), 10 + (-20 * i));
        }

        ShapeDisplayMechanic damageMechanic = ((particle, current, addition, count) -> {
            if (lastHit + 500 < System.currentTimeMillis()) return;

            ColorableParticle p = ((ColorableParticle) particle);

            p.getColor().setBlue(0);
            p.getColor().setGreen(0);
        });

        getEye().addMechanic(ShapeDisplayMechanic.Phase.AFTER_ROTATION, damageMechanic);
        wings.addMechanic(ShapeDisplayMechanic.Phase.AFTER_ROTATION, damageMechanic);

        watcher.addMechanic(ShapeDisplayMechanic.Phase.AFTER_ROTATION, ((particle, current, addition, count) -> {
            for (Player p : world.getPlayers()) {
                double[] direction = ParticleSFX.getDirection(p.getLocation(), current);
                Vector vec = new Vector();

                vec.zero().add(addition);
                rot.set(direction[0], direction[1], 0);
                rot.apply(vec);
                current.add(vec);

                //i have NO idea whats causing this error, but its harmless so whatever
                try {
                    particle.displayForPlayers(current, p);
                } catch (IndexOutOfBoundsException | NullPointerException ignored) {}

                current.subtract(vec);
            }
        }));
    }

    @Override
    public void run() {
        if (!started && !world.getNearbyEntities(summoningCircle.getCenter(), 5, 5, 5).isEmpty()) {
            started = true;

            summoningCircle.setParticleFrequency(2000);
            sphere.start();
            watcher.stop();
        }

        if (!started) return;

        if (Attacks.hasNoTarget()) {
            reset();

            return;
        }

        counter++;

        if (counter < 500) {
            summoningCircle.rotate(0, (counter / 10D), 0);
            summoningCircle.setXRadius(summoningCircle.getXRadius() + 0.04);
            summoningCircle.setZRadius(summoningCircle.getZRadius() + 0.04);
            sphere.setParticleFrequency(sphere.getParticleFrequency() + 4);
            sphere.rotate(0, (counter / 10D), 0);
            sphere.setXRadius(sphere.getXRadius() - 1);
            sphere.setYRadius(sphere.getYRadius() - 1);
            sphere.setZRadius(sphere.getZRadius() - 1);
        }

        if (counter % 15 == 0 && counter <= 470) {
            world.playSound(summoningCircle.getCenter(), Sound.ZOMBIE_UNFECT, 100, 0);
        }

        if (counter == 500) {
            boss = new EntityPlayer(MinecraftServer.getServer(), ((CraftWorld) world).getHandle(), new GameProfile(UUID.randomUUID(), ""), new PlayerInteractManager(((CraftWorld) world).getHandle()));
            bossbar = (Wither) world.spawnEntity(new Location(world, 0.5, 0, 0.5), EntityType.WITHER);
            EntityWither nmsWither = ((CraftWither) bossbar).getHandle();
            NBTTagCompound tag = nmsWither.getNBTTag();

            if (tag == null) {
                tag = new NBTTagCompound();
            }

            boss.getProfile().getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTcwMTEzNjMyNDM1MSwKICAicHJvZmlsZUlkIiA6ICI0MzJhNmI2MDA4YmY0NTFhOGYzMmUxMTljYWUxOGZkMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJsZXRza2lzcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81NGJiODkyYTg0N2RmNDVlOWI2ZmUxMmNiNjQ0NWJjYTEzY2QwOTY0ZWI5MTI4Y2U3MTEwZWJmM2JhNjJkYWZmIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=", "keikW3GamW9B073uLLKyHamiIE5VenU89G0wlm0zn6yNOu8G/hPujeWh84AC4v5R46cSu1C1aYirICvSdiO2tIPIckFQxkXc9vF6KI4X67kuZTiomPX8NJgc/S7NJYnZPoqdqvYiYUnfAZ05crkf9jt93gyvqhMe19RnWO7/OVU1mHmrQjvqyTHw2Rhz27misfrbv72FBrx2lP6xO+/4mkbME4VoYLDt57oq4XUMCAQUnK6+Msdej0p2fMAv+TNz1Ximns/64mp6qdzXT1iCpMwp/TPzd3oL8sHTl1PsykrrIt9BEotEjBoiwY09YWtAjqyorxMTSB2faSDB1zLfG2O2dtzni3XmWQPWZN2riVJFOqcyO7hqHj36iBTmT8IsFbESSqBDDGIgp2FlBqBt2DIVEz6MgjjEucJNFCrUZ+baNE2yxTVG3q6pAck+RX578vnwDTn+A9ixQtjub0Z0JaOVRJFEiwjviGaFlIqMVF3T3n6QrDKJkyaaCB6PqegoNTEYgVwkvS0c1EF6lyTPcJL+8M08SyGAccLlGUHJbagEZZf7D3xkiY2H9vbpsXNxpRuJ7vPrjUzHR2Vs0Qc8qC5yGFqvfI2QyQtMbJYvKWgacqhGBO5gbVdFtyfG7xyjcDJMiOO9U3sYPgnqn44baAvHuVdS+WCLUdfRTcVBmPE="));
            boss.getAttributeInstance(GenericAttributes.maxHealth).setValue(500);
            boss.getDataWatcher().watch(10, (byte) 127);
            boss.setPosition(0.5, 36, -36.5);
            boss.setHealth(50);
            bossbar.setMetadata("bossfight-entity", new FixedMetadataValue(Main.getInstance(), true));
            bossbar.setCustomName("§5Azazel");
            nmsWither.c(tag);
            tag.setInt("NoAI", 1);
            tag.setInt("Invul", 879);
            tag.setInt("PersistenceRequired", 1);
            tag.setInt("Silent", 1);
            nmsWither.f(tag);
            wings.start();
            sphere.stop();
            summoningCircle.stop();
            world.playSound(summoningCircle.getCenter(), Sound.WITHER_DEATH, 100, 0);
            getEye().setRadius(5).move(0, 10, -37);
            getEye().setCurrentFrame(0);
            getEye().start();

            for (Player p : world.getPlayers()) {
                sendBossCreationPackets(p);
            }
        }

        if (counter > 500 && !isDying) {
            String name = bossbar.getCustomName().replaceAll("§k|§r§5", "");
            int i = rng.nextInt(2, 8);

            bossbar.setCustomName(name.substring(0, i) + "§k" + name.charAt(i) + "§r§5" + name.substring(i + 1, 8));
            bossbar.setHealth(bossbar.getMaxHealth() * (boss.getHealth() / boss.getMaxHealth()));

            for (Player p : world.getPlayers()) {
                PlayerConnection pc = ((CraftPlayer) p).getHandle().playerConnection;
                Location bossbar = p.getLocation().multiply(32).add(p.getLocation().getDirection().multiply(1500));
                Location boss = this.boss.getBukkitEntity().getLocation();

                boss.setDirection(p.getLocation().subtract(boss).toVector());
                pc.sendPacket(new PacketPlayOutEntityTeleport(this.bossbar.getEntityId(), bossbar.getBlockX(), bossbar.getBlockY(), bossbar.getBlockZ(), (byte) 0, (byte) 0, false));
                pc.sendPacket(new PacketPlayOutEntityHeadRotation(this.boss, (byte) ((boss.getYaw() % 360) * 256 / 360)));
                pc.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(this.boss.getId(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) ((boss.getPitch() % 360) * 256 / 360), true));
            }

            if (!isEscaping && Attacks.getSpecialAttack().isActivated()) {
                startedEscaping = counter;
                isEscaping = true;
            }
        }

        if (counter > 500 && !isEscaping && !isDying) {
            if (!Attacks.getSpecialAttack().isStarting() || !Attacks.getSpecialAttack().isRunning()) {
                if (counter % 600 == 0) {
                    lastAttack = Attacks.randomAttack(600, Attacks.SPECIAL, lastAttack);
                }

                if (counter % 1200 == 0 && boss.getHealth() <= 375) {
                    lastAttack2 = Attacks.randomAttack(1200, Attacks.SPECIAL, lastAttack2);
                }
            }

            if (!Attacks.getSpecialAttack().isRunning() && boss.getHealth() <= 50) {
                for (Attacks attack : Attacks.values()) {
                    attack.getMethods().forceStop();
                }

                Attacks.getSpecialAttack().run(0);
            }

            if ((counter - 500) % 80 == 0) wingFlapping = !wingFlapping;

            if (wingFlapping) {
                wings.move(0, -0.035, 0.015);

                for (int k = 0; k <= 1; k++) {
                    wings.getShape(k).rotate(0.5, 0, 0.5 - k);
                }
            } else {
                wings.move(0, 0.035, -0.015);

                for (int k = 0; k <= 1; k++) {
                    wings.getShape(k).rotate(-0.5, 0, -0.5 + k);
                }
            }
        }

        if (counter - startedEscaping == 0 || counter - startedDying == 0) {
            for (Attacks attack : Attacks.values()) {
                if (attack == Attacks.SPECIAL) continue;

                attack.getMethods().forceStop();
            }

            for (int i = 0; i < 2; i++) {
                wings.getShape(i).setRotation(0, 0, 0);
            }

            wings.move(wings.getClonedCenter().subtract(boss.locX, boss.locY + 3.5, boss.locZ - 0.25).multiply(-1));
            bossbar.remove();
        }

        if (isDying) {
            if (counter - startedDying == 0) {
                for (int i = 0; i < 1000; i++) {
                    deathAnimLocs.add(new Location(world, rng.nextDouble(400) - 200, rng.nextDouble(400) - 200, rng.nextDouble(400) - 200));
                    deathAnimLocs.get(i).add(boss.locX, boss.locY, boss.locZ);
                    modifiedDeathAnimLocs.add(deathAnimLocs.get(i).clone());
                }
            }

            if (counter - startedDying <= 65) {
                for (int i = 0; i < 1000; i++) {
                    Location origin = deathAnimLocs.get(i);
                    Location current = modifiedDeathAnimLocs.get(i);

                    world.spigot().playEffect(current, Effect.CLOUD, 0, 0, 0, 0, 0, 0, 0, 1000);
                    vec.setX(boss.locX - current.getX());
                    vec.setY(boss.locY - current.getY());
                    vec.setZ(boss.locZ - current.getZ());
                    current.add(vec.normalize().multiply(origin.distance(boss.getBukkitEntity().getLocation()) / 65));
                }
            }

            if (counter - startedDying == 20) {
                for (int i = 0; i < 2; i++) {
                    wings.getShape(i).setParticleFrequency((int) (wings.getShape(i).getParticleFrequency() * 0.99));
                }

                ParticleDustColored dust = new ParticleDustColored();

                ShapeDisplayMechanic mechanic = (particle, current, addition, count) -> {
                    if (count == 1) {
                        Color color = ((ColorableParticle) particle).getColor();

                        color.setRed(color.getRed() + rng.nextInt(255 - color.getRed()));
                        color.setGreen(color.getGreen() + rng.nextInt(255 - color.getGreen()));
                        color.setBlue(color.getBlue() + rng.nextInt(255 - color.getBlue()));
                        dust.setColor(color.getRed(), color.getGreen(), color.getBlue());
                    }

                    current.add(addition);
                    dust.display(current);
                };

                wings.addPlayer(UUID.randomUUID());
                getEye().setParticleFrequency((int) (getEye().getParticleFrequency() * 0.99));
                getEye().setAxisRotation(-90, 0, 0);
                getEye().removeMechanic(1);
                getEye().addMechanic(ShapeDisplayMechanic.Phase.AFTER_ROTATION, mechanic);
                wings.addMechanic(ShapeDisplayMechanic.Phase.AFTER_ROTATION, mechanic);
            }

            if (counter - startedDying >= 30 && counter - startedDying <= 65) {
                wings.scale(0.95);
                getEye().scale(0.95);
                getEye().rotate(0, 0, 10);
                wings.move(0, -0.06, 0);
                getEye().move(0, -0.12, 0);
            }

            if (counter - startedDying == 65) {
                sendEntityDestroyPacket();
                dyingShape.start();
                getEye().stop();
                wings.stop();
            }

            if (counter - startedDying >= 65 && counter - startedDying <= 70) {
                for (int i = 0; i < 5; i++) {
                    world.playSound(boss.getBukkitEntity().getLocation(), Sound.EXPLODE, 100, (float) rng.nextDouble(2));
                }
            }

            if (counter - startedDying >= 65 && counter - startedDying <= 127) {
                dyingShape.getLocation(0).subtract(0, 1.2, 0);
                dyingShape.getLocation(1).add(0, 6.8, 0);
            }

            if (counter - startedDying >= 100) {
                dyingShape.getParticle().setOffsetX((counter - startedDying - 100) * 0.3);
                dyingShape.getParticle().setOffsetY((counter - startedDying - 100) * 0.3);
                dyingShape.getParticle().setOffsetZ((counter - startedDying - 100) * 0.3);
            }

            if (counter - startedDying >= 175) {
                for (Player p : world.getPlayers()) {
                    world.playEffect(p.getLocation().add(0, 1, 0), Effect.PORTAL, 0);
                }
            }

            if (counter - startedDying == 250) {
                for (Player p : world.getPlayers()) {
                    MinecraftServer.getServer().getPlayerList().moveToWorld(((CraftPlayer) p).getHandle(), 0, false);
                    p.getInventory().clear();
                }

                modifiedDeathAnimLocs.clear();
                deathAnimLocs.clear();
                dyingShape.stop();
            }
        }

        if (isEscaping) {
            if (counter - startedEscaping == 0) {
                getEye().setCurrentFrame(193);
            }

            if (counter - startedEscaping == 20) {
                getEye().stop();

                for (int i = 0; i < 208; i++) {
                    getEye().removeFrame(0);
                }

                getEye().addImage(new File("plugins/eyeportal.gif"));
                getEye().setAxisRotation(-90, 0, 0);
                getEye().removeMechanic(1);
                getEye().removePlayer(0);
                getEye().start();
            }

            if (counter - startedEscaping >= 20 && counter - startedEscaping <= 110) {
                for (int i = 0; i < 2; i++) {
                    wings.getShape(i).setParticleFrequency((int) (wings.getShape(i).getParticleFrequency() * 0.99));
                }

                wings.scale(0.99);
                getEye().scale(1.01);
                getEye().setParticleFrequency((int) (getEye().getParticleFrequency() * 1.01));
                getEye().rotate(0, 0.22, 1);
                getEye().move(0.0075, -0.05, -0.05);
                wings.move(0, -0.016, 0);
            }

            if (counter - startedEscaping > 120 && counter - startedEscaping <= 150) {
                getEye().move(0, -0.05, 0.185);
            }

            if (counter - startedEscaping == 165) {
                sendEntityDestroyPacket();
                wings.stop();
            }

            if (counter - startedEscaping == 172) {
                getEye().setFrameDelay(Integer.MAX_VALUE);
            }

            if (counter - startedEscaping == 180) {
                getEye().stop();
                getEye().setParticle(new ParticleSwirlTransparent());
                getEye().display();
                getEye().display();
            }
        }
    }

    public static void sendBossCreationPackets(Player player) {
        EntityPlayer boss = Main.getBossfight().getEntity();

        if (boss == null) return;

        ScoreboardTeam team = new ScoreboardTeam(MinecraftServer.getServer().getWorld().getScoreboard(), "boss");
        PlayerConnection pc = ((CraftPlayer) player).getHandle().playerConnection;

        team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
        pc.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, boss));
        pc.sendPacket(new PacketPlayOutNamedEntitySpawn(boss));
        pc.sendPacket(new PacketPlayOutEntityMetadata(boss.getId(), boss.getDataWatcher(), true));
        pc.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        pc.sendPacket(new PacketPlayOutScoreboardTeam(team, Collections.singletonList(boss.getName()), 3));

        new BukkitRunnable() {
            @Override
            public void run() {
                pc.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, boss));
            }
        }.runTaskLater(Main.getInstance(), 20);
    }

    public void handleDamage(double damage, boolean bypassImmunity) {
        if (!bypassImmunity && lastHit + 500 > System.currentTimeMillis()) return;

        for (Player p : world.getPlayers()) {
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityStatus(boss, (byte) 2));
        }

        if (boss.getBukkitEntity().getHealth() - damage > 0) {
            boss.getBukkitEntity().setHealth(boss.getBukkitEntity().getHealth() - damage);
        } else {
            startedDying = counter + 1;
            isDying = true;
        }

        world.playSound(boss.getBukkitEntity().getLocation(), Sound.WITHER_IDLE, 5, 2);

        lastHit = System.currentTimeMillis();
    }

    private void reset() {
        for (Attacks attack : Attacks.values()) {
            attack.getMethods().forceStop();
        }

        for (Player p : world.getPlayers()) {
            Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(p, null));
        }

        if (bossbar != null) {
            bossbar.remove();
        }

        sendEntityDestroyPacket();

        boss = null;

        summoningCircle.stop();
        dyingShape.stop();
        watcher.stop();
        wings.stop();
        sphere.stop();
        Main.reset();
    }

    private void sendEntityDestroyPacket() {
        for (Player p : world.getPlayers()) {
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(boss.getId()));
        }
    }

    public EntityPlayer getEntity() {
        return boss;
    }

    public ParticleImage getEye() {
        return (ParticleImage) watcher.getShape(1);
    }
}













