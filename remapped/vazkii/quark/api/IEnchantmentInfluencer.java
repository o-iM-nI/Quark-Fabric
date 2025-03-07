package vazkii.quark.api;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Implement on a Block to make it influence matrix enchanting
 */
public interface IEnchantmentInfluencer {

	@Nullable DyeColor getEnchantmentInfluenceColor(BlockView world, BlockPos pos, BlockState state);
	
	default int getInfluenceStack(BlockView world, BlockPos pos, BlockState state) {
		return 1;
	}
	
}
