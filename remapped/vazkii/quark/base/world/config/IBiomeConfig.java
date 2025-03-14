package vazkii.quark.base.world.config;

import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import vazkii.quark.base.module.config.IConfigType;

public interface IBiomeConfig extends IConfigType {

	public default boolean canSpawn(Biome b) {
		return canSpawn(b.getRegistryName(), b.getCategory());
	}
	
	public boolean canSpawn(Identifier res, Biome.Category category);
	
}
