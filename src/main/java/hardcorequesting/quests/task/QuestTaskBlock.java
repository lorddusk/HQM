package hardcorequesting.quests.task;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskItems;
import hardcorequesting.util.NBTCompareUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public abstract class QuestTaskBlock extends QuestTaskItems {
    public static Method getSilkTouchDrop = null;

    public QuestTaskBlock(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);

        if (QuestTaskBlock.getSilkTouchDrop == null) {
            String methodName = "func_180643_i";

            if ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
                methodName = "getSilkTouchDrop";
            }
            try {
                getSilkTouchDrop = Block.class.getDeclaredMethod(methodName, IBlockState.class);
                getSilkTouchDrop.setAccessible(true);
            } catch (NoSuchMethodException e) {
                getSilkTouchDrop = null;
                HardcoreQuesting.LOG.error("Was unable to derive `getSilkTouchDrop`, block-related tasks will be at disadvantage.");
            }
        }
    }

    public static ItemStack itemForState (IBlockState state) {
        Block block = state.getBlock();

        if (getSilkTouchDrop != null) {
            try {
                return (ItemStack) getSilkTouchDrop.invoke(block, state);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        Item itemBlock = Item.getItemFromBlock(block);

        int meta = 0;

        if (itemBlock.getHasSubtypes()) {
            meta = block.getMetaFromState(state);
        }

        return new ItemStack(itemBlock, 1, meta);
    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskItems.class;
    }

    public abstract GuiEditMenuItem.Type getMenuTypeId();

    @Override
    public void onUpdate(EntityPlayer player) {
    }

    public void checkProgress(BlockEvent event, IBlockState state, EntityPlayer player) {
        // TODO: Unhappy with this solution
        Collection<IProperty<?>> keys = state.getPropertyKeys();

        if (keys.contains(BlockSlab.HALF)) {
            state = state.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);
        } else if (keys.contains(BlockDoor.HALF)) {
            state = state.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER);
        }

        NonNullList<ItemStack> consume = NonNullList.create();

        World world = event.getWorld();
        BlockPos pos = event.getPos();

        Block block = state.getBlock();

        ItemStack stateStack = itemForState(state);

        NonNullList<ItemStack> drops = NonNullList.create();
        block.getDrops(drops, world, pos, state, 0);

        ItemStack drop = ItemStack.EMPTY;

        for (ItemStack droppedStack : drops) {
            if (droppedStack.getItem() != stateStack.getItem()) continue;

            if (droppedStack.getMetadata() != stateStack.getMetadata()) continue;

            drop = droppedStack;
            break;
        }

        if (drops.size() == 1 && stateStack.isEmpty()) {
            drop = drops.get(0);
        }

        if (drop.isEmpty()) drop = stateStack;

        for (ItemRequirement ireq : items) {
            if (!ireq.hasItem) continue;

            ItemStack istack = ireq.getStack();

            if (istack.getItem() != drop.getItem() || istack.getMetadata() != drop.getMetadata()) continue;

            if (istack.hasTagCompound() && !drop.hasTagCompound()) continue; // rip

            if (!istack.hasTagCompound() && !drop.hasTagCompound()) {
                consume.add(istack.copy());
            } else if (NBTCompareUtil.arePartiallySimilar(drop.getTagCompound(), istack.getTagCompound())) {
                consume.add(istack.copy());
            }
        }

        if (consume.size() != 0) {
            increaseItems(consume, (QuestDataTaskItems) getData(player), player.getPersistentID());
        }
    }
}

