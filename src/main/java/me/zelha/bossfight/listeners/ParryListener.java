package me.zelha.bossfight.listeners;

import hm.zelha.particlesfx.util.LocationSafe;
import me.zelha.bossfight.Main;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParryListener implements Listener {

    private static final Map<BukkitRunnable, Pair<Location, Player>> parryMap = new HashMap<>();
    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        boolean hasParried = false;

        if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!p.getItemInHand().getType().name().contains("SWORD")) return;

        if (cooldownMap.containsKey(p.getUniqueId()) && cooldownMap.get(p.getUniqueId()) + 3000 > System.currentTimeMillis()) {
            String text = "{\"text\":\"§cParrying is on cooldown for " + (cooldownMap.get(p.getUniqueId()) + 3000 - System.currentTimeMillis()) / 1000D + "s\"}";

            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(text), (byte) 2));

            return;
        }

        for (BukkitRunnable runnable : parryMap.keySet()) {
            Location l = parryMap.get(runnable).getKey();
            Vector start = p.getLocation().add(0, 1.625, 0).toVector();
            Vector end = start.clone().add(p.getLocation().getDirection().multiply(3));
            Vector center = l.toVector();
            Vector a = start.clone().subtract(center);
            Vector b = end.clone().subtract(center);
            Vector c = start.clone().subtract(end);
            double r = l.getPitch();
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

            parryMap.put(runnable, Pair.of(l, p));
            p.getWorld().playSound(l, Sound.ANVIL_LAND, 1, 0.7f);

            hasParried = true;

            break;
        }

        cooldownMap.put(p.getUniqueId(), System.currentTimeMillis());

        if (!hasParried) {
            p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 1, 1.5f);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                cooldownMap.remove(p.getUniqueId());
            }
        }.runTaskLater(Main.getInstance(), 60);
    }

    public static Player getParryPlayer(BukkitRunnable runnable) {
        if (parryMap.get(runnable) == null) return null;

        return parryMap.get(runnable).getValue();
    }

    public static void listenForParry(BukkitRunnable runnable, LocationSafe location, double radius) {
        location.setPitch((float) radius);

        parryMap.put(runnable, Pair.of(location, null));
    }

    public static void stopParryListening(BukkitRunnable runnable) {
        parryMap.remove(runnable);
    }
}





















