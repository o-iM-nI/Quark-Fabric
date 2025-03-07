package vazkii.quark.addons.oddities.tile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.addons.oddities.container.EnchantmentMatrix;
import vazkii.quark.addons.oddities.container.MatrixEnchantingContainer;
import vazkii.quark.addons.oddities.container.EnchantmentMatrix.Piece;
import vazkii.quark.addons.oddities.module.MatrixEnchantingModule;
import vazkii.quark.api.IEnchantmentInfluencer;

public class MatrixEnchantingTableTileEntity extends BaseEnchantingTableTile implements NamedScreenHandlerFactory {

	public static final int OPER_ADD = 0;
	public static final int OPER_PLACE = 1;
	public static final int OPER_REMOVE = 2;
	public static final int OPER_ROTATE = 3;
	public static final int OPER_MERGE = 4;

	public static final String TAG_STACK_MATRIX = "quark:enchantingMatrix";
	
	private static final String TAG_MATRIX = "matrix";
	private static final String TAG_MATRIX_UUID_LESS = "uuidLess";
	private static final String TAG_MATRIX_UUID_MOST = "uuidMost";
	private static final String TAG_CHARGE = "charge";

	public EnchantmentMatrix matrix;
	private boolean matrixDirty = false;
	public boolean clientMatrixDirty = false;
	private UUID matrixId;

	public final Map<Enchantment, Integer> influences = new HashMap<>();
	public int bookshelfPower, enchantability, charge;

	public MatrixEnchantingTableTileEntity() {
		super(MatrixEnchantingModule.tileEntityType);
	}
	
	@Override
	public void tick() {
		super.tick();

		ItemStack item = getStack(0);
		if(item.isEmpty()) {
			if(matrix != null) {
				matrixDirty = true;
				matrix = null;
			}
		} else {
			loadMatrix(item);

			if(world.getTime() % 20 == 0 || matrixDirty)
				updateEnchantPower();
		}
		
		if(charge <= 0 && !world.isClient) {
			ItemStack lapis = getStack(1);
			if(!lapis.isEmpty()) {
				lapis.decrement(1);
				charge += MatrixEnchantingModule.chargePerLapis;
				sync();
			}
		}

		if(matrixDirty) {
			makeOutput();
			matrixDirty = false;
		}
	}

	public void onOperation(PlayerEntity player, int operation, int arg0, int arg1, int arg2) {
		if(matrix == null)
			return;

		switch(operation) {
		case OPER_ADD:
			apply(m -> generateAndPay(m, player));
			break;
		case OPER_PLACE:
			apply(m -> m.place(arg0, arg1, arg2));
			break;
		case OPER_REMOVE:
			apply(m -> m.remove(arg0));
			break;
		case OPER_ROTATE:
			apply(m -> m.rotate(arg0));
			break;
		case OPER_MERGE:
			apply(m -> m.merge(arg0, arg1));
			break;
		}
	}

	private void apply(Predicate<EnchantmentMatrix> oper) {
		if(oper.test(matrix)) {
			ItemStack item = getStack(0);
			commitMatrix(item);
		}
	}

	private boolean generateAndPay(EnchantmentMatrix matrix, PlayerEntity player) {
		if(matrix.canGeneratePiece(bookshelfPower, enchantability) && matrix.validateXp(player, bookshelfPower)) {
			boolean creative = player.isCreative();
			int cost = matrix.getNewPiecePrice();
			if(charge > 0 || creative) {
				if (matrix.generatePiece(influences, bookshelfPower)) {
					if (!creative) {
						player.addExperienceLevels(-cost);
						charge = Math.max(charge - 1, 0);
					}
				}
			}
		}

		return true;
	}

	private void makeOutput() {
		if(world.isClient)
			return;

		setStack(2, ItemStack.EMPTY);
		ItemStack in = getStack(0);
		if(!in.isEmpty() && matrix != null && !matrix.placedPieces.isEmpty()) {
			ItemStack out = in.copy();
			boolean book = false;
			if(out.getItem() == Items.BOOK) {
				out = new ItemStack(Items.ENCHANTED_BOOK);
				book = true;
			}

			Map<Enchantment, Integer> enchantments = new HashMap<>();

			for(int i : matrix.placedPieces) {
				Piece p = matrix.pieces.get(i);

				if (p != null && p.enchant != null) {
					for (Enchantment o : enchantments.keySet())
						if (o == p.enchant || !p.enchant.canCombine(o) || !o.canCombine(p.enchant))
							return; // Incompatible

					enchantments.put(p.enchant, p.level);
				}
			}

			if(book) 
				for(Entry<Enchantment, Integer> e : enchantments.entrySet())
					EnchantedBookItem.addEnchantment(out, new EnchantmentLevelEntry(e.getKey(), e.getValue()));
			else {
				EnchantmentHelper.set(enchantments, out);
				ItemNBTHelper.getNBT(out).remove(TAG_STACK_MATRIX);
			}

			setStack(2, out);
		}
	}

	private void loadMatrix(ItemStack stack) {
		if(matrix == null || matrix.target != stack) {
			if(matrix != null)
				matrixDirty = true;
			matrix = null;
			
			if(stack.isEnchantable()) {
				matrix = new EnchantmentMatrix(stack, world.random);
				matrixDirty = true;
				makeUUID();

				if(ItemNBTHelper.verifyExistence(stack, TAG_STACK_MATRIX)) {
					CompoundTag cmp = ItemNBTHelper.getCompound(stack, TAG_STACK_MATRIX, true);
					if(cmp != null)
						matrix.readFromNBT(cmp);
				}
			}
		}
	}

	private void commitMatrix(ItemStack stack) {
		if(world.isClient)
			return;

		CompoundTag cmp = new CompoundTag();
		matrix.writeToNBT(cmp);
		ItemNBTHelper.setCompound(stack, TAG_STACK_MATRIX, cmp);

		matrixDirty = true;
		makeUUID();
		sync();
	}

	private void makeUUID() {
		if(!world.isClient)
			matrixId = UUID.randomUUID();
	}

	private void updateEnchantPower() {
		ItemStack item = getStack(0);
		influences.clear();
		if(item.isEmpty())
			return;

		enchantability = item.getItem().getItemEnchantability(item);

		boolean allowWater = MatrixEnchantingModule.allowUnderwaterEnchanting;
		float power = 0;
		for (int j = -1; j <= 1; ++j) {
			for (int k = -1; k <= 1; ++k) {
				if(isAirGap(j, k, allowWater)) {
					power += getEnchantPowerAt(world, pos.add(k * 2, 0, j * 2));
					power += getEnchantPowerAt(world, pos.add(k * 2, 1, j * 2));
					if (k != 0 && j != 0) {
						power += getEnchantPowerAt(world, pos.add(k * 2, 0, j));
						power += getEnchantPowerAt(world, pos.add(k * 2, 1, j));
						power += getEnchantPowerAt(world, pos.add(k, 0, j * 2));
						power += getEnchantPowerAt(world, pos.add(k, 1, j * 2));
					}
				}
			}
		}

		bookshelfPower = Math.min((int) power, MatrixEnchantingModule.maxBookshelves);
	}
	
	private boolean isAirGap(int j, int k, boolean allowWater) {
		if(j != 0 || k != 0) {
			BlockPos test = pos.add(k, 0, j);
			BlockPos testUp = test.up();
			
			return (world.isAir(test) || (allowWater && world.getBlockState(test).getBlock() == Blocks.WATER))
					&& (world.isAir(testUp) || (allowWater && world.getBlockState(testUp).getBlock() == Blocks.WATER));
		}
		
		return false;
	}
	
	private float getEnchantPowerAt(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		if(MatrixEnchantingModule.allowInfluencing) {
			Block block = state.getBlock();
			if(block instanceof IEnchantmentInfluencer) {
				IEnchantmentInfluencer influencer = (IEnchantmentInfluencer) block;
				DyeColor ord = influencer.getEnchantmentInfluenceColor(world, pos, state);
				int count = influencer.getInfluenceStack(world, pos, state);
				
				if(ord != null) {
					List<Enchantment> influencedEnchants = MatrixEnchantingModule.candleInfluences.get(ord);
					for(Enchantment e : influencedEnchants) {
						int curr = influences.getOrDefault(e, 0);
						if(curr < MatrixEnchantingModule.influenceMax)
							influences.put(e, Math.min(MatrixEnchantingModule.influenceMax, curr + count));
					}
				}
			}
		}
		
		return state.getEnchantPowerBonus(world, pos);
	}

	@Override
	public void writeSharedNBT(CompoundTag cmp) {
		super.writeSharedNBT(cmp);

		CompoundTag matrixCmp = new CompoundTag();
		if(matrix != null) {
			matrix.writeToNBT(matrixCmp);

			cmp.put(TAG_MATRIX, matrixCmp);
			if(matrixId != null) {
				cmp.putLong(TAG_MATRIX_UUID_LESS, matrixId.getLeastSignificantBits());
				cmp.putLong(TAG_MATRIX_UUID_MOST, matrixId.getMostSignificantBits());
			}
		}
		cmp.putInt(TAG_CHARGE, charge);
	}

	@Override
	public void readSharedNBT(CompoundTag cmp) {
		super.readSharedNBT(cmp);

		if(cmp.contains(TAG_MATRIX)) {
			long least = cmp.getLong(TAG_MATRIX_UUID_LESS);
			long most = cmp.getLong(TAG_MATRIX_UUID_MOST);
			UUID newId = new UUID(most, least);

			if(!newId.equals(matrixId)) {
				CompoundTag matrixCmp = cmp.getCompound(TAG_MATRIX);
				matrixId = newId;
				matrix = new EnchantmentMatrix(getStack(0), new Random());
				matrix.readFromNBT(matrixCmp);
			}
			clientMatrixDirty = true;
		} else matrix = null;
		
		charge = cmp.getInt(TAG_CHARGE);
	}

	@Override
	public ScreenHandler createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new MatrixEnchantingContainer(id, inv, this);
	}

	@Override
	public Text getDisplayName() {
		return getName();
	}

}
