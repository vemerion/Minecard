package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
}
