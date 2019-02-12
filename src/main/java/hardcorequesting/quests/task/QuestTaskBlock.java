package hardcorequesting.quests.task;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskItems;
import hardcorequesting.util.NBTCompareUtil;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

import java.lang.reflect.Method;
import java.util.Collection;

public abstract class QuestTaskBlock extends QuestTaskItems {
    public static Method getSilkTouchDrop = null;
    public static final String NULL_NAME = "item.null.name";

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

        ItemStack res = ItemStack.EMPTY;

        if (getSilkTouchDrop != null) {
            try {
                res = (ItemStack) getSilkTouchDrop.invoke(block, state);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        if (!res.isEmpty()) return res;

        return genericForState(state);
    }

    public static ItemStack genericForState (IBlockState state) {
        Block block = state.getBlock();

        int meta = 0;

        Item itemBlock = Item.getItemFromBlock(block);
        ItemStack res = ItemStack.EMPTY;
        if (itemBlock == Items.AIR) {
            // TODO: This assumption could fail dramaticlaly
            ItemBlockSpecial iBlock = new ItemBlockSpecial(block);
            if (iBlock.getHasSubtypes()) {
                meta = block.getMetaFromState(state);
            }

            res = new ItemStack(iBlock, 1, meta);
        } else {
            if (itemBlock.getHasSubtypes()) {
                meta = block.getMetaFromState(state);
            }

            res = new ItemStack(itemBlock, 1, meta);
        }

        if (res.isEmpty() || res.getDisplayName().equals(NULL_NAME)) {
            ItemBlock iBlock = new ItemBlock(block);
            if (iBlock.getHasSubtypes()) {
                meta = block.getMetaFromState(state);
            }

            res = new ItemStack(iBlock, 1, meta);
        }

        if (res.isEmpty() || res.getDisplayName().equals(NULL_NAME)) {
            // give up in despair
            return ItemStack.EMPTY;
        } else {
            return res;
        }
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

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        Block block = state.getBlock();

        NonNullList<ItemStack> consume = NonNullList.create();
        ItemStack drop = block.getPickBlock(state, null, world, pos, player);

        /* * * CUSTOM OVERRIDES * * * /
                 THESE ARE
                    EVIL
        /* * * * * * * * * * * * * * */
        if (state.getBlock() instanceof BlockFlowerPot) {
            drop = new ItemStack(Items.FLOWER_POT);
        }

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

