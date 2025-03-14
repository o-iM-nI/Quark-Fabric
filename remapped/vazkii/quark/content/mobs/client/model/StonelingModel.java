package vazkii.quark.content.mobs.client.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import vazkii.quark.content.mobs.entity.StonelingEntity;

public class StonelingModel extends EntityModel<StonelingEntity> {

	private final ModelPart body;
	private final ModelPart arm_right;
	private final ModelPart arm_left;
	private final ModelPart leg_right;
	private final ModelPart leg_left;

	public StonelingModel() {
		textureWidth = 32;
		textureHeight = 32;

		body = new ModelPart(this);
		body.setPivot(0.0F, 14.0F, 0.0F);

		ModelPart head = new ModelPart(this);
		head.setPivot(0.0F, 0.0F, 0.0F);
		body.addChild(head);
		
		// addBox = addCuboid
		
		head.addCuboid(null, -3.0F, -2.0F, -3.0F, 6, 8, 6, 0.0F, 0, 0);
		head.addCuboid(null, -1.0F, -4.0F, -5.0F, 2, 4, 2, 0.0F, 8, 24);
		head.addCuboid(null, -1.0F, 6.0F, -3.0F, 2, 2, 2, 0.0F, 16, 20);
		head.addCuboid(null, -1.0F, -4.0F, 3.0F, 2, 4, 2, 0.0F, 0, 24);
		head.addCuboid(null, -1.0F, -4.0F, -3.0F, 2, 2, 6, 0.0F, 16, 24);
		head.addCuboid(null, -1.0F, -4.0F, -1.0F, 2, 2, 2, 0.0F, 24, 20);
		head.addCuboid(null, -1.0F, 1.0F, -5.0F, 2, 2, 2, 0.0F, 18, 0);
		head.addCuboid(null, -4.0F, -1.0F, -3.0F, 1, 2, 2, 0.0F, 0, 0);
		head.addCuboid(null, 3.0F, -1.0F, -3.0F, 1, 2, 2, 0.0F, 0, 0);

		arm_right = new ModelPart(this);
		arm_right.setPivot(-3.0F, 2.0F, 0.0F);
		setRotationAngle(arm_right, 3.1416F, 0.0F, 0.0F);
		body.addChild(arm_right);
		arm_right.addCuboid(null, -2.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, 0, 14);

		arm_left = new ModelPart(this);
		arm_left.setPivot(3.0F, 2.0F, 0.0F);
		setRotationAngle(arm_left, 3.1416F, 0.0F, 0.0F);
		body.addChild(arm_left);
		arm_left.addCuboid(null, 0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, 8, 14);

		leg_right = new ModelPart(this);
		leg_right.setPivot(-2.0F, 4.0F, 0.0F);
		body.addChild(leg_right);
		leg_right.addCuboid(null, -1.0F, 2.0F, -1.0F, 2, 4, 2, 0.0F, 16, 14);

		leg_left = new ModelPart(this);
		leg_left.setPivot(1.0F, 4.0F, 0.0F);
		body.addChild(leg_left);
		leg_left.addCuboid(null, 0.0F, 2.0F, -1.0F, 2, 4, 2, 0.0F, 24, 14);
	}
	
	@Override
	public void setRotationAngles(StonelingEntity stoneling, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		leg_right.pitch = MathHelper.cos(limbSwing * 0.6662F) * limbSwingAmount;
		leg_left.pitch = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
		
		ItemStack carry = stoneling.getCarryingItem();
		if(carry.isEmpty() && !stoneling.hasPassengers()) {
			arm_right.pitch = 0F;
			arm_left.pitch = 0F;
		} else {
			arm_right.pitch = 3.1416F;
			arm_left.pitch = 3.1416F;
		}
	}

	@Override
	public void render(MatrixStack matrix, VertexConsumer vb, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
		body.render(matrix, vb, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
	}
	
	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.pitch = x;
		modelRenderer.yaw = y;
		modelRenderer.roll = z;
	}
}
