package me.zelha.bossfight;

import com.mojang.authlib.GameProfile;
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

        boss.setPosition(0.5, 27, 0.5);

        for (Player p : world.getPlayers()) {
            PlayerConnection pc = ((CraftPlayer) p).getHandle().playerConnection;

            pc.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, boss));
            pc.sendPacket(new PacketPlayOutNamedEntitySpawn(boss));
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













