package me.zelha.bossfight.listeners;

import hm.zelha.particlesfx.particles.ParticleCloud;
import hm.zelha.particlesfx.particles.parents.Particle;
import me.zelha.bossfight.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DashListener implements Listener {

    private final Particle particle = new ParticleCloud(new Vector(), 1, 1.5, 1, 50);
    private final Set<UUID> cooldown = new HashSet<>();

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        if (p.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.AIR) return;
        if (e.getFrom().toVector().equals(e.getTo().toVector())) return;
        if (cooldown.contains(uuid)) return;
        if (!p.isBlocking()) return;
        if (!p.isSneaking()) return;

        p.setVelocity(new Location(p.getWorld(), e.getTo().getX(), e.getTo().getY(), e.getTo().getZ()).subtract(e.getFrom().toVector()).toVector().multiply(150).setY(0));
        particle.display(p.getLocation());
        cooldown.add(uuid);

        new BukkitRunnable() {
            @Override
            public void run() {
                cooldown.remove(uuid);
            }
        }.runTaskLater(Main.getInstance(), 60);
    }
}
