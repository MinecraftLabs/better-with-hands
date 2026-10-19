package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.renderer.GLRenderer;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererPlayer.class, remap = false)
public abstract class MobRendererPlayerMixin {
    @Shadow
    protected abstract StaticEntityModel getActiveModel(Player player);

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
