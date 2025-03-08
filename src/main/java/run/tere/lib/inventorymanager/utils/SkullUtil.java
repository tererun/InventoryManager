package run.tere.lib.inventorymanager.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkullUtil {

    public static ItemStack createSkull(String urlStr, String name) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setDisplayName(name);
        itemStack.setItemMeta(skullMeta);

        PlayerProfile playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures texture = playerProfile.getTextures();
        try {
            URL url = new URL(urlStr);
            texture.setSkin(url);
            playerProfile.setTextures(texture);
            skullMeta.setOwnerProfile(playerProfile);
            itemStack.setItemMeta(skullMeta);
        } catch (Exception e) {
            e.printStackTrace();
            return itemStack;
        }
        return itemStack;
    }

    public static ItemStack createPlayerSkull(String playerName) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

}
