package mustapelto.deepmoblearning.common.items;

import mustapelto.deepmoblearning.common.DMLRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemGlitchIngot extends ItemBase {
    public ItemGlitchIngot() {
        super("glitch_infused_ingot", 64);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        String glitchFragment = new ItemStack(DMLRegistry.ITEM_GLITCH_FRAGMENT).getDisplayName();
        tooltip.add(I18n.format("deepmoblearning.glitch_ingot.tooltip_1", glitchFragment));
        tooltip.add(I18n.format("deepmoblearning.glitch_ingot.tooltip_2"));
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(ItemStack stack) {
        return TextFormatting.AQUA + super.getItemStackDisplayName(stack) + TextFormatting.RESET;
    }
}
