package vazkii.quark.content.automation.module;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.automation.block.RedstoneInductorBlock;
import vazkii.quark.content.automation.block.RedstoneRandomizerBlock;

/**
 * @author WireSegal
 * Created at 10:34 AM on 8/26/19.
 */
@LoadModule(category = ModuleCategory.AUTOMATION)
public class RedstoneCircuitModule extends QuarkModule {

    @Config(flag = "redstone_randomizer") public static boolean enableRandomizer = true;
    @Config(flag = "redstone_inductor") public static boolean enableInductor = true;

    @Override
    public void construct() {
        new RedstoneRandomizerBlock("redstone_randomizer", this, ItemGroup.REDSTONE,
                Block.Properties.of(Material.SUPPORTED).strength(0).sounds(BlockSoundGroup.WOOD));
        new RedstoneInductorBlock("redstone_inductor", this, ItemGroup.REDSTONE,
                Block.Properties.of(Material.SUPPORTED).strength(0).sounds(BlockSoundGroup.WOOD));
    }
}
