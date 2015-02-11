package com.lulan.shincolle.ai;

import com.lulan.shincolle.entity.BasicEntityShip;
import com.lulan.shincolle.reference.AttrID;
import com.lulan.shincolle.utility.LogHelper;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
/**SHIP FOLLOW OWNER AI
 * �|������Howner, ���|�]���䤣����|�N������H
 * �Z���W�Lmax dist��Ĳ�o����, ���쨫�imin dist�Z���ɰ���
 * �Z���W�L40��|����teleport��owner����
 * 
 * @parm entity, move speed, min dist, max dist
 */
public class EntityAIShipFollowOwner extends EntityAIBase {
    private BasicEntityShip ThePet;
    private EntityLivingBase TheOwner;
    World TheWorld;
    private static final double TP_DIST = 3000D;	//teleport condition ~ 54 blocks
    private PathNavigate PetPathfinder;
    private int FindCooldown;
    private double MaxDistSq;
    private double MinDistSq;
    private double DistSqToOwner;
    
    //���u�e�i���\��
    private double distX, distY, distZ, motX, motY, motZ;	//��ؼЪ����u�Z��(������)
    private float rotYaw;

    public EntityAIShipFollowOwner(BasicEntityShip entity, float MinDist, float MaxDist) {
        this.ThePet = entity;
        this.TheWorld = entity.worldObj;
        this.PetPathfinder = entity.getNavigator();
        this.MinDistSq = MinDist * MinDist;
        this.MaxDistSq = MaxDist * MaxDist;
        this.DistSqToOwner = 0D;
        this.setMutexBits(3);
    }

    //��owner�B�ؼжW�Lmax dist��Ĳ�oAI, Ĳ�o�ᦹ��k���A����, �אּ�������cont exec
    public boolean shouldExecute() {
        EntityLivingBase OwnerEntity = this.ThePet.getOwner();

        //get owner distance
        if(OwnerEntity != null) {
        	this.TheOwner = OwnerEntity;
        	
        	//�p�⪽�u�Z��
        	this.distX = this.TheOwner.posX - this.ThePet.posX;
    		this.distY = this.TheOwner.posY - this.ThePet.posY - 1;
    		this.distZ = this.TheOwner.posZ - this.ThePet.posZ;
        	this.DistSqToOwner = this.distX*this.distX + this.distY*this.distY + this.distZ*this.distZ;

        	if(!this.ThePet.isSitting() && !this.ThePet.getLeashed() && DistSqToOwner > this.MaxDistSq) {
                return true;
        	}
        }
        
        return false;
    }

    //�ؼ��٨S����min dist�Ϊ̶Z���W�LTP_DIST���~��AI
    public boolean continueExecuting() {
    	//�p�⪽�u�Z��
    	this.distX = this.TheOwner.posX - this.ThePet.posX;
		this.distY = this.TheOwner.posY - this.ThePet.posY - 1;
		this.distZ = this.TheOwner.posZ - this.ThePet.posZ;
    	this.DistSqToOwner = this.distX*this.distX + this.distY*this.distY + this.distZ*this.distZ;
    	
    	//�Z���W�L�ǰe�Z��
    	if(this.DistSqToOwner > this.TP_DIST) {
    		return true;
    	}

    	if(this.DistSqToOwner > this.MinDistSq && !this.ThePet.isSitting()) {
    		if(this.ThePet.getShipDepth() > 0D) {	//�Ω�G�餤����, �G�餤��path�g�`false
    			return true;
    		}
    		
    		if(!this.PetPathfinder.noPath()) {		//�Ω󳰤W����, path find�i���`�B�@
    			return true;
    		}
    	}
    	return false;
    }

    public void startExecuting() {
    	this.rotYaw = 0F;
        this.FindCooldown = 0;
        this.PetPathfinder.setAvoidsWater(false);
        this.PetPathfinder.setEnterDoors(true);
        this.PetPathfinder.setCanSwim(true);
    }

    public void resetTask() {
        this.TheOwner = null;
        this.PetPathfinder.clearPathEntity();
    }

    public void updateTask() {
    	this.FindCooldown--;
    	this.motY = 0D;
    	
    	//�]�w�Y����V
        this.ThePet.getLookHelper().setLookPositionWithEntity(this.TheOwner, 10.0F, (float)this.ThePet.getVerticalFaceSpeed());

        if(!this.ThePet.isSitting() && !this.ThePet.getLeashed()) {
        	//�Z���W�L�ǰe�Z��, �����ǰe��ؼФW
        	if(this.DistSqToOwner > this.TP_DIST) {
        		this.ThePet.posX = this.TheOwner.posX;
        		this.ThePet.posY = this.TheOwner.posY + 0.5D;
        		this.ThePet.posZ = this.TheOwner.posZ;
        		this.ThePet.setPosition(this.ThePet.posX, this.ThePet.posY, this.ThePet.posZ);
        	}
        	
    		//�b�G�餤, �Ī��u����
        	if(this.ThePet.getShipDepth() > 0D) {
        		//�B�~�[�Wy�b�t��, getPathToXYZ��Ů��G�����L��, �]��y�b�t�׭n�t�~�[
        		if(this.distY > 1.5D && this.ThePet.getShipDepth() > 1.5D) {  //�קK�����u��
        			this.motY = 0.2F;
        		}
        		else if(this.distY < -1D) {
        			this.motY = -0.2F;
        		}
        		
        		//�Y���u�i��, �h�������u����
        		if(this.ThePet.getEntitySenses().canSee(this.TheOwner)) {
        			double PetSpeed = this.ThePet.getFinalState(AttrID.MOV);
        			this.motX = (this.distX / this.DistSqToOwner) * PetSpeed * 6D;
        			this.motZ = (this.distZ / this.DistSqToOwner) * PetSpeed * 6D;

        			this.ThePet.motionY = this.motY;
        			this.ThePet.getMoveHelper().setMoveTo(this.ThePet.posX+this.motX, this.ThePet.posY+this.motY, this.ThePet.posZ+this.motZ, 1D);
        			
        			//�Y��������F��, �h���ո���
	        		if(this.ThePet.isCollidedHorizontally) {
	        			this.ThePet.setPosition(this.ThePet.posX, this.ThePet.posY + 0.3D, this.ThePet.posZ);
	        		}
        			return;
        		}
           	}
        	
        	//�Ccd���@�����|
        	if(this.FindCooldown <= 0) {
    			this.FindCooldown = 10;

            	if(!this.PetPathfinder.tryMoveToEntityLiving(this.TheOwner, 1D)) {
            		LogHelper.info("DEBUG : AI try move fail, teleport entity");
            		if(this.DistSqToOwner > this.MaxDistSq) {
                    	//���Ƨ�18���ؼЪ����H���I, �����ǰe��ت��a
                    	int offsetX, offsetZ, i, j, k;
                    	Block targetBlock;
                    	for(int t=0; t<18; t++) {
                    		offsetX = this.TheWorld.rand.nextInt(7);
                        	offsetZ = this.TheWorld.rand.nextInt(7);
                            i = (int)(this.TheOwner.posX) - 3 + offsetX;
                            j = (int)(this.TheOwner.posZ) - 3 + offsetZ;
                            k = (int)(this.TheOwner.boundingBox.minY) - 1;
                            targetBlock = this.TheWorld.getBlock(i, k, j);
                            //bug: getBlock always get air from water block
                            if(targetBlock != null && 
                               (targetBlock.isSideSolid(TheWorld, i, k, j, ForgeDirection.UP) ||
                            	targetBlock == Blocks.water ||
                            	targetBlock == Blocks.lava)) {
                            	
                            	this.ThePet.setLocationAndAngles((double)(i+0.5D), (double)(k+0.5D), (double)(j+0.5D), this.ThePet.rotationYaw, this.ThePet.rotationPitch);
                            	this.PetPathfinder.clearPathEntity();
                                return;
                            }
                    	}
                    }
                }//end !try move to owner
            }//end path find cooldown
        }
    }
	
	
}