package vazkii.quark.content.world.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.multichunk.MultiChunkFeatureGenerator;
import vazkii.quark.content.world.module.ChorusVegetationModule;

public class ChorusVegetationGenerator extends MultiChunkFeatureGenerator {

	public ChorusVegetationGenerator() {
		super(DimensionConfig.end(false), () -> true, 2093);
	}
	
	@Override
	public int getFeatureRadius() {
		return ChorusVegetationModule.radius;
	}

	@Override
	public BlockPos[] getSourcesInChunk(ChunkRegion world, Random random, ChunkGenerator generator, BlockPos chunkCorner) {
		if(!chunkCorner.isWithinDistance(Vec3i.ZERO, 1050) && ChorusVegetationModule.rarity > 0 && random.nextInt(ChorusVegetationModule.rarity) == 0) {
			Biome b = getBiome(world, chunkCorner);
			if(b.getRegistryName().equals(BiomeKeys.END_HIGHLANDS.getValue()))
				return new BlockPos[] { chunkCorner };
		}
		
		return new BlockPos[0];
	}
	
	@Override
	public void generateChunkPart(BlockPos src, ChunkGenerator generator, Random rand, BlockPos pos, ChunkRegion worldIn) {
		for(int i = 0; i < ChorusVegetationModule.chunkAttempts; i++) {
			BlockPos placePos = pos.add(rand.nextInt(16), 100, rand.nextInt(16));
			
			Biome b = getBiome(worldIn, placePos);
			double chance = getChance(b);
			
			double dist = ((placePos.getX() - src.getX()) * (placePos.getX() - src.getX())) + ((placePos.getZ() - src.getZ()) * (placePos.getZ() - src.getZ()));
			int ditherStart = 6;
			
			ditherStart *= ditherStart;
			if(dist > ditherStart)
				chance *= (1 - (Math.atan((dist - ditherStart) / 50) / (Math.PI / 2)));
			
			if(chance > 0 && rand.nextDouble() < chance) {
				while(placePos.getY() > 40) {
					BlockState stateAt = worldIn.getBlockState(placePos);
					if(stateAt.getBlock() == Blocks.END_STONE)
						break;
					
					placePos = placePos.down();
				}
				
				if(worldIn.getBlockState(placePos).getBlock() == Blocks.END_STONE && worldIn.getBlockState(placePos.up()).isAir()) {
					Block block = (rand.nextDouble() < 0.1) ? ChorusVegetationModule.chorus_twist : ChorusVegetationModule.chorus_weeds;
					worldIn.setBlockState(placePos.up(), block.getDefaultState(), 2);
				}
			}
		}
	}
	
	private double getChance(Biome b) {
		Identifier res = b.getRegistryName();
		
		if(res.equals(BiomeKeys.END_HIGHLANDS.getValue()))
			return ChorusVegetationModule.highlandsChance;
		else if(res.equals(BiomeKeys.END_MIDLANDS.getValue()))
			return ChorusVegetationModule.midlandsChance;
		else return ChorusVegetationModule.otherEndBiomesChance;
	}


}
