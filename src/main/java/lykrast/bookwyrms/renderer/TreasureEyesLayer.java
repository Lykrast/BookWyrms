package lykrast.bookwyrms.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import lykrast.bookwyrms.BookWyrms;
import lykrast.bookwyrms.entity.BookWyrmEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;

public class TreasureEyesLayer<T extends BookWyrmEntity, M extends BookWyrmModel<T>> extends EyesLayer<T, M> {
	private static final RenderType EYES = RenderType.eyes(BookWyrms.rl("textures/entity/treasure_eyes.png"));

	public TreasureEyesLayer(RenderLayerParent<T, M> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack p_116983_, MultiBufferSource p_116984_, int p_116985_, T wyrm, float p_116987_, float p_116988_, float p_116989_, float p_116990_, float p_116991_, float p_116992_) {
		//I'm not even gonna try to see what all those parameters are, I just need to get the wyrm
		if (wyrm.isTreasure()) super.render(p_116983_, p_116984_, p_116985_, wyrm, p_116987_, p_116988_, p_116989_, p_116990_, p_116991_, p_116992_);
	}

	@Override
	public RenderType renderType() {
		return EYES;
	}

}
