package mod.vemerion.minecard.advancement;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.GameUtil;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ModFinishGameTrigger extends SimpleCriterionTrigger<ModFinishGameTrigger.Instance> {

	public static final Codec<Type> TYPE_CODEC = GameUtil.enumCodec(Type.class, Type::getName);

	public static enum Type {
		COMPLETE_TUTORIAL("complete_tutorial"), WIN_GAME("win_game"), WIN_AI("win_ai");

		private final String name;

		private Type(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	static final ResourceLocation ID = new ResourceLocation(Main.MODID, "finish_game");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public ModFinishGameTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate,
			DeserializationContext pConditionsParser) {
		return new Instance(pEntityPredicate,
				TYPE_CODEC.parse(JsonOps.INSTANCE, pJson.get("type")).getOrThrow(false, s -> {
				}));
	}

	public void trigger(ServerPlayer pPlayer, Type type) {
		this.trigger(pPlayer, instance -> {
			return instance.type.equals(type);
		});
	}

	public static class Instance extends AbstractCriterionTriggerInstance {
		private final Type type;

		public Instance(EntityPredicate.Composite pPlayer, Type type) {
			super(ModFinishGameTrigger.ID, pPlayer);
			this.type = type;
		}

		public static ModFinishGameTrigger.Instance create(Type type) {
			return new ModFinishGameTrigger.Instance(EntityPredicate.Composite.ANY, type);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext pConditions) {
			var json = super.serializeToJson(pConditions);
			json.add("type", TYPE_CODEC.encodeStart(JsonOps.INSTANCE, type).getOrThrow(false, s -> {
			}));
			return json;
		}
	}
}