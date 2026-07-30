package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.util.helper.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererPlayer.class, remap = false)
public class ModelBipedMixin {

    private static final float HOLD_ITEM_ARM_PITCH = 0.3141593F;

    @Inject(
        method = "setupAnimations(Lnet/minecraft/core/entity/player/Player;Lorg/useless/dragonfly/models/entity/StaticEntityModel;FI)Lorg/useless/dragonfly/models/entity/StaticEntityModel;",
        at = @At("RETURN")
    )
    private void applyOffhandPose(Player player, StaticEntityModel modelIn, float f, int i, CallbackInfoReturnable<StaticEntityModel> cir) {
        StaticEntityModel model = cir.getReturnValue();
        if (model == null) return;

        ItemStack offhand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        boolean hasOffhandItem = offhand != null;

        BoneTransform body = model.getTransform("body");
        BoneTransform armRight = model.getTransform("rightArm");
        BoneTransform armLeft = model.getTransform("leftArm");
        BoneTransform head = model.getTransform("head");

        if (body == null || armRight == null || armLeft == null || head == null) return;

        if (hasOffhandItem) {
            armLeft.rotX = armLeft.rotX * 0.5 - HOLD_ITEM_ARM_PITCH;
        }

        float swingProgress = OffhandSwingHelper.getOffhandSwingProgress();
        if (swingProgress <= 0.0F) return;

        double bodyRotation = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI * 2.0F) * -0.2F;
        body.rotY += bodyRotation;
        armRight.rotY += bodyRotation;
        armLeft.rotY += bodyRotation;
        armLeft.rotX += body.rotX;

        double f6 = 1.0 - swingProgress;
        f6 = f6 * f6 * f6 * f6;
        f6 = 1.0 - f6;
        double f7 = MathHelper.sin((float)(f6 * Math.PI));
        double f8 = MathHelper.sin(swingProgress * (float) Math.PI) * -(head.rotX - 0.7) * 0.75;

        armLeft.rotX -= (f7 * 1.2 + f8);
        armLeft.rotY += bodyRotation * 2.0;
        armLeft.rotZ = MathHelper.sin(swingProgress * (float) Math.PI) * 0.4;
    }
}
