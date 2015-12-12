package hardcorequesting.client.interfaces;

import hardcorequesting.Translator;
import hardcorequesting.tileentity.PortalType;
import hardcorequesting.tileentity.TileEntityPortal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class GuiEditMenuPortal extends GuiEditMenuExtended {
    private TileEntityPortal portal;
    private GuiEditMenuPortal self = this;


    private static final int CHECK_BOX_X = 20;
    private static final int CHECK_BOX_Y = 110;
    private static final int CHECK_BOX_OFFSET = 12;

    public GuiEditMenuPortal(GuiBase gui, EntityPlayer player, TileEntityPortal portal) {
        super(gui, player, true, 20, 30, 20, 130);


        this.portal = portal.copy();

        buttons.add(new LargeButton("hqm.portalMenu.edit", 40, 80) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return !self.portal.getType().isPreset();
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                gui.setEditMenu(new GuiEditMenuItemPortal(gui, self, player, self.portal.getItem()));
            }
        });


        checkboxes.add(new CheckBox("hqm.portalMenu.collisionOnComplete", CHECK_BOX_X, CHECK_BOX_Y) {
            @Override
            public boolean getValue() {
                return self.portal.isCompletedCollision();
            }

            @Override
            public void setValue(boolean val) {
                self.portal.setCompletedCollision(val);
            }
        });

        checkboxes.add(new CheckBox("hqm.portalMenu.texOnComplete", CHECK_BOX_X, CHECK_BOX_Y + CHECK_BOX_OFFSET) {
            @Override
            public boolean getValue() {
                return self.portal.isCompletedTexture();
            }

            @Override
            public void setValue(boolean val) {
                self.portal.setCompletedTexture(val);
            }
        });

        checkboxes.add(new CheckBox("hqm.portalMenu.collisionNonComplete", CHECK_BOX_X, CHECK_BOX_Y + CHECK_BOX_OFFSET * 2) {
            @Override
            public boolean getValue() {
                return self.portal.isUncompletedCollision();
            }

            @Override
            public void setValue(boolean val) {
                self.portal.setUncompletedCollision(val);
            }
        });

        checkboxes.add(new CheckBox("Use textures when not completed", CHECK_BOX_X, CHECK_BOX_Y + CHECK_BOX_OFFSET * 3) {
            @Override
            public boolean getValue() {
                return self.portal.isUncompletedTexture();
            }

            @Override
            public void setValue(boolean val) {
                self.portal.setUncompletedTexture(val);
            }
        });
    }

    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);

        gui.drawCenteredString(portal.getCurrentQuest() != null ? portal.getCurrentQuest().getName() : Translator.translate("hqm.portalMenu.noQuest"), 0, 5, 1F, 170, 20, 0x404040);
        if (!portal.getType().isPreset()) {
            gui.drawItem(portal.getItem(), 20, 80, mX, mY, false);
        }

    }

    @Override
    protected void onArrowClick(boolean left) {
        if (left) {
            portal.setType(PortalType.values()[(portal.getType().ordinal() + PortalType.values().length - 1) % PortalType.values().length]);
        } else {
            portal.setType(PortalType.values()[(portal.getType().ordinal() + 1) % PortalType.values().length]);
        }
    }

    @Override
    protected String getArrowText() {
        return portal.getType().getName();
    }

    @Override
    protected String getArrowDescription() {
        return portal.getType().getDescription();
    }

    @Override
    protected void save(GuiBase gui) {

        portal.sendToServer();
    }

    public boolean doesRequiredDoublePage() {
        return false;
    }

    public void setItem(ItemStack item) {
        portal.setItem(item);
    }


}
