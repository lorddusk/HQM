package hardcorequesting.client.interfaces.edit;

import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.LargeButton;
import hardcorequesting.client.interfaces.TextBoxLogic;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestSet;
import hardcorequesting.quests.task.*;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reputation.ReputationMarker;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEditMenuTextEditor extends GuiEditMenu {

    private static final int TEXT_HEIGHT = 9;
    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int LINES_PER_PAGE = 21;
    protected TextBoxLogic text;
    private Quest quest;
    private QuestTask task;
    private QuestSet questSet;
    private Group group;
    private GroupTier groupTier;
    private Reputation reputation;
    private ReputationMarker reputationMarker;
    private int location = -1;
    private int mob = -1;
    private int tame = -1;
    private int advancementId = -1;
    private boolean isName;

    protected GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, String txt, boolean isName) {
        super(gui, player, false);
        if (txt != null && !txt.isEmpty()) {
            txt = txt.replace("\n", "\\n");
        }

        this.text = new TextBoxLogic(gui, txt, 140, true);
        this.isName = isName;
        buttons.add(new LargeButton("hqm.textEditor.copyAll", 185, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                GuiScreen.setClipboardString(text.getText());
            }
        });

        buttons.add(new LargeButton("hqm.textEditor.paste", 245, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                String clip = GuiScreen.getClipboardString();
                if (!clip.isEmpty()) {
                    clip = clip.replace("\n", "\\n");
                }
                text.addText(gui, clip);
            }
        });

        buttons.add(new LargeButton("hqm.textEditor.clear", 185, 40) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                text.setTextAndCursor(gui, "");
            }
        });

        buttons.add(new LargeButton("hqm.textEditor.clearPaste", 245, 40) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                String clip = GuiScreen.getClipboardString();
                if (!clip.isEmpty()) {
                    clip = clip.replace("\n", "\\n");
                }
                text.setTextAndCursor(gui, clip);
            }
        });
    }

    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, QuestTask task, boolean isName) {
        this(gui, player, isName ? task.getDescription() : task.getLongDescription(), isName);
        this.task = task;
    }

    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, Quest quest, boolean isName) {
        this(gui, player, isName ? quest.getName() : quest.getDescription(), isName);
        this.quest = quest;
    }

    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, QuestSet questSet, boolean isName) {
        this(gui, player, isName ? questSet.getName() : questSet.getDescription(), isName);
        this.questSet = questSet;
    }


    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player) {
        this(gui, player, Quest.getRawMainDescription(), false);
    }

    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, Group group) {
        this(gui, player, group.getName(), true);
        this.group = group;
    }


    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, GroupTier groupTier) {
        this(gui, player, groupTier.getName(), true);
        this.groupTier = groupTier;
    }

    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, QuestTaskLocation task, int id, QuestTaskLocation.Location location) {
        this(gui, player, location.getName(), true);
        this.task = task;
        this.location = id;
    }

    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, QuestTaskAdvancement task, int id, QuestTaskAdvancement.AdvancementTask advancement) {
        this(gui, player, advancement.getName(), true);
        this.task = task;
        this.advancementId = id;
    }

    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, QuestTaskTame task, int id, QuestTaskTame.Tame tame) {
        this(gui, player, tame.getName(), true);
        this.task = task;
        this.tame = id;
    }

    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, QuestTaskMob task, int id, QuestTaskMob.Mob mob) {
        this(gui, player, mob.getName(), true);
        this.task = task;
        this.mob = id;
    }

    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, Reputation reputation) {
        this(gui, player, reputation.getName(), true);
        this.reputation = reputation;
    }

    public GuiEditMenuTextEditor(GuiQuestBook gui, EntityPlayer player, ReputationMarker reputationMarker) {
        this(gui, player, reputationMarker.getName(), true);
        this.reputationMarker = reputationMarker;
    }


    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);
        int page = text.getCursorLine(gui) / LINES_PER_PAGE;
        gui.drawString(text.getLines(), page * LINES_PER_PAGE, LINES_PER_PAGE, START_X, START_Y, 1F, 0x404040);
        gui.drawCursor(START_X + text.getCursorPositionX(gui) - 1, START_Y + text.getCursorPositionY(gui) - 3 - page * LINES_PER_PAGE * TEXT_HEIGHT, 10, 1F, 0xFF909090);
    }

    @Override
    public void onKeyTyped(GuiBase gui, char c, int k) {
        super.onKeyTyped(gui, c, k);
        text.onKeyStroke(gui, c, k);
    }

    @Override
    public void save(GuiBase gui) {
        String str = text.getText();
        if (str == null || str.isEmpty()) {
            str = Translator.translate("hqm.textEditor.unnamed");
        }

        if (!isName && group == null && groupTier == null) {
            str = str.replace("\\n", "\n");
        }

        if (quest != null) {
            if (isName) {
                quest.setName(str);
            } else {
                quest.setDescription(str);
            }
        } else if (task != null) {
            if (location != -1) {
                while (gui.getStringWidth(str) > 110) {
                    str = str.substring(0, str.length() - 1);
                }
                ((QuestTaskLocation) task).setName(location, str, player);
            } else if (tame != -1) {
                while (gui.getStringWidth(str) > 110) {
                    str = str.substring(0, str.length() - 1);
                }
                ((QuestTaskTame) task).setName(tame, str, player);
            } else if (mob != -1) {
                while (gui.getStringWidth(str) > 110) {
                    str = str.substring(0, str.length() - 1);
                }
                ((QuestTaskMob) task).setName(mob, str, player);
            } else if (advancementId != -1) {
                 while (gui.getStringWidth(str) > 110) {
                     str = str.substring(0, str.length() -1);
                 }
                ((QuestTaskAdvancement) task).setName(advancementId, str, player);
            } else if (isName) {
                task.setDescription(str);
            } else {
                task.setLongDescription(str);
            }
        } else if (questSet != null) {
            if (isName) {
                if (!questSet.setName(str)) {
                    player.sendMessage(new TextComponentTranslation("hqm.editMode.rename.invalid_set").setStyle(new Style().setBold(true).setColor(TextFormatting.RED)));
                }
            } else {
                questSet.setDescription(str);
            }
        } else if (group != null) {
            group.setName(str);
        } else if (groupTier != null) {
            while (gui.getStringWidth(str) > 110) {
                str = str.substring(0, str.length() - 1);
            }
            groupTier.setName(str);
        } else if (reputation != null) {
            reputation.setName(str);
        } else if (reputationMarker != null) {
            reputationMarker.setName(str);
        } else {
            Quest.setMainDescription(str);
        }

        if (isName) {
            SaveHelper.add(SaveHelper.EditType.NAME_CHANGE);
        } else {
            SaveHelper.add(SaveHelper.EditType.DESCRIPTION_CHANGE);
        }
    }
}
