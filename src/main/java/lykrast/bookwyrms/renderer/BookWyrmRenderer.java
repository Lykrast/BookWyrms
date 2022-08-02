package lykrast.bookwyrms.renderer;

import lykrast.bookwyrms.BookWyrms;
import lykrast.bookwyrms.entity.BookWyrmEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BookWyrmRenderer extends MobRenderer<BookWyrmEntity, BookWyrmModel<BookWyrmEntity>> {
	private static final ResourceLocation TEXTURE = BookWyrms.rl("textures/entity/book_wyrm_grey.png");
	
	public BookWyrmRenderer(Context context) {
		super(context, new BookWyrmModel<>(context.bakeLayer(BookWyrmModel.MODEL)), 0.7f);
	}

	@Override
	public ResourceLocation getTextureLocation(BookWyrmEntity p_114482_) {
		return TEXTURE;
	}

}
