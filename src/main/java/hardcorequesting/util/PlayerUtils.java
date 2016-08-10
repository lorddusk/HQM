package hardcorequesting.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;

import java.util.UUID;

public class PlayerUtils {

    public static EntityPlayerMP getPlayerFromUUID(UUID uuid) {
        ServerConfigurationManager configurationManager = MinecraftServer.getServer().getConfigurationManager();
        for (String username : configurationManager.getAllUsernames()) {
            EntityPlayerMP playerByUsername = configurationManager.getPlayerByUsername(username);
            if(playerByUsername.getUniqueID().equals(uuid))
                return playerByUsername;
        }
        return null;
    }

    public static void clearInv(EntityPlayer player) {
        InventoryPlayer inventory = player.inventory;
        for (int i = 0; i < inventory.mainInventory.length; i++) {
            inventory.mainInventory[i] = null;
        }
        for (int i = 0; i < inventory.armorInventory.length; i++) {
            inventory.armorInventory[i] = null;
        }
    }

}
