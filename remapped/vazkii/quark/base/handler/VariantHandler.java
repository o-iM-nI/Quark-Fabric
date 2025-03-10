package vazkii.quark.base.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.block.QuarkSlabBlock;
import vazkii.quark.base.block.QuarkStairsBlock;
import vazkii.quark.base.block.QuarkWallBlock;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;

public class VariantHandler {
	
	public static final List<QuarkSlabBlock> SLABS = new LinkedList<>();
	public static final List<QuarkStairsBlock> STAIRS = new LinkedList<>();
	public static final List<QuarkWallBlock> WALLS = new LinkedList<>();
	
	public static void addSlabStairsWall(IQuarkBlock block) {
		addSlabAndStairs(block);
		addWall(block);
	}
	
	public static void addSlabAndStairs(IQuarkBlock block) {
		addSlab(block);
		addStairs(block);
	}
	
	public static void addSlab(IQuarkBlock block) {
		SLABS.add(new QuarkSlabBlock(block));
	}
	
	public static void addStairs(IQuarkBlock block) {
		STAIRS.add(new QuarkStairsBlock(block));
	}
	
	public static void addWall(IQuarkBlock block) {
		WALLS.add(new QuarkWallBlock(block));
	}

	public static FlowerPotBlock addFlowerPot(Block block, String name, Function<Block.Properties, Block.Properties> propertiesFunc) {
		Block.Properties props = Block.Properties.of(Material.SUPPORTED).strength(0F);
		props = propertiesFunc.apply(props);
		
		FlowerPotBlock potted = new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, block::getBlock, props);
		RenderLayerHandler.setRenderType(potted, RenderTypeSkeleton.CUTOUT);
		Identifier resLoc = block.getBlock().getRegistryName();
		if (resLoc == null)
			resLoc = new Identifier("missingno");
		
		RegistryHelper.registerBlock(potted, "potted_" + name, false);
		((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(resLoc, () -> potted);
		
		return potted;
	}
	
	public static AbstractBlock.Settings realStateCopy(IQuarkBlock parent) {
		AbstractBlock.Settings props = AbstractBlock.Settings.copy(parent.getBlock());
		if(parent instanceof IVariantsShouldBeEmissive)
			props = props.emissiveLighting((s, r, p) -> true);
		
		return props;
	}
	
	public static interface IVariantsShouldBeEmissive {}

}
