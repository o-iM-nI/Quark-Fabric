package vazkii.quark.content.building.module;

import java.util.Map;

import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.client.render.ColoredItemFrameRenderer;
import vazkii.quark.content.building.client.render.GlassItemFrameRenderer;
import vazkii.quark.content.building.entity.ColoredItemFrameEntity;
import vazkii.quark.content.building.entity.GlassItemFrameEntity;
import vazkii.quark.content.building.item.QuarkItemFrameItem;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 11:00 AM on 8/25/19.
 */
@LoadModule(category = ModuleCategory.BUILDING)
public class ItemFramesModule extends QuarkModule {
    public static Item glassFrame;
    private static Map<DyeColor, Item> coloredFrames = Maps.newEnumMap(DyeColor.class);

    public static EntityType<GlassItemFrameEntity> glassFrameEntity;
    public static EntityType<ColoredItemFrameEntity> coloredFrameEntity;

    public static Item getColoredFrame(DyeColor color) {
        return coloredFrames.getOrDefault(color, Items.ITEM_FRAME);
    }

    @Override
    public void construct() {
        glassFrameEntity = EntityType.Builder.<GlassItemFrameEntity>create(GlassItemFrameEntity::new, SpawnGroup.MISC)
                .setDimensions(0.5F, 0.5F)
                .setTrackingRange(10)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> new GlassItemFrameEntity(glassFrameEntity, world))
                .build("glass_frame");
        RegistryHelper.register(glassFrameEntity, "glass_frame");

        coloredFrameEntity = EntityType.Builder.<ColoredItemFrameEntity>create(ColoredItemFrameEntity::new, SpawnGroup.MISC)
                .setDimensions(0.5F, 0.5F)
                .setTrackingRange(10)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> new ColoredItemFrameEntity(coloredFrameEntity, world))
                .setShouldReceiveVelocityUpdates(false)
                .build("colored_frame");
        RegistryHelper.register(coloredFrameEntity, "colored_frame");

        glassFrame = new QuarkItemFrameItem("glass_item_frame", this, GlassItemFrameEntity::new,
                new Item.Settings().group(ItemGroup.DECORATIONS));

        for(DyeColor color : DyeColor.values())
            coloredFrames.put(color, new QuarkItemFrameItem(color.getName() + "_item_frame", this, // name
                    (world, pos, dir) -> new ColoredItemFrameEntity(world, pos, dir, color.getId()),
                    new Item.Settings().group(ItemGroup.DECORATIONS)));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientSetup() {
        MinecraftClient mc = MinecraftClient.getInstance();

        RenderingRegistry.registerEntityRenderingHandler(glassFrameEntity, (manager) -> new GlassItemFrameRenderer(manager, mc.getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(coloredFrameEntity, (manager) -> new ColoredItemFrameRenderer(manager, mc.getItemRenderer()));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void modelRegistry() {
        ModelLoader.addSpecialModel(new ModelIdentifier(new Identifier(Quark.MOD_ID, "glass_frame"), "inventory"));
        for (DyeColor color : DyeColor.values()) {
            ModelLoader.addSpecialModel(new ModelIdentifier(new Identifier(Quark.MOD_ID, color.asString() + "_frame_empty"), "inventory"));
            ModelLoader.addSpecialModel(new ModelIdentifier(new Identifier(Quark.MOD_ID, color.asString() + "_frame_map"), "inventory"));
        }
    }
}
