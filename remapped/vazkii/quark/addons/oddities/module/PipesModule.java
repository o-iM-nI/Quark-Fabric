package vazkii.quark.addons.oddities.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.addons.oddities.block.PipeBlock;
import vazkii.quark.addons.oddities.client.render.PipeTileEntityRenderer;
import vazkii.quark.addons.oddities.tile.PipeTileEntity;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.handler.RequiredModTooltipHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.ODDITIES, requiredMod = Quark.ODDITIES_ID)
public class PipesModule extends QuarkModule {

    public static BlockEntityType<PipeTileEntity> tileEntityType;

	@Config(description = "How long it takes for an item to cross a pipe. Bigger = slower.") 
	private static int pipeSpeed = 5;
	
	@Config(description = "Set to 0 if you don't want pipes to have a max amount of items")
	public static int maxPipeItems = 16;
	
	@Config(description = "When items eject or are absorbed by pipes, should they make sounds?")
	public static boolean doPipesWhoosh = true;
    
	public static Block pipe;
	
	public static int effectivePipeSpeed;
	
    @Override
    public void construct() {
    	pipe = new PipeBlock(this);
    	
    	tileEntityType = BlockEntityType.Builder.create(PipeTileEntity::new, pipe).build(null);
		RegistryHelper.register(tileEntityType, "pipe");
    }
    
    @Override
    public void configChanged() {
    	effectivePipeSpeed = pipeSpeed * 2;
    }

	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		ClientRegistry.bindTileEntityRenderer(tileEntityType, PipeTileEntityRenderer::new);
		
		RequiredModTooltipHandler.map(pipe, Quark.ODDITIES_ID);
	}

    @Override
    @Environment(EnvType.CLIENT)
    public void modelRegistry() {
        ModelLoader.addSpecialModel(new ModelIdentifier(new Identifier(Quark.MOD_ID, "pipe_flare"), "inventory"));
    }
	
}
