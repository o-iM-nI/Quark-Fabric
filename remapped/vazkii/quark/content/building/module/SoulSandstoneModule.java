package vazkii.quark.content.building.module;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING)
public class SoulSandstoneModule extends QuarkModule {

	@Override
	public void construct() {
		Block.Properties props = Block.Properties.of(Material.STONE, MaterialColor.BROWN)
				.requiresTool()
        		.harvestTool(ToolType.PICKAXE)
        		.strength(0.8F);
		
		VariantHandler.addSlabStairsWall(new QuarkBlock("soul_sandstone", this, ItemGroup.BUILDING_BLOCKS, props));
		new QuarkBlock("chiseled_soul_sandstone", this, ItemGroup.BUILDING_BLOCKS, props);
		VariantHandler.addSlab(new QuarkBlock("cut_soul_sandstone", this, ItemGroup.BUILDING_BLOCKS, props));
		VariantHandler.addSlabAndStairs(new QuarkBlock("smooth_soul_sandstone", this, ItemGroup.BUILDING_BLOCKS, props));
	}
	
}
