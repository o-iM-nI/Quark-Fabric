package vazkii.quark.base.handler;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author WireSegal
 * Created at 11:01 AM on 9/1/19.
 */
public class SimilarBlockTypeHandler {

    public static List<String> getBasicShulkerBoxes() {
        return ImmutableSet.of(Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX,
                Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX,
                Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX,
                Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX)
                .stream().map(IForgeRegistryEntry<Block>::getRegistryName).map(Objects::toString).collect(Collectors.toList());
    }

    public static boolean isShulkerBox(ItemStack stack) {
        return isShulkerBox(stack.getItem().getRegistryName()) && !stack.isEmpty() && stack.getMaxCount() == 1;
    }

    public static boolean isShulkerBox(Identifier loc) {
        if (loc == null)
            return false;

        String locStr = loc.toString();

        if (GeneralConfig.shulkerBoxes.contains(locStr))
            return true;

        return GeneralConfig.interpretShulkerBoxLikeBlocks && locStr.contains("shulker_box");
    }
}
