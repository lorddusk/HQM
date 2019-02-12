package hqm.net;

import hqm.HQM;
import hqm.api.IQuestbook;
import hqm.client.gui.GuiQuestbook;
import hqm.io.IOHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("Duplicates")
public class NetManager{
    
    public static final SimpleNetworkWrapper SNW = new SimpleNetworkWrapper(HQM.MODNAME);
    public static final int CS_OPEN_BOOK = 1; // Client -> Server when the player wants to open a questbook
    public static final int SC_OPEN_BOOK = 2; // Server -> Client response for message 1, when the book can be opened
    
    public static void init(){
        SNW.registerMessage(PacketCompund.class, PacketCompund.class, 0, Side.SERVER);
        SNW.registerMessage(PacketCompund.class, PacketCompund.class, 1, Side.CLIENT);
    }
    
    private static void sendNBTPacketToServer(int id, EntityPlayer sender, Consumer<NBTTagCompound> consumer){
        NBTTagCompound data = new NBTTagCompound();
        consumer.accept(data);
        data.setUniqueId("PacketSender", sender.getPersistentID());
        data.setInteger("PacketSenderWorld", sender.getEntityWorld().provider.getDimension());
        SNW.sendToServer(new PacketCompund(id, data));
    }
    
    private static void sendNBTPacketToPlayer(int id, EntityPlayer receiver, Consumer<NBTTagCompound> consumer){
        if(receiver instanceof EntityPlayerMP){
            NBTTagCompound data = new NBTTagCompound();
            consumer.accept(data);
            data.setUniqueId("PacketSender", receiver.getUniqueID());
            data.setInteger("PacketSenderWorld", receiver.getEntityWorld().provider.getDimension());
            SNW.sendTo(new PacketCompund(id, data), (EntityPlayerMP) receiver);
        }
    }
    
    static void onNBTPacket(int id, @Nonnull NBTTagCompound data){
        EntityPlayer sender = null;
        World world = DimensionManager.getWorld(data.getInteger("PacketSenderWorld"));
        UUID senderId = data.getUniqueId("PacketSender");
        if(world != null && senderId != null){
            sender = world.getPlayerEntityByUUID(senderId);
        }
        if(sender == null){
            System.out.println("Packet can't be processed, since the player doesn't exist on one side!");
            return;
        }
        switch(id){
            case CS_OPEN_BOOK: {
                if(data.hasKey("QuestbookMost", Constants.NBT.TAG_LONG) && data.hasKey("QuestbookLeast", Constants.NBT.TAG_LONG)){
                    onQuestbookOpeningRequest(sender, data.getUniqueId("Questbook"));
                }
                break;
            }
            case SC_OPEN_BOOK: {
                if(data.hasKey("QuestbookMost", Constants.NBT.TAG_LONG) && data.hasKey("QuestbookLeast", Constants.NBT.TAG_LONG)){
                    onQuestbookOpeningGranted(data.getUniqueId("Questbook"));
                }
            }
        }
    }
    
    /*
        Space for all the packets that have to construct themself
     */
    // client -> server
    public static void requestQuestbookOpening(@Nonnull EntityPlayer player, @Nonnull UUID questbookId){
        sendNBTPacketToServer(CS_OPEN_BOOK, player, nbt -> nbt.setUniqueId("Questbook", questbookId));
    }
    
    public static void onQuestbookOpeningRequest(@Nonnull EntityPlayer player, @Nonnull UUID questbookId){
        IQuestbook questbook = IOHandler.getQuestbook(questbookId);
        if(questbook != null){
            if(questbook.canExist(player.getEntityWorld())){
                grantQuestbookOpening(player, questbookId);
            } else {
                player.sendMessage(new TextComponentString("Questbook can not be opened in this dimension!"));
            }
        }
    }
    
    // server -> client
    public static void grantQuestbookOpening(@Nonnull EntityPlayer player, @Nonnull UUID questbookId){
        sendNBTPacketToPlayer(SC_OPEN_BOOK, player, nbt -> nbt.setUniqueId("Questbook", questbookId));
    }
    
    public static void onQuestbookOpeningGranted(@Nonnull UUID questbookId){
        IQuestbook questbook = IOHandler.getQuestbook(questbookId);
        if(questbook != null){
            Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new GuiQuestbook(questbook)));
        }
    }
    
}
