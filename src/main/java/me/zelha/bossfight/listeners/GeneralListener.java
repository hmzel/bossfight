package me.zelha.bossfight.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class GeneralListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().teleport(new Location(Bukkit.getWorld("zelha"), 0.5, 27, 0.5));
    }
}
