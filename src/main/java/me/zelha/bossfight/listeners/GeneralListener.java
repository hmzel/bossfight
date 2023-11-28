package me.zelha.bossfight.listeners;

import me.zelha.bossfight.Bossfight;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class GeneralListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().teleport(new Location(Bukkit.getWorld("zelha"), 0.5, 27, 20.5, 180, 0));

        if (Bossfight.getEntity() != null) {

        }
    }
}
