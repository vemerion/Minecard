package mod.vemerion.minecard.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.data.EmptyModelData;

public class GameBackground implements GuiEventListener, NarratableEntry {

	private static final int GRID_WIDTH = 16;
	private static final int GRID_HEIGHT = 9;
	private static final int BASE_Z = -20;
	private static final int BASE_LIGHT = 12 << 20;

	private static final Map<Character, BlockState> SYMBOLS = Map.of('g', Blocks.GRASS_BLOCK.defaultBlockState(), 's',
			Blocks.SAND.defaultBlockState(), 'w', Blocks.WATER.defaultBlockState());
	private static final BlockState LOG = Blocks.OAK_LOG.defaultBlockState();
	private static final BlockState LEAVES = Blocks.OAK_LEAVES.defaultBlockState();

	private static final char[][] GRID = {
			{ 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's',
					's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's' },
			{ 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's',
					's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's' },
			{ 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's',
					's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's' },
			{ 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's',
					's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's' },
			{ 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's',
					's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's' },
			{ 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's',
					's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's' },
			{ 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's',
					's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's' },
			{ 'g', 'g', 'g', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's',
					's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's' },
			{ 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 's', 's', 'g', 'g', 'g', 'g', 'g', 's', 's', 's', 's', 's',
					's', 's', 's', 's', 's', 's', 's', 's', 's', 's', 's' },
			{ 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 's',
					's', 's', 'g', 'g', 'g', 'g', 'g', 'g', 's', 's', 's' },
			{ 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g',
					'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 's' },
			{ 'w', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g',
					'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g' },
			{ 'w', 'w', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g',
					'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g' },
			{ 'w', 'w', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g',
					'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g' },
			{ 'w', 'w', 'w', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g',
					'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g' },
			{ 'w', 'w', 'w', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g',
					'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g' },
			{ 'w', 'w', 'w', 'w', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g',
					'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g' },
			{ 'w', 'w', 'w', 'w', 'w', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g',
					'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g' } };

	private GameScreen screen;
	private float blockWidth;
	private float blockHeight;
	private List<BackgroundObject> objects;

	public GameBackground(GameScreen screen) {
		this.screen = screen;
		this.blockWidth = screen.width / (float) (GRID_WIDTH * 2);
		this.blockHeight = screen.height / (float) (GRID_HEIGHT * 2);
		this.objects = new ArrayList<>();
		objects.add(new TreasureChest(new Vector3f(28 * blockWidth, 2 * blockHeight, BASE_Z + 1)));
		objects.add(new LightSource(new Vector3f(4 * blockWidth, 12 * blockHeight, BASE_Z + 1),
				Blocks.REDSTONE_LAMP.defaultBlockState()));
		objects.add(new LightSource(new Vector3f(30 * blockWidth, 4 * blockHeight, BASE_Z + 1),
				Blocks.CAMPFIRE.defaultBlockState()));
		objects.add(new Corpse(new Vector3f(30 * blockWidth, 2 * blockHeight, BASE_Z + 20)));
		objects.add(new Fish(new Vector3f(1 * blockWidth, 15 * blockHeight, BASE_Z + 15)));
		objects.add(new Cactus(new Vector3f(29 * blockWidth, 6 * blockHeight, BASE_Z + 1)));
	}

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		for (var object : objects)
			if (object.click(pMouseX, pMouseY, pButton))
				return true;
		return GuiEventListener.super.mouseReleased(pMouseX, pMouseY, pButton);
	}

	public void tick() {
		for (var object : objects)
			object.tick();
	}

	private int light(float x, float y) {
		int light = BASE_LIGHT;
		for (var object : objects)
			light |= object.getLight(x, y);
		return light;
	}

	public void render(PoseStack ps, int mouseX, int mouseY, BufferSource source, float partialTick) {
		for (var object : objects)
			object.render(ps, source);

		var poseStack = new PoseStack();
		for (int i = 0; i < GRID.length; i++) {
			for (int j = 0; j < GRID[i].length; j++) {
				renderBlock(poseStack, null, SYMBOLS.get(GRID[i][j]), source, j * blockWidth, i * blockHeight, BASE_Z);
			}
		}

		// Tree
		renderBlock(poseStack, null, LOG, source, 3 * blockWidth, 15 * blockHeight, BASE_Z + 1);
		renderBlock(poseStack, null, LEAVES, source, 3 * blockWidth, 15 * blockHeight, BASE_Z + 2);
		renderBlock(poseStack, null, LEAVES, source, 4 * blockWidth, 15 * blockHeight, BASE_Z + 1);
		renderBlock(poseStack, null, LEAVES, source, 2 * blockWidth, 15 * blockHeight, BASE_Z + 1);
		renderBlock(poseStack, null, LEAVES, source, 3 * blockWidth, 16 * blockHeight, BASE_Z + 1);
		renderBlock(poseStack, null, LEAVES, source, 3 * blockWidth, 14 * blockHeight, BASE_Z + 1);

		// Skeleton support
		renderBlock(poseStack, null, LOG, source, 30 * blockWidth, 1 * blockHeight, BASE_Z + 1);
	}

	private void renderBlock(PoseStack poseStack, BlockEntity blockEntity, BlockState block, BufferSource source,
			float x, float y, float z) {
		var mc = screen.getMinecraft();
		poseStack.pushPose();
		poseStack.translate(x, y, z);
		poseStack.scale(blockWidth, -blockHeight, 1);
		poseStack.mulPose(new Quaternion(90 - 15 * (z - BASE_Z), 0, 0, true));
		if (blockEntity != null) {
			mc.getBlockEntityRenderDispatcher().getRenderer(blockEntity).render(blockEntity, mc.getFrameTime(),
					poseStack, source, light(x, y), OverlayTexture.NO_OVERLAY);
		} else if (block.getBlock() != Blocks.WATER) {
			mc.getBlockRenderer().renderSingleBlock(block, poseStack, source, light(x, y), OverlayTexture.NO_OVERLAY,
					EmptyModelData.INSTANCE);
		} else {
			renderFluid(poseStack, block, source, x, y, z);
		}

		poseStack.popPose();
	}

	private void renderFluid(PoseStack poseStack, BlockState block, BufferSource source, float x, float y, float z) {
		var bufferbuilder = source.getBuffer(RenderType.text(new ResourceLocation("textures/block/water_still.png")));
		var mc = screen.getMinecraft();

		int frames = 512 / 16;
		float index = mc.level.getGameTime() % frames;

		bufferbuilder.vertex(x, y + blockHeight, z).color(0, 0.4f, 1f, 1f).uv(0, (index + 1) / frames).uv2(light(x, y))
				.endVertex();
		bufferbuilder.vertex(x + blockWidth, y + blockHeight, z).color(0, 0.4f, 1f, 1f).uv(1, (index + 1) / frames)
				.uv2(light(x, y)).endVertex();
		bufferbuilder.vertex(x + blockWidth, y, z).color(0, 0.4f, 1f, 1f).uv(1, index / frames).uv2(light(x, y))
				.endVertex();
		bufferbuilder.vertex(x, y, z).color(0, 0.4f, 1f, 1f).uv(0, index / frames).uv2(light(x, y)).endVertex();
	}

	private class BackgroundObject {

		protected Vector3f pos;

		protected BackgroundObject(Vector3f pos) {
			this.pos = pos;
		}

		protected void tick() {

		}

		protected void render(PoseStack poseStack, BufferSource source) {

		}

		protected boolean click(double x, double y, int pButton) {
			return false;
		}

		protected int getLight(float x, float y) {
			return BASE_LIGHT;
		}

		protected boolean contains(double x, double y) {
			return x > pos.x() && x < pos.x() + blockWidth && y > pos.y() && y < pos.y() + blockHeight;
		}
	}

	private class TreasureChest extends BackgroundObject {

		private static final ItemStack[] ITEMS = { Items.DIAMOND.getDefaultInstance(),
				Items.EMERALD.getDefaultInstance(), Items.NETHER_STAR.getDefaultInstance() };

		private ChestBlockEntity blockEntity;
		private boolean isOpen = false;
		private int index;

		private TreasureChest(Vector3f pos) {
			super(pos);
			this.blockEntity = new ChestBlockEntity(BlockPos.ZERO,
					Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST));
		}

		@Override
		protected void tick() {
			ChestBlockEntity.lidAnimateTick(null, null, null, blockEntity);
		}

		@Override
		protected boolean click(double x, double y, int pButton) {
			if (pButton == InputConstants.MOUSE_BUTTON_LEFT && contains(x, y)) {
				if (!isOpen)
					index = (index + 1) % ITEMS.length;
				screen.getMinecraft().getSoundManager()
						.play(SimpleSoundInstance.forUI(isOpen ? SoundEvents.CHEST_CLOSE : SoundEvents.CHEST_OPEN, 1));
				isOpen = !isOpen;
				blockEntity.triggerEvent(1, isOpen ? 1 : 0);
				return true;
			}
			return false;
		}

		@Override
		protected void render(PoseStack poseStack, BufferSource source) {
			renderBlock(poseStack, blockEntity, blockEntity.getBlockState(), source, pos.x(), pos.y(), pos.z());
			poseStack.pushPose();
			poseStack.translate(pos.x() + blockWidth / 2, pos.y() + blockHeight * 0.35, pos.z() + 0.5f);
			poseStack.scale(10, -10, 10);
			screen.getMinecraft().getItemRenderer().renderStatic(ITEMS[index], ItemTransforms.TransformType.NONE,
					light(pos.x(), pos.y()), OverlayTexture.NO_OVERLAY, poseStack, source, 0);
			poseStack.popPose();
		}
	}

	private class LightSource extends BackgroundObject {

		private BlockState state;

		protected LightSource(Vector3f pos, BlockState state) {
			super(pos);
			this.state = state.setValue(BlockStateProperties.LIT, false);
		}

		@Override
		protected boolean click(double x, double y, int pButton) {
			if (pButton == InputConstants.MOUSE_BUTTON_LEFT && contains(x, y)) {
				state = state.cycle(BlockStateProperties.LIT);
				return true;
			}
			return super.click(x, y, pButton);
		}

		@Override
		protected void render(PoseStack poseStack, BufferSource source) {
			renderBlock(poseStack, null, state, source, pos.x(), pos.y(), pos.z());
		}

		@Override
		protected int getLight(float x, float y) {
			int light = 0;

			if (state.getValue(BlockStateProperties.LIT)) {
				int distance = (int) ((Math.abs(pos.x() - x) / blockWidth) + Math.abs((pos.y() - y) / blockHeight));
				light = Math.max(0, 15 - distance);
			}

			return BASE_LIGHT | light << 4;
		}

	}

	private class Corpse extends BackgroundObject {

		private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/skeleton/skeleton.png");

		private SkeletonModel<Skeleton> model;

		protected Corpse(Vector3f pos) {
			super(pos);
			this.model = new SkeletonModel<>(screen.getMinecraft().getEntityModels().bakeLayer(ModelLayers.SKELETON));
			prepareModel();
		}

		private void prepareModel() {
			model.leftLeg.xRot = -1f;
			model.leftLeg.yRot = -0.3f;
			model.rightLeg.xRot = -1f;
			model.rightLeg.yRot = 0.3f;
			model.head.zRot = 0.2f;
			model.head.xRot = 0.2f;
			model.leftArm.zRot = -0.2f;
			model.rightArm.zRot = 0.2f;
		}

		@Override
		protected boolean click(double x, double y, int pButton) {
			if (pButton == InputConstants.MOUSE_BUTTON_LEFT && contains(x, y)) {
				for (var part : List.of(model.leftArm, model.rightLeg, model.leftLeg, model.rightArm, model.head,
						model.body)) {
					if (part.visible) {
						part.visible = false;
						screen.getMinecraft().getSoundManager()
								.play(SimpleSoundInstance.forUI(SoundEvents.SKELETON_HURT, 1));
						break;
					}
				}
				return true;
			}
			return super.click(x, y, pButton);
		}

		@Override
		protected void render(PoseStack poseStack, BufferSource source) {
			poseStack.pushPose();
			poseStack.translate(pos.x() + blockWidth / 2, pos.y() - blockHeight / 2, pos.z());
			poseStack.scale(22, 22, 22);
			poseStack.mulPose(new Quaternion(-45, 160, 0, true));
			model.renderToBuffer(poseStack, source.getBuffer(model.renderType(TEXTURE)), light(pos.x(), pos.y()),
					OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
			poseStack.popPose();
		}

	}

	private class Fish extends BackgroundObject {

		private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/dolphin.png");
		private static final int SWIM_DURATION = 20 * 2;

		private DolphinModel<Dolphin> model;
		private int swimTimer;

		protected Fish(Vector3f pos) {
			super(pos);
			this.model = new DolphinModel<>(screen.getMinecraft().getEntityModels().bakeLayer(ModelLayers.DOLPHIN));
			prepareModel();
		}

		private void prepareModel() {
		}

		@Override
		protected boolean click(double x, double y, int pButton) {
			if (swimTimer == 0 && pButton == InputConstants.MOUSE_BUTTON_LEFT && contains(x, y)) {
				swimTimer = SWIM_DURATION;
				screen.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.DOLPHIN_AMBIENT, 1));
				return true;
			}
			return super.click(x, y, pButton);
		}

		@Override
		protected void tick() {
			if (swimTimer > 0)
				swimTimer--;
			if (swimTimer == SWIM_DURATION * 0.9) {
				screen.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.DOLPHIN_JUMP, 1));
			} else if (swimTimer == SWIM_DURATION * 1 / 4) {
				screen.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.DOLPHIN_SPLASH, 1));
			}
		}

		@Override
		protected void render(PoseStack poseStack, BufferSource source) {
			var mc = screen.getMinecraft();
			var progress = swimTimer == 0 ? 0 : (SWIM_DURATION - swimTimer + mc.getFrameTime()) / SWIM_DURATION;
			poseStack.pushPose();
			poseStack.translate(pos.x() + blockWidth * 1.5f,
					pos.y() + blockHeight * 2 + Mth.sin(progress * Mth.TWO_PI) * blockHeight * 1.5,
					pos.z() - Mth.cos(progress * Mth.TWO_PI) * 25);

			poseStack.scale(15, -15, 15);
			poseStack.translate(-1, 0, 0);
			model.root().getChild("body").xRot = -Mth.HALF_PI - progress * Mth.PI * 2;
			model.renderToBuffer(poseStack, source.getBuffer(model.renderType(TEXTURE)), LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
			poseStack.popPose();
		}

	}

	private class Cactus extends BackgroundObject {

		private Vector3f startPos;
		private Random rand;
		private BlockState state;

		protected Cactus(Vector3f pos) {
			super(pos);
			this.startPos = new Vector3f(pos.x(), pos.y(), pos.z());
			this.state = Blocks.CACTUS.defaultBlockState();
			this.rand = new Random();
		}

		@Override
		protected boolean click(double x, double y, int pButton) {
			if (pButton == InputConstants.MOUSE_BUTTON_LEFT && contains(x, y)) {
				screen.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_HURT, 1));
				pos.setX(startPos.x() + rand.nextInt(-1, 2) * blockWidth);
				pos.setY(startPos.y() + rand.nextInt(-1, 2) * blockHeight);
				return true;
			}
			return super.click(x, y, pButton);
		}

		@Override
		protected void render(PoseStack poseStack, BufferSource source) {
			renderBlock(poseStack, null, state, source, pos.x(), pos.y(), pos.z());
		}
	}

}
