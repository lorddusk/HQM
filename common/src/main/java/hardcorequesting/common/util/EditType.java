package hardcorequesting.common.util;

import hardcorequesting.common.client.interfaces.GuiColor;
import net.minecraft.client.resources.language.I18n;

public enum EditType {
    QUEST_CREATE(BaseEditType.ADD, Type.QUEST),
    QUEST_REMOVE(BaseEditType.REMOVE, Type.QUEST),
    TASK_CREATE(BaseEditType.ADD, Type.TASK),
    TASK_REMOVE(BaseEditType.REMOVE, Type.TASK),
    TASK_CHANGE_TYPE(BaseEditType.ADD, Type.TASK_TYPE),
    REQUIREMENT_CHANGE(BaseEditType.CHANGE, Type.REQUIREMENT),
    REQUIREMENT_REMOVE(BaseEditType.REMOVE, Type.REQUIREMENT),
    REPEATABILITY_CHANGED(BaseEditType.CHANGE, Type.REPEATABILITY),
    VISIBILITY_CHANGED(BaseEditType.CHANGE, Type.VISIBILITY),
    PARENT_REQUIREMENT_CHANGED(BaseEditType.CHANGE, Type.PARENT),
    OPTION_CHANGE(BaseEditType.ADD, Type.OPTION),
    OPTION_REMOVE(BaseEditType.REMOVE, Type.OPTION),
    NAME_CHANGE(BaseEditType.CHANGE, Type.NAME),
    DESCRIPTION_CHANGE(BaseEditType.CHANGE, Type.DESCRIPTION),
    ICON_CHANGE(BaseEditType.CHANGE, Type.ICON),
    QUEST_SIZE_CHANGE(BaseEditType.CHANGE, Type.QUEST_SIZE),
    QUEST_MOVE(BaseEditType.MOVE, Type.QUEST),
    QUEST_CHANGE_SET(BaseEditType.MOVE, Type.BETWEEN_SETS),
    SET_CREATE(BaseEditType.ADD, Type.SET),
    SET_REMOVE(BaseEditType.REMOVE, Type.SET),
    REWARD_CREATE(BaseEditType.ADD, Type.REWARD),
    REWARD_CHANGE(BaseEditType.CHANGE, Type.REWARD),
    REWARD_REMOVE(BaseEditType.REMOVE, Type.REWARD),
    MONSTER_CREATE(BaseEditType.ADD, Type.MONSTER),
    MONSTER_CHANGE(BaseEditType.CHANGE, Type.MONSTER),
    MONSTER_REMOVE(BaseEditType.REMOVE, Type.MONSTER),
    LOCATION_CREATE(BaseEditType.ADD, Type.LOCATION),
    LOCATION_CHANGE(BaseEditType.CHANGE, Type.LOCATION),
    LOCATION_REMOVE(BaseEditType.REMOVE, Type.LOCATION),
    ADVANCEMENT_CREATE(BaseEditType.ADD, Type.ADVANCEMENT),
    ADVANCEMENT_CHANGE(BaseEditType.CHANGE, Type.ADVANCEMENT),
    ADVANCEMENT_REMOVE(BaseEditType.REMOVE, Type.ADVANCEMENT),
    COMPLETE_CHECK_CREATE(BaseEditType.ADD, Type.COMPLETION),
    COMPLETE_CHECK_CHANGE(BaseEditType.CHANGE, Type.COMPLETION),
    COMPLETE_CHECK_REMOVE(BaseEditType.REMOVE, Type.COMPLETION),
    TIER_CREATE(BaseEditType.ADD, Type.TIER),
    TIER_CHANGE(BaseEditType.CHANGE, Type.TIER),
    TIER_REMOVE(BaseEditType.REMOVE, Type.TIER),
    GROUP_CREATE(BaseEditType.ADD, Type.GROUP),
    GROUP_CHANGE(BaseEditType.CHANGE, Type.GROUP),
    GROUP_REMOVE(BaseEditType.REMOVE, Type.GROUP),
    GROUP_ITEM_CREATE(BaseEditType.ADD, Type.GROUP_ITEM),
    GROUP_ITEM_CHANGE(BaseEditType.REMOVE, Type.GROUP_ITEM),
    GROUP_ITEM_REMOVE(BaseEditType.REMOVE, Type.GROUP_ITEM),
    DEATH_CHANGE(BaseEditType.CHANGE, Type.DEATH),
    TASK_ITEM_CREATE(BaseEditType.ADD, Type.TASK_ITEM),
    TASK_ITEM_CHANGE(BaseEditType.CHANGE, Type.TASK_ITEM),
    TASK_ITEM_REMOVE(BaseEditType.REMOVE, Type.TASK_ITEM),
    REPUTATION_ADD(BaseEditType.ADD, Type.REPUTATION),
    REPUTATION_REMOVE(BaseEditType.REMOVE, Type.REPUTATION),
    REPUTATION_MARKER_CREATE(BaseEditType.ADD, Type.REPUTATION_MARKER),
    REPUTATION_MARKER_CHANGE(BaseEditType.CHANGE, Type.REPUTATION_MARKER),
    REPUTATION_MARKER_REMOVE(BaseEditType.REMOVE, Type.REPUTATION_MARKER),
    REPUTATION_TASK_CREATE(BaseEditType.ADD, Type.REPUTATION_TASK),
    REPUTATION_TASK_CHANGE(BaseEditType.CHANGE, Type.REPUTATION_TASK),
    REPUTATION_TASK_REMOVE(BaseEditType.REMOVE, Type.REPUTATION_TASK),
    REPUTATION_REWARD_CHANGE(BaseEditType.CHANGE, Type.REPUTATION_REWARD),
    KILLS_CHANGE(BaseEditType.CHANGE, Type.KILLS),
    REPUTATION_BAR_ADD(BaseEditType.ADD, Type.REPUTATION_BAR),
    REPUTATION_BAR_MOVE(BaseEditType.MOVE, Type.REPUTATION_BAR),
    REPUTATION_BAR_CHANGE(BaseEditType.CHANGE, Type.REPUTATION_BAR),
    REPUTATION_BAR_REMOVE(BaseEditType.REMOVE, Type.REPUTATION_BAR),
    COMMAND_ADD(BaseEditType.ADD, Type.COMMAND),
    COMMAND_CHANGE(BaseEditType.CHANGE, Type.COMMAND),
    COMMAND_REMOVE(BaseEditType.REMOVE, Type.COMMAND);
    
    private final BaseEditType basType;
    private final Type type;
    
    EditType(BaseEditType basType, Type type) {
        this.basType = basType;
        this.type = type;
    }
    
    public String translate(int number) {
        return basType.translate() + " " + type.translate() + ": " + basType.colour + number;
    }
    
    public enum BaseEditType {
        ADD("added", GuiColor.GREEN),
        CHANGE("changed", GuiColor.ORANGE),
        MOVE("moved", GuiColor.ORANGE),
        REMOVE("removed", GuiColor.RED);
        
        private final String id;
        private final GuiColor colour;
        
        BaseEditType(String id, GuiColor colour) {
            this.id = id;
            this.colour = colour;
        }
        
        public EditType with(Type type) {
            for (EditType editType : EditType.values()) {
                if (this == editType.basType && editType.type == type)
                    return editType;
            }
            throw new IllegalArgumentException("Type " + type + " does not have a base type " + this);
        }
        
        private String translate() {
            return I18n.get("hqm.editType." + id);
        }
    }
    
    public enum Type {
        QUEST("quest"),
        TASK("task"),
        TASK_TYPE("taskType"),
        REQUIREMENT("req"),
        REPEATABILITY("repeat"),
        VISIBILITY("vis"),
        PARENT("parent"),
        OPTION("option"),
        NAME("name"),
        DESCRIPTION("desc"),
        ICON("icon"),
        QUEST_SIZE("questSize"),
        SET("set"),
        REWARD("reward"),
        MONSTER("monster"),
        LOCATION("location"),
        TIER("tier"),
        GROUP("group"),
        GROUP_ITEM("groupItem"),
        DEATH("death"),
        TASK_ITEM("taskItem"),
        REPUTATION("rep"),
        REPUTATION_MARKER("repMark"),
        REPUTATION_TASK("repTask"),
        REPUTATION_REWARD("repReward"),
        KILLS("kills"),
        REPUTATION_BAR("repBar"),
        BETWEEN_SETS("betweenSets"),
        COMMAND("command"),
        ADVANCEMENT("advancement"),
        COMPLETION("questCompletion"),
        TASK_BLOCK("taskBlock");
        
        private final String id;
        
        Type(String id) {
            this.id = id;
        }
        
        private String translate() {
            return I18n.get("hqm.editType." + id);
        }
    }
}
