package mod.vemerion.minecard.screen.animation;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.TransformationHelper;

public class GameOverAnimation extends Animation {

	private int timer;
	private Player player;
	private boolean defeat;
	private List<GameOverObject> objects;

	public GameOverAnimation(Minecraft mc, Player player, boolean defeat) {
		super(mc, () -> {
		});
		this.player = player;
		this.defeat = defeat;
		this.objects = new ArrayList<>();
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var poseStack = new PoseStack();
		poseStack.translate(mc.screen.width / 2, mc.screen.height / 2 + 40, 100);
		poseStack.scale(50, -50, 50);

		var xRotOSaved = player.xRotO;
		var yBodyRotOSaved = player.yBodyRotO;
		var yBodyRotSaved = player.yBodyRot;
		var getYRotSaved = player.getYRot();
		var getXRotSaved = player.getXRot();
		var yHeadRotOSaved = player.yHeadRotO;
		var yHeadRotSaved = player.yHeadRot;
		player.xRotO = defeat ? 50 : Mth.cos((timer + partialTick) / 5f) * 20;
		player.yBodyRotO = 0;
		player.yBodyRot = 0;
		player.setYRot(!defeat ? 0 : Mth.cos((timer + partialTick) / 7f) * 20);
		player.setXRot(player.xRotO);
		player.yHeadRot = player.getYRot();
		player.yHeadRotO = player.getYRot();

		mc.getEntityRenderDispatcher().getRenderer(player).render(player, 0, partialTick, poseStack, source,
				LightTexture.FULL_BRIGHT);
		player.xRotO = xRotOSaved;
		player.yBodyRotO = yBodyRotOSaved;
		player.yBodyRot = yBodyRotSaved;
		player.setYRot(getYRotSaved);
		player.setXRot(getXRotSaved);
		player.yHeadRotO = yHeadRotOSaved;
		player.yHeadRot = yHeadRotSaved;

		for (var object : objects) {
			object.render(mouseX, mouseY, source, partialTick);
		}
	}

	@Override
	public void tick() {
		timer++;

		if (timer % 3 == 0) {
			if (!defeat) {
				objects.add(new VictoryObject(new Vector2f((float) Mth.lerp(Math.random(), 20, mc.screen.width - 20),
						mc.screen.height + 20)));
			} else {
				objects.add(
						new DefeatObject(new Vector2f((float) Mth.lerp(Math.random(), 20, mc.screen.width - 20), -20)));
			}
		}

		for (var object : objects) {
			object.tick();
		}

		for (int i = objects.size() - 1; i >= 0; i--) {
			if (objects.get(i).isDone()) {
				objects.remove(i);
			}
		}
	}

	private abstract class GameOverObject {

		protected static final int SCALE = 25;
		protected static final int Z = 10;

		public abstract void tick();

		public abstract void render(int mouseX, int mouseY, BufferSource source, float partialTick);

		public abstract boolean isDone();
	}

	private class VictoryObject extends GameOverObject {

		private static final float velocity = -3;

		private Vector2f pos;
		private final ItemStack stack = Items.EMERALD.getDefaultInstance();
		private int timer;

		private VictoryObject(Vector2f pos) {
			this.pos = pos;
		}

		@Override
		public void tick() {
			pos.y += velocity;
			timer++;
		}

		@Override
		public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
			var poseStack = new PoseStack();
			poseStack.pushPose();
			poseStack.translate(pos.x, pos.y + partialTick * velocity, Z);
			poseStack.scale(SCALE, -SCALE, SCALE);
			poseStack.mulPose(TransformationHelper.quatFromXYZ(0, timer * 10, 0, true));

			mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, poseStack, source, null, 0);
			poseStack.popPose();
		}

		@Override
		public boolean isDone() {
			return pos.y < -SCALE;
		}

	}

	private class DefeatObject extends GameOverObject {

		private static final float velocity = 2;

		private Vector2f pos;
		private final ItemStack stack = Items.ROTTEN_FLESH.getDefaultInstance();
		private int timer;

		private DefeatObject(Vector2f pos) {
			this.pos = pos;
		}

		@Override
		public void tick() {
			pos.y += velocity;
			timer++;
		}

		@Override
		public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
			var poseStack = new PoseStack();
			poseStack.pushPose();
			float rot = ((Mth.cos((timer + partialTick) / 8f) + 1) / 2f) * Mth.PI;
			poseStack.translate(pos.x + Mth.cos(rot) * 20, pos.y + partialTick * velocity + Mth.sin(rot) * 20, Z);
			poseStack.scale(SCALE, -SCALE, SCALE);
			poseStack.mulPose(TransformationHelper.quatFromXYZ(0, 0, timer * 10, true));

			mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, poseStack, source, null, 0);
			poseStack.popPose();
		}

		@Override
		public boolean isDone() {
			return pos.y > mc.screen.height;
		}

	}

}
