package vazkii.quark.content.world.module;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GenerationStep.Feature;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.content.world.block.MonsterBoxBlock;
import vazkii.quark.content.world.gen.MonsterBoxGenerator;
import vazkii.quark.content.world.tile.MonsterBoxTileEntity;
	
@LoadModule(category = ModuleCategory.WORLD, hasSubscriptions = true)
public class MonsterBoxModule extends QuarkModule {

	public static final String TAG_MONSTER_BOX_SPAWNED = "quark:monster_box_spawned";
	public static final Identifier MONSTER_BOX_LOOT_TABLE = new Identifier(Quark.MOD_ID, "misc/monster_box");
	
	public static BlockEntityType<MonsterBoxTileEntity> tileEntityType;
	
	@Config(description = "The chance for the monster box generator to try and place one in a chunk, 1 is 100%\nThis can be higher than 100% if you want multiple per chunk, , 0 is 0%") 
	public static double chancePerChunk = 0.8;
	
	@Config public static int minY = 5;
	@Config public static int maxY = 30;
	@Config public static int minMobCount = 5;
	@Config public static int maxMobCount = 8;
	@Config public static DimensionConfig dimensions = DimensionConfig.overworld(false);
	@Config public static boolean enableExtraLootTable = true;
	
	public static Block monster_box = null;
	
	@Override
	public void construct() {
		monster_box = new MonsterBoxBlock(this);
		
        tileEntityType = BlockEntityType.Builder.create(MonsterBoxTileEntity::new, monster_box).build(null);
        RegistryHelper.register(tileEntityType, "monster_box");
	}
	
	@Override
	public void setup() {
		WorldGenHandler.addGenerator(this, new MonsterBoxGenerator(dimensions), Feature.UNDERGROUND_DECORATION, WorldGenWeights.MONSTER_BOXES);
	}
	
	@SubscribeEvent
	public void onDrops(LivingDropsEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if(enableExtraLootTable && entity.getEntityWorld() instanceof ServerWorld && entity.getPersistentData().getBoolean(TAG_MONSTER_BOX_SPAWNED) && entity.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
			LootTable loot = ((ServerWorld) entity.getEntityWorld()).getServer().getLootManager().getTable(MONSTER_BOX_LOOT_TABLE);
			if(loot != null)
				loot.generateLoot(new LootContext.Builder((ServerWorld) entity.getEntityWorld()).build(LootContextTypes.EMPTY), entity::dropStack);
		}
	}
	
}
