package com.sajmonoriginal.betterwithhands.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRendererBiped;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.useless.dragonfly.models.entity.BoneTransform;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererBiped.class, remap = false)
public abstract class MobRendererBipedMixin {
    @Unique
    private static final double HOLD_ITEM_PITCH = 18.0F * MathHelper.DEG_TO_RAD;

    @Inject(
        method = "setupAnimations(Lnet/minecraft/core/entity/Mob;Lorg/useless/dragonfly/models/entity/StaticEntityModel;FI)Lorg/useless/dragonfly/models/entity/StaticEntityModel;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/entity/Mob;isSneaking()Z")
    )
    private void poseOffhandArm(Mob entity, StaticEntityModel model, float partialTick, int layer, CallbackInfoReturnable<StaticEntityModel> cir,
                                @Local(ordinal = 0) BoneTransform rightArm, @Local(ordinal = 1) BoneTransform leftArm,
                                @Local(ordinal = 4) BoneTransform torso, @Local(ordinal = 6) BoneTransform head) {
        if (!(entity instanceof Player player) || player.getHeldObject() != null) {
            return;
        }

        boolean offhandIsLeft = !player.isLeftHanded();
        BoneTransform arm = offhandIsLeft ? leftArm : rightArm;
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
        arm.rotZ = -Math.sin(swing * Math.PI) * 0.4F * side;
    }
}
