package mod.vemerion.minecard.screen;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.renderer.CardItemRenderer;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.util.TransformationHelper;

public class GameTutorial implements GuiEventListener, NarratableEntry {

	public static enum UnlockAction {
		NONE, PLAY_CARD, END_TURN, GAME_OVER, MULLIGAN
	}

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
	private Vec2 position = new Vec2(30, 200);
	private Vec2 dragPos;
	private ArrowButton back, forward;
	private ClientCard card;

	private final Step[] steps = { new Step(Component.translatable(Helper.tutorial(0))),
			new Step(Component.translatable(Helper.tutorial(1))), new Step(Component.translatable(Helper.tutorial(2))),
			new Step(Component.translatable(Helper.tutorial(3)), null, () -> card.setDamage(10), UnlockAction.NONE),
			new Step(Component.translatable(Helper.tutorial(4)),
					new Rect((w, h) -> w / 2 + 19, (w, h) -> h / 2 - 36, 20, 20)),
			new Step(Component.translatable(Helper.tutorial(5)),
					new Rect((w, h) -> w / 2 + 18, (w, h) -> h / 2 - 7, 20, 20)),
			new Step(Component.translatable(Helper.tutorial(6)),
					new Rect((w, h) -> w / 2 - 31, (w, h) -> h / 2 - 7, 20, 20)),
			new Step(Component.translatable(Helper.tutorial(7)),
					new Rect((w, h) -> w / 2 + 35, (w, h) -> h / 2 - 50, 20, 100)),
			new Step(Component.translatable(Helper.tutorial(8)),
					new Rect((w, h) -> w - 28, (w, h) -> h / 2 - 24, 14, 14)),
			new Step(Component.translatable(Helper.tutorial(9)),
					new Rect((w, h) -> w - 14, (w, h) -> h / 2 - 24, 14, 14)),
			new Step(Component.translatable(Helper.tutorial(10)),
					new Rect((w, h) -> w / 2 - 40, (w, h) -> h / 2 + 10, 86, 40)),
			new Step(Component.translatable(Helper.tutorial(11))),
			new Step(Component.translatable(Helper.tutorial(12)), new Rect((w, h) -> 0, (w, h) -> h / 2, 500, 500)),
			new Step(Component.translatable(Helper.tutorial(13)),
					new Rect((w, h) -> w - 19, (w, h) -> h - 110, 18, 62)),
			new Step(Component.translatable(Helper.tutorial(14)),
					new Rect((w, h) -> w / 2 - 130, (w, h) -> h - 54, 260, 50)),
			new Step(Component.translatable(Helper.tutorial(15))),
			new Step(Component.translatable(Helper.tutorial(16))),
			new Step(Component.translatable(Helper.tutorial(17)),
					new Rect((w, h) -> w / 2 - 45, (w, h) -> h - 90, 90, 30)),
			new Step(Component.translatable(Helper.tutorial(18)),
					new Rect((w, h) -> w / 2 - 130, (w, h) -> h - 118, 260, 52), null, UnlockAction.MULLIGAN),
			new Step(Component.translatable(Helper.tutorial(19))),
			new Step(Component.translatable(Helper.tutorial(20),
					Cards.getInstance(true).get(EntityType.PLAYER).getHealth())),
			new Step(Component.translatable(Helper.tutorial(21)),
					new Rect((w, h) -> w - 210, (w, h) -> h - 66, 80, 12)),
			new Step(Component.translatable(Helper.tutorial(22))),
			new Step(Component.translatable(Helper.tutorial(23)), new Rect((w, h) -> 0, (w, h) -> 0, 18, 320), null,
					UnlockAction.PLAY_CARD),
			new Step(Component.translatable(Helper.tutorial(24)), new Rect((w, h) -> 0, (w, h) -> 0, 18, 320)),
			new Step(Component.translatable(Helper.tutorial(25)), new Rect((w, h) -> 0, (w, h) -> 0, 18, 320)),
			new Step(Component.translatable(Helper.tutorial(26))),
			new Step(Component.translatable(Helper.tutorial(27)),
					new Rect((w, h) -> w - 26, (w, h) -> h / 2 - 12, 24, 24)),
			new Step(Component.translatable(Helper.tutorial(28)), null, null, UnlockAction.END_TURN),
			new Step(Component.translatable(Helper.tutorial(29))),
			new Step(Component.translatable(Helper.tutorial(30)), null, null, UnlockAction.GAME_OVER) };

	public GameTutorial(GameScreen screen, int initialStep) {
		this.screen = screen;
		this.index = Math.min(initialStep, steps.length - 1);
		this.mc = screen.getMinecraft();
		this.creeper = new TutorialCreeper();
		this.back = new ArrowButton(() -> (int) position.x - 5 - ARROW_SIZE, () -> (int) position.y + 2, false);
		this.forward = new ArrowButton(() -> (int) position.x + 5, () -> (int) position.y + 2, true);
		this.card = new ClientCard(Cards.TUTORIAL_CARD_TYPE.create(),
				new Vec2((screen.width - ClientCard.CARD_WIDTH) / 2, (screen.height - ClientCard.CARD_HEIGHT) / 2),
				screen);
		this.card.appear(2);
		CardItemRenderer.getEntity(card, mc.level).setCustomName(Component.translatable(Helper.gui("cardy")));
		initSteps();
	}

	private void initSteps() {
		for (int i = 0; i <= index; i++) {
			if (steps[i].action != null) {
				steps[i].action.run();
			}
			steps[i].locked = false;
		}
	}

	public void unlock(UnlockAction action) {
		for (int i = index + 1; i < steps.length; i++) {
			if (steps[i].locked) {
				if (steps[i].unlockAction == action) {
					steps[i].locked = false;
					index = i;
					screen.setTutorial(index);
				}
				return;
			}
		}
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
		card.tick();

		steps[index].tick();
		creeper.tick();
	}

	public void render(PoseStack poseStack, int mouseX, int mouseY, BufferSource source, float partialTick) {
		if (index > 1 && index < 11) {
			poseStack.pushPose();
			poseStack.translate(0, 0, 100);
			card.render(poseStack, mouseX, mouseY, source, partialTick);
			poseStack.popPose();
			source.endBatch();
		}

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

			// Head rotation
			var step = steps[index];
			if (step.highlight != null) {
				Vec2 target = new Vec2(step.highlight.getX() + step.highlight.getWidth() / 2,
						step.highlight.getY() + step.highlight.getHeight() / 2);
				double direction = Mth.RAD_TO_DEG * Mth.atan2(position.y - HEAD - target.y, position.x - target.x);
				float distance = Mth.clamp(Mth.abs(target.x - position.x), 0, 300) / 300f;
				if (direction > -90 && direction < 90) { // Left
					creeper.yHeadRot = Mth.lerp(0.2f, creeper.yHeadRot, 40 + distance * 90);
					direction = Mth.clamp(direction, -75, 75);
					creeper.setXRot((float) Mth.rotLerp(0.2f, creeper.getXRot(), (float) -direction));
				} else { // Right
					creeper.yHeadRot = Mth.lerp(0.2f, creeper.yHeadRot, -distance * 60);
					direction = direction > 90 ? Math.max(130, direction) : Math.min(-130, direction);
					creeper.setXRot((float) Mth.rotLerp(0.2f, creeper.getXRot(), (float) direction + 180));
				}
			} else {
				creeper.yHeadRot = Mth.lerp(0.2f, creeper.yHeadRot, 15);
				creeper.setXRot((float) Mth.rotLerp(0.2f, creeper.getXRot(), 0));
			}
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
			poseStack.pushPose();
			poseStack.translate(position.x, position.y, 300);
			poseStack.scale(30, -30, 30);
			poseStack.mulPose(TransformationHelper.quatFromXYZ(0, 20, 0, true));
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
		private Runnable action;
		private int timer;
		private boolean locked;
		private UnlockAction unlockAction;

		private Step(Component text, Rect highlight, Runnable action, UnlockAction unlockAction) {
			this.text = text;
			this.highlight = highlight;
			this.action = action;
			this.unlockAction = unlockAction;
			if (unlockAction != UnlockAction.NONE)
				locked = true;
		}

		private Step(Component text, Rect highlight) {
			this(text, highlight, null, UnlockAction.NONE);
		}

		private Step(Component text) {
			this(text, null);
		}

		private void tick() {
			timer++;

			if (action != null) {
				action.run();
				action = null;
			}
		}

		private void render(float partialTick) {
			drawTextBubble();
			if (highlight != null)
				drawHighlight(partialTick);
		}

		private void drawHighlight(float partialTick) {
			int alpha = (int) Mth.lerp((Mth.sin((timer + partialTick) / 5) + 1) / 2, 0, 170);
			GuiComponent.fill(new PoseStack(), highlight.getX(), highlight.getY(),
					highlight.getX() + highlight.getWidth(), highlight.getY() + highlight.getHeight(),
					FastColor.ARGB32.color(alpha, 200, 30, 30));
		}

		private void drawTextBubble() {
			var lines = mc.font.split(text, MAX_BUBBLE_WIDTH);
			var height = lines.size() * mc.font.lineHeight + BUBBLE_PADDING * 2;
			var pos = new Vec3i((int) (position.x + BUBBLE_X_OFFSET), (int) (position.y + BUBBLE_Y_OFFSET - height), 0);
			var poseStack = new PoseStack();
			poseStack.translate(0, 0, 200);

			if (lines.isEmpty())
				return;

			// Bubble
			var bubbleWidth = lines.stream().map(s -> mc.font.width(s)).max(Integer::compare).get();
			var bubble = new Rect(pos.getX() - BUBBLE_PADDING, pos.getY() - BUBBLE_PADDING,
					bubbleWidth + BUBBLE_PADDING * 2, height);
			GuiComponent.fill(poseStack, bubble.getX() - BUBBLE_BORDER, bubble.getY() - BUBBLE_BORDER,
					bubble.getX() + bubble.getWidth() + BUBBLE_BORDER,
					bubble.getY() + bubble.getHeight() + BUBBLE_BORDER, 0xff000000);
			GuiComponent.fill(poseStack, bubble.getX(), bubble.getY(), bubble.getX() + bubble.getWidth(),
					bubble.getY() + bubble.getHeight(), 0xffffffff);

			// Point
			for (int i = 0; i < 10; i++) {
				GuiComponent.fill(poseStack, bubble.getX(), bubble.getY() + bubble.getHeight() + BUBBLE_BORDER,
						bubble.getX() + 10 - i * 1, bubble.getY() + bubble.getHeight() + i * 1 + BUBBLE_BORDER,
						0xff000000);
			}

			// Text
			float y = 0;
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
			super(xPos.get(), yPos.get(), ARROW_SIZE, ARROW_SIZE, Component.empty());
			this.xPos = xPos;
			this.yPos = yPos;
			this.forward = forward;
		}

		@Override
		public void onPress() {
			int prev = index;
			if (forward) {
				if (index + 1 < steps.length && !steps[index + 1].locked)
					index++;
			} else {
				index = Math.max(0, index - 1);
			}

			if (index > prev) {
				screen.setTutorial(index);
			}
		}

		private void updatePosition() {
			setX(xPos.get());
			setY(yPos.get());
		}

		@Override
		public void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
			pPoseStack.pushPose();
			pPoseStack.translate(0, 0, 200);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, TEXTURE);
			setShaderColor();
			RenderSystem.enableDepthTest();
			blit(pPoseStack, getX(), getY(), forward ? 0 : ARROW_SIZE, 0, width, height, ARROW_SIZE * 2, ARROW_SIZE);
			pPoseStack.popPose();
			RenderSystem.setShaderColor(1, 1, 1, 1);
		}

		private void setShaderColor() {
			if ((forward && (index >= steps.length - 1 || steps[index + 1].locked)) || (!forward && index == 0)) {
				RenderSystem.setShaderColor(0.1f, 0.1f, 0.1f, 1);
			} else if (isHovered) {
				RenderSystem.setShaderColor(0.6f, 0.6f, 1, 1);
			} else {
				RenderSystem.setShaderColor(1, 1, 1, 1);
			}
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

		}

	}

	private class Rect {
		private BiFunction<Integer, Integer, Integer> x;
		private BiFunction<Integer, Integer, Integer> y;
		private int width;
		private int height;

		Rect(BiFunction<Integer, Integer, Integer> x, BiFunction<Integer, Integer, Integer> y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		Rect(int x, int y, int width, int height) {
			this((screenWidth, screenHeight) -> x, (screenWidth, screenHeight) -> y, width, height);
		}

		private int getX() {
			return x.apply(screen.width, screen.height);
		}

		private int getY() {
			return y.apply(screen.width, screen.height);
		}

		private int getWidth() {
			return width;
		}

		private int getHeight() {
			return height;
		}
	}

	@Override
	public void setFocused(boolean pFocused) {

	}

	@Override
	public boolean isFocused() {
		return false;
	}
}
