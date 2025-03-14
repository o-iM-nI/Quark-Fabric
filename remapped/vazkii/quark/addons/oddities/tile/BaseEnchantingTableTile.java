package vazkii.quark.addons.oddities.tile;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Nameable;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.MathHelper;
import vazkii.arl.block.tile.TileSimpleInventory;
import vazkii.quark.base.handler.MiscUtil;

import javax.annotation.Nonnull;
import java.util.Random;

public abstract class BaseEnchantingTableTile extends TileSimpleInventory implements Tickable, Nameable {

	public int tickCount;
	public float pageFlip, pageFlipPrev, flipT, flipA, bookSpread, bookSpreadPrev, bookRotation, bookRotationPrev, tRot;

	private static final Random rand = new Random();
	private Text customName;
	
	public BaseEnchantingTableTile(BlockEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public int size() {
		return 3;
	}

	@Override
	public boolean isAutomationEnabled() {
		return false;
	}

	@Nonnull
	@Override
	public CompoundTag toTag(CompoundTag compound) {
		super.toTag(compound);

		if(hasCustomName())
			compound.putString("CustomName", Text.Serializer.toJson(customName));

		return compound;
	}

	@Override 
	public void fromTag(BlockState p_230337_1_, CompoundTag compound) {
		super.fromTag(p_230337_1_, compound);

		if(compound.contains("CustomName", 8))
			customName = Text.Serializer.fromJson(compound.getString("CustomName"));
	}

	@Override
	public void tick() {
		performVanillaUpdate();
	}

	private void performVanillaUpdate() {
		this.bookSpreadPrev = this.bookSpread;
		this.bookRotationPrev = this.bookRotation;
		PlayerEntity entityplayer = this.world.getClosestPlayer((this.pos.getX() + 0.5F), (this.pos.getY() + 0.5F), (this.pos.getZ() + 0.5F), 3.0D, false);

		if (entityplayer != null)
		{
			double d0 = entityplayer.getX() - (this.pos.getX() + 0.5F);
			double d1 = entityplayer.getZ() - (this.pos.getZ() + 0.5F);
			this.tRot = (float)MathHelper.atan2(d1, d0);
			this.bookSpread += 0.1F;

			if (this.bookSpread < 0.5F || rand.nextInt(40) == 0)
			{
				float f1 = this.flipT;

				do {
					this.flipT += (rand.nextInt(4) - rand.nextInt(4));
				} while (!(f1 != this.flipT));
			}
		}
		else
		{
			this.tRot += 0.02F;
			this.bookSpread -= 0.1F;
		}

		while (this.bookRotation >= (float)Math.PI)
		{
			this.bookRotation -= ((float)Math.PI * 2F);
		}

		while (this.bookRotation < -(float)Math.PI)
		{
			this.bookRotation += ((float)Math.PI * 2F);
		}

		while (this.tRot >= (float)Math.PI)
		{
			this.tRot -= ((float)Math.PI * 2F);
		}

		while (this.tRot < -(float)Math.PI)
		{
			this.tRot += ((float)Math.PI * 2F);
		}

		float f2 = this.tRot - this.bookRotation;

		while (f2 >= Math.PI)
			f2 -= (Math.PI * 2F);

		while (f2 < -Math.PI)
			f2 += (Math.PI * 2F);

		this.bookRotation += f2 * 0.4F;
		this.bookSpread = MathHelper.clamp(this.bookSpread, 0.0F, 1.0F);
		++this.tickCount;
		this.pageFlipPrev = this.pageFlip;
		float f = (this.flipT - this.pageFlip) * 0.4F;
		f = MathHelper.clamp(f, -0.2F, 0.2F);
		this.flipA += (f - this.flipA) * 0.9F;
		this.pageFlip += this.flipA;
	}

	public void dropItem(int i) {
		ItemStack stack = getStack(i);
		if(!stack.isEmpty())
			ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
	}

	@Nonnull
	@Override
	public Text getName() {
		return hasCustomName() ? customName : new TranslatableText("container.enchant");
	}

	@Override
	public boolean hasCustomName() {
		return customName != null;
	}

	public void setCustomName(Text customNameIn) {
		customName = customNameIn;
	}

	@Override
	public void inventoryChanged(int i) {
		super.inventoryChanged(i);
		sync();
	}
	
	@Override
	protected boolean needsToSyncInventory() {
		return true;
	}
	
	@Override
	public void sync() {
		MiscUtil.syncTE(this);
	}
	
}
