package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.util.OffhandSwingHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.Cube;
import net.minecraft.client.render.model.ModelBiped;
import net.minecraft.core.util.helper.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = ModelBiped.class, remap = false)
public class ModelBipedMixin {
    @Shadow
    public Cube armLeft;
    
    @Shadow
    public Cube armRight;
    
    @Shadow
    public Cube body;
    
    @Shadow
    public Cube head;

    @Inject(method = "setupAnimation", at = @At("TAIL"))
    private void applyOffhandSwing(float limbSwing, float limbYaw, float ticksExisted, float headYaw, float headPitch, float scale, CallbackInfo ci) {
        float swingProgress = OffhandSwingHelper.getOffhandSwingProgress();
        if (swingProgress > 0.0F) {
            float bodyRotation = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI * 2.0F) * -0.2F;
            this.body.yRot += bodyRotation;
            this.armRight.yRot += bodyRotation;
            this.armLeft.yRot += bodyRotation;
            this.armLeft.xRot += this.body.xRot;
            
            float f6 = 1.0F - swingProgress;
            f6 = f6 * f6 * f6 * f6;
            f6 = 1.0F - f6;
            float f7 = MathHelper.sin(f6 * (float) Math.PI);
            float f8 = MathHelper.sin(swingProgress * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
            
            this.armLeft.xRot -= (f7 * 1.2F + f8);
            this.armLeft.yRot += bodyRotation * 2.0F;
            this.armLeft.zRot = MathHelper.sin(swingProgress * (float) Math.PI) * 0.4F;
        }
    }
}
