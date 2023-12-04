package me.zelha.bossfight.attacks;

import me.zelha.bossfight.Bossfight;
import me.zelha.bossfight.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Attack {

    protected final World world = Bukkit.getWorld("zelha");
    protected final boolean allowMultiple;
    protected int counter = 0;

    protected Attack(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public BukkitTask run(int ticks) {
        if (!allowMultiple && counter != 0) {
            Attacks.randomAttack(ticks);

            return null;
        }

        return new BukkitRunnable() {
            @Override
            public void run() {
                attack();

                counter++;

                if (counter == ticks) {
                    cancel();
                    reset();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }

    protected abstract void attack();

    protected void reset() {
        counter = 0;
    }

    protected void damageNearby(Location l, double distance, double damage, @Nullable Entity source) {
        List<Player> players = world.getPlayers();

        players.add(Main.getBossfight().getEntity().getBukkitEntity());

        for (Player p : players) {
            if (l.distanceSquared(p.getLocation()) > Math.pow(distance, 2)) continue;

            p.damage(damage, source);
        }
    }
}
