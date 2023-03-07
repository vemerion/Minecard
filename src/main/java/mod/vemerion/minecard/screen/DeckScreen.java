package mod.vemerion.minecard.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mod.vemerion.minecard.menu.DeckMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DeckScreen extends AbstractContainerScreen<DeckMenu> {

	private static final ResourceLocation BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");

	public DeckScreen(DeckMenu menu, Inventory inv, Component titleIn) {
		super(menu, inv, titleIn);
		this.imageHeight = 114 + 3 * 18;
		this.inventoryLabelY = imageHeight - 94;
	}

	@Override
	protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, BACKGROUND);
		int x = (width - imageWidth) / 2;
		int y = (height - imageHeight) / 2;
		blit(pPoseStack, x, y, 0, 0, imageWidth, 3 * 18 + 17);
		blit(pPoseStack, x, y + 3 * 18 + 17, 0, 126, imageWidth, 96);
		renderTooltip(pPoseStack, pMouseX, pMouseY);
	}
}
