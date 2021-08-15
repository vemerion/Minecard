package mod.vemerion.minecard.gametest;

import mod.vemerion.minecard.Cards;
import mod.vemerion.minecard.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class CardLootModifierTest {

	@GameTest(template = Main.MODID + ".test")
	public void creeperShouldDropCard(GameTestHelper helper) {
		Player player = helper.makeMockPlayer();
		helper.succeedWhen(() -> {
			if (helper.getLevel()
					.getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(1, 2, 1))),
							e -> e.getItem().getItem() == Cards.CREEPER_CARD)
					.isEmpty())
				throw new GameTestAssertException("Expected " + Cards.CREEPER_CARD.getRegistryName() + " to exist");
		});
		for (int i = 0; i < 20; i++) {
			helper.runAtTickTime(i * 3, () -> {
				for (int j = 0; j < 5; j++) {
					Creeper creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, new BlockPos(1, 2, 1));
					creeper.hurt(DamageSource.playerAttack(player), Float.MAX_VALUE);
				}
			});
		}
	}
}
