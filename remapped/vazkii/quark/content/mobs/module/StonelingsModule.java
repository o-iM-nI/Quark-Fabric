package vazkii.quark.content.mobs.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction.Location;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.EntitySpawnHandler;
import vazkii.quark.base.world.config.BiomeTypeConfig;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.config.EntitySpawnConfig;
import vazkii.quark.content.mobs.client.render.StonelingRenderer;
import vazkii.quark.content.mobs.entity.StonelingEntity;
import vazkii.quark.content.mobs.item.DiamondHeartItem;

@LoadModule(category = ModuleCategory.MOBS, hasSubscriptions = true)
public class StonelingsModule extends QuarkModule {
	
	public static EntityType<StonelingEntity> stonelingType;

	@Config
	public static int maxYLevel = 24;
	@Config
	public static DimensionConfig dimensions = DimensionConfig.overworld(false);
	@Config 
	public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(80, 1, 1, new BiomeTypeConfig(true, Biome.Category.NONE, Biome.Category.NETHER, Biome.Category.THEEND));
	@Config(flag = "stoneling_drop_diamond_heart")
	public static boolean enableDiamondHeart = true;
	@Config
	public static boolean cautiousStonelings = true;
	@Config
	public static boolean tamableStonelings = true;

	public static Item diamondHeart;

	@Override
	public void construct() {
		diamondHeart = new DiamondHeartItem("diamond_heart", this, new Item.Settings().group(ItemGroup.MISC));

		stonelingType = EntityType.Builder.create(StonelingEntity::new, SpawnGroup.CREATURE)
				.setDimensions(0.5F, 0.9F)
				.setTrackingRange(80)
				.setUpdateInterval(3)
				.setShouldReceiveVelocityUpdates(true)
				.setCustomClientFactory((spawnEntity, world) -> new StonelingEntity(stonelingType, world))
				.build("stoneling");
		RegistryHelper.register(stonelingType, "stoneling");

		EntitySpawnHandler.registerSpawn(this, stonelingType, SpawnGroup.MONSTER, Location.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, StonelingEntity::spawnPredicate, spawnConfig);
		EntitySpawnHandler.addEgg(stonelingType, 0xA1A1A1, 0x505050, spawnConfig);
	}
	
	@Override
	public void setup() {
		DefaultAttributeRegistry.put(stonelingType, StonelingEntity.prepareAttributes().build());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(stonelingType, StonelingRenderer::new);
	}

}
