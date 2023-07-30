package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardVisibility;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import mod.vemerion.minecard.network.AnimationMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class AnimationAbility extends CardAbility {

	public static final Codec<AnimationAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(ResourceLocation.CODEC.fieldOf("animation").forGetter(AnimationAbility::getAnimation))
					.apply(instance, AnimationAbility::new)));

	private final ResourceLocation animation;

	public AnimationAbility(ResourceLocation animation) {
		super(Set.of());
		this.animation = animation;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.ANIMATION.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			List<Card> collected, ItemStack icon) {
		for (var receiver : receivers) {
			receiver.receiver(new AnimationMessage(card.getId(), collected.stream().filter(c -> {
				return state.getGame().calcVisibility(receiver.getId(), c) == CardVisibility.VISIBLE
						|| state.getGame().calcVisibility(state.getId(), c) == CardVisibility.ENEMY_HAND;
			}).map(c -> c.getId()).collect(Collectors.toList()), animation));
		}
	}

	public ResourceLocation getAnimation() {
		return animation;
	}

}