package me.zelha.bossfight;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import hm.zelha.particlesfx.shapers.ParticleImage;
import hm.zelha.particlesfx.util.ParticleSFX;
import net.minecraft.server.v1_8_R3.TileEntitySkull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Utils {
    public static ItemStack getCustomSkull(String base64) {
        World world = Bukkit.getWorld("zelha");
        Block block = new Location(world, 0, 13, 0).getBlock();

        block.setType(Material.AIR);
        block.setType(Material.SKULL);

        TileEntitySkull tileSkull = (TileEntitySkull) ((CraftWorld) world).getTileEntityAt(0, 13, 0);
        GameProfile profile = (tileSkull.getGameProfile() != null) ? tileSkull.getGameProfile() : new GameProfile(UUID.randomUUID(), "");

        profile.getProperties().put("textures", new Property("textures", base64));
        tileSkull.setGameProfile(profile);
        tileSkull.update();

        return block.getDrops().toArray(new ItemStack[0])[0];
    }

    public static void faceSlowly(ParticleImage toRotate, Location toFace, double speed) {
        double[] direction = ParticleSFX.getDirection(toFace, toRotate.getCenter());
        double pitchInc, yawInc;
        double pitch = toRotate.getPitch();
        double yaw = toRotate.getYaw();
        double wantedPitch = direction[0];
        double wantedYaw = direction[1];

        if (pitch + speed <= wantedPitch) {
            pitchInc = speed;
        } else if (pitch - speed >= wantedPitch) {
            pitchInc = -speed;
        } else {
            pitchInc = wantedPitch - pitch;
        }

        if (yaw + (360 - wantedYaw) < Math.abs(yaw - wantedYaw)) {
            yawInc = -speed;
        } else if (wantedYaw + (360 - yaw) < Math.abs(yaw - wantedYaw)) {
            yawInc = speed;
        } else if (yaw + speed <= wantedYaw) {
            yawInc = speed;
        } else if (yaw - speed >= wantedYaw) {
            yawInc = -speed;
        } else {
            yawInc = wantedYaw - yaw;
        }

        if (yaw + yawInc > 360) {
            yawInc = yawInc - 360;
        }

        if (yaw + yawInc < 0) {
            yawInc = yawInc + 360;
        }

        toRotate.rotate(pitchInc, yawInc, 0);
    }
}
