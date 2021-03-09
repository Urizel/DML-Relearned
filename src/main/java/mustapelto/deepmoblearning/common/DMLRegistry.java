package mustapelto.deepmoblearning.common;

import com.google.common.collect.ImmutableMap;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.common.blocks.*;
import mustapelto.deepmoblearning.common.entities.EntityItemGlitchFragment;
import mustapelto.deepmoblearning.common.items.*;
import mustapelto.deepmoblearning.common.metadata.MetadataManagerDataModels;
import mustapelto.deepmoblearning.common.metadata.MetadataManagerLivingMatter;
import mustapelto.deepmoblearning.common.tiles.TileEntityLootFabricator;
import mustapelto.deepmoblearning.common.tiles.TileEntitySimulationChamber;
import mustapelto.deepmoblearning.common.tiles.TileEntityTrialKeystone;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber
public class DMLRegistry {
    public static final NonNullList<Item> registeredItems = NonNullList.create();
    public static final NonNullList<BlockBase> registeredBlocks = NonNullList.create();

    // Dynamic items, referenced by ID
    private static ImmutableMap<String, ItemLivingMatter> registeredLivingMatter;
    private static ImmutableMap<String, ItemPristineMatter> registeredPristineMatter;

    // Blocks
    public static final BlockInfusedIngot BLOCK_INFUSED_INGOT = new BlockInfusedIngot();
    public static final BlockMachineCasing BLOCK_MACHINE_CASING = new BlockMachineCasing();
    public static final BlockSimulationChamber BLOCK_SIMULATION_CHAMBER = new BlockSimulationChamber();
    public static final BlockLootFabricator BLOCK_LOOT_FABRICATOR = new BlockLootFabricator();
    public static final BlockTrialKeystone BLOCK_TRIAL_KEYSTONE = new BlockTrialKeystone();

    // Items
    public static final ItemDataModel ITEM_DATA_MODEL = new ItemDataModel();
    public static final ItemDeepLearner ITEM_DEEP_LEARNER = new ItemDeepLearner();
    public static final ItemPolymerClay ITEM_POLYMER_CLAY = new ItemPolymerClay();
    public static final ItemCreativeModelLearner ITEM_CREATIVE_MODEL_LEARNER = new ItemCreativeModelLearner();
    public static final ItemSootedRedstone ITEM_SOOTED_REDSTONE = new ItemSootedRedstone();
    public static final ItemSootedPlate ITEM_SOOTED_PLATE = new ItemSootedPlate();
    public static final ItemGlitchIngot ITEM_GLITCH_INGOT = new ItemGlitchIngot();
    public static final ItemGlitchFragment ITEM_GLITCH_FRAGMENT = new ItemGlitchFragment();
    public static final ItemGlitchHeart ITEM_GLITCH_HEART = new ItemGlitchHeart();
    public static final ItemTrialKey ITEM_TRIAL_KEY = new ItemTrialKey();

    // Armor and Weapons
    public static final ItemGlitchArmor.ItemGlitchHelmet ITEM_GLITCH_HELMET = new ItemGlitchArmor.ItemGlitchHelmet();
    public static final ItemGlitchArmor.ItemGlitchChestplate ITEM_GLITCH_CHESTPLATE = new ItemGlitchArmor.ItemGlitchChestplate();
    public static final ItemGlitchArmor.ItemGlitchLeggings ITEM_GLITCH_LEGGINGS = new ItemGlitchArmor.ItemGlitchLeggings();
    public static final ItemGlitchArmor.ItemGlitchBoots ITEM_GLITCH_BOOTS = new ItemGlitchArmor.ItemGlitchBoots();
    public static final ItemGlitchSword ITEM_GLITCH_SWORD = new ItemGlitchSword();

    // Entity ID
    private static int entityId = 0;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        DMLRelearned.logger.info("Registering Blocks...");

        registeredBlocks.add(BLOCK_INFUSED_INGOT);
        registeredBlocks.add(BLOCK_MACHINE_CASING);
        registeredBlocks.add(BLOCK_SIMULATION_CHAMBER);
        registeredBlocks.add(BLOCK_LOOT_FABRICATOR);
        registeredBlocks.add(BLOCK_TRIAL_KEYSTONE);

        IForgeRegistry<Block> registry = event.getRegistry();
        registeredBlocks.forEach(registry::register);

        // Register tile entities
        GameRegistry.registerTileEntity(TileEntitySimulationChamber.class, new ResourceLocation(DMLConstants.ModInfo.ID, "simulation_chamber"));
        GameRegistry.registerTileEntity(TileEntityLootFabricator.class, new ResourceLocation(DMLConstants.ModInfo.ID, "extraction_chamber"));
        GameRegistry.registerTileEntity(TileEntityTrialKeystone.class, new ResourceLocation(DMLConstants.ModInfo.ID, "trial_keystone"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        DMLRelearned.logger.info("Registering Items...");

        // Misc Items
        registeredItems.add(ITEM_DATA_MODEL);
        registeredItems.add(ITEM_POLYMER_CLAY);
        registeredItems.add(ITEM_DEEP_LEARNER);
        registeredItems.add(ITEM_CREATIVE_MODEL_LEARNER);
        registeredItems.add(ITEM_SOOTED_REDSTONE);
        registeredItems.add(ITEM_SOOTED_PLATE);
        registeredItems.add(ITEM_GLITCH_INGOT);
        registeredItems.add(ITEM_GLITCH_FRAGMENT);
        registeredItems.add(ITEM_GLITCH_HEART);
        registeredItems.add(ITEM_TRIAL_KEY);

        // Glitch Armor and Sword
        registeredItems.add(ITEM_GLITCH_HELMET);
        registeredItems.add(ITEM_GLITCH_CHESTPLATE);
        registeredItems.add(ITEM_GLITCH_LEGGINGS);
        registeredItems.add(ITEM_GLITCH_BOOTS);
        registeredItems.add(ITEM_GLITCH_SWORD);

        DMLRelearned.logger.info("Registering Living Matter...");
        ImmutableMap.Builder<String, ItemLivingMatter> livingMatterBuilder = ImmutableMap.builder();
        MetadataManagerLivingMatter.INSTANCE.getDataStore().forEach((key, value) -> livingMatterBuilder.put(key, new ItemLivingMatter(value)));
        registeredLivingMatter = livingMatterBuilder.build();
        registeredItems.addAll(registeredLivingMatter.values());

        DMLRelearned.logger.info("Registering Data Models and Pristine Matter...");
        ImmutableMap.Builder<String, ItemPristineMatter> pristineMatterBuilder = ImmutableMap.builder();
        MetadataManagerDataModels.INSTANCE.getDataStore().forEach((key, value) -> {
            pristineMatterBuilder.put(key, new ItemPristineMatter(value));
        });
        registeredPristineMatter = pristineMatterBuilder.build();
        registeredItems.addAll(registeredPristineMatter.values());

        IForgeRegistry<Item> registry = event.getRegistry();
        registeredItems.forEach(registry::register);

        // Register ItemBlocks
        registeredBlocks.forEach(block -> registry.register(block.getItemBlock()));
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> registry = event.getRegistry();

        final ResourceLocation itemGlitchFragmentRegistryName = new ResourceLocation(DMLConstants.ModInfo.ID, "item_glitch_fragment");

        EntityEntry itemGlitchFragment = EntityEntryBuilder.create()
                .entity(EntityItemGlitchFragment.class)
                .id(itemGlitchFragmentRegistryName, entityId++)
                .name(itemGlitchFragmentRegistryName.getResourcePath())
                .tracker(64, 1, true)
                .build();

        registry.registerAll(itemGlitchFragment);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        DMLRelearned.logger.info("Registering Dynamic Recipes...");
        IForgeRegistry<IRecipe> registry = event.getRegistry();
        registry.registerAll(MetadataManagerDataModels.INSTANCE.getCraftingRecipes().toArray(new IRecipe[0]));
    }

    public static ItemStack getLivingMatter(String id) {
        if (registeredLivingMatter.containsKey(id))
            return new ItemStack(registeredLivingMatter.get(id));
        if (registeredLivingMatter.size() > 0)
            return new ItemStack(registeredLivingMatter.values().asList().get(0));
        return ItemStack.EMPTY;
    }

    public static ItemStack getPristineMatter(String id) {
        if (registeredPristineMatter.containsKey(id))
            return new ItemStack(registeredPristineMatter.get(id));
        return ItemStack.EMPTY;
    }
}
