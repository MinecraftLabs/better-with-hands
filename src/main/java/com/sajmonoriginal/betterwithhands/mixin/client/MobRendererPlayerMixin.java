package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererPlayer.class, remap = false)
public abstract class MobRendererPlayerMixin {
    @Unique
    private static final double HOLD_ITEM_PITCH = 18.0F * MathHelper.DEG_TO_RAD;

    @Shadow
    protected abstract StaticEntityModel getActiveModel(Player player);

    @Inject(
        method = "setupAnimations(Lnet/minecraft/core/entity/player/Player;Lorg/useless/dragonfly/models/entity/StaticEntityModel;FI)Lorg/useless/dragonfly/models/entity/StaticEntityModel;",
        at = @At("RETURN")
    )
    private void poseOffhandArm(Player player, StaticEntityModel modelIn, float partialTick, int layer, CallbackInfoReturnable<StaticEntityModel> cir) {
        StaticEntityModel model = cir.getReturnValue();
        if (model == null || player.getHeldObject() != null) {
            return;
        }

        boolean offhandIsLeft = !player.isLeftHanded();
        BoneTransform arm = model.getTransform(offhandIsLeft ? "leftArm" : "rightArm");
        BoneTransform torso = model.getTransform("torso");
        BoneTransform head = model.getTransform("head");
        if (arm == null || torso == null || head == null) {
            return;
        }

        if (((OffhandCapable) player.inventory).betterWithHands$getOffhandItem() != null) {
            arm.rotX = arm.rotX * 0.5 - HOLD_ITEM_PITCH;
        }

        float swing = ((OffhandSwingCapable) player).betterWithHands$getOffhandSwingProgress(partialTick);
        if (swing <= 0.0F) {
            return;
        }

        double side = offhandIsLeft ? -1.0 : 1.0;
        double torsoYaw = Math.sin(Math.sqrt(swing) * Math.PI * 2.0 * side) * 0.2F;
        torso.rotY += torsoYaw;
        float eased = 1.0F - swing;
        eased *= eased;
        eased *= eased;
        eased = 1.0F - eased;
        double lift = Math.sin(eased * Math.PI);
        double pitch = Math.sin(swing * Math.PI) * -(Math.toRadians(head.rotX) - 0.7F) * 0.75;
        arm.rotX -= lift * 1.2 + pitch;
        arm.rotY += torsoYaw * 2.0;
        arm.rotZ -= Math.sin(swing * Math.PI) * 0.4F * side;
    }

    @Inject(
        method = "renderAdditional(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/player/Player;F)V",
        at = @At("RETURN")
    )
    private void renderOffhandItem(TessellatorGeneral tessellator, Player player, float partialTick, CallbackInfo ci) {
        ItemStack offhand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        if (offhand == null || player.getHeldObject() != null) {
            return;
        }

        boolean offhandIsLeft = !player.isLeftHanded();
        GLRenderer.pushFrame();
        StaticEntityModel model = this.getActiveModel(player);
        if (model != null) {
            model.translateToBone(offhandIsLeft ? "leftItem" : "rightItem");
        }
        if (offhandIsLeft) {
            GLRenderer.modelM4f().scale(1.0F, 1.0F, -1.0F);
        } else {
            GLRenderer.modelM4f().scale(-1.0F, 1.0F, 1.0F);
        }
        GLRenderer.modelM4f().scale(15.99F, 15.99F, 15.99F);
        ItemModelDispatcher.getInstance()
            .getDispatch(offhand)
            .render(tessellator, player, offhand, offhandIsLeft ? "thirdperson_lefthand" : "thirdperson_righthand",
                true, 1, GLRenderer.getLightIndex(), partialTick, offhandIsLeft);
        GLRenderer.popFrame();
    }
}
