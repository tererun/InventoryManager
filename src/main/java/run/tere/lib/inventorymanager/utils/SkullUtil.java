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
            String prepare = new String(Base64.getDecoder().decode(urlStr));
            Matcher matcher = Pattern.compile("[\"{a-zA-Z:]+(http://[a-zA-Z0-9./]+)[}\"]+").matcher(prepare);
            if (!matcher.matches()) return itemStack;
            URL url = new URL(matcher.group(1));
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

}
