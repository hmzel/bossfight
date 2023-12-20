package me.zelha.bossfight.attacks;

import me.zelha.bossfight.Main;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntitySilverfish;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Attack {

    protected final World world = Bukkit.getWorld("zelha");
    private final List<BukkitRunnable> runnables = new ArrayList<>();
    private final Entity damageEntity = new EntitySilverfish(((CraftWorld) world).getHandle());
    private final boolean allowMultiple;
    protected int counter = 0;

    protected Attack(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public boolean run(int ticks) {
        if (!allowMultiple && isRunning()) {
            return false;
        }

        BukkitRunnable runnable = new BukkitRunnable() {

            private int counter = 0;

            @Override
            public void run() {
                attack();

                Attack.this.counter++;
                counter++;

                if (counter == ticks) {
                    runnables.remove(this);
                    cancel();

                    if (isRunning()) return;

                    reset();
                }
            }
        };

        runnables.add(runnable);
        runnable.runTaskTimer(Main.getInstance(), 0, 1);

        return true;
    }

    public void forceStop() {
        for (BukkitRunnable runnable : runnables) {
            runnable.cancel();
        }

        runnables.clear();
        reset();
    }

    public boolean isRunning() {
        return !runnables.isEmpty();
    }

    protected abstract void attack();

    protected void reset() {
        counter = 0;
    }

    /**
     * @return whether the boss was damaged
     */
    protected boolean damageNearby(Location l, double distance, double damage, @Nullable Location damageLocation) {
        for (Player p : world.getPlayers()) {
            if (l.distanceSquared(p.getLocation()) > Math.pow(distance, 2)) continue;

            if (damageLocation != null) {
                damageEntity.setPosition(damageLocation.getX(), damageLocation.getY(), damageLocation.getZ());
            } else {
                damageEntity.setPosition(l.getX(), l.getY(), l.getZ());
            }

            p.damage(damage, damageEntity.getBukkitEntity());
        }

        Attacks.getSpecialAttack().handleCubeDamage(l, distance);

        if (Main.getBossfight().getEntity() == null) return false;
        if (l.distanceSquared(Main.getBossfight().getEntity().getBukkitEntity().getLocation().add(0, 1, 0)) > Math.pow(distance, 2)) return false;

        return Main.getBossfight().handleDamage(damage, false);
    }

    protected Player getTarget() {
        List<Player> players = world.getPlayers();

        for (Player p : new ArrayList<>(players)) {
            if (p.getGameMode() == GameMode.SPECTATOR) {
                players.remove(p);
            }
        }

        if (players.isEmpty()) return null;

        return players.get(ThreadLocalRandom.current().nextInt(players.size()));
    }
}
