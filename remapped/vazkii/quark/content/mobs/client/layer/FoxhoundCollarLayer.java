/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [Jul 13, 2019, 13:31 AM (EST)]
 */
package vazkii.quark.content.mobs.client.layer;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import vazkii.quark.base.Quark;
import vazkii.quark.content.mobs.client.model.FoxhoundModel;
import vazkii.quark.content.mobs.entity.FoxhoundEntity;

public class FoxhoundCollarLayer extends FeatureRenderer<FoxhoundEntity, FoxhoundModel> {

	private static final Identifier WOLF_COLLAR = new Identifier(Quark.MOD_ID, "textures/model/entity/foxhound/collar.png");

	public FoxhoundCollarLayer(FeatureRendererContext<FoxhoundEntity, FoxhoundModel> renderer) {
		super(renderer);
	}

	@Override
	public void render(MatrixStack matrix, VertexConsumerProvider buffer, int light, FoxhoundEntity foxhound,  float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
		if (foxhound.isTamed() && !foxhound.isInvisible()) {
			float[] afloat = foxhound.getCollarColor().getColorComponents();
			renderModel(getContextModel(), WOLF_COLLAR, matrix, buffer, light, foxhound, afloat[0], afloat[1], afloat[2]);
		}
	}

}
