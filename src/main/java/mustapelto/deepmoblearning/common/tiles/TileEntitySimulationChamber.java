package mustapelto.deepmoblearning.common.tiles;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.common.DMLConfig;
import mustapelto.deepmoblearning.common.inventory.ItemHandlerDataModel;
import mustapelto.deepmoblearning.common.inventory.ItemHandlerInputWrapper;
import mustapelto.deepmoblearning.common.inventory.ItemHandlerOutput;
import mustapelto.deepmoblearning.common.inventory.ItemHandlerPolymerClay;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModel;
import mustapelto.deepmoblearning.common.util.CraftingState;
import mustapelto.deepmoblearning.common.util.DataModelHelper;
import mustapelto.deepmoblearning.common.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

public class TileEntitySimulationChamber extends TileEntityMachine {
    private final ItemHandlerDataModel inputDataModel = new ItemHandlerDataModel() {
        @Override
        protected void onContentsChanged(int slot) {
            onDataModelChanged();
        }
    };
    private final ItemHandlerInputWrapper dataModelWrapper = new ItemHandlerInputWrapper(inputDataModel);
    private final ItemHandlerPolymerClay inputPolymer = new ItemHandlerPolymerClay();
    private final ItemHandlerInputWrapper polymerWrapper = new ItemHandlerInputWrapper(inputPolymer);
    private final ItemHandlerOutput outputLiving = new ItemHandlerOutput();
    private final ItemHandlerOutput outputPristine = new ItemHandlerOutput();

    private boolean pristineSuccess = false;

    public TileEntitySimulationChamber() {
        super(DMLConstants.SimulationChamber.ENERGY_CAPACITY, DMLConstants.SimulationChamber.ENERGY_IN_MAX);
    }

    //
    // CRAFTING
    //

    @Override
    protected void startCrafting() {
        super.startCrafting();

        // Calculate Pristine Matter success
        int pristineChance = DataModelHelper.getPristineChance(getDataModel());
        int random = ThreadLocalRandom.current().nextInt(100);
        pristineSuccess = (random < pristineChance);

        // Consume Polymer Clay
        inputPolymer.voidItem();
    }

    @Override
    protected boolean canStartCrafting() {
        return super.canStartCrafting() && hasDataModel() && hasPolymerClay() && canDataModelSimulate() && !isLivingMatterOutputFull() && !isPristineMatterOutputFull();
    }

    @Override
    protected void finishCrafting() {
        super.finishCrafting();

        ItemStack dataModel = getDataModel();

        MetadataDataModel dataModelMetadata = DataModelHelper.getDataModelMetadata(dataModel);
        if (dataModelMetadata == null)
            return;

        DataModelHelper.addSimulation(dataModel);

        ItemStack oldLivingMatterOutput = outputLiving.getStackInSlot(0);
        ItemStack newLivingMatterOutput = dataModelMetadata.getLivingMatter(oldLivingMatterOutput.getCount() + 1);
        outputLiving.setStackInSlot(0, newLivingMatterOutput);

        if (pristineSuccess) {
            ItemStack oldPristineMatterOutput = outputPristine.getStackInSlot(0);
            ItemStack newPristineMatterOutput = dataModelMetadata.getPristineMatter(oldPristineMatterOutput.getCount() + 1);
            outputPristine.setStackInSlot(0, newPristineMatterOutput);
        }
    }

    @Override
    protected void resetCrafting() {
        super.resetCrafting();
        pristineSuccess = false;
    }

    @Override
    protected int getCraftingDuration() {
        return DMLConfig.GENERAL_SETTINGS.SIMULATION_CHAMBER_PROCESSING_TIME;
    }

    public boolean isPristineSuccess() {
        return pristineSuccess;
    }

    @Override
    public int getCraftingEnergyCost() {
        return DataModelHelper.getSimulationEnergy(getDataModel());
    }

    @Override
    protected CraftingState updateCraftingState() {
        if (!hasDataModel())
            return CraftingState.IDLE;
        else if (!canStartCrafting() || !canContinueCrafting())
            return CraftingState.ERROR;

        return CraftingState.RUNNING;
    }

    //
    // INVENTORY
    //

    /**
     * (Server only) Reset simulation state on data model change.
     */
    private void onDataModelChanged() {
        if (!world.isRemote) {
            resetCrafting();
        }
    }

    public ItemStack getDataModel() {
        return inputDataModel.getStackInSlot(0);
    }

    public boolean hasDataModel() {
        return ItemStackHelper.isDataModel(getDataModel());
    }

    public boolean canDataModelSimulate() {
        return DataModelHelper.canSimulate(getDataModel());
    }

    public ItemStack getPolymerClay() {
        return inputPolymer.getStackInSlot(0);
    }

    public boolean hasPolymerClay() {
        return ItemStackHelper.isPolymerClay(getPolymerClay());
    }

    public boolean isLivingMatterOutputFull() {
        ItemStack livingMatterStack = outputLiving.getStackInSlot(0);

        if (livingMatterStack.isEmpty())
            return false;

        boolean stackIsFull = (livingMatterStack.getCount() == outputLiving.getSlotLimit(0));
        boolean stackMatchesDataModel = DataModelHelper.getDataModelMatchesLivingMatter(getDataModel(), livingMatterStack);

        return (stackIsFull || !stackMatchesDataModel);
    }

    public boolean isPristineMatterOutputFull() {
        ItemStack pristineMatterStack = outputPristine.getStackInSlot(0);

        if (pristineMatterStack.isEmpty())
            return false;

        boolean stackIsFull = (pristineMatterStack.getCount() == outputPristine.getSlotLimit(0));
        boolean stackMatchesDataModel = DataModelHelper.getDataModelMatchesPristineMatter(getDataModel(), pristineMatterStack);

        return (stackIsFull || !stackMatchesDataModel);
    }

    //
    // CAPABILITIES
    //

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) ||
                super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                        new CombinedInvWrapper(inputDataModel, inputPolymer, outputLiving, outputPristine)
                );
            } else {
                if (!DMLConfig.GENERAL_SETTINGS.LEGACY_MACHINE_SIDEDNESS) {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                            new CombinedInvWrapper(dataModelWrapper, polymerWrapper, outputLiving, outputPristine)
                    );
                } else {
                    if (facing == EnumFacing.UP) {
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new CombinedInvWrapper(inputDataModel, inputPolymer));
                    } else {
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new CombinedInvWrapper(outputPristine, outputLiving));
                    }
                }
            }
        }

        return super.getCapability(capability, facing);
    }

    //
    // CLIENT/SERVER SYNC
    //

    @Override
    public ByteBuf getUpdateData() {
        ByteBuf buf = super.getUpdateData();

        buf.writeBoolean(pristineSuccess);

        return buf;
    }

    @Override
    public void handleUpdateData(ByteBuf buf) {
        super.handleUpdateData(buf);

        pristineSuccess = buf.readBoolean();
    }

    //
    // NBT WRITE/READ
    //

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTTagCompound inventory = new NBTTagCompound();
        inventory.setTag(INPUT_DATA_MODEL, inputDataModel.serializeNBT());
        inventory.setTag(INPUT_POLYMER, inputPolymer.serializeNBT());
        inventory.setTag(OUTPUT_LIVING, outputLiving.serializeNBT());
        inventory.setTag(OUTPUT_PRISTINE, outputPristine.serializeNBT());
        compound.setTag(INVENTORY, inventory);

        compound.getCompoundTag(CRAFTING).setBoolean(PRISTINE_SUCCESS, pristineSuccess);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (isLegacyNBT(compound)) {
            readLegacyNBT(compound);
        } else {
            NBTTagCompound inventory = compound.getCompoundTag(INVENTORY);
            inputDataModel.deserializeNBT(inventory.getCompoundTag(INPUT_DATA_MODEL));
            inputPolymer.deserializeNBT(inventory.getCompoundTag(INPUT_POLYMER));
            outputLiving.deserializeNBT(inventory.getCompoundTag(OUTPUT_LIVING));
            outputPristine.deserializeNBT(inventory.getCompoundTag(OUTPUT_PRISTINE));

            pristineSuccess = NBTHelper.getBoolean(compound.getCompoundTag(CRAFTING), PRISTINE_SUCCESS, false);
        }
    }

    @Override
    protected boolean isLegacyNBT(NBTTagCompound compound) {
        return compound.hasKey(LEGACY_DATA_MODEL) ||
                compound.hasKey(LEGACY_POLYMER) ||
                compound.hasKey(LEGACY_LIVING) ||
                compound.hasKey(LEGACY_PRISTINE) ||
                compound.hasKey(LEGACY_PRISTINE_SUCCESS);
    }

    @Override
    protected void readLegacyNBT(NBTTagCompound compound) {


        pristineSuccess = compound.getBoolean(LEGACY_PRISTINE_SUCCESS);
    }

    // NBT Tag Names
    private static final String INPUT_DATA_MODEL = "inputDataModel";
    private static final String INPUT_POLYMER = "inputPolymer";
    private static final String OUTPUT_LIVING = "outputLiving";
    private static final String OUTPUT_PRISTINE = "outputPristine";
    private static final String PRISTINE_SUCCESS = "pristineSuccess";

    // Tag names from old mod, for backwards compatibility
    private static final String LEGACY_DATA_MODEL = "dataModel";
    private static final String LEGACY_POLYMER = "polymer";
    private static final String LEGACY_LIVING = "lOutput";
    private static final String LEGACY_PRISTINE = "pOutput";
    private static final String LEGACY_PRISTINE_SUCCESS = "craftSuccess";
}
