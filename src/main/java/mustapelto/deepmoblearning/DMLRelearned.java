package mustapelto.deepmoblearning;

import mustapelto.deepmoblearning.common.DMLGuiHandler;
import mustapelto.deepmoblearning.common.DMLRegistry;
import mustapelto.deepmoblearning.common.ServerProxy;
import mustapelto.deepmoblearning.common.capabilities.CapabilityPlayerTrial;
import mustapelto.deepmoblearning.common.capabilities.CapabilityPlayerTrialStorage;
import mustapelto.deepmoblearning.common.capabilities.ICapabilityPlayerTrial;
import mustapelto.deepmoblearning.common.metadata.MetadataManager;
import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(modid = DMLConstants.ModInfo.ID, name = DMLConstants.ModInfo.NAME, version = DMLConstants.ModInfo.VERSION,
    dependencies = DMLConstants.ModDependencies.DEP_STRING)
public class DMLRelearned
{
    @Mod.Instance(DMLConstants.ModInfo.ID)
    public static DMLRelearned instance;

    public static Logger logger;

    @SidedProxy(
            clientSide = "mustapelto.deepmoblearning.client.ClientProxy",
            serverSide = "mustapelto.deepmoblearning.common.ServerProxy"
    )
    public static ServerProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();


        // Initialize Metadata Manager (copy/read config files and deserialize JSON)
        try {
            MetadataManager.init(event);
        } catch (IOException e) {
            DMLRelearned.logger.fatal("File IO error while creating/reading mod config files! This mod will not function properly. {}", e.getMessage());
        }

        // Network Stuff
        DMLPacketHandler.registerPackets();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new DMLGuiHandler());

        // Initialize Capabilities
        CapabilityManager.INSTANCE.register(ICapabilityPlayerTrial.class, new CapabilityPlayerTrialStorage(), CapabilityPlayerTrial::new);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.registerGuiRenderers();

        // Finalize Metadata (create recipes and other stuff that may depend on other mods' items being registered)
        MetadataManager.finalizeData();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {

    }

    public static final CreativeTabs creativeTab = new CreativeTabs(DMLConstants.ModInfo.ID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(DMLRegistry.ITEM_DEEP_LEARNER);
        }
    };
}

