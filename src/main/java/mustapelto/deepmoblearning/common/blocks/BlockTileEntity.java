package mustapelto.deepmoblearning.common.blocks;

import mustapelto.deepmoblearning.DMLRelearned;
import mustapelto.deepmoblearning.common.tiles.TileEntityMachine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public abstract class BlockTileEntity extends BlockBase {
    protected static final PropertyDirection FACING = BlockHorizontal.FACING;
    private final int GUI_ID;

    /**
     * @param name     Block id (for internal use)
     * @param material Material the block behaves like
     */
    protected BlockTileEntity(String name, Material material, int guiId) {
        super(name, material);
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        GUI_ID = guiId;
    }

    //
    // TileEntity
    //

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public abstract TileEntity createTileEntity(World world, IBlockState state);

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = Objects.requireNonNull(worldIn.getTileEntity(pos));
        IItemHandler inventory = Objects.requireNonNull(tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null));

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                EntityItem item = new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
                item.setDefaultPickupDelay();
                worldIn.spawnEntity(item);
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote)
            return false;

        ItemStack mainHand = playerIn.getHeldItemMainhand();
        boolean isHoldingWrench = mainHand.getItem().getToolClasses(mainHand).contains("wrench");

        if (!playerIn.isSneaking() && !isHoldingWrench)
            playerIn.openGui(DMLRelearned.instance, GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    //
    // React to block changes
    //

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityMachine) {
            ((TileEntityMachine) tile).onBlockPlaced();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityMachine) {
            ((TileEntityMachine) tile).onNeighborChange();
        }
    }

    //
    // Facing
    //

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    @Nonnull
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
}
