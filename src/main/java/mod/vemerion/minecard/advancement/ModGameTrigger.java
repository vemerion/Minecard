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

public class ModGameTrigger extends SimpleCriterionTrigger<ModGameTrigger.Instance> {

	static final ResourceLocation ID = new ResourceLocation(Main.MODID, "game");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public ModGameTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate,
			DeserializationContext pConditionsParser) {
		return new Instance(pEntityPredicate,
				ResourceLocation.CODEC.parse(JsonOps.INSTANCE, pJson.get("id")).getOrThrow(false, s -> {
				}));
	}

	public void trigger(ServerPlayer pPlayer, ResourceLocation id) {
		this.trigger(pPlayer, instance -> {
			return instance.id.equals(id);
		});
	}

	public static class Instance extends AbstractCriterionTriggerInstance {
		private final ResourceLocation id;

		public Instance(EntityPredicate.Composite pPlayer, ResourceLocation id) {
			super(ModGameTrigger.ID, pPlayer);
			this.id = id;
		}

		public static ModGameTrigger.Instance create(ResourceLocation id) {
			return new ModGameTrigger.Instance(EntityPredicate.Composite.ANY, id);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext pConditions) {
			var json = super.serializeToJson(pConditions);
			json.add("id", ResourceLocation.CODEC.encodeStart(JsonOps.INSTANCE, id).getOrThrow(false, s -> {
			}));
			return json;
		}
	}
}