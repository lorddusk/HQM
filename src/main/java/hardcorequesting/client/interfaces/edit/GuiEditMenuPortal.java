package hardcorequesting.client.interfaces.edit;

public class GuiEditMenuPortal {}
/*
public class GuiEditMenuPortal extends GuiEditMenuExtended {
    
    private static final int CHECK_BOX_X = 20;
    private static final int CHECK_BOX_Y = 110;
    private static final int CHECK_BOX_OFFSET = 12;
    private PortalBlockEntity portal;
    private GuiEditMenuPortal self = this;
    
    public GuiEditMenuPortal(GuiBase gui, PlayerEntity player, PortalBlockEntity portal) {
        super(gui, player, true, 20, 30, 20, 130);
        
        
        this.portal = portal.copy();
        
        buttons.add(new LargeButton("hqm.portalMenu.edit", 40, 80) {
            @Override
            public boolean isEnabled(GuiBase gui, PlayerEntity player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, PlayerEntity player) {
                return !self.portal.getPortalType().isPreset();
            }
            
            @Override
            public void onClick(GuiBase gui, PlayerEntity player) {
                gui.setEditMenu(new GuiEditMenuItemPortal(gui, self, player, self.portal.getStack()));
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
        if (!portal.getPortalType().isPreset()) {
            gui.drawItemStack(portal.getStack(), 20, 80, mX, mY, false);
        }
        
    }
    
    @Override
    protected void onArrowClick(boolean left) {
        if (left) {
            portal.setPortalType(PortalType.values()[(portal.getPortalType().ordinal() + PortalType.values().length - 1) % PortalType.values().length]);
        } else {
            portal.setPortalType(PortalType.values()[(portal.getPortalType().ordinal() + 1) % PortalType.values().length]);
        }
    }
    
    @Override
    protected String getArrowText() {
        return portal.getPortalType().getName();
    }
    
    @Override
    protected String getArrowDescription() {
        return portal.getPortalType().getDescription();
    }
    
    @Override
    public void save(GuiBase gui) {
        
        portal.sendToServer();
    }
    
    public boolean doesRequiredDoublePage() {
        return false;
    }
    
    public void setItem(ItemStack stack) {
        portal.setStack(stack);
    }
    
    
}

*/