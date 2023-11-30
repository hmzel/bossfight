package me.zelha.bossfight.listeners;

import hm.zelha.particlesfx.particles.ParticleCloud;
import hm.zelha.particlesfx.particles.parents.Particle;
import me.zelha.bossfight.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AirJumpListener implements Listener {

    private final Particle particle = new ParticleCloud(new Vector(), 0.6, -0.4, 0.6, 50);
    private final Set<UUID> cooldown = new HashSet<>();

    @EventHandler
    public void onCrouch(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) return;

        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        if (cooldown.contains(uuid)) return;
        if (p.getLocation().subtract(0, 0.5, 0).getBlock().getType() != Material.AIR) return;

        p.setVelocity(p.getLocation().getDirection().multiply(2));
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
