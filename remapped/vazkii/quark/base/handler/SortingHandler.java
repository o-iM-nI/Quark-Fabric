package vazkii.quark.base.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MinecartItem;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.TridentItem;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.api.ICustomSorting;
import vazkii.quark.api.QuarkCapabilities;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.management.module.InventorySortingModule;

public final class SortingHandler {

	private static final Comparator<ItemStack> FALLBACK_COMPARATOR = jointComparator(
			Comparator.comparingInt((ItemStack s) -> Item.getRawId(s.getItem())),
			SortingHandler::damageCompare,
			(ItemStack s1, ItemStack s2) -> s2.getCount() - s1.getCount(),
			(ItemStack s1, ItemStack s2) -> s2.hashCode() - s1.hashCode());

	private static final Comparator<ItemStack> FOOD_COMPARATOR = jointComparator(
			SortingHandler::foodHealCompare,
			SortingHandler::foodSaturationCompare);

	private static final Comparator<ItemStack> TOOL_COMPARATOR = jointComparator(
			SortingHandler::toolPowerCompare,
			SortingHandler::enchantmentCompare,
			SortingHandler::damageCompare);

	private static final Comparator<ItemStack> SWORD_COMPARATOR = jointComparator(
			SortingHandler::swordPowerCompare,
			SortingHandler::enchantmentCompare,
			SortingHandler::damageCompare);

	private static final Comparator<ItemStack> ARMOR_COMPARATOR = jointComparator(
			SortingHandler::armorSlotAndToughnessCompare,
			SortingHandler::enchantmentCompare,
			SortingHandler::damageCompare);

	private static final Comparator<ItemStack> BOW_COMPARATOR = jointComparator(
			SortingHandler::enchantmentCompare,
			SortingHandler::damageCompare);

	public static void sortInventory(PlayerEntity player, boolean forcePlayer) {
		if (!ModuleLoader.INSTANCE.isModuleEnabled(InventorySortingModule.class))
			return;

		ScreenHandler c = player.currentScreenHandler;
		if (forcePlayer || c == null)
			c = player.playerScreenHandler;

//		boolean backpack = c instanceof ContainerBackpack;
		boolean playerContainer = c == player.playerScreenHandler; // || backpack;

		for (Slot s : c.slots) {
			Inventory inv = s.inventory;
			if ((inv == player.inventory) == playerContainer) {
				if (!playerContainer && s instanceof SlotItemHandler) {
					sortInventory(((SlotItemHandler) s).getItemHandler());
				} else {
					InvWrapper wrapper = new InvWrapper(inv);
					if (playerContainer)
						sortInventory(wrapper, 9, 36);
					else sortInventory(wrapper);
				}
				break;
			}
		}

//		if (backpack)
//			for (Slot s : c.inventorySlots)
//				if (s instanceof SlotItemHandler) {
//					sortInventory(((SlotItemHandler) s).getItemHandler());
//					break;
//				}
	}

	public static void sortInventory(IItemHandler handler) {
		sortInventory(handler, 0);
	}

	public static void sortInventory(IItemHandler handler, int iStart) {
		sortInventory(handler, iStart, handler.getSlots());
	}

	public static void sortInventory(IItemHandler handler, int iStart, int iEnd) {
		List<ItemStack> stacks = new ArrayList<>();
		List<ItemStack> restore = new ArrayList<>();

		for (int i = iStart; i < iEnd; i++) {
			ItemStack stackAt = handler.getStackInSlot(i);
			restore.add(stackAt.copy());
			if (!stackAt.isEmpty())
				stacks.add(stackAt.copy());
		}

		mergeStacks(stacks);
		sortStackList(stacks);

		if (setInventory(handler, stacks, iStart, iEnd) == ActionResult.FAIL)
			setInventory(handler, restore, iStart, iEnd);
	}

	private static ActionResult setInventory(IItemHandler inventory, List<ItemStack> stacks, int iStart, int iEnd) {
		for (int i = iStart; i < iEnd; i++) {
			int j = i - iStart;
			ItemStack stack = j >= stacks.size() ? ItemStack.EMPTY : stacks.get(j);

			if (!stack.isEmpty() && !inventory.isItemValid(i, stack))
				return ActionResult.PASS;
		}

		for (int i = iStart; i < iEnd; i++) {
			int j = i - iStart;
			ItemStack stack = j >= stacks.size() ? ItemStack.EMPTY : stacks.get(j);

			inventory.extractItem(i, inventory.getSlotLimit(i), false);
			if (!stack.isEmpty())
				if (!inventory.insertItem(i, stack, false).isEmpty())
					return ActionResult.FAIL;
		}

		return ActionResult.SUCCESS;
	}

	private static void mergeStacks(List<ItemStack> list) {
		for (int i = 0; i < list.size(); i++) {
			ItemStack set = mergeStackWithOthers(list, i);
			list.set(i, set);
		}

		list.removeIf((ItemStack stack) -> stack.isEmpty() || stack.getCount() == 0);
	}

	private static ItemStack mergeStackWithOthers(List<ItemStack> list, int index) {
		ItemStack stack = list.get(index);
		if (stack.isEmpty())
			return stack;

		for (int i = 0; i < list.size(); i++) {
			if (i == index)
				continue;

			ItemStack stackAt = list.get(i);
			if (stackAt.isEmpty())
				continue;

			if (stackAt.getCount() < stackAt.getMaxCount() && ItemStack.areItemsEqualIgnoreDamage(stack, stackAt) && ItemStack.areTagsEqual(stack, stackAt)) {
				int setSize = stackAt.getCount() + stack.getCount();
				int carryover = Math.max(0, setSize - stackAt.getMaxCount());
				stackAt.setCount(carryover);
				stack.setCount(setSize - carryover);

				if (stack.getCount() == stack.getMaxCount())
					return stack;
			}
		}

		return stack;
	}

	public static void sortStackList(List<ItemStack> list) {
		list.sort(SortingHandler::stackCompare);
	}

	private static int stackCompare(ItemStack stack1, ItemStack stack2) {
		if (stack1 == stack2)
			return 0;
		if (stack1.isEmpty())
			return -1;
		if (stack2.isEmpty())
			return 1;

		if(hasCustomSorting(stack1) && hasCustomSorting(stack2)) {
			ICustomSorting sort1 = getCustomSorting(stack1);
			ICustomSorting sort2 = getCustomSorting(stack2);
			if (sort1.getSortingCategory().equals(sort2.getSortingCategory()))
				return sort1.getItemComparator().compare(stack1, stack2);
		}

		ItemType type1 = getType(stack1);
		ItemType type2 = getType(stack2);

		if (type1 == type2)
			return type1.comparator.compare(stack1, stack2);

		return type1.ordinal() - type2.ordinal();
	}

	private static ItemType getType(ItemStack stack) {
		for (ItemType type : ItemType.values())
			if (type.fitsInType(stack))
				return type;

		throw new RuntimeException("Having an ItemStack that doesn't fit in any type is impossible.");
	}

	private static Predicate<ItemStack> classPredicate(Class<? extends Item> clazz) {
		return (ItemStack s) -> !s.isEmpty() && clazz.isInstance(s.getItem());
	}

	private static Predicate<ItemStack> inverseClassPredicate(Class<? extends Item> clazz) {
		return classPredicate(clazz).negate();
	}

	private static Predicate<ItemStack> itemPredicate(List<Item> list) {
		return (ItemStack s) -> !s.isEmpty() && list.contains(s.getItem());
	}

	public static Comparator<ItemStack> jointComparator(Comparator<ItemStack> finalComparator, Comparator<ItemStack>[] otherComparators) {
		if (otherComparators == null)
			return jointComparator(finalComparator);

		Comparator<ItemStack>[] resizedArray = Arrays.copyOf(otherComparators, otherComparators.length + 1);
		resizedArray[otherComparators.length] = finalComparator;
		return jointComparator(resizedArray);
	}

	@SafeVarargs
	public static Comparator<ItemStack> jointComparator(Comparator<ItemStack>... comparators) {
		return jointComparatorFallback((ItemStack s1, ItemStack s2) -> {
			for (Comparator<ItemStack> comparator : comparators) {
				if (comparator == null)
					continue;

				int compare = comparator.compare(s1, s2);
				if (compare == 0)
					continue;

				return compare;
			}

			return 0;
		}, FALLBACK_COMPARATOR);
	}

	private static Comparator<ItemStack> jointComparatorFallback(Comparator<ItemStack> comparator, Comparator<ItemStack> fallback) {
		return (ItemStack s1, ItemStack s2) -> {
			int compare = comparator.compare(s1, s2);
			if (compare == 0)
				return fallback == null ? 0 : fallback.compare(s1, s2);

			return compare;
		};
	}

	private static Comparator<ItemStack> listOrderComparator(List<Item> list) {
		return (ItemStack stack1, ItemStack stack2) -> {
			Item i1 = stack1.getItem();
			Item i2 = stack2.getItem();
			if (list.contains(i1)) {
				if (list.contains(i2))
					return list.indexOf(i1) - list.indexOf(i2);
				return 1;
			}

			if (list.contains(i2))
				return -1;

			return 0;
		};
	}

	private static List<Item> list(Object... items) {
		List<Item> itemList = new ArrayList<>();
		for (Object o : items)
			if (o != null) {
				if (o instanceof Item)
					itemList.add((Item) o);
				else if (o instanceof Block)
					itemList.add(((Block) o).asItem());
				else if (o instanceof ItemStack)
					itemList.add(((ItemStack) o).getItem());
				else if (o instanceof String) {
					Registry.ITEM.getOrEmpty(new Identifier((String) o)).ifPresent(itemList::add);
				}
			}

		return itemList;
	}

	private static int foodHealCompare(ItemStack stack1, ItemStack stack2) {
		return stack2.getItem().getFoodComponent().getHunger() - stack1.getItem().getFoodComponent().getHunger();
	}

	private static int foodSaturationCompare(ItemStack stack1, ItemStack stack2) {
		return (int) (stack2.getItem().getFoodComponent().getSaturationModifier() * 100 - stack1.getItem().getFoodComponent().getSaturationModifier() * 100);
	}

	private static int enchantmentCompare(ItemStack stack1, ItemStack stack2) {
		return enchantmentPower(stack2) - enchantmentPower(stack1);
	}

	private static int enchantmentPower(ItemStack stack) {
		if (!stack.hasEnchantments())
			return 0;

		Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
		int total = 0;

		for (Integer i : enchantments.values())
			total += i;

		return total;
	}

	private static int toolPowerCompare(ItemStack stack1, ItemStack stack2) {
		ToolMaterial mat1 = ((MiningToolItem) stack1.getItem()).getMaterial();
		ToolMaterial mat2 = ((MiningToolItem) stack2.getItem()).getMaterial();
		return (int) (mat2.getMiningSpeedMultiplier() * 100 - mat1.getMiningSpeedMultiplier() * 100);
	}

	private static int swordPowerCompare(ItemStack stack1, ItemStack stack2) {
		ToolMaterial mat1 = ((SwordItem) stack1.getItem()).getMaterial();
		ToolMaterial mat2 = ((SwordItem) stack2.getItem()).getMaterial();
		return (int) (mat2.getAttackDamage() * 100 - mat1.getAttackDamage() * 100);
	}

	private static int armorSlotAndToughnessCompare(ItemStack stack1, ItemStack stack2) {
		ArmorItem armor1 = (ArmorItem) stack1.getItem();
		ArmorItem armor2 = (ArmorItem) stack2.getItem();

		EquipmentSlot slot1 = armor1.getSlotType();
		EquipmentSlot slot2 = armor2.getSlotType();

		if (slot1 == slot2)
			return armor2.getMaterial().getProtectionAmount(slot2) - armor2.getMaterial().getProtectionAmount(slot1);

		return slot2.getEntitySlotId() - slot1.getEntitySlotId();
	}

	public static int damageCompare(ItemStack stack1, ItemStack stack2) {
		return stack1.getDamage() - stack2.getDamage();
	}
	
	static boolean hasCustomSorting(ItemStack stack) {
		return stack.getCapability(QuarkCapabilities.SORTING, null).isPresent();
	}

	static ICustomSorting getCustomSorting(ItemStack stack) {
		return stack.getCapability(QuarkCapabilities.SORTING, null).orElse(null);
	}

	private enum ItemType {

		FOOD(ItemStack::isFood, FOOD_COMPARATOR),
		TORCH(list(Blocks.TORCH)),
		TOOL_PICKAXE(classPredicate(PickaxeItem.class), TOOL_COMPARATOR),
		TOOL_SHOVEL(classPredicate(ShovelItem.class), TOOL_COMPARATOR),
		TOOL_AXE(classPredicate(AxeItem.class), TOOL_COMPARATOR),
		TOOL_SWORD(classPredicate(SwordItem.class), SWORD_COMPARATOR),
		TOOL_GENERIC(classPredicate(MiningToolItem.class), TOOL_COMPARATOR),
		ARMOR(classPredicate(ArmorItem.class), ARMOR_COMPARATOR),
		BOW(classPredicate(BowItem.class), BOW_COMPARATOR),
		CROSSBOW(classPredicate(CrossbowItem.class), BOW_COMPARATOR),
		TRIDENT(classPredicate(TridentItem.class), BOW_COMPARATOR),
		ARROWS(classPredicate(ArrowItem.class)),
		POTION(classPredicate(PotionItem.class)),
		MINECART(classPredicate(MinecartItem.class)),
		RAIL(list(Blocks.RAIL, Blocks.POWERED_RAIL, Blocks.DETECTOR_RAIL, Blocks.ACTIVATOR_RAIL)),
		DYE(classPredicate(DyeItem.class)),
		ANY(inverseClassPredicate(BlockItem.class)),
		BLOCK(classPredicate(BlockItem.class));

		private final Predicate<ItemStack> predicate;
		private final Comparator<ItemStack> comparator;

		@SafeVarargs
		@SuppressWarnings("varargs")
		ItemType(List<Item> list, Comparator<ItemStack>... comparators) {
			this(itemPredicate(list), jointComparator(listOrderComparator(list), comparators));
		}

		ItemType(Predicate<ItemStack> predicate) {
			this(predicate, FALLBACK_COMPARATOR);
		}

		ItemType(Predicate<ItemStack> predicate, Comparator<ItemStack> comparator) {
			this.predicate = predicate;
			this.comparator = comparator;
		}

		public boolean fitsInType(ItemStack stack) {
			return predicate.test(stack);
		}

	}

}

