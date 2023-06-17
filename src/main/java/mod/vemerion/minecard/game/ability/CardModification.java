package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.helper.Helper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;

public class CardModification {

	public static final Codec<CardModification> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder
							.create(instance -> instance
									.group(CardVariable.CODEC.fieldOf("output").forGetter(CardModification::getOutput),
											CardOperator.CODEC.fieldOf("operator")
													.forGetter(CardModification::getOperator))
									.apply(instance, CardModification::new)));

	private CardVariable output;
	private CardOperator operator;

	public CardModification(CardVariable output, CardOperator operator) {
		this.output = output;
		this.operator = operator;
	}

	public CardVariable getOutput() {
		return output;
	}

	public CardOperator getOperator() {
		return operator;
	}

	public Component getText() {
		return new TranslatableComponent(Helper.gui("card_modification"), output.getDescription(),
				new TranslatableComponent(Helper.gui(output == CardVariable.HEALTH ? "plus_equal" : "equal")),
				operator.getDescription());
	}
}
