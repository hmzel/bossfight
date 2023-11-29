package me.zelha.bossfight;

import hm.zelha.particlesfx.util.ParticleSFX;
import me.zelha.bossfight.listeners.GeneralListener;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        ParticleSFX.setPlugin(this);
        getServer().getPluginManager().registerEvents(new GeneralListener(), this);

        World world = new WorldCreator("zelha").type(WorldType.FLAT).generatorSettings("3;minecraft:air;9").generateStructures(false).createWorld();

        //using this to check if the world has been generated before
        if (world.getTime() != 18013) {
            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setTime(18013);

            //the .5 helps smooth out the circle
            double y = 26.5, x = -y, z = -y;

            //generating central platform (ew, numbers)
            while (y >= 1) {
                if (-x * -x + -z * -z <= y * y) {
                    if (!world.getChunkAt((int) x, (int) z).isLoaded()) {
                        world.loadChunk((int) x, (int) z);
                    }

                    world.getBlockAt((int) x, (int) y, (int) z).setType(Material.ENDER_STONE);
                }

                z++;

                if (z > y) {
                    z = -y;
                    x++;
                }

                if (x > y) {
                    y--;
                    x = -y;
                    z = -y;
                }
            }

            double complete = 0;

            //generating the outer ring
            for (double i = Math.PI; i >= Math.PI - 10; i -= 0.01) {
                Location loc = new Location(world, (100 * Math.sin(i)), y, (100 * Math.cos(i)));
                boolean halfDone = false;
                double r = 0.5;
                x = loc.getBlockX() - r;
                z = loc.getZ() - r;
                y = 41;

                while (y != 11) {
                    if ((loc.getBlockX() - x) * (loc.getBlockX() - x) + (loc.getBlockZ() - z) * (loc.getBlockZ() - z) <= r * r) {
                        if (!world.getChunkAt((int) x, (int) z).isLoaded()) {
                            world.loadChunk((int) x, (int) z);
                        }

                        world.getBlockAt((int) x, (int) y, (int) z).setType(Material.ENDER_STONE);
                    }

                    z++;

                    if (z > loc.getZ() + r) {
                        z = loc.getZ() - r;
                        x++;
                    }

                    if (x > loc.getX() + r) {
                        if (halfDone) r--; else r++;

                        y--;
                        x = loc.getX() - r;
                        z = loc.getZ() - r;

                        if (r >= 15.5) halfDone = true;
                    }
                }

                complete += 0.1;
                System.out.println(complete + "% complete");
            }
        }

        for (Player player : getServer().getOnlinePlayers()) {
            Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, null));
        }

        new Bossfight().runTaskTimer(this, 0, 1);
    }

    @Override
    public void onDisable() {
        if (Bossfight.getEntity() != null) {
            for (Player p : Bukkit.getWorld("zelha").getPlayers()) {
                PlayerConnection pc = ((CraftPlayer) p).getHandle().playerConnection;

                pc.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, Bossfight.getEntity()));
                pc.sendPacket(new PacketPlayOutEntityDestroy(Bossfight.getEntity().getId()));
            }
        }
    }

    public static Main getInstance() {
        return instance;
    }
}
