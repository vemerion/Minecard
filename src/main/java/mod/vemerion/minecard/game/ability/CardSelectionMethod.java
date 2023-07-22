package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameState;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardSelectionMethods;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class CardSelectionMethod {

	public static final Codec<CardSelectionMethod> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> ModCardSelectionMethods.getRegistry().getCodec().dispatch("type",
					CardSelectionMethod::getType, CardSelectionMethodType::codec));

	private Component description;

	private CardSelectionMethod() {
	}

	protected abstract CardSelectionMethodType<?> getType();

	protected abstract Object[] getDescriptionArgs();

	public Component getDescription() {
		if (description == null) {
			description = new TranslatableComponent(getType().getTranslationKey(), getDescriptionArgs());
		}
		return description;
	}

	public abstract List<Card> select(GameState state, CardAbility ability, List<Card> candidates);

	public abstract void createChoice(List<Receiver> receivers, CardAbility ability, PlayerState state,
			List<Card> candidates);

	public static class CardSelectionMethodType<T extends CardSelectionMethod>
			extends ForgeRegistryEntry<CardSelectionMethodType<?>> {
		private final Codec<T> codec;

		public CardSelectionMethodType(Codec<T> codec) {
			this.codec = codec;
		}

		Codec<T> codec() {
			return codec;
		}

		public String getTranslationKey() {
			return Util.makeDescriptionId(ModCardSelectionMethods.CARD_SELECTION_METHODS.getRegistryName().getPath(),
					getRegistryName());
		}
	}

	public static class All extends CardSelectionMethod {

		public static final All ALL = new All();

		public static final Codec<All> CODEC = Codec.unit(ALL);

		public All() {
		}

		@Override
		protected CardSelectionMethodType<?> getType() {
			return ModCardSelectionMethods.ALL.get();
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] {};
		}

		@Override
		public List<Card> select(GameState state, CardAbility ability, List<Card> candidates) {
			return candidates;
		}

		@Override
		public void createChoice(List<Receiver> receivers, CardAbility ability, PlayerState state,
				List<Card> candidates) {

		}
	}

	public static class Random extends CardSelectionMethod {

		public static final Codec<Random> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(Codec.INT.fieldOf("count").forGetter(Random::getCount),
								Codec.BOOL.fieldOf("repeat").forGetter(Random::getRepeat))
						.apply(instance, Random::new)));

		public final int count;
		public final boolean repeat;

		public Random(int count, boolean repeat) {
			this.count = count;
			this.repeat = repeat;
		}

		@Override
		protected CardSelectionMethodType<?> getType() {
			return ModCardSelectionMethods.RANDOM.get();
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { count, repeat };
		}

		public int getCount() {
			return count;
		}

		public boolean getRepeat() {
			return repeat;
		}

		@Override
		public List<Card> select(GameState state, CardAbility ability, List<Card> candidates) {
			var result = new ArrayList<Card>();
			for (int i = 0; i < count; i++) {
				if (candidates.isEmpty())
					break;
				var picked = state.getRandom().nextInt(candidates.size());
				result.add(candidates.get(picked));
				if (!repeat) {
					candidates.remove(picked);
				}
			}
			return result;
		}

		@Override
		public void createChoice(List<Receiver> receivers, CardAbility ability, PlayerState state,
				List<Card> candidates) {

		}
	}

	public static class Choice extends CardSelectionMethod {

		public static final Codec<Choice> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder
				.create(instance -> instance.group(Codec.BOOL.fieldOf("discover").forGetter(Choice::isDiscover))
						.apply(instance, Choice::new)));

		public final boolean discover;

		public Choice(boolean discover) {
			this.discover = discover;
		}

		@Override
		protected CardSelectionMethodType<?> getType() {
			return ModCardSelectionMethods.CHOICE.get();
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { new TranslatableComponent(ModCardSelectionMethods.CHOICE.get().getTranslationKey()
					+ (discover ? ".discovered" : ".selected")) };
		}

		@Override
		public List<Card> select(GameState state, CardAbility ability, List<Card> candidates) {
			var result = new ArrayList<Card>();
			var choices = state.getCurrentPlayerState().getChoices();
			if (choices == null) {
				Main.LOGGER.debug(
						"No choices made (can only make choice is ability trigger is 'summon'). Will pick random card");
				result.add(candidates.get(state.getRandom().nextInt(candidates.size())));
			} else {
				choices.getSelected(ability).ifPresent(c -> result.add(c));
			}
			return result;
		}

		@Override
		public void createChoice(List<Receiver> receivers, CardAbility ability, PlayerState state,
				List<Card> candidates) {
			state.getChoices().addChoice(receivers, ability, candidates, !discover);
		}

		public boolean isDiscover() {
			return discover;
		}
	}
}
