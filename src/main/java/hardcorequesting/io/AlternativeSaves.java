package hardcorequesting.io;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.io.adapter.MinecraftAdapter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
public class AlternativeSaves {

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(NBTTagCompound.class, MinecraftAdapter.NBT_TAG_COMPOUND)
            .setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES).create();

    /* THis is for a newer version of the json save system
    public class ItemStackType{
        private NBTTagCompound stackAsNBT;
        public ItemStackType(ItemStack stack){
            this.stackAsNBT = stack.writeToNBT(new NBTTagCompound());
        }
        public ItemStack getStack(){
            if(stackAsNBT != null && stackAsNBT.hasNoTags()){
                return new ItemStack(stackAsNBT);
            }
            return ItemStack.EMPTY;
        }
    }
    */

    @Deprecated // TODO This should be replaced with the inner class above, while a json system reset
    public class ItemStackType{
        private String id;
        private int damage;
        private int amount;
        private NBTTagCompound nbt;
        public ItemStackType(ItemStack stack){
            this.id = stack.getItem().getRegistryName().toString();
            this.damage = stack.getItemDamage();
            this.amount = stack.getCount();
            this.nbt = stack.getTagCompound();
        }
        public ItemStack getStack(){
            return new ItemStack(Item.getByNameOrId(this.id), this.amount, this.damage, this.nbt);
        }
    }

    public class BagGroupType{
        private String id;
        private List<ItemStackType> items = new ArrayList<>();
        private String name;
        private int limit;
        public BagGroupType(Group group){
            this.id = group.getId();
            for(ItemStack stack : group.getItems()){
                items.add(new ItemStackType(stack));
            }
            this.name = group.getName();
            this.limit = group.getLimit();
        }
        public Group getBagGroup(){
            Group group = new Group(this.id);
            for(ItemStackType stackType : this.items){
                group.getItems().add(stackType.getStack());
            }
            group.setName(this.name);
            group.setLimit(this.limit);
            if(!Group.getGroups().containsKey(group)){
                Group.add(group);
            }
            return group;
        }
    }

    public class BagGroupTierType{
        private String name;
        private String colour;
        private int[] weights;
        private List<BagGroupType> groups;
        public BagGroupTierType(GroupTier tier){
            this.name = tier.getName();
            this.colour = tier.getColor().getName();
            this.weights = tier.getWeights();
            for(Group group : Group.getGroups().values()){
                if(group.getTier() == tier){
                    groups.add(new BagGroupType(group));
                }
            }
        }
        public GroupTier getGroupTier(){
            GroupTier tier = new GroupTier(this.name, GuiColor.valueOf(this.colour), this.weights);
            for(BagGroupType groupType : this.groups){
                groupType.getBagGroup().setTier(tier);
            }
            return tier;
        }
    }

}
