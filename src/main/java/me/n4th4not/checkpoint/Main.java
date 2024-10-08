package me.n4th4not.checkpoint;

import cpw.mods.modlauncher.Environment;
import me.n4th4not.checkpoint.client.render.block.WaystoneModel;
import me.n4th4not.checkpoint.client.render.block.WaystoneRenderer;
import me.n4th4not.checkpoint.level.block.BrokenWaystoneBlock;
import me.n4th4not.checkpoint.level.block.BrokenWaystoneEntity;
import me.n4th4not.checkpoint.level.block.WaystoneBlock;
import me.n4th4not.checkpoint.level.block.WaystoneEntity;
import me.n4th4not.checkpoint.listener.PlayerListener;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@Mod.EventBusSubscriber
@Mod(Main.ID)
public class Main {
    public static final String ID = "checkpoint";

    /**
     * Registries
     */
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ID);

    /**
     * Items
     */
    private static final Item.Properties WAYSTONE_BLOCK_ITEM_PROPERTIES = new Item.Properties().stacksTo(64).tab(CreativeModeTab.TAB_DECORATIONS);
    private static final Item.Properties MATERIAL_ITEM_PROPERTIES = new Item.Properties().stacksTo(64).tab(CreativeModeTab.TAB_MATERIALS);
    private static <T extends Item> RegistryObject<T> registerItem(String name, Supplier<T> item) {
        return ITEMS.register(name, item);
    }

    public static final RegistryObject<Item> WAYSTONE_CORE = registerItem("waystone_core", () -> new Item(MATERIAL_ITEM_PROPERTIES));

    /**
     * Blocks
     */
    public static final RegistryObject<WaystoneBlock> WAYSTONE = registerBlockWithItem("waystone", WaystoneBlock::new, WAYSTONE_BLOCK_ITEM_PROPERTIES);
    public static final RegistryObject<BlockEntityType<WaystoneEntity>> WAYSTONE_TILE = registerTile("waystone", () -> BlockEntityType.Builder.of(WaystoneEntity::new, WAYSTONE.get()).build(null));

    public static final RegistryObject<BrokenWaystoneBlock> BROKEN_WAYSTONE = registerBlockWithItem("broken_waystone", BrokenWaystoneBlock::new, WAYSTONE_BLOCK_ITEM_PROPERTIES);
    public static final RegistryObject<BlockEntityType<BrokenWaystoneEntity>> BROKEN_WAYSTONE_TILE = registerTile("broken_waystone", () -> BlockEntityType.Builder.of(BrokenWaystoneEntity::new, BROKEN_WAYSTONE.get()).build(null));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> block, Item.Properties prop) {
        RegistryObject<T> result = registerBlock(name, block);
        registerItem(name, supplify(result, prop));
        return result;
    }
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerTile(String name, Supplier<BlockEntityType<T>> build) {
        return TILES.register(name, build);
    }

    private static <T extends Block> Supplier<BlockItem> supplify(Supplier<T> block, Item.Properties prop) {
        return () -> new BlockItem(block.get(), prop);
    }

    /**
     * Entities
     */
    private static <T extends Entity> RegistryObject<EntityType<T>> registerEntity(String name, EntityType.Builder<T> build, MobCategory cat) {
        return ENTITIES.register(name, () -> build.build(name));
    }

    /**
     * Renderers
     */

    public static final ResourceLocation SHEET = new ResourceLocation("textures/atlas/signs.png");
    public static ModelLayerLocation WAYSTONE_MODEL;

    private static ModelLayerLocation createModel(String name) {
        return new ModelLayerLocation(new ResourceLocation(ID, name), "body");
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WAYSTONE_TILE.get(), WaystoneRenderer::new);
    }

    private void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition((WAYSTONE_MODEL = createModel("waystone")), () ->  WaystoneModel.createLayer(CubeDeformation.NONE));
    }

    private void registerTexture(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(SHEET)) event.addSprite(WaystoneRenderer.GLYPHS);
    }



    public Main() {
        if (ModList.get().isLoaded("waystones")) {
            throw new IllegalStateException("Remove 'Checkpoint' mod,this mod, from your 'mods' folder because your are decided to use 'Waystones' because it is not possible to use both");
        }
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onConstructRegistries);

        if (FMLEnvironment.dist.isClient()) {
            bus.addListener(this::registerRenderers);
            bus.addListener(this::registerLayer);
            bus.addListener(this::registerTexture);
        }

        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, PlayerListener::onSetSpawn);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, PlayerListener::onSentChunk);
    }

    void onConstructRegistries(FMLConstructModEvent event) {
        event.enqueueWork(AdditionalGameRules::register);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        TILES.register(bus);
        ITEMS.register(bus);
    }
}
