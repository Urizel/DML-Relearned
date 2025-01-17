package mustapelto.deepmoblearning.common.inventory;

import mustapelto.deepmoblearning.common.util.TrialKeyHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotTrialKey extends SlotItemHandler {
    public SlotTrialKey(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return TrialKeyHelper.isAttuned(stack);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
