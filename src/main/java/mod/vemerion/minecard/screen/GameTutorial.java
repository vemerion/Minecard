package mod.vemerion.minecard.screen;

import java.util.function.Supplier;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.helper.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec2;

public class GameTutorial implements GuiEventListener, NarratableEntry {

	private static record Rect(int x, int y, int width, int height) {
	}

	private final Step[] steps = { new Step(new Rect(150, 150, 30, 30), new TranslatableComponent(Helper.tutorial(0))),
			new Step(new Rect(0, 0, 50, 50), new TranslatableComponent(Helper.tutorial(1))) };

	private static final int BUBBLE_BORDER = 2;
	private static final int BUBBLE_PADDING = 5;
	private static final int BUBBLE_X_OFFSET = 20;
	private static final int BUBBLE_Y_OFFSET = -48;
	private static final int MAX_BUBBLE_WIDTH = 100;
	private static final int ARROW_SIZE = 20;

	private GameScreen screen;
	private Minecraft mc;
	private TutorialCreeper creeper;
	private int index;
	private int timer;
	private Vec2 position = new Vec2(150, 150);
	private Vec2 dragPos;
	private ArrowButton back, forward;

	public GameTutorial(GameScreen screen) {
		this.screen = screen;
		this.mc = screen.getMinecraft();
		this.creeper = new TutorialCreeper();
		this.back = new ArrowButton(() -> (int) position.x - 5 - ARROW_SIZE, () -> (int) position.y + 2, false);
		this.forward = new ArrowButton(() -> (int) position.x + 5, () -> (int) position.y + 2, true);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (back.mouseClicked(pMouseX, pMouseY, pButton)) {
			return true;
		} else if (forward.mouseClicked(pMouseX, pMouseY, pButton)) {
			return true;
		} else if (creeper.mouseClicked(pMouseX, pMouseY, pButton)) {
			dragPos = new Vec2((float) pMouseX, (float) pMouseY);
			return true;
		}
		return false;
	}

	@Override
	public void mouseMoved(double pMouseX, double pMouseY) {
		if (dragPos != null) {
			position = new Vec2(position.x + (float) pMouseX - dragPos.x, position.y + (float) pMouseY - dragPos.y);
			position = new Vec2(
					Mth.clamp(position.x, TutorialCreeper.WIDTH / 2, screen.width - TutorialCreeper.WIDTH / 2),
					Mth.clamp(position.y, TutorialCreeper.HEIGHT, screen.height));
			dragPos = new Vec2((float) pMouseX, (float) pMouseY);
			back.updatePosition();
			forward.updatePosition();
		}
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		dragPos = null;
		return false;
	}

	public void tick() {
		timer++;

		steps[index].tick();
		creeper.tick();
	}

	public void render(PoseStack poseStack, int mouseX, int mouseY, BufferSource source, float partialTick) {
		steps[index].render(partialTick);

		back.render(poseStack, mouseX, mouseY, partialTick);
		forward.render(poseStack, mouseX, mouseY, partialTick);

		creeper.render(poseStack, mouseX, mouseY, source, partialTick);
	}

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	private class TutorialCreeper {
		private static final int WIDTH = 30;
		private static final int HEIGHT = 52;
		private static final int HEAD = 40;

		private Creeper creeper;
		private float swell, swell0;

		private TutorialCreeper() {
			this.creeper = new Creeper(EntityType.CREEPER, mc.level) {
				@Override
				public float getSwelling(float pPartialTicks) {
					return Mth.lerp(pPartialTicks, swell0, swell);
				}
			};
		}

		private void tick() {
			creeper.yHeadRotO = creeper.yHeadRot;
			creeper.xRotO = creeper.getXRot();
			swell0 = swell;
			swell *= 0.9;
		}

		public boolean mouseClicked(double x, double y, int pButton) {
			if (pButton == InputConstants.MOUSE_BUTTON_LEFT && inside(x, y)) {
				swell += 0.3;
				mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.CREEPER_HURT, 1f));

				return true;
			}
			return false;
		}

		private void render(PoseStack poseStack, int mouseX, int mouseY, BufferSource source, float partialTick) {
			var step = steps[index];

			// Head rotation
			if (step.highlight != null) {
				Vec2 target = new Vec2(step.highlight.x + step.highlight.width / 2,
						step.highlight.y + step.highlight.height / 2);
				double direction = Mth.RAD_TO_DEG * Mth.atan2(position.y - HEAD - target.y, position.x - target.x);
				float distance = Mth.clamp(Mth.abs(target.x - position.x), 0, 300) / 300f;
				if (direction > -90 && direction < 90) { // Left
					creeper.yHeadRot = Mth.lerp(0.05f, creeper.yHeadRot, 40 + distance * 90);
					direction = Mth.clamp(direction, -75, 75);
					creeper.setXRot((float) -Mth.lerp(0.8, creeper.getXRot(), direction));
				} else { // Right
					creeper.yHeadRot = Mth.lerp(0.05f, creeper.yHeadRot, -distance * 60);
					direction = direction > 90 ? Math.max(130, direction) : Math.min(-130, direction);
					creeper.setXRot((float) Mth.lerp(0.8, creeper.getXRot(), direction + 180));
				}
			}

			poseStack.pushPose();
			poseStack.translate(position.x, position.y, 300);
			poseStack.scale(30, -30, 30);
			poseStack.mulPose(new Quaternion(0, 20, 0, true));
			mc.getEntityRenderDispatcher().getRenderer(creeper).render(creeper, 0, 0, poseStack, source,
					LightTexture.FULL_BRIGHT);
			poseStack.popPose();
		}

		private boolean inside(double x, double y) {
			return x > position.x - WIDTH / 2 && x < position.x + WIDTH / 2 && y > position.y - HEIGHT
					&& y < position.y;
		}
	}

	private class Step {

		private Rect highlight;
		private Component text;
		private int timer;

		private Step(Rect highlight, Component text) {
			this.highlight = highlight;
			this.text = text;
		}

		private void tick() {
			timer++;
		}

		private void render(float partialTick) {
			drawTextBubble();
			drawHighlight(partialTick);
		}

		private void drawHighlight(float partialTick) {
			int alpha = (int) Mth.lerp((Mth.sin((timer + partialTick) / 5) + 1) / 2, 0, 170);
			GuiComponent.fill(new PoseStack(), highlight.x, highlight.y, highlight.x + highlight.width,
					highlight.y + highlight.height, FastColor.ARGB32.color(alpha, 200, 30, 30));
		}

		private void drawTextBubble() {
			var lines = mc.font.split(text, MAX_BUBBLE_WIDTH);
			var height = lines.size() * mc.font.lineHeight + BUBBLE_PADDING * 2;
			var pos = new Vec3i(position.x + BUBBLE_X_OFFSET, position.y + BUBBLE_Y_OFFSET - height, 0);
			var poseStack = new PoseStack();

			if (lines.isEmpty())
				return;

			// Bubble
			var bubbleWidth = lines.stream().map(s -> mc.font.width(s)).max(Integer::compare).get();
			var bubble = new Rect(pos.getX() - BUBBLE_PADDING, pos.getY() - BUBBLE_PADDING,
					bubbleWidth + BUBBLE_PADDING * 2, height);
			GuiComponent.fill(poseStack, bubble.x - BUBBLE_BORDER, bubble.y - BUBBLE_BORDER,
					bubble.x + bubble.width + BUBBLE_BORDER, bubble.y + bubble.height + BUBBLE_BORDER, 0xff000000);
			GuiComponent.fill(poseStack, bubble.x, bubble.y, bubble.x + bubble.width, bubble.y + bubble.height,
					0xffffffff);

			// Point
			for (int i = 0; i < 10; i++) {
				GuiComponent.fill(poseStack, bubble.x, bubble.y + bubble.height + BUBBLE_BORDER, bubble.x + 10 - i * 1,
						bubble.y + bubble.height + i * 1 + BUBBLE_BORDER, 0xff000000);
			}

			// Text
			float y = 0;
			poseStack.translate(0, 0, 200);
			for (var line : lines) {
				mc.font.draw(poseStack, line, pos.getX(), pos.getY() + y, 0);
				y += 9.5;
			}
		}
	}

	private class ArrowButton extends AbstractButton {

		private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/arrow.png");

		private final Supplier<Integer> xPos;
		private final Supplier<Integer> yPos;
		private final boolean forward;

		public ArrowButton(Supplier<Integer> xPos, Supplier<Integer> yPos, boolean forward) {
			super(xPos.get(), yPos.get(), ARROW_SIZE, ARROW_SIZE, TextComponent.EMPTY);
			this.xPos = xPos;
			this.yPos = yPos;
			this.forward = forward;
		}

		@Override
		public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

		}

		@Override
		public void onPress() {
			if (forward) {
				index = Math.min(steps.length - 1, index + 1);
			} else {
				index = Math.max(0, index - 1);
			}
		}

		private void updatePosition() {
			this.x = xPos.get();
			this.y = yPos.get();
		}

		@Override
		public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
			pPoseStack.pushPose();
			pPoseStack.translate(0, 0, 200);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, TEXTURE);

			RenderSystem.enableDepthTest();
			RenderSystem.setShaderColor(isHovered ? 0.6f : 1, isHovered ? 0.6f : 1, 1, 1);
			blit(pPoseStack, x, y, forward ? 0 : ARROW_SIZE, 0, width, height, ARROW_SIZE * 2, ARROW_SIZE);
			pPoseStack.popPose();
		}

	}
}
