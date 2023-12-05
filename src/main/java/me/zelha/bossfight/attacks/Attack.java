package me.zelha.bossfight.attacks;

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
import java.util.concurrent.ThreadLocalRandom;

public abstract class Attack {

    protected final World world = Bukkit.getWorld("zelha");
    protected final boolean allowMultiple;
    protected int counter = 0;
    private int running = 0;

    protected Attack(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public BukkitTask run(int ticks) {
        if (!allowMultiple && running > 0) {
            Attacks.randomAttack(ticks);

            return null;
        }

        running++;

        return new BukkitRunnable() {

            private int counter = 0;

            @Override
            public void run() {
                attack();

                Attack.this.counter++;
                counter++;

                if (counter == ticks) {
                    cancel();

                    running--;

                    if (running != 0) return;

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
        for (Player p : world.getPlayers()) {
            if (l.distanceSquared(p.getLocation()) > Math.pow(distance, 2)) continue;

            p.damage(damage, source);
        }

        if (l.distanceSquared(Main.getBossfight().getEntity().getBukkitEntity().getLocation()) > Math.pow(distance, 2)) return;

        Main.getBossfight().handleDamage(damage);
    }

    protected Player getTarget() {
        List<Player> players = world.getPlayers();

        if (players.isEmpty()) return null;

        return players.get(ThreadLocalRandom.current().nextInt(players.size()));
    }
}
