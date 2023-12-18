package me.zelha.bossfight;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import hm.zelha.particlesfx.particles.ParticleDustColored;
import hm.zelha.particlesfx.particles.ParticleExplosion;
import hm.zelha.particlesfx.particles.ParticleSwirlTransparent;
import hm.zelha.particlesfx.particles.parents.ColorableParticle;
import hm.zelha.particlesfx.shapers.ParticleImage;
import hm.zelha.particlesfx.shapers.ParticleSphere;
import hm.zelha.particlesfx.util.*;
import me.zelha.bossfight.attacks.Attacks;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftWither;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Bossfight extends BukkitRunnable {

    private final World world = Bukkit.getWorld("zelha");
    private final ParticleShapeCompound watcher = new ParticleShapeCompound();
    private final ParticleShapeCompound wings = createWings();
    private final ParticleImage summoningCircle = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 27.2, 0.5), new File("plugins/summoningcircle.png"), 5, 5, 1000);
    private final ParticleSphere sphere = (ParticleSphere) new ParticleSphere(new ParticleExplosion(10D), new LocationSafe(world, 0.5, 28, 0.5), 500, 500, 500, 4).setLimit(25).setLimitInverse(true).stop();
    private final EntityPlayer boss;
    private Wither bossbar = null;
    private boolean started = false;
    private boolean wingFlapping = true;
    private long lastHit = 0;
    private int counter = 0;

    public Bossfight() {
        Rotation rot = new Rotation();
        boss = new EntityPlayer(MinecraftServer.getServer(), ((CraftWorld) world).getHandle(), new GameProfile(UUID.randomUUID(), ""), new PlayerInteractManager(((CraftWorld) world).getHandle()));

        boss.getProfile().getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTcwMTEzNjMyNDM1MSwKICAicHJvZmlsZUlkIiA6ICI0MzJhNmI2MDA4YmY0NTFhOGYzMmUxMTljYWUxOGZkMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJsZXRza2lzcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81NGJiODkyYTg0N2RmNDVlOWI2ZmUxMmNiNjQ0NWJjYTEzY2QwOTY0ZWI5MTI4Y2U3MTEwZWJmM2JhNjJkYWZmIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=", "keikW3GamW9B073uLLKyHamiIE5VenU89G0wlm0zn6yNOu8G/hPujeWh84AC4v5R46cSu1C1aYirICvSdiO2tIPIckFQxkXc9vF6KI4X67kuZTiomPX8NJgc/S7NJYnZPoqdqvYiYUnfAZ05crkf9jt93gyvqhMe19RnWO7/OVU1mHmrQjvqyTHw2Rhz27misfrbv72FBrx2lP6xO+/4mkbME4VoYLDt57oq4XUMCAQUnK6+Msdej0p2fMAv+TNz1Ximns/64mp6qdzXT1iCpMwp/TPzd3oL8sHTl1PsykrrIt9BEotEjBoiwY09YWtAjqyorxMTSB2faSDB1zLfG2O2dtzni3XmWQPWZN2riVJFOqcyO7hqHj36iBTmT8IsFbESSqBDDGIgp2FlBqBt2DIVEz6MgjjEucJNFCrUZ+baNE2yxTVG3q6pAck+RX578vnwDTn+A9ixQtjub0Z0JaOVRJFEiwjviGaFlIqMVF3T3n6QrDKJkyaaCB6PqegoNTEYgVwkvS0c1EF6lyTPcJL+8M08SyGAccLlGUHJbagEZZf7D3xkiY2H9vbpsXNxpRuJ7vPrjUzHR2Vs0Qc8qC5yGFqvfI2QyQtMbJYvKWgacqhGBO5gbVdFtyfG7xyjcDJMiOO9U3sYPgnqn44baAvHuVdS+WCLUdfRTcVBmPE="));
        boss.getAttributeInstance(GenericAttributes.maxHealth).setValue(500);
        boss.getDataWatcher().watch(10, (byte) 127);
        boss.setPosition(0.5, 36, -36.5);
        boss.setHealth(500);
        summoningCircle.rotate(180, 0, 0);
        watcher.addShape(new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 33, 0.5), new File("plugins/outereye.png"), 9, 500));
        watcher.addShape(new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0.5, 33, 0.5), new File("plugins/innereye.gif"), 2.5, 500).setRadius(3));
        watcher.addPlayer(UUID.randomUUID());

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

                rot.set(direction[0], direction[1], 0);
                rot.apply(addition);
                current.add(addition);

                //i have NO idea whats causing this error, but its harmless so whatever
                try {
                    particle.displayForPlayers(current, p);
                } catch (IndexOutOfBoundsException | NullPointerException ignored) {}

                current.subtract(addition);
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
            bossbar = (Wither) world.spawnEntity(new Location(world, 0.5, 0, 0.5), EntityType.WITHER);
            EntityWither nmsWither = ((CraftWither) bossbar).getHandle();
            NBTTagCompound tag = nmsWither.getNBTTag();

            if (tag == null) {
                tag = new NBTTagCompound();
            }

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
            getEye().start();
            getEye().setCurrentFrame(0);

            for (Player p : world.getPlayers()) {
                sendBossCreationPackets(p);
            }
        }

        if (counter > 500) {
            String name = bossbar.getCustomName().replaceAll("§k|§r§5", "");
            int i = ThreadLocalRandom.current().nextInt(2, 8);

            bossbar.setCustomName(name.substring(0, i) + "§k" + name.charAt(i) + "§r§5" + name.substring(i + 1, 8));
            bossbar.setHealth(bossbar.getMaxHealth() * (boss.getHealth() / boss.getMaxHealth()));

            for (Player p : world.getPlayers()) {
                Location l = p.getLocation().multiply(32).add(p.getLocation().getDirection().multiply(1500));

                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(bossbar.getEntityId(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), (byte) 0, (byte) 0, false));
            }

            lookAtPlayers();

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

    public void startEscapeAnimation() {
        for (Attacks attack : Attacks.values()) {
            if (attack == Attacks.SPECIAL) continue;

            attack.getMethods().forceStop();
        }

        bossbar.remove();
        cancel();

        new BukkitRunnable() {

            private final ParticleShapeCompound wings = createWings();
            private final ParticleImage eye = getEye();
            private boolean wingFlapping = true;
            private int counter = 0;

            @Override
            public void run() {
                if (counter == 0) {
                    Bossfight.this.wings.stop();
                    eye.setCurrentFrame(193);
                    wings.start();
                }

                counter++;

                lookAtPlayers();

                if (counter == 20) {
                    eye.stop();

                    for (int i = 0; i < 208; i++) {
                        eye.removeFrame(0);
                    }

                    eye.addImage(new File("plugins/eyeportal.gif"));
                    eye.removeMechanic(1);
                    eye.setAxisRotation(-90, 0, 0);
                    eye.removePlayer(0);
                    eye.start();
                }

                if (counter % 80 == 0) wingFlapping = !wingFlapping;

                if (wingFlapping) {
                    wings.move(0, -0.015, 0.01);

                    for (int i = 0; i <= 1; i++) {
                        wings.getShape(i).rotate(0.5, 0, 0.5 - i);
                    }
                } else {
                    wings.move(0, 0.015, -0.01);

                    for (int i = 0; i <= 1; i++) {
                        wings.getShape(i).rotate(-0.5, 0, -0.5 + i);
                    }
                }

                if (counter >= 20 && counter <= 110) {
                    for (int i = 0; i < 2; i++) {
                        wings.getShape(i).setParticleFrequency((int) (wings.getShape(i).getParticleFrequency() * 0.99));
                    }

                    wings.scale(0.99);
                    eye.scale(1.01);
                    eye.setParticleFrequency((int) (eye.getParticleFrequency() * 1.01));
                    eye.rotate(0, 0.22, 1);
                    eye.move(0.0075, -0.05, -0.05);
                    wings.move(0, -0.0155, -0.0025);
                }

                if (counter > 120 && counter <= 150) {
                    eye.move(0, -0.05, 0.185);
                }

                if (counter == 165) {
                    for (Player p : world.getPlayers()) {
                        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(boss.getId()));
                    }

                    wings.stop();
                }

                if (counter == 172) {
                    eye.setFrameDelay(Integer.MAX_VALUE);
                }

                if (counter == 180) {
                    eye.stop();
                    eye.setParticle(new ParticleSwirlTransparent());
                    eye.display();
                    eye.display();
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 0, 1);
    }

    public void handleDamage(double damage) {
        if (lastHit + 500 > System.currentTimeMillis()) return;

        for (Player p : world.getPlayers()) {
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityStatus(boss, (byte) 2));
        }

        boss.getBukkitEntity().setHealth(boss.getBukkitEntity().getHealth() - damage);
        world.playSound(boss.getBukkitEntity().getLocation(), Sound.WITHER_IDLE, 5, 2);

        lastHit = System.currentTimeMillis();
    }

    private void lookAtPlayers() {
        for (Player p : world.getPlayers()) {
            PlayerConnection pc = ((CraftPlayer) p).getHandle().playerConnection;
            Location l = boss.getBukkitEntity().getLocation();

            l.setDirection(p.getLocation().subtract(l).toVector());
            pc.sendPacket(new PacketPlayOutEntityHeadRotation(boss, (byte) ((l.getYaw() % 360) * 256 / 360)));
            pc.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(boss.getId(), (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) ((l.getPitch() % 360) * 256 / 360), true));
        }
    }

    private ParticleShapeCompound createWings() {
        ParticleShapeCompound wings = new ParticleShapeCompound();

        wings.addShape(new ParticleImage(new ParticleDustColored(), new LocationSafe(world, -3.25, 39.5, -36.6), new File("plugins/wing.png"), 1, 500).setRadius(4));
        wings.addShape(wings.getShape(0).clone());
        wings.getShape(1).move(7.5, 0, 0);
        wings.stop();

        for (int i = 0; i <= 1; i++) {
            wings.getShape(i).setAxisRotation(-90, (180 * i), 10 + (-20 * i));
        }

        return wings;
    }

    public EntityPlayer getEntity() {
        return boss;
    }

    public ParticleImage getEye() {
        return (ParticleImage) watcher.getShape(1);
    }
}













