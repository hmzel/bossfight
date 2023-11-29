package me.zelha.bossfight.attacks;

import me.zelha.bossfight.Bossfight;
import me.zelha.bossfight.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Attack extends BukkitRunnable {

    protected final World world = Bukkit.getWorld("zelha");

    public void attack(int ticks) {
        runTaskTimer(Main.getInstance(), 0, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                Attack.this.cancel();
            }
        }.runTaskLater(Main.getInstance(), ticks);
    }

    protected void damageNearby(Location l, double distance, double damage, @Nullable Entity source) {
        List<Player> players = world.getPlayers();

        players.add(Bossfight.getEntity().getBukkitEntity());

        for (Player p : players) {
            if (l.distanceSquared(p.getLocation()) > Math.pow(distance, 2)) continue;

            p.damage(damage, source);
        }
    }
}
