package me.zelha.bossfight.listeners;

import me.zelha.bossfight.Bossfight;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;

public class GeneralListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        p.teleport(new Location(Bukkit.getWorld("zelha"), 0.5, 27, 20.5, 180, 0));
        Bossfight.sendBossCreationPackets(p);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (e.getPlayer().getWorld().getName().equals("zelha") && e.getReason().equals("Flying is not enabled on this server")) {
            e.setCancelled(true);
        }
    }
}
