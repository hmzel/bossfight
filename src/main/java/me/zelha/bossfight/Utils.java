package me.zelha.bossfight;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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

        block.setType(Material.SKULL);

        TileEntitySkull tileSkull = (TileEntitySkull) ((CraftWorld) world).getTileEntityAt(0, 13, 0);
        GameProfile profile = (tileSkull.getGameProfile() != null) ? tileSkull.getGameProfile() : new GameProfile(UUID.randomUUID(), "");

        profile.getProperties().put("textures", new Property("textures", base64));
        tileSkull.setGameProfile(profile);
        tileSkull.update();

        return block.getDrops().toArray(new ItemStack[0])[0];
    }
}
