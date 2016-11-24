package com.lulan.shincolle.block;

import javax.annotation.Nullable;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.tileentity.BasicTileLockable;

/** block with tile and facing
 * 
 */
abstract public class BasicBlockFacingContainer extends BasicBlockFacing implements ITileEntityProvider
{
	
	
	public BasicBlockFacingContainer()
	{
		this(Material.ROCK);
	}
	
	public BasicBlockFacingContainer(Material material)
	{
		this(Material.ROCK, Material.ROCK.getMaterialMapColor());
	}

	public BasicBlockFacingContainer(Material material, MapColor color)
    {
        super(material, color);
        this.isBlockContainer = true;
    }
	
	//new tile entity instance in child class 
	@Override
	abstract public TileEntity createNewTileEntity(World world, int i);
	
	/**右鍵點到方塊時呼叫此方法
	 * 參數: world,方塊x,y,z,玩家,玩家面向,玩家點到的x,y,z
	 */	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack item, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		//client端: 只需要收到true
        if (world.isRemote)
        {
            return true;
        }
        
        //server端: 若玩家不是sneaking, 則開啟gui
        if (!player.isSneaking())
        {
        	TileEntity tile = world.getTileEntity(pos);
        	
        	//open gui
        	if (tile instanceof BasicTileLockable && ((BasicTileLockable) tile).getGuiIntID() >= 0)
        	{
        		player.openGui(ShinColle.instance, ((BasicTileLockable) tile).getGuiIntID(), world, pos.getX(), pos.getY(), pos.getZ());
                return true;
        	}
        }

		return false;
    }


}