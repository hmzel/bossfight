package me.zelha.bossfight.listeners;

import me.zelha.bossfight.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParryListener implements Listener {

    private static final Map<Location, Player> parryMap = new HashMap<>();
    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (cooldownMap.containsKey(p.getUniqueId()) && cooldownMap.get(p.getUniqueId()) + 3000 > System.currentTimeMillis()) return;

        for (Location l : parryMap.keySet()) {
            Vector start = p.getLocation().add(0, 1.625, 0).toVector();
            Vector end = start.clone().add(p.getLocation().getDirection().multiply(3));
            Vector center = l.toVector();
            Vector a = start.clone().subtract(center);
            Vector b = end.clone().subtract(center);
            Vector c = start.clone().subtract(end);
            double r = p.getLocation().getPitch();
            boolean inCircle = false;

            if (start.distance(center) > r + 3) continue;

            if (a.length() < r || b.length() < r) {
                inCircle = true;
            } else if (a.angle(b) < Math.PI / 2.65) {
                continue;
            }

            if (a.length() * Math.sqrt(1 - Math.pow(a.dot(c) / (a.length() * c.length()), 2)) <= r) {
                inCircle = true;
            }

            if (!inCircle) continue;

            parryMap.put(l, p);

            break;
        }

        cooldownMap.put(p.getUniqueId(), System.currentTimeMillis());

        new BukkitRunnable() {
            @Override
            public void run() {
                cooldownMap.remove(p.getUniqueId());
            }
        }.runTaskLater(Main.getInstance(), 60);
    }

    public static Player getParryPlayer(Location l) {
        return parryMap.get(l);
    }

    public static void listenForParry(Location location, double radius) {
        location.setPitch((float) radius);

        parryMap.put(location, null);
    }

    public static void stopParryListening(Location l) {
        parryMap.remove(l);
    }
}





















