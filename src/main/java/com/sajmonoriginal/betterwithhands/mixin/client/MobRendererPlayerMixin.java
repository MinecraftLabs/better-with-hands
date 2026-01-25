package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.model.ModelBase;
import net.minecraft.client.render.model.ModelBiped;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererPlayer.class, remap = false)
public abstract class MobRendererPlayerMixin extends MobRenderer<Player> {
    @Shadow
    private ModelBiped modelBipedMain;

    protected MobRendererPlayerMixin(ModelBase model, float shadowSize) {
        super(model, shadowSize);
    }

    @SuppressWarnings("java:S107")
    @Inject(method = "render(Lnet/minecraft/client/render/tessellator/Tessellator;Lnet/minecraft/core/entity/player/Player;DDDFF)V", at = @At("HEAD"))
    private void setOffhandSwingProgress(Tessellator tessellator, Player player, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci) {
        float swingProgress = ((OffhandSwingCapable) player).betterWithHands$getOffhandSwingProgress(partialTick);
        OffhandSwingHelper.setOffhandSwingProgress(swingProgress);
    }

    @SuppressWarnings("java:S107")
    @Inject(method = "render(Lnet/minecraft/client/render/tessellator/Tessellator;Lnet/minecraft/core/entity/player/Player;DDDFF)V", at = @At("RETURN"))
    private void clearOffhandSwingProgress(Tessellator tessellator, Player player, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci) {
        OffhandSwingHelper.setOffhandSwingProgress(0.0F);
    }

    @Inject(method = "renderSpecials(Lnet/minecraft/core/entity/player/Player;F)V", at = @At("RETURN"))
    private void renderOffhandItem(Player player, float partialTick, CallbackInfo ci) {
        ItemStack offhand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        if (offhand == null) {
            return;
        }

        GL11.glPushMatrix();
        
        this.modelBipedMain.armLeft.translateTo(0.0625F);
        
        GL11.glScalef(-1.0F, 1.0F, 1.0F);
        
        ItemModel model = ItemModelDispatcher.getInstance().getDispatch(offhand.getItem());
        model.renderItemThirdPerson(
            Tessellator.instance,
            this.renderDispatcher.itemRenderer,
            player,
            offhand,
            true
        );
        
        GL11.glPopMatrix();
    }
}
