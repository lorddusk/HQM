package hardcorequesting.common.items;

public class PortalBlockItem {}
/*
public class PortalBlockItem extends BlockItem {
    
    public PortalBlockItem(Block block, Item.Settings settings) {
        super(block, settings);
    }
    
    private String formatBoolean(boolean val) {
        return (val ? GuiColor.GREEN : GuiColor.GRAY) + String.valueOf(val);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        try {
            if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains("Portal")) {
                CompoundTag compound = stack.getTag().getCompound("Portal");
                if (compound.contains(PortalBlockEntity.NBT_QUEST + "Most")) {
                    Quest quest = Quest.getQuest(compound.getUuid(PortalBlockEntity.NBT_QUEST));
                    if (quest != null) {
                        tooltip.add(new LiteralText(GuiColor.GREEN + "Quest: " + quest.getName()));
                    } else {
                        tooltip.add(new LiteralText(GuiColor.GRAY + "No quest selected."));
                    }
                } else {
                    tooltip.add(new LiteralText(GuiColor.GRAY + "No quest selected."));
                }
                
                PortalType type = PortalType.values()[compound.getByte(PortalBlockEntity.NBT_TYPE)];
                tooltip.add(new LiteralText(GuiColor.ORANGE + "Type: " + type.getName()));
                if (!type.isPreset()) {
                    if (compound.contains(PortalBlockEntity.NBT_ITEM_STACK)) {
                        CompoundTag tag = compound.getCompound(PortalBlockEntity.NBT_ITEM_STACK);
                        
                        tooltip.add(new LiteralText(GuiColor.YELLOW + "Item: " + ItemStack.fromTag(tag).getName().asFormattedString()));
                    } else {
                        tooltip.add(new LiteralText(GuiColor.GRAY + "No stack selected."));
                    }
                }
                
                boolean completedCollision, completedTexture, uncompletedCollision, uncompletedTexture;
                if (compound.contains(PortalBlockEntity.NBT_COLLISION)) {
                    completedCollision = compound.getBoolean(PortalBlockEntity.NBT_COLLISION);
                    completedTexture = compound.getBoolean(PortalBlockEntity.NBT_COLLISION);
                    uncompletedCollision = compound.getBoolean(PortalBlockEntity.NBT_NOT_COLLISION);
                    uncompletedTexture = compound.getBoolean(PortalBlockEntity.NBT_NOT_TEXTURES);
                } else {
                    completedCollision = completedTexture = false;
                    uncompletedCollision = uncompletedTexture = true;
                }
                
                tooltip.add(new LiteralText(" "));
                tooltip.add(new LiteralText("Completed collision: " + formatBoolean(completedCollision)));
                tooltip.add(new LiteralText("Completed textures: " + formatBoolean(completedTexture)));
                tooltip.add(new LiteralText("Uncompleted collision: " + formatBoolean(uncompletedCollision)));
                tooltip.add(new LiteralText("Uncompleted textures: " + formatBoolean(uncompletedTexture)));
                
            }
        } catch (Exception ignored) {
        } //just to make sure it doesn't crash because it tries to get some weird quests or items or whatever
    }
}
 */