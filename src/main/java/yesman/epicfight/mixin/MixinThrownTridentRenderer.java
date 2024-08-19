package yesman.epicfight.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.projectile.ThrownTridentPatch;

import org.joml.Vector3f;

@Mixin(value = ThrownTridentRenderer.class)
public abstract class MixinThrownTridentRenderer extends EntityRenderer<ThrownTrident> {
	protected MixinThrownTridentRenderer(Context p_174008_) {
		super(p_174008_);
	}

	@Shadow
	@Final
	private TridentModel model;

	@Inject(at = @At(value = "HEAD"), method = "render(Lnet/minecraft/world/entity/projectile/ThrownTrident;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", cancellable = true)
	private void epicfight_render(ThrownTrident tridentEntity, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource multiSourceBuffer, int packedLight, CallbackInfo info) {
		ThrownTridentRenderer instnace = (ThrownTridentRenderer) ((Object) this);

		poseStack.pushPose();
		ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(tridentEntity, ThrownTridentPatch.class);

		if (tridentPatch != null) {
			if (tridentPatch.isInnateActivated()) {
				Entity owner = tridentEntity.getOwner();
				Vec3 toOwner = owner.position().subtract(tridentEntity.position());
				Vec3 toOwnerHorizontalNorm = owner.position().subtract(tridentEntity.position()).subtract(0, toOwner.y, 0).normalize();
				Vec3 toOwnerNorm = toOwner.normalize();
				Vec3 rotAxis = toOwnerHorizontalNorm.cross(toOwnerNorm).normalize();
				float deg = (float) (MathUtils.getAngleBetween(toOwnerNorm, toOwnerHorizontalNorm) * (180D / Math.PI));

				poseStack.mulPose(QuaternionUtils.rotationDegrees(new Vector3f((float) rotAxis.x, (float) rotAxis.y, (float) rotAxis.z), deg));
				poseStack.mulPose(QuaternionUtils.XP.rotationDegrees(90.0F));
				poseStack.mulPose(QuaternionUtils.ZP.rotationDegrees(Mth.lerp(partialTicks, tridentEntity.xRotO, tridentEntity.getXRot()) + 90.0F));
				poseStack.translate(0.0D, -0.8D, -0.0D);
			} else {
				poseStack.mulPose(QuaternionUtils.YP.rotationDegrees(Mth.lerp(partialTicks, tridentEntity.yRotO, tridentEntity.getYRot()) - 90.0F));
				poseStack.mulPose(QuaternionUtils.ZP.rotationDegrees(Mth.lerp(partialTicks, tridentEntity.xRotO, tridentEntity.getXRot()) + 90.0F));
			}
		} else {
			poseStack.mulPose(QuaternionUtils.YP.rotationDegrees(Mth.lerp(partialTicks, tridentEntity.yRotO, tridentEntity.getYRot()) - 90.0F));
			poseStack.mulPose(QuaternionUtils.ZP.rotationDegrees(Mth.lerp(partialTicks, tridentEntity.xRotO, tridentEntity.getXRot()) + 90.0F));
		}

		VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(multiSourceBuffer, this.model.renderType(instnace.getTextureLocation(tridentEntity)), false, tridentEntity.isFoil());
		this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();

		super.render(tridentEntity, yRot, partialTicks, poseStack, multiSourceBuffer, packedLight);

		info.cancel();
	}
}