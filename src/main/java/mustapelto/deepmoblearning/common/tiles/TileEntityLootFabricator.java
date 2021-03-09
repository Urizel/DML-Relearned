package mustapelto.deepmoblearning.common.tiles;

import io.netty.buffer.ByteBuf;
import mustapelto.deepmoblearning.DMLConstants;
import mustapelto.deepmoblearning.common.DMLConfig;
import mustapelto.deepmoblearning.common.inventory.ItemHandlerInputWrapper;
import mustapelto.deepmoblearning.common.inventory.ItemHandlerOutput;
import mustapelto.deepmoblearning.common.inventory.ItemHandlerPristineMatter;
import mustapelto.deepmoblearning.common.items.ItemPristineMatter;
import mustapelto.deepmoblearning.common.metadata.MetadataDataModel;
import mustapelto.deepmoblearning.common.util.CraftingState;
import mustapelto.deepmoblearning.common.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityLootFabricator extends TileEntityMachine {
    private final ItemHandlerPristineMatter inputPristineMatter = new ItemHandlerPristineMatter() {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if ((slot == 0) && (pristineMatterMetadata != ItemPristineMatter.getDataModelMetadata(getStackInSlot(slot))))
                onPristineTypeChanged();

            markDirty();
        }
    };
    private final ItemHandlerInputWrapper pristineMatterWrapper = new ItemHandlerInputWrapper(inputPristineMatter);

    private final ItemHandlerOutput output = new ItemHandlerOutput(16);

    private MetadataDataModel pristineMatterMetadata;
    private int outputItemIndex = -1; // index of selected output item in Pristine Matter's associated item list. -1 = no item selected

    public TileEntityLootFabricator() {
        super(DMLConstants.LootFabricator.ENERGY_CAPACITY, DMLConstants.LootFabricator.ENERGY_IN_MAX);
    }

    //
    // CRAFTING
    //

    // Loot Fabricator consumes Pristine Matter only when crafting is finished
    // This allows to change output when crafting has already started
    // without losing Pristine Matter

    @Override
    protected boolean canStartCrafting() {
        return super.canStartCrafting() && hasPristineMatter() && hasRoomForOutput() && isValidOutputItem();
    }

    @Override
    protected void finishCrafting() {
        super.finishCrafting();

        ItemStack outputItem = getOutputItem();
        if (outputItem.isEmpty())
            return; // Crafting without selected output (shouldn't happen) i.e. something went wrong -> don't do anything

        output.addItemToAvailableSlots(outputItem);
        inputPristineMatter.voidItem();
    }

    @Override
    protected int getCraftingDuration() {
        return DMLConfig.GENERAL_SETTINGS.LOOT_FABRICATOR_PROCESSING_TIME;
    }

    @Override
    public int getCraftingEnergyCost() {
        return DMLConfig.GENERAL_SETTINGS.LOOT_FABRICATOR_RF_COST;
    }

    public MetadataDataModel getPristineMatterMetadata() {
        return pristineMatterMetadata;
    }

    private ItemStack getOutputItem() {
        if (outputItemIndex == -1)
            return ItemStack.EMPTY;

        if (pristineMatterMetadata == null)
            return ItemStack.EMPTY;

        return pristineMatterMetadata.getLootItem(outputItemIndex);
    }

    @Override
    protected CraftingState updateCraftingState() {
        if (!crafting && !hasPristineMatter())
            return CraftingState.IDLE;
        else if (!canStartCrafting() || !canContinueCrafting())
            return CraftingState.ERROR;

        return CraftingState.RUNNING;
    }

    //
    // INVENTORY
    //

    /**
     * (Server only) Reset crafting state on pristine matter change.
     */
    private void onPristineTypeChanged() {
        outputItemIndex = -1;
        pristineMatterMetadata = inputPristineMatter.getPristineMatterMetadata();
        resetCrafting();
    }

    public boolean hasPristineMatter() {
        return ItemStackHelper.isPristineMatter(inputPristineMatter.getStackInSlot(0));
    }

    public boolean hasRoomForOutput() {
        return output.hasRoomForItem(getOutputItem());
    }

    public void setOutputItemIndex(int index) {
        if (outputItemIndex == index)
            return; // Index didn't change -> do nothing

        outputItemIndex = index;
        if (isInvalidOutputItemIndex())
            outputItemIndex = -1;

        resetCrafting(); // resetCrafting takes care of updating output index to client
    }

    public int getOutputItemIndex() {
        return outputItemIndex;
    }

    private boolean isInvalidOutputItemIndex() {
        return outputItemIndex != -1 && !isValidOutputItem();
    }

    private boolean isValidOutputItem() {
        return !getOutputItem().isEmpty();
    }

    //
    // CAPABILITIES
    //

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) ||
                super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                        new CombinedInvWrapper(inputPristineMatter, output)
                );
            } else {
                if (!DMLConfig.GENERAL_SETTINGS.LEGACY_MACHINE_SIDEDNESS) {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                            new CombinedInvWrapper(pristineMatterWrapper, output)
                    );
                } else {
                    if (facing == EnumFacing.UP) {
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inputPristineMatter);
                    } else {
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(output);
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

        buf.writeInt(outputItemIndex);

        return buf;
    }

    @Override
    public void handleUpdateData(ByteBuf buf) {
        super.handleUpdateData(buf);

        outputItemIndex = buf.readInt();
    }

    //
    // NBT WRITE/READ
    //

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTTagCompound inventory = new NBTTagCompound();
        inventory.setTag(PRISTINE_INPUT, inputPristineMatter.serializeNBT());
        inventory.setTag(OUTPUT, output.serializeNBT());
        compound.setTag(INVENTORY, inventory);

        compound.getCompoundTag(CRAFTING).setInteger(OUTPUT_ITEM_INDEX, outputItemIndex);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (NBTHelper.isLegacyNBT(compound)) {
            // Original DML tag -> use old (non-nested) tag names
            inputPristineMatter.deserializeNBT(compound.getCompoundTag(PRISTINE_OLD));
            output.deserializeNBT(compound.getCompoundTag(OUTPUT));

            // Old system stores ItemStack, too much hassle to read from that so we set to "nothing".
            // I.e. players will have to restart all their loot fabricators once after switching.
            onPristineTypeChanged();
        } else {
            // DML:Relearned tag -> use new (nested) tag names
            NBTTagCompound inventory = compound.getCompoundTag(INVENTORY);
            inputPristineMatter.deserializeNBT(inventory.getCompoundTag(PRISTINE_INPUT));
            output.deserializeNBT(inventory.getCompoundTag(OUTPUT));

            pristineMatterMetadata = inputPristineMatter.getPristineMatterMetadata();
            outputItemIndex = NBTHelper.getInteger(compound.getCompoundTag(CRAFTING), OUTPUT_ITEM_INDEX, -1);

            if (isInvalidOutputItemIndex()) // Invalid index, e.g. config changed between world loads, or something else went wrong
                onPristineTypeChanged();
        }
    }

    // NBT Tag Names
    private static final String PRISTINE_INPUT = "inputPristine";
    private static final String OUTPUT = "output";
    private static final String OUTPUT_ITEM_INDEX = "outputItemIndex";

    private static final String PRISTINE_OLD = "pristine";
}
