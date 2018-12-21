package hardcorequesting.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileBase extends TileEntity{
    
    @Override
    public final void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        this.readTile(compound, NBTType.SAVE);
    }
    
    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound compound){
        compound = super.writeToNBT(compound);
        this.writeTile(compound, NBTType.SAVE);
        return compound;
    }
    
    public void writeTile(NBTTagCompound nbt, NBTType type){}
    
    public void readTile(NBTTagCompound nbt, NBTType type){}
    
    public SPacketUpdateTileEntity getSyncPacket(){
        return new SPacketUpdateTileEntity(this.getPos(), -1, this.getUpdateTag());
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        if(pkt != null){
            pkt.getNbtCompound();
            this.receiveSyncPacket(pkt.getNbtCompound());
        }
    }
    
    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        return this.getSyncPacket();
    }
    
    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag(){
        return this.writeToNBT(new NBTTagCompound());
    }
    
    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag){
        this.readFromNBT(tag);
    }
    
    protected void receiveSyncPacket(@Nonnull NBTTagCompound nbt){
        this.readTile(nbt, NBTType.SYNC);
    }
    
    public void syncToClientsNearby(){
        if(!this.world.isRemote){
            for(EntityPlayer player : this.world.playerEntities){
                if(player instanceof EntityPlayerMP){
                    ((EntityPlayerMP) player).connection.sendPacket(this.getUpdatePacket());
                }
            }
        }
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
    
    public enum NBTType {
        SAVE,
        SYNC
    }
}
