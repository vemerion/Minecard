package mod.vemerion.minecard.advancement;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ModCollectCardTrigger extends SimpleCriterionTrigger<ModCollectCardTrigger.Instance> {

	static final ResourceLocation ID = new ResourceLocation(Main.MODID, "collect_card");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public ModCollectCardTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate,
			DeserializationContext pConditionsParser) {
		return new Instance(pEntityPredicate,
				ResourceLocation.CODEC.parse(JsonOps.INSTANCE, pJson.get("card")).getOrThrow(false, s -> {
				}));
	}

	public void trigger(ServerPlayer pPlayer, ResourceLocation card) {
		this.trigger(pPlayer, instance -> {
			return instance.card.equals(card);
		});
	}

	public static class Instance extends AbstractCriterionTriggerInstance {
		private final ResourceLocation card;

		public Instance(EntityPredicate.Composite pPlayer, ResourceLocation card) {
			super(ModCollectCardTrigger.ID, pPlayer);
			this.card = card;
		}

		public static ModCollectCardTrigger.Instance create(ResourceLocation card) {
			return new ModCollectCardTrigger.Instance(EntityPredicate.Composite.ANY, card);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext pConditions) {
			var json = super.serializeToJson(pConditions);
			json.add("card", ResourceLocation.CODEC.encodeStart(JsonOps.INSTANCE, card).getOrThrow(false, s -> {
			}));
			return json;
		}
	}
}