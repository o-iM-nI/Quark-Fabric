package vazkii.quark.base.client.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.screen.slot.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import vazkii.quark.addons.oddities.client.screen.BackpackInventoryScreen;
import vazkii.quark.api.IQuarkButtonIgnored;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.handler.InventoryTransferHandler;
import vazkii.quark.base.module.QuarkModule;

@Environment(EnvType.CLIENT)
@EventBusSubscriber(modid = Quark.MOD_ID, value = Dist.CLIENT)
public final class InventoryButtonHandler {

	private static final Multimap<ButtonTargetType, ButtonProviderHolder> providers = Multimaps.newSetMultimap(new HashMap<>(), TreeSet::new);

	@SubscribeEvent
	public static void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
		if(GeneralConfig.printScreenClassnames)
			Quark.LOG.info("Opened screen {}", event.getGui().getClass().getName());
		
		if(event.getGui() instanceof HandledScreen && !(event.getGui() instanceof IQuarkButtonIgnored) && !GeneralConfig.ignoredScreens.contains(event.getGui().getClass().getName())) {
			MinecraftClient mc = MinecraftClient.getInstance();
			HandledScreen<?> screen = (HandledScreen<?>) event.getGui();

			if(screen instanceof InventoryScreen || screen.getClass().getName().contains("CuriosScreen"))
				applyProviders(event, ButtonTargetType.PLAYER_INVENTORY, screen, s -> s.inventory == mc.player.inventory && s.getSlotIndex() == 17);
			else {
				if(InventoryTransferHandler.accepts(screen.getScreenHandler(), mc.player)) { 
					applyProviders(event, ButtonTargetType.CONTAINER_INVENTORY, screen, s -> s.inventory != mc.player.inventory && s.getSlotIndex() == 8);
					applyProviders(event, ButtonTargetType.CONTAINER_PLAYER_INVENTORY, screen, s -> s.inventory == mc.player.inventory && s.getSlotIndex() == 17);
				}
			}
		}
	}

	private static Collection<ButtonProviderHolder> forGui(Screen gui) {
		Set<ButtonProviderHolder> holders = new HashSet<>();
		if (gui instanceof HandledScreen) {
			HandledScreen<?> screen = (HandledScreen<?>) gui;

			if (gui instanceof InventoryScreen)
				holders.addAll(providers.get(ButtonTargetType.PLAYER_INVENTORY));
			else {
				MinecraftClient mc = MinecraftClient.getInstance();
				if(InventoryTransferHandler.accepts(screen.getScreenHandler(), mc.player)) {
					holders.addAll(providers.get(ButtonTargetType.CONTAINER_INVENTORY));
					holders.addAll(providers.get(ButtonTargetType.CONTAINER_PLAYER_INVENTORY));
				}
			}
		}

		return holders;
	}

	@SubscribeEvent
	public static void mouseInputEvent(GuiScreenEvent.MouseClickedEvent.Pre pressed) {
		Screen gui = pressed.getGui();
		if (gui instanceof HandledScreen) {
			HandledScreen<?> screen = (HandledScreen<?>) gui;

			Collection<ButtonProviderHolder> holders = forGui(screen);

			for (ButtonProviderHolder holder : holders) {
				if (holder.keybind != null &&
						holder.keybind.matchesMouse(pressed.getButton()) &&
						(holder.keybind.getKeyModifier() == KeyModifier.NONE || holder.keybind.getKeyModifier().isActive(KeyConflictContext.GUI))) {
					holder.pressed.accept(screen);
				}
			}
		}
	}

	@SubscribeEvent
	public static void keyboardInputEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Post pressed) {
		Screen gui = pressed.getGui();
		if (gui instanceof HandledScreen) {
			HandledScreen<?> screen = (HandledScreen<?>) gui;

			Collection<ButtonProviderHolder> holders = forGui(screen);

			for (ButtonProviderHolder holder : holders) {
				if (holder.keybind != null &&
						holder.keybind.matchesKey(pressed.getKeyCode(), pressed.getScanCode()) &&
						(holder.keybind.getKeyModifier() == KeyModifier.NONE || holder.keybind.getKeyModifier().isActive(KeyConflictContext.GUI))) {
					holder.pressed.accept(screen);
				}
			}
		}

	}

	private static void applyProviders(GuiScreenEvent.InitGuiEvent.Post event, ButtonTargetType type, HandledScreen<?> screen, Predicate<Slot> slotPred) {
		Collection<ButtonProviderHolder> holders = providers.get(type);
		if(!holders.isEmpty()) {
			for(Slot slot : screen.getScreenHandler().slots)
				if(slotPred.test(slot)) {
					int x = slot.x + 6;
					int y = slot.y - 13;
					
					if(screen instanceof BackpackInventoryScreen)
						y -= 60;

					if(ModList.get().isLoaded("consoleexperience"))
						x -= 15;
					
					for(ButtonProviderHolder holder : holders) {
						ButtonWidget button = holder.getButton(screen, x, y);
						if(button != null) {
							event.addWidget(button);
							x -= 12;
						}
					}

					return;
				}
		}
	}

	public static void addButtonProvider(QuarkModule module, ButtonTargetType type, int priority, KeyBinding binding, Consumer<HandledScreen<?>> onKeybind, ButtonProvider provider) {
		providers.put(type, new ButtonProviderHolder(module, priority, provider,
				binding, onKeybind));
	}

	public static void addButtonProvider(QuarkModule module, ButtonTargetType type, int priority, String keybindName, Consumer<HandledScreen<?>> onKeybind, ButtonProvider provider) {
		addButtonProvider(module, type, priority, ModKeybindHandler.init(keybindName, null, ModKeybindHandler.INV_GROUP), onKeybind, provider);
	}

	public static void addButtonProvider(QuarkModule module, ButtonTargetType type, int priority, ButtonProvider provider) {
		providers.put(type, new ButtonProviderHolder(module, priority, provider));
	}

	public enum ButtonTargetType {
		PLAYER_INVENTORY,
		CONTAINER_INVENTORY,
		CONTAINER_PLAYER_INVENTORY
	}

	public interface ButtonProvider {
		ButtonWidget provide(HandledScreen<?> parent, int x, int y);
	}

	private static class ButtonProviderHolder implements Comparable<ButtonProviderHolder> {

		private final int priority;
		private final QuarkModule module;
		private final ButtonProvider provider;

		private final KeyBinding keybind;
		private final Consumer<HandledScreen<?>> pressed;

		public ButtonProviderHolder(QuarkModule module, int priority, ButtonProvider provider, KeyBinding keybind, Consumer<HandledScreen<?>> onPressed) {
			this.module = module;
			this.priority = priority;
			this.provider = provider;
			this.keybind = keybind;
			this.pressed = onPressed;
		}

		public ButtonProviderHolder(QuarkModule module, int priority, ButtonProvider provider) {
			this(module, priority, provider, null, (screen) -> {});
		}

		@Override
		public int compareTo(@Nonnull ButtonProviderHolder o) {
			return priority - o.priority;
		}

		public ButtonWidget getButton(HandledScreen<?> parent, int x, int y) {
			return module.enabled ? provider.provide(parent, x, y) : null;
		}

	}

}
