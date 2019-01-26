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
import net.minecraft.init.Blocks;
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
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import static net.minecraft.block.BlockDoublePlant.EnumPlantType;

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

        ItemStack drop = ItemStack.EMPTY;

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        Block block = state.getBlock();

        /* Block of manual overrides:
            These aren't pretty but there's no way around them.
        */
        if (block instanceof BlockBed) {
            // Tile entity information needs to be fetched
            drop = ((BlockBed) block).getItem(world, pos, state);
        } else if (block instanceof BlockCauldron) {
            // The silkTouch and normal drops aren't sufficient for this
            drop = new ItemStack(Items.CAULDRON, 1, 0);
        } else if (block instanceof BlockSkull) {
            // While the drop provides accurate information,
            // the skull stored in the item stack does not
            // have the same item (Blocks.SKULL versus Items.SKULL)
            drop = ((BlockSkull) block).getItem(world, pos, state);
        }

        if (keys.contains(BlockSlab.HALF)) {
            state = state.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);
        } else if (keys.contains(BlockDoor.HALF)) {
            state = state.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER);
        } else if (keys.contains(BlockBed.PART)) {
            state = state.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD);
        } else if (keys.contains(BlockDoublePlant.HALF)) {
            EnumPlantType type = world.getBlockState(pos.down()).getValue(BlockDoublePlant.VARIANT);
            drop = new ItemStack(Blocks.DOUBLE_PLANT, 1, type.getMeta());
        }

        NonNullList<ItemStack> consume = NonNullList.create();
        NonNullList<ItemStack> drops = NonNullList.create();
        block.getDrops(drops, world, pos, state, 0);
        drops.removeIf(ItemStack::isEmpty);

        ItemStack stateStack = itemForState(state);

        if (drops.size() == 0) {
            state = world.getBlockState(pos.down());
            block.getDrops(drops, world, pos.down(), state, 0);
            drops.removeIf(ItemStack::isEmpty);
        }

        if (drop.isEmpty()) {
            for (ItemStack droppedStack : drops) {
                if (droppedStack.getItem() != stateStack.getItem()) continue;

                if (!stateStack.isItemStackDamageable() && droppedStack.getMetadata() != stateStack.getMetadata()) continue;

                drop = droppedStack;
                break;
            }

            if (drops.size() == 1 && stateStack.isEmpty()) {
                drop = drops.get(0);
            }

            if (drop.isEmpty()) {
                if (!stateStack.isEmpty()) {
                    drop = stateStack;
                } else {
                    drop = genericForState(state);
                }
            }

            if (drop.isEmpty()) {
                // wing and a prayer
                drop = drops.get(0);
            }
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

