package hardcorequesting.quests.task;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTaskItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class QuestTaskItemsConsume extends QuestTaskItems {
    
    public QuestTaskItemsConsume(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
    
    public boolean increaseFluid(FluidVolume fluidVolume, QuestDataTaskItems data, UUID playerId) {
        boolean updated = false;
        
        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (item.fluid == null || item.required == data.progress[i]) {
                continue;
            }
            
            if (fluidVolume != null && fluidVolume.getRawFluid() != null && fluidVolume.canMerge(item.fluid)) {
                //System.out.println(stack.amount);
                int amount = Math.min(item.required - data.progress[i], fluidVolume.getAmount());
                data.progress[i] += amount;
                fluidVolume.split(amount);
                updated = true;
                break;
            }
        }
        
        
        if (updated) {
            doCompletionCheck(data, playerId);
        }
        
        return updated;
    }
    
    @Override
    public void onUpdate(PlayerEntity player) {
        if (increaseItems(player.inventory.main, (QuestDataTaskItems) getData(player), player.getUuid())) {
            player.inventory.markDirty();
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.CONSUME_TASK;
    }
    
    @Override
    public boolean allowManual() {
        return true;
    }
}
