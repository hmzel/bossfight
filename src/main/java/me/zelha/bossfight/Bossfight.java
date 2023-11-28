package me.zelha.bossfight;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import hm.zelha.particlesfx.particles.ParticleExplosion;
import hm.zelha.particlesfx.shapers.ParticleSphere;
import hm.zelha.particlesfx.util.LocationSafe;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Bossfight extends BukkitRunnable {

    private static EntityPlayer boss = null;
    private final World world = Bukkit.getWorld("zelha");
//    private final ParticleShapeCompound watcher = new ParticleShapeCompound();
//    private final ParticleImage summoningCircle = new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0, 27.2, 0), new File("plugins/summoningcircle.png"), 5, 5, 1000);
    private final ParticleSphere sphere = (ParticleSphere) new ParticleSphere(new ParticleExplosion(10D), new LocationSafe(world, 0, 28, 0), 500, 500, 500, 4).setLimit(25).setLimitInverse(true).stop();
    private boolean started = false;
    private int counter = 0;

    public Bossfight() {
//        summoningCircle.rotate(180, 0, 0);
//        watcher.addShape(new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0, 33, 0), new File("plugins/outereye.png"), 9, 500));
//        watcher.addShape(new ParticleImage(new ParticleDustColored(), new LocationSafe(world, 0, 33, 0), new File("plugins/innereye.gif"), 2.5, 500).setRadius(3));

        boss = new EntityPlayer(MinecraftServer.getServer(), ((CraftWorld) world).getHandle(), new GameProfile(UUID.randomUUID(), ""), new PlayerInteractManager(((CraftWorld) world).getHandle()));

        boss.getProfile().getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTcwMTEzNjMyNDM1MSwKICAicHJvZmlsZUlkIiA6ICI0MzJhNmI2MDA4YmY0NTFhOGYzMmUxMTljYWUxOGZkMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJsZXRza2lzcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81NGJiODkyYTg0N2RmNDVlOWI2ZmUxMmNiNjQ0NWJjYTEzY2QwOTY0ZWI5MTI4Y2U3MTEwZWJmM2JhNjJkYWZmIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=", "keikW3GamW9B073uLLKyHamiIE5VenU89G0wlm0zn6yNOu8G/hPujeWh84AC4v5R46cSu1C1aYirICvSdiO2tIPIckFQxkXc9vF6KI4X67kuZTiomPX8NJgc/S7NJYnZPoqdqvYiYUnfAZ05crkf9jt93gyvqhMe19RnWO7/OVU1mHmrQjvqyTHw2Rhz27misfrbv72FBrx2lP6xO+/4mkbME4VoYLDt57oq4XUMCAQUnK6+Msdej0p2fMAv+TNz1Ximns/64mp6qdzXT1iCpMwp/TPzd3oL8sHTl1PsykrrIt9BEotEjBoiwY09YWtAjqyorxMTSB2faSDB1zLfG2O2dtzni3XmWQPWZN2riVJFOqcyO7hqHj36iBTmT8IsFbESSqBDDGIgp2FlBqBt2DIVEz6MgjjEucJNFCrUZ+baNE2yxTVG3q6pAck+RX578vnwDTn+A9ixQtjub0Z0JaOVRJFEiwjviGaFlIqMVF3T3n6QrDKJkyaaCB6PqegoNTEYgVwkvS0c1EF6lyTPcJL+8M08SyGAccLlGUHJbagEZZf7D3xkiY2H9vbpsXNxpRuJ7vPrjUzHR2Vs0Qc8qC5yGFqvfI2QyQtMbJYvKWgacqhGBO5gbVdFtyfG7xyjcDJMiOO9U3sYPgnqn44baAvHuVdS+WCLUdfRTcVBmPE="));
        boss.setPosition(0.5, 27, 0.5);
        boss.getDataWatcher().watch(10, (byte) 127);

        for (Player p : world.getPlayers()) {
            PlayerConnection pc = ((CraftPlayer) p).getHandle().playerConnection;

            pc.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, boss));
            pc.sendPacket(new PacketPlayOutNamedEntitySpawn(boss));
            pc.sendPacket(new PacketPlayOutEntityMetadata(boss.getId(), boss.getDataWatcher(), true));
//            pc.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, boss));
        }
    }

    @Override
    public void run() {
//        if (!started && !world.getNearbyEntities(summoningCircle.getCenter(), 5, 5, 5).isEmpty()) {
//            started = true;
//
//            summoningCircle.setParticleFrequency(2000);
//            sphere.start();
//            watcher.stop();
//        }
//
//        if (!started) {
//            Player p = null;
//            double distance = Double.MAX_VALUE;
//
//            for (Player player : world.getPlayers()) {
//                if (player.getLocation().distanceSquared(summoningCircle.getCenter()) < distance) {
//                    p = player;
//                    distance = player.getLocation().distanceSquared(summoningCircle.getCenter());
//                }
//            }
//
//            if (p != null) {
//                watcher.face(p.getLocation());
//            }
//
//            return;
//        }
//
//        counter++;
//
//        if (counter < 500) {
//            summoningCircle.rotate(0, (counter / 10D), 0);
//            summoningCircle.setXRadius(summoningCircle.getXRadius() + 0.04);
//            summoningCircle.setZRadius(summoningCircle.getZRadius() + 0.04);
//            sphere.setParticleFrequency(sphere.getParticleFrequency() + 4);
//            sphere.rotate(0, (counter / 10D), 0);
//            sphere.setXRadius(sphere.getXRadius() - 1);
//            sphere.setYRadius(sphere.getYRadius() - 1);
//            sphere.setZRadius(sphere.getZRadius() - 1);
//        }
//
//        if (counter % 15 == 0 && counter <= 470) {
//            world.playSound(summoningCircle.getCenter(), Sound.ZOMBIE_UNFECT, 100, 0);
//        }
//
//        if (counter == 500) {
//            sphere.stop();
//            summoningCircle.stop();
//            world.playSound(summoningCircle.getCenter(), Sound.WITHER_DEATH, 100, 0);
//        }
    }

    public static EntityPlayer getEntity() {
        return boss;
    }
}













