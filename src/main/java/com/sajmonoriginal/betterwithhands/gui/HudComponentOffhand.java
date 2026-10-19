package com.sajmonoriginal.betterwithhands.gui;

import com.sajmonoriginal.betterwithhands.client.HandIndicator;
import com.sajmonoriginal.betterwithhands.option.IndicatorType;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.hud.HudIngame;
import net.minecraft.client.gui.hud.component.ComponentAnchor;
import net.minecraft.client.gui.hud.component.HudComponentMovable;
import net.minecraft.client.gui.hud.component.HudComponents;
import net.minecraft.client.gui.hud.component.layout.Layout;
import net.minecraft.client.gui.hud.component.layout.LayoutSnap;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.render.Lighting;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.renderer.BlendFactor;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.LightIndexHelper;

@Environment(EnvType.CLIENT)
public class HudComponentOffhand extends HudComponentMovable {
    private static final String SLOT_SPRITE = "better_with_hands:gui/hud/offhand_slot";
    private static final int SLOT_SIZE = 22;
    private static final int HOTBAR_GAP = 4;
    private static final long EQUIP_ANIMATION_MS = 300L;

    private ItemStack previousOffhand = null;
    private ItemStack shownOffhand = null;
    private long animationStart = 0L;
    private boolean animatingIn = false;

    public HudComponentOffhand(String key, Layout layout) {
        super(key, SLOT_SIZE, SLOT_SIZE, layout);
    }

    public static LayoutSnap defaultLayout(boolean leftHanded) {
        return leftHanded
            ? new LayoutSnap(HudComponents.HOTBAR, ComponentAnchor.CENTER_RIGHT, ComponentAnchor.CENTER_LEFT, HOTBAR_GAP, 0)
            : new LayoutSnap(HudComponents.HOTBAR, ComponentAnchor.CENTER_LEFT, ComponentAnchor.CENTER_RIGHT, -HOTBAR_GAP, 0);
    }

    private static boolean isDefaultLayout(Layout layout, boolean leftHanded) {
        LayoutSnap expected = defaultLayout(leftHanded);
        return layout instanceof LayoutSnap snap
            && snap.getParent() == expected.getParent()
            && snap.getParentAnchor() == expected.getParentAnchor()
            && snap.getAnchor() == expected.getAnchor()
            && snap.getXOffset() == expected.getXOffset()
            && snap.getYOffset() == expected.getYOffset();
    }

    private void followHandedness() {
        boolean leftHanded = GameSettings.PLAYER_LEFT_HANDED.value;
        if (isDefaultLayout(getLayout(), !leftHanded)) {
            setLayout(defaultLayout(leftHanded));
        }
    }

    @Override
    public boolean isVisible() {
        return GameSettings.IMMERSIVE_MODE.drawHotbar() && mc.thePlayer != null
            && ((OffhandCapable) mc.thePlayer.inventory).betterWithHands$isOffhandEnabled();
    }

    @Override
    public void render(HudIngame hud, int xSizeScreen, int ySizeScreen, float partialTick) {
        Player player = mc.thePlayer;
        if (player == null || !((OffhandCapable) player.inventory).betterWithHands$isOffhandEnabled()) {
            return;
        }
        followHandedness();

        ItemStack offhand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        if (!isSameItem(offhand, this.previousOffhand)) {
            this.animationStart = System.currentTimeMillis();
            this.animatingIn = offhand != null;
        }
        this.previousOffhand = offhand;
        if (offhand != null) {
            this.shownOffhand = offhand;
        }

        float animation = Math.min(1.0F, (float) (System.currentTimeMillis() - this.animationStart) / EQUIP_ANIMATION_MS);
        float progress = this.animatingIn ? animation : 1.0F - animation;
        if (progress <= 0.01F && offhand == null) {
            this.shownOffhand = null;
        }

        int x = getLayout().getComponentX(this, xSizeScreen);
        int y = getLayout().getComponentY(this, ySizeScreen);
        if (progress > 0.01F) {
            int slotY = y + (int) ((1.0F - progress) * SLOT_SIZE);
            GLRenderer.pushFrame();
            GLRenderer.enableState(State.BLEND);
            GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
            GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, progress);
            hud.drawGuiIcon(x, slotY, SLOT_SIZE, SLOT_SIZE, TextureRegistry.getTexture(SLOT_SPRITE));
            GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (this.shownOffhand != null) {
                renderItem(this.shownOffhand, x + 3, slotY + 3, partialTick);
            }
            GLRenderer.popFrame();
        }

        if (HandIndicator.highlightOffhand(player)) {
            drawIndicator(hud, x - 1, y - 1);
        }
    }

    @Override
    public void renderPreview(Gui gui, Layout layout, int xSizeScreen, int ySizeScreen) {
        followHandedness();
        int x = layout.getComponentX(this, xSizeScreen);
        int y = layout.getComponentY(this, ySizeScreen);
        GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        gui.drawGuiIcon(x, y, SLOT_SIZE, SLOT_SIZE, TextureRegistry.getTexture(SLOT_SPRITE));
        if (HandIndicator.type() != IndicatorType.NONE) {
            drawIndicator(gui, x - 1, y - 1);
        }
    }

    private static void drawIndicator(Gui gui, int x, int y) {
        TessellatorGeneral tessellator = GLRenderer.getTessellator();
        GLRenderer.pushFrame();
        GLRenderer.enableState(State.BLEND);
        GLRenderer.setBlendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
        GLRenderer.setColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        TextureRegistry.guiSpriteAtlas.bind();
        tessellator.startDrawingQuads();
        HandIndicator.addIndicator(gui, tessellator, x, y);
        tessellator.draw();
        GLRenderer.popFrame();
    }

    private static void renderItem(ItemStack stack, int x, int y, float partialTick) {
        ItemModel model = ItemModelDispatcher.getInstance().getDispatch(stack.getItem());
        GLRenderer.enableState(State.DEPTH_TEST);
        Lighting.enableInventoryLight();
        float bounce = stack.animationsToGo - partialTick;
        if (bounce > 0.0F) {
            float scale = 1.0F + bounce / 5.0F;
            GLRenderer.pushFrame();
            GLRenderer.modelM4f().translate(x + 8, y + 12, 0.0F);
            GLRenderer.modelM4f().scale(1.0F / scale, (scale + 1.0F) / 2.0F, 1.0F);
            GLRenderer.modelM4f().translate(-(x + 8), -(y + 12), 0.0F);
        }
        model.renderGui(GLRenderer.getTessellator(), null, stack, x, y, LightIndexHelper.lightIndex2i(15, 15), partialTick);
        if (bounce > 0.0F) {
            GLRenderer.popFrame();
        }
        model.renderItemOverlayIntoGUI(GLRenderer.getTessellator(), mc.font, mc.textureManager, stack, x, y, null, 1.0F);
        Lighting.disable();
        GLRenderer.disableState(State.DEPTH_TEST);
    }

    private static boolean isSameItem(ItemStack a, ItemStack b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.itemID == b.itemID && (a.isItemStackDamageable() || a.getMetadata() == b.getMetadata());
    }
}
