package mustapelto.deepmoblearning.common.util;

import com.google.common.collect.ImmutableList;
import mustapelto.deepmoblearning.common.items.ItemDataModel;
import mustapelto.deepmoblearning.common.items.ItemDeepLearner;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModel;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModelTier;
import mustapelto.deepmoblearning.common.metadata.MetadataManagerDataModelTiers;
import mustapelto.deepmoblearning.common.metadata.MetadataManagerDataModels;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Helper methods for Data Model ItemStacks
 */
public class DataModelHelper {
    //
    // NBT Getters/Setters
    //

    public static int getTierLevel(ItemStack stack) {
        return NBTHelper.getInteger(stack, "tier", 0);
    }

    public static void setTierLevel(ItemStack stack, int tier) {
        NBTHelper.setInteger(stack, "tier", tier);
    }

    public static int getCurrentTierDataCount(ItemStack stack) {
        if (NBTHelper.hasKey(stack, "simulationCount") || NBTHelper.hasKey(stack, "killCount")) {
            // Update DeepMobLearning NBT to DMLRelearned format
            // i.e. "simulationCount" and "killCount" combined to a single value "dataCount"
            int currentSimulations = NBTHelper.getInteger(stack, "simulationCount", 0);
            int currentKills = NBTHelper.getInteger(stack, "killCount", 0);

            NBTHelper.removeKey(stack, "simulationCount");
            NBTHelper.removeKey(stack, "killCount");

            MetadataDataModelTier tierData = getTierData(stack);
            if (tierData.isInvalid())
                return 0;

            NBTHelper.setInteger(stack, "dataCount", currentSimulations + currentKills * tierData.getKillMultiplier());
        }
        return NBTHelper.getInteger(stack, "dataCount", 0);
    }

    public static void setCurrentTierDataCount(ItemStack stack, int data) {
        NBTHelper.setInteger(stack, "dataCount", data);
    }

    public static int getTotalKillCount(ItemStack stack) {
        return NBTHelper.getInteger(stack, "totalKillCount", 0);
    }

    public static void setTotalKillCount(ItemStack stack, int count) {
        NBTHelper.setInteger(stack, "totalKillCount", count);
    }

    public static int getTotalSimulationCount(ItemStack stack) {
        return NBTHelper.getInteger(stack, "totalSimulationCount", 0);
    }

    public static void setTotalSimulationCount(ItemStack stack, int count) {
        NBTHelper.setInteger(stack, "totalSimulationCount", count);
    }

    //
    // Calculated Getters
    //

    @Nullable
    public static MetadataDataModel getDataModelMetadata(ItemStack stack) {
        String metadataKey = NBTHelper.getString(stack, ItemDataModel.NBT_METADATA_KEY, "");
        return MetadataManagerDataModels.INSTANCE.getByKey(metadataKey);
    }

    @Nonnull
    private static MetadataDataModelTier getTierData(ItemStack stack) {
        return MetadataManagerDataModelTiers.INSTANCE.getByLevel(getTierLevel(stack));
    }

    @Nonnull
    private static MetadataDataModelTier getNextTierData(ItemStack stack) {
        return MetadataManagerDataModelTiers.INSTANCE.getByLevel(getTierLevel(stack) + 1);
    }

    public static boolean isAtMaxTier(ItemStack stack) {
        // also true if "over max" (in case config has been changed on a running world to include fewer tiers)
        return getTierLevel(stack) >= MetadataManagerDataModelTiers.INSTANCE.getMaxLevel();
    }

    public static boolean canSimulate(ItemStack stack) {
        // Can this model be run in a simulation chamber?
        MetadataDataModelTier data = getTierData(stack);
        return !data.isInvalid() && data.getCanSimulate();
    }

    public static String getTierDisplayNameFormatted(ItemStack stack) {
        MetadataDataModelTier data = getTierData(stack);
        return !data.isInvalid() ? data.getDisplayNameFormatted() : "";
    }

    public static String getTierDisplayNameFormatted(ItemStack stack, String template) {
        MetadataDataModelTier data = getTierData(stack);
        return !data.isInvalid() ? data.getDisplayNameFormatted(template) : "";
    }

    public static String getNextTierDisplayNameFormatted(ItemStack stack) {
        MetadataDataModelTier data = getNextTierData(stack);
        return !data.isInvalid() ? data.getDisplayNameFormatted() : "";
    }

    public static int getTierRequiredData(ItemStack stack) {
        MetadataDataModelTier data = getTierData(stack);
        return !data.isInvalid() ? data.getDataToNext() : 0;
    }

    public static int getTierKillMultiplier(ItemStack stack) {
        MetadataDataModelTier data = getTierData(stack);
        return !data.isInvalid() ? data.getKillMultiplier() : 0;
    }

    public static int getKillsToNextTier(ItemStack stack) {
        int dataRequired = getTierRequiredData(stack);
        int dataCurrent = getCurrentTierDataCount(stack);
        int killMultiplier = getTierKillMultiplier(stack);
        return isAtMaxTier(stack) ? 0 : MathHelper.divideAndRoundUp(dataRequired - dataCurrent, killMultiplier);
    }

    public static int getSimulationEnergy(ItemStack stack) {
        MetadataDataModel data = getDataModelMetadata(stack);
        return (data != null) ? data.getSimulationRFCost() : 0;
    }

    public static int getPristineChance(ItemStack stack) {
        MetadataDataModelTier data = getTierData(stack);
        return !data.isInvalid() ? data.getPristineChance() : 0;
    }

    /** Test if Data Model type matches Living Matter type
     *
     * @param dataModel Data Model stack to compare
     * @param livingMatter Living Matter stack to compare
     * @return true if Data Model's associated Living Matter matches Living Matter stack
     */
    public static boolean getDataModelMatchesLivingMatter(ItemStack dataModel, ItemStack livingMatter) {
        MetadataDataModel data = getDataModelMetadata(dataModel);

        return data != null && data.getLivingMatter().isItemEqual(livingMatter);
    }

    /** Test if Data Model type matches Pristine Matter type
     *
     * @param dataModel Data Model stack to compare
     * @param pristineMatter Pristine Matter stack to compare
     * @return true if Data Model's associated Pristine Matter matches Pristine Matter stack
     */
    public static boolean getDataModelMatchesPristineMatter(ItemStack dataModel, ItemStack pristineMatter) {
        MetadataDataModel data = getDataModelMetadata(dataModel);
        return data != null && data.getPristineMatter().isItemEqual(pristineMatter);
    }

    /** Filter out non-data model stacks and return filtered list
     *
     * @param stackList List of ItemStacks to filter
     * @return List of Data Model ItemStacks
      */

    public static ImmutableList<ItemStack> getDataModelStacksFromList(NonNullList<ItemStack> stackList) {
        return stackList.stream()
                .filter(ItemStackHelper::isDataModel)
                .collect(ImmutableList.toImmutableList());
    }

    public static ItemStack getHighestTierDataModelFromList(List<ItemStack> stackList) {
        return stackList.stream()
                .max(Comparator.comparingInt(DataModelHelper::getTierLevel))
                .orElse(ItemStack.EMPTY);
    }

    //
    // Data Manipulation
    //

    public static void addSimulation(ItemStack stack) {
        increaseDataCount(stack, 1);
        setTotalSimulationCount(stack, getTotalSimulationCount(stack) + 1);
        tryIncreaseTier(stack);
    }

    private static void increaseDataCount(ItemStack stack, int amount) {
        int data = getCurrentTierDataCount(stack);
        setCurrentTierDataCount(stack, data + amount);
    }

    public static void addKill(ItemStack stack, EntityPlayerMP player) {
        MetadataDataModelTier tierData = getTierData(stack);
        if (tierData.isInvalid())
            return;

        int increase = tierData.getKillMultiplier();

        // TODO: Trial stuff

        if (ItemStackHelper.isGlitchSword(player.getHeldItemMainhand()) /* && no trial active */)
            increase *= 2;
        increaseDataCount(stack, increase);

        // Update appropriate total count
        setTotalKillCount(stack, getTotalKillCount(stack) + 1);

        if (tryIncreaseTier(stack)) {
            player.sendMessage(
                    new TextComponentTranslation(
                            "deepmoblearning.data_model.reached_tier",
                            stack.getDisplayName(),
                            getTierDisplayNameFormatted(stack)
                    )
            );
        }
    }

    /**
     * Increase tier of data model if current data has reached required data
     *
     * @param stack DataModel stack to process
     * @return true if tier could be increased, otherwise false
     */
    private static boolean tryIncreaseTier(ItemStack stack) {
        if (isAtMaxTier(stack))
            return false;

        int currentData = getCurrentTierDataCount(stack);
        int requiredData = getTierRequiredData(stack);

        if (currentData >= requiredData) {
            setCurrentTierDataCount(stack, currentData - requiredData); // extra data carries over to higher tier
            setTierLevel(stack, getTierLevel(stack) + 1);

            return true;
        }

        return false;
    }

    //
    // Inventory Data Manipulation (e.g. Creative Model Learner)
    //
    public static void findAndLevelUpModels(NonNullList<ItemStack> inventory, EntityPlayerMP player, CreativeLevelUpAction action) {
        for (ItemStack inventoryStack : inventory) {
            if (ItemStackHelper.isDeepLearner(inventoryStack)) {
                NonNullList<ItemStack> deepLearnerContents = ItemDeepLearner.getContainedItems(inventoryStack);
                for (ItemStack modelStack : deepLearnerContents) {
                    if (ItemStackHelper.isDataModel(modelStack)) {
                        int tier = getTierLevel(modelStack);
                        switch (action) {
                            case DECREASE_TIER:
                                if (tier > 0)
                                    setTierLevel(modelStack, tier - 1);
                                break;
                            case INCREASE_TIER:
                                if (!isAtMaxTier(modelStack))
                                    setTierLevel(modelStack, tier + 1);
                                break;
                            case INCREASE_KILLS:
                                if (!isAtMaxTier(modelStack))
                                    addKill(modelStack, player);
                        }
                    }
                }
                ItemDeepLearner.setContainedItems(inventoryStack, deepLearnerContents);
            }
        }
    }

    public enum CreativeLevelUpAction {
        INCREASE_TIER(0),
        INCREASE_KILLS(1),
        DECREASE_TIER(2);

        private final int value;
        private static final HashMap<Integer, CreativeLevelUpAction> map = new HashMap<>();

        CreativeLevelUpAction(int value) {
            this.value = value;
        }

        static {
            for (CreativeLevelUpAction creativeLevelUpAction : CreativeLevelUpAction.values())
                map.put(creativeLevelUpAction.value, creativeLevelUpAction);
        }

        public int toInt() {
            return value;
        }

        public static CreativeLevelUpAction fromInt(int value) {
            return map.get(value);
        }
    }
}
