package hardcorequesting.items;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.death.DeathType;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.util.Translator;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class ItemHeart extends Item {
    private int value;
    
    public ItemHeart(int value) {
        super(new Item.Settings()
                .maxCount(64)
                .group(HardcoreQuesting.HQMTab));
        this.value = value;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            ItemStack stack = player.getStackInHand(hand);
            if (value == 3) {
                if (!QuestingData.isHardcoreActive()) {
                    player.sendMessage(new TranslatableText("hqm.message.noHardcoreYet"));
                } else if (QuestingData.getQuestingData(player).getRawLives() < HQMConfig.getInstance().Hardcore.MAX_LIVES) {
                    QuestingData.getQuestingData(player).addLives(player, 1);
                    player.sendMessage(new TranslatableText("hqm.message.addOne"));
                    int lives = QuestingData.getQuestingData(player).getLives();
                    player.sendMessage(new TranslatableText("hqm.message.haveRemaining", lives));
                    SoundHandler.play(Sounds.LIFE, player);
                    if (!player.abilities.creativeMode) {
                        stack.decrement(1);
                        
                    }
                } else {
                    player.sendMessage(new TranslatableText("hqm.message.haveMaxLives"));
                }
            }
            if (value == 4) {
                if (!QuestingData.isHardcoreActive()) {
                    player.sendMessage(new TranslatableText("hqm.message.noHardcoreYet"));
                } else {
                    SoundHandler.play(Sounds.ROTTEN, player);
                    player.sendMessage(new TranslatableText("hqm.message.eatRottenHearth"));
                    QuestingData.getQuestingData(player).removeLifeAndSendMessage(player);
                    DeathType.HQM.onDeath(player);
                    
                    if (!player.abilities.creativeMode)
                        stack.increment(1);
                }
                
            }
            return TypedActionResult.success(stack);
        }
        return super.use(world, player, hand);
    }
    
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity entityPlayer = (PlayerEntity) entity;
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
                        stack = new ItemStack(ModItems.rottenHeart);
                        entityPlayer.sendMessage(new TranslatableText("hqm.message.hearthDecay"));
                    } else {
                        tagCompound.putInt("RotTime", newRot - 1);
                    }
                }
            }
        }
    }
    
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        
        if (value == 3) {
            tooltip.add(new LiteralText(Translator.translate("item.hqm:hearts_heart.tooltip")));
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
                    tooltip.add(new LiteralText(Translator.translate("item.hqm:hearts_heart.freshness", percentage)));
                }
            }
        }
        if (value == 4) {
            tooltip.add(new LiteralText(Translator.translate("item.hqm:hearts_rottenheart.tooltip")));
        }
    }
    
    @Override
    public boolean hasEnchantmentGlint(ItemStack stack) {
        return value == 3 || value == 4;
    }
}
