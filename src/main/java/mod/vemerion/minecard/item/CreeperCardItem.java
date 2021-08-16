package mod.vemerion.minecard.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class CreeperCardItem extends CardItem {

	public CreeperCardItem() {
		super(() -> EntityType.CREEPER);
	}

	@Override
	public int getUseDuration(ItemStack pStack) {
		return 20;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
		if (!(pLivingEntity instanceof Player player && player.isCreative()))
			pStack.shrink(1);
		pLevel.explode(pLivingEntity, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), 3,
				Explosion.BlockInteraction.DESTROY);
		return pStack;
	}

	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
		ItemStack stack = pPlayer.getItemInHand(pHand);
		pPlayer.startUsingItem(pHand);
		return pLevel.isClientSide ? InteractionResultHolder.pass(stack) : InteractionResultHolder.consume(stack);
	}
}
