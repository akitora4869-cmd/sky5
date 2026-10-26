package jp.skycombat;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** PlayerModel includes jacket, sleeves, pants and hat layers. */
public class EstelleRenderer extends HumanoidMobRenderer<Companion, PlayerModel<Companion>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(SkyCombat.ID, "textures/entity/estelle.png");
    public EstelleRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }
    @Override public ResourceLocation getTextureLocation(Companion entity) { return TEXTURE; }
}
