package com.sajmonoriginal.betterwithhands.mixin.client;

import com.sajmonoriginal.betterwithhands.util.OffhandCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingCapable;
import com.sajmonoriginal.betterwithhands.util.OffhandSwingHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.client.render.item.model.ItemModel;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.useless.dragonfly.models.entity.StaticEntityModel;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererPlayer.class, remap = false)
public abstract class MobRendererPlayerMixin {

    @Shadow
    protected abstract StaticEntityModel getActiveModel(Player player);

    @Inject(
        method = "setupAnimations(Lnet/minecraft/core/entity/player/Player;Lorg/useless/dragonfly/models/entity/StaticEntityModel;FI)Lorg/useless/dragonfly/models/entity/StaticEntityModel;",
        at = @At("HEAD")
    )
    private void setOffhandSwingProgress(Player player, StaticEntityModel model, float partialTick, int i, CallbackInfoReturnable<StaticEntityModel> cir) {
        float swingProgress = ((OffhandSwingCapable) player).betterWithHands$getOffhandSwingProgress(partialTick);
        OffhandSwingHelper.setOffhandSwingProgress(swingProgress);
    }

    @Inject(
        method = "renderAdditional(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/player/Player;F)V",
        at = @At("RETURN")
    )
    private void renderOffhandItem(TessellatorGeneral tessellator, Player player, float partialTick, CallbackInfo ci) {
        ItemStack offhand = ((OffhandCapable) player.inventory).betterWithHands$getOffhandItem();
        if (offhand == null) return;

        GLRenderer.pushFrame();

        StaticEntityModel activeModel = this.getActiveModel(player);
        if (activeModel != null) {
            activeModel.translateToBone("leftItem");
        }

        GLRenderer.modelM4f().scale(1.0F, 1.0F, -1.0F);
        GLRenderer.modelM4f().scale(15.99F, 15.99F, 15.99F);

        ItemModel itemModel = ItemModelDispatcher.getInstance().getDispatch(offhand);
        itemModel.render(tessellator, player, offhand, "thirdperson_lefthand", true, 1, GLRenderer.getLightIndex(), partialTick, true);

        GLRenderer.popFrame();
    }
}
