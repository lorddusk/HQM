package hardcorequesting.common.items;

import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.client.sounds.Sounds;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.death.DeathType;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ItemHeart extends Item {
    private int value;
    
    public ItemHeart(int value) {
        super(new Item.Properties()
                .stacksTo(64)
                .arch$tab(ModCreativeTabs.HQMTab));
        this.value = value;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            QuestingDataManager questingDataManager = QuestingDataManager.getInstance();
            ItemStack stack = player.getItemInHand(hand);
            if (value == 3) {
                if (!questingDataManager.isHardcoreActive()) {
                    player.sendSystemMessage(Translator.translatable("hqm.message.noHardcoreYet"));
                } else if (questingDataManager.getQuestingData(player).getRawLives() < HQMConfig.getInstance().Hardcore.MAX_LIVES) {
                    questingDataManager.getQuestingData(player).addLives(player, 1);
                    player.sendSystemMessage(Translator.translatable("hqm.message.addOne"));
                    int lives = questingDataManager.getQuestingData(player).getLives();
                    player.sendSystemMessage(Translator.translatable("hqm.message.haveRemaining", lives));
                    SoundHandler.play(Sounds.LIFE, player);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                        
                    }
                } else {
                    player.sendSystemMessage(Translator.translatable("hqm.message.haveMaxLives"));
                }
            }
            if (value == 4) {
                if (!questingDataManager.isHardcoreActive()) {
                    player.sendSystemMessage(Translator.translatable("hqm.message.noHardcoreYet"));
                } else {
                    SoundHandler.play(Sounds.ROTTEN, player);
                    player.sendSystemMessage(Translator.translatable("hqm.message.eatRottenHearth"));
                    questingDataManager.getQuestingData(player).removeLifeAndSendMessage(player);
                    DeathType.HQM.onDeath(player);
                    
                    if (!player.getAbilities().instabuild)
                        stack.grow(1);
                }
                
            }
            return InteractionResultHolder.success(stack);
        }
        return super.use(world, player, hand);
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (entity instanceof Player) {
            Player entityPlayer = (Player) entity;
            if (value == 3 && HQMConfig.getInstance().Hardcore.HEART_ROT_ENABLE) {
                CompoundTag tagCompound = stack.getTag();
                if (tagCompound == null) {
                    tagCompound = new CompoundTag();
                    stack.setTag(tagCompound);
                }
                if (!tagCompound.contains("RotTime")) {
                    int rot = (HQMConfig.getInstance().Hardcore.HEART_ROT_TIME * 20);
                    tagCompound.putInt("MaxRot", rot);
                    tagCompound.putInt("RotTime", rot);
                } else {
                    int newRot = tagCompound.getInt("RotTime");
                    if (newRot <= 0) {
                        // TODO who wrote this code lmao -bikeshedaniel
                        stack = new ItemStack(ModItems.rottenHeart.get());
                        entityPlayer.sendSystemMessage(Translator.translatable("hqm.message.hearthDecay"));
                    } else {
                        tagCompound.putInt("RotTime", newRot - 1);
                    }
                }
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        
        if (value == 3) {
            tooltip.add(Translator.translatable("item.hqm:hearts_heart.tooltip"));
            if (HQMConfig.getInstance().Hardcore.HEART_ROT_ENABLE) {
                CompoundTag tagCompound = stack.getTag();
                if (tagCompound == null) {
                    tagCompound = new CompoundTag();
                    stack.setTag(tagCompound);
                }
                if (tagCompound.contains("RotTime")) {
                    int rot = tagCompound.getInt("RotTime");
                    int maxRot = tagCompound.getInt("MaxRot");
                    float percentage = (float) ((rot * 100) / maxRot);
                    tooltip.add(Translator.translatable("item.hqm:hearts_heart.freshness", percentage));
                }
            }
        }
        if (value == 4) {
            tooltip.add(Translator.translatable("item.hqm:hearts_rottenheart.tooltip"));
        }
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return value == 3 || value == 4;
    }
}
