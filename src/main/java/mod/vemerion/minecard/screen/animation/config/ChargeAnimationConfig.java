package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.ChargeAnimation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class ChargeAnimationConfig extends AnimationConfig {

	public static final Codec<ChargeAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ForgeRegistries.ENTITIES.getCodec().fieldOf("entity").forGetter(ChargeAnimationConfig::getEntity))
			.apply(instance, ChargeAnimationConfig::new));

	private final EntityType<?> entity;

	public ChargeAnimationConfig(EntityType<?> entity) {
		this.entity = entity;
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.CHARGE.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		for (var target : targets)
			game.addAnimation(new ChargeAnimation(game.getMinecraft(), origin.getPosition(), target, entity, () -> {
			}));
	}

	public EntityType<?> getEntity() {
		return entity;
	}
}
