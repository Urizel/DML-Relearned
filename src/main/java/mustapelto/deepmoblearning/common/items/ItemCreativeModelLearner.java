package mustapelto.deepmoblearning.common.items;

import mustapelto.deepmoblearning.client.util.KeyboardHelper;
import mustapelto.deepmoblearning.common.network.DMLPacketHandler;
import mustapelto.deepmoblearning.common.network.MessageLevelUpModel;
import mustapelto.deepmoblearning.common.util.DataModelHelper.CreativeLevelUpAction;
import mustapelto.deepmoblearning.common.util.StringHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemCreativeModelLearner extends ItemBase {
    public ItemCreativeModelLearner() {
        super("creative_model_learner", 1);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (playerIn.world.isRemote) {
            CreativeLevelUpAction action;
            if (KeyboardHelper.isHoldingSneakKey())
                action = CreativeLevelUpAction.INCREASE_TIER;
            else if (KeyboardHelper.isHoldingSprintKey())
                action = CreativeLevelUpAction.DECREASE_TIER;
            else
                action = CreativeLevelUpAction.INCREASE_KILLS;

            DMLPacketHandler.network.sendToServer(new MessageLevelUpModel(action));
        }

        return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (!KeyboardHelper.isHoldingSneakKey()) {
            String sneakString = TextFormatting.RESET + "" + TextFormatting.ITALIC + KeyboardHelper.getSneakKeyName() + TextFormatting.RESET + "" + TextFormatting.GRAY;
            tooltip.add(TextFormatting.GRAY + I18n.format("deepmoblearning.general.more_info", sneakString) + TextFormatting.RESET);
        } else {
            String sneakName = KeyboardHelper.getSneakKeyName();
            String sprintName = KeyboardHelper.getSprintKeyName();
            String useName = KeyboardHelper.getUseKeyName();

            String increaseTier = StringHelper.getFormattedString(TextFormatting.ITALIC, sneakName + " + " + useName, TextFormatting.GRAY);
            String decreaseTier = StringHelper.getFormattedString(TextFormatting.ITALIC, sprintName + " + " + useName, TextFormatting.GRAY);
            String increaseKills = StringHelper.getFormattedString(TextFormatting.ITALIC, useName, TextFormatting.GRAY);
            tooltip.add(I18n.format("deepmoblearning.creative_model_learner.increase_tier", increaseTier));
            tooltip.add(I18n.format("deepmoblearning.creative_model_learner.decrease_tier", decreaseTier));
            tooltip.add(I18n.format("deepmoblearning.creative_model_learner.increase_kills", increaseKills));
        }
    }
}
