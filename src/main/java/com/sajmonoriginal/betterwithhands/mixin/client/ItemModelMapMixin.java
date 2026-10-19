package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.client.OffhandFirstPerson;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.ItemRenderer;
import net.minecraft.client.render.MapItemRenderer;
import net.minecraft.client.render.PlayerSkinParser;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.client.render.item.model.ItemModelMap;
import net.minecraft.client.render.renderer.CullFace;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.renderer.State;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemMap;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.saveddata.maps.ItemMapSavedData;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = ItemModelMap.class, remap = false)
public abstract class ItemModelMapMixin {
    @Unique
    private static final String MAP_BACKGROUND = "/assets/minecraft/textures/misc/mapbg.png";

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    @Final
    private MapItemRenderer renderMapInstance;

    @Inject(method = "renderFirstPerson", at = @At("HEAD"), cancellable = true)
    private void renderInOneHand(TessellatorGeneral tessellator, ItemRenderer itemRenderer, Player player, ItemStack itemStack,
                                 byte lightIndex, float partialTick, CallbackInfo ci) {
        boolean offhandPass = ((OffhandFirstPerson) itemRenderer).betterWithHands$isRenderingOffhand();
        boolean otherHandBusy = offhandPass || ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem() != null;
        if (!ItemMap.hasInitialized(itemStack) || !otherHandBusy) {
            return;
        }
        ci.cancel();

        boolean leftSide = offhandPass != player.isLeftHanded();
        float equip = itemRenderer.getEquippedProgress(partialTick);
        float swing = player.getSwingProgress(partialTick);
        float swingRoot = MathHelper.sqrt_float(swing);
        float swingRootSin = MathHelper.sin(swingRoot * (float) Math.PI);
        Matrix4f model = GLRenderer.modelM4f();

        GLRenderer.pushFrame();
        if (player.isLeftHanded()) {
            model.scale(-1.0F, 1.0F, 1.0F);
        }
        GLRenderer.setCullFace(leftSide ? CullFace.FRONT : CullFace.BACK);
        model.translate(0.125F, -0.125F, 0.0F);

        GLRenderer.pushFrame();
        model.rotateZ((float) Math.toRadians(10.0F));
        model.translate(-swingRootSin * 0.3F, MathHelper.sin(swingRoot * (float) Math.PI * 2.0F) * 0.4F, -MathHelper.sin(swing * (float) Math.PI) * 0.4F);
        model.translate(0.64F, -0.6F - (1.0F - equip) * 0.6F, -0.72F);
        model.rotateY((float) Math.toRadians(45.0F));
        model.rotateY((float) Math.toRadians(swingRootSin * 70.0F));
        model.rotateZ((float) Math.toRadians(-MathHelper.sin(swing * swing * (float) Math.PI) * 20.0F));
        this.mc.textureManager.bindDownloadableTexture(
            this.mc.thePlayer.skinURL,
            this.mc.thePlayer.getEntityTexture(),
            this.mc.thePlayer.slimModel ? PlayerSkinParser.instanceAlex : PlayerSkinParser.instanceSteve
        );
        model.translate(-1.0F, 3.6F, 3.5F);
        model.rotateZ((float) Math.toRadians(120.0F));
        model.rotateX((float) Math.toRadians(200.0F));
        model.rotateY((float) Math.toRadians(-135.0F));
        model.translate(5.6F, 0.0F, 0.0F);
        if (leftSide) {
            model.scale(-1.0F, 1.0F, 1.0F);
            GLRenderer.setCullFace(CullFace.BACK);
        }
        model.scale(0.0625F, 0.0625F, -0.0625F);
        model.translate(0.0F, 16.0F, 0.0F);
        model.rotateX((float) Math.PI);
        model.translate(0.0F, -16.0F, 0.0F);
        model.translate(0.0F, 8.0F, 0.0F);
        MobRendererPlayer playerRenderer = (MobRendererPlayer) EntityRendererDispatcher.instance.<Player>getRenderer(this.mc.thePlayer);
        playerRenderer.drawFirstPersonHand(GLRenderer.getTessellator(), this.mc.thePlayer, leftSide);
        GLRenderer.popFrame();

        model.translate(0.51F, -0.08F - (1.0F - equip) * 1.2F, -0.75F);
        model.translate(-0.5F * swingRootSin, 0.4F * MathHelper.sin(swingRoot * (float) Math.PI * 2.0F) - 0.3F * swingRootSin,
            -0.3F * MathHelper.sin(swing * (float) Math.PI));
        model.rotateX((float) Math.toRadians(swingRootSin * -45.0F));
        model.rotateY((float) Math.toRadians(swingRootSin * -30.0F));
        model.rotateY((float) Math.PI);
        model.rotateZ((float) Math.PI);
        model.scale(0.38F, 0.38F, 0.38F);
        model.translate(-0.5F, -0.5F, 0.0F);
        model.scale(0.0078125F, 0.0078125F, 0.0078125F);
        if (leftSide) {
            model.translate(128.0F, 0.0F, 0.0F);
            model.scale(-1.0F, 1.0F, 1.0F);
        }
        GLRenderer.disableState(State.CULL_FACE);

        this.mc.textureManager.bindTexture(this.mc.textureManager.loadTexture(MAP_BACKGROUND));
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-7.0, 135.0, 0.0, 0.0, 1.0);
        tessellator.addVertexWithUV(135.0, 135.0, 0.0, 1.0, 1.0);
        tessellator.addVertexWithUV(135.0, -7.0, 0.0, 1.0, 0.0);
        tessellator.addVertexWithUV(-7.0, -7.0, 0.0, 0.0, 0.0);
        tessellator.draw();
        ItemMapSavedData mapData = Items.MAP.getOrCreateSavedData(itemStack, this.mc.currentWorld);
        if (mapData != null) {
            this.renderMapInstance.renderMap(tessellator, mapData);
        }
        GLRenderer.popFrame();
    }
}
