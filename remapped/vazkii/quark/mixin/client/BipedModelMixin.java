package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import vazkii.quark.content.tweaks.client.emote.EmoteHandler;

@Mixin(BipedEntityModel.class)
public class BipedModelMixin<T extends LivingEntity> {

	@Inject(method = "setAngles", at = @At("RETURN"))
	private void updateEmotes(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo callbackInfo) {
		EmoteHandler.updateEmotes(entityIn);
	}
}
