package me.zelha.bossfight.listeners;

import me.zelha.bossfight.Bossfight;
import me.zelha.bossfight.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;

public class GeneralListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerInventory inv = p.getInventory();

        inv.clear();
        inv.addItem(new ItemStack(Material.DIAMOND_SWORD));
        inv.setHelmet(new ItemStack(Material.IRON_HELMET));
        inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        inv.setBoots(new ItemStack(Material.IRON_BOOTS));
        p.teleport(new Location(Bukkit.getWorld("zelha"), 0.5, 27, 20.5, 180, 0));
        p.setGameMode(GameMode.SURVIVAL);
        p.setSaturation(20);
        p.setHealth(20);
        Bossfight.sendBossCreationPackets(p);

        for (ItemStack item : inv.getArmorContents()) {
            ItemMeta meta = item.getItemMeta();

            meta.spigot().setUnbreakable(true);
            item.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!e.getEntity().getWorld().getName().equals("zelha")) return;

        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);

            return;
        }

        if (!(e.getEntity() instanceof Player)) {
            e.setCancelled(true);

            return;
        }

        Player p = (Player) e.getEntity();

        if (p.getHealth() - e.getFinalDamage() <= 0) {
            if (Main.getBossfight().getEntity() == null) {
                p.teleport(new Location(Bukkit.getWorld("zelha"), 0.5, 27, 20.5, 180, 0));
                p.setHealth(20);
            } else {
                p.setGameMode(GameMode.SPECTATOR);
            }

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location l = p.getLocation();

        if (!p.getWorld().getName().equals("zelha")) return;
        if (Math.pow(l.getX(), 2) + Math.pow(l.getZ(), 2) <= 6889) return;

        p.setVelocity(l.toVector().setY(0).multiply(-1));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!e.getEntity().getWorld().getName().equals("zelha")) return;
        if (!(e.getDamager() instanceof Player)) return;
        if (((Player) e.getDamager()).getTargetBlock((HashSet<Material>) null, 3).getType() == Material.AIR) return;

        Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent((Player) e.getDamager(), Action.LEFT_CLICK_AIR, ((Player) e.getDamager()).getItemInHand(), null, null));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getWorld().getName().equals("zelha")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFallingBlock(EntityChangeBlockEvent e) {
        if (e.getBlock().getWorld().getName().equals("zelha")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.getEntity().getWorld().getName().equals("zelha")) {
            e.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onArmorStand(PlayerArmorStandManipulateEvent e) {
        if (e.getPlayer().getWorld().getName().equals("zelha")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (e.getPlayer().getWorld().getName().equals("zelha") && e.getReason().equals("Flying is not enabled on this server")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent e) {
        if (e.getWorld().getName().equals("zelha")) {
            e.setCancelled(true);
        }
    }
}
