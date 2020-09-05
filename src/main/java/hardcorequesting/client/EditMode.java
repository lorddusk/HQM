package hardcorequesting.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

public enum EditMode {
    NORMAL("normal"),
    MOVE("move"),
    CREATE("create"),
    REQUIREMENT("req"),
    SIZE("size"),
    RENAME("rename"),
    ITEM("item"),
    TASK("task"),
    DELETE("delete"),
    SWAP("swap"),
    SWAP_SELECT("swapSelect"),
    TIER("tier"),
    BAG("bag"),
    LOCATION("location"),
    REPEATABLE("repeatable"),
    TRIGGER("trigger"),
    MOB("mob"),
    QUEST_SELECTION("questSelection"),
    QUEST_OPTION("questOption"),
    CHANGE_TASK("changeTask"),
    REQUIRED_PARENTS("reqParents"),
    REPUTATION("rep"),
    REPUTATION_VALUE("repValue"),
    REPUTATION_TASK("repTask"),
    REPUTATION_REWARD("repReward"),
    REP_BAR_CREATE("repBarCreate"),
    REP_BAR_CHANGE("repBarChange"),
    COMMAND_CREATE("commandCreate"),
    COMMAND_CHANGE("commandChange");
    
    private String id;
    
    EditMode(String id) {
        this.id = id;
    }
    
    @Environment(EnvType.CLIENT)
    public String getName() {
        return I18n.get("hqm.editMode." + id + ".title");
    }
    
    @Environment(EnvType.CLIENT)
    public String getDescription() {
        return I18n.get("hqm.editMode." + id + ".desc");
    }
}
