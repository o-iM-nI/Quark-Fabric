package vazkii.quark.content.management.module;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.client.handler.ModKeybindHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.ChangeHotbarMessage;

@LoadModule(category = ModuleCategory.MANAGEMENT, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class HotbarChangerModule extends QuarkModule {

	@Environment(EnvType.CLIENT)
	private static KeyBinding changeHotbarKey;

	private static final Identifier WIDGETS = new Identifier("textures/gui/widgets.png");

	private static final int ANIMATION_TIME = 10;
	private static final int MAX_HEIGHT = 90;
	private static final int ANIM_PER_TICK = MAX_HEIGHT / ANIMATION_TIME;

	public static int height = 0;
	public static int currentHeldItem = -1;
	public static boolean animating;
	public static boolean keyDown;
	public static boolean hotbarChangeOpen, shifting;

	@Override
	public void clientSetup() {
		changeHotbarKey = ModKeybindHandler.init("change_hotbar", "z", ModKeybindHandler.MISC_GROUP);
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onMouseInput(InputEvent.MouseInputEvent event) {
		acceptInput();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		acceptInput();
	}

	private void acceptInput() {
		MinecraftClient mc = MinecraftClient.getInstance();
		boolean down = changeHotbarKey.isPressed();
		boolean wasDown = keyDown;
		keyDown = down;
		if(mc.isWindowFocused()) {
			if(down && !wasDown)
				hotbarChangeOpen = !hotbarChangeOpen;
			else if(hotbarChangeOpen)
				for(int i = 0; i < 3; i++)
					if(mc.options.keysHotbar[i].isPressed()) {
						QuarkNetwork.sendToServer(new ChangeHotbarMessage(i + 1));
						hotbarChangeOpen = false;
						currentHeldItem = mc.player.inventory.selectedSlot;
						return;
					}

		}
	}
	
	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void hudPre(RenderGameOverlayEvent.Pre event) {
		float shift = -getRealHeight(event.getPartialTicks()) + 22;
		if(shift < 0)
			if(event.getType() == ElementType.HEALTH) {
				event.getMatrixStack().translate(0, shift, 0);
				shifting = true;
			} else if(shifting && (event.getType() == ElementType.DEBUG || event.getType() == ElementType.POTION_ICONS)) {
				event.getMatrixStack().translate(0, -shift, 0);
				shifting = false;
			}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void hudPost(RenderGameOverlayEvent.Post event) {
		if(height <= 0)
			return;

		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerEntity player = mc.player;
		MatrixStack matrix = event.getMatrixStack();

		if(event.getType() == ElementType.HOTBAR) {
			Window res = event.getWindow();
			float realHeight = getRealHeight(event.getPartialTicks());
			float xStart = res.getScaledWidth() / 2f - 91;
			float yStart = res.getScaledHeight() - realHeight;

			ItemRenderer render = mc.getItemRenderer();

			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			mc.textureManager.bindTexture(WIDGETS);
			for(int i = 0; i < 3; i++) {
				matrix.push();
				RenderSystem.color4f(1F, 1F, 1F, 0.75F);
				matrix.translate(xStart, yStart + i * 21, 0);
				mc.inGameHud.drawTexture(matrix, 0, 0, 0, 0, 182, 22);
				matrix.pop();
			}

			for(int i = 0; i < 3; i++)
				mc.textRenderer.drawWithShadow(matrix, Formatting.BOLD + Integer.toString(i + 1), xStart - 9, yStart + i * 21 + 7, 0xFFFFFF);

			for(int i = 0; i < 27; i++) {
				ItemStack invStack = player.inventory.getStack(i + 9);
				int x = (int) (xStart + (i % 9) * 20 + 3);
				int y = (int) (yStart + (i / 9) * 21 + 3);

				render.renderInGuiWithOverrides(invStack, x, y);
				render.renderGuiItemOverlay(mc.textRenderer, invStack, x, y);
			}
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onTick(ClientTickEvent event) {
		if(event.phase == Phase.END) {
			PlayerEntity player = MinecraftClient.getInstance().player;
			if(player != null && currentHeldItem != -1 && player.inventory.selectedSlot != currentHeldItem) {
				player.inventory.selectedSlot = currentHeldItem;
				currentHeldItem = -1;	
			}
		} 

		if(hotbarChangeOpen && height < MAX_HEIGHT) {
			height += ANIM_PER_TICK;
			animating = true;
		} else if(!hotbarChangeOpen && height > 0) {
			height -= ANIM_PER_TICK;
			animating = true;
		} else animating = false;
	}

	private float getRealHeight(float part) {
		if(!animating)
			return height;
		return height + part * ANIM_PER_TICK * (hotbarChangeOpen ? 1 : -1);
	}

}
