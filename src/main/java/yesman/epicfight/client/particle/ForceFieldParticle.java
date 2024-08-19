package yesman.epicfight.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.Mesh.RawMesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.DragonCrystalLinkPhase;

@OnlyIn(Dist.CLIENT)
public class ForceFieldParticle extends TexturedCustomModelParticle {
	private LivingEntityPatch<?> caster;
	
	public ForceFieldParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RawMesh particleMesh, ResourceLocation texture) {
		super(level, x, y, z, xd, yd, zd, particleMesh, texture);
		this.lifetime = DragonCrystalLinkPhase.CHARGING_TICK;
		this.hasPhysics = false;
		this.roll = (float)xd;
		this.pitch = (float)zd;
		
		Entity entity = level.getEntity((int)Double.doubleToLongBits(yd));
		
		if (entity != null) {
			this.caster = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
		}
	}
	
	@Override
	public ParticleRenderType getRenderType() {
		return EpicFightParticleRenderTypes.PARTICLE_MODEL_NO_NORMAL;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		this.yaw += 36.0F;
		this.scale += (float)(Math.max(30 - this.age, 0)) / 140.0F;
		
		if (this.caster != null && this.caster.getStunShield() <= 0.0F) {
			this.remove();
		}
		
		for (int x = -1; x <= 1; x+=2) {
			for (int z = -1; z <= 1; z += 2) {
				Vec3 rand = new Vec3(Math.random() * x, Math.random(), Math.random() * z).normalize().scale(10.0D);
				this.level.addParticle(EpicFightParticles.DUST_CONTRACTIVE.get(), this.x + rand.x, this.y + rand.y - 1.0D, this.z + rand.z, -rand.x, -rand.y, -rand.z);
			}
		}
	}
	
	@Override
	protected void setupPoseStack(PoseStack poseStack, Camera camera, float partialTicks) {
		float yaw = Mth.lerp(partialTicks, this.yawO, this.yaw);
		Vec3 vec3 = camera.getPosition();
		float x = (float)(Mth.lerp(partialTicks, this.xo, this.x) - vec3.x());
		float y = (float)(Mth.lerp(partialTicks, this.yo, this.y) - vec3.y());
		float z = (float)(Mth.lerp(partialTicks, this.zo, this.z) - vec3.z());
		float scale = (float)Mth.lerp((double)partialTicks, this.scaleO, this.scale);
		poseStack.translate(x, y, z);
		poseStack.mulPose(QuaternionUtils.XP.rotationDegrees(this.pitch));
		poseStack.mulPose(QuaternionUtils.ZP.rotationDegrees(this.roll));
		poseStack.mulPose(QuaternionUtils.YP.rotationDegrees(yaw));
		poseStack.scale(scale, scale, scale);
	}
	
	@Override
	public int getLightColor(float p_107086_) {
		int i = super.getLightColor(p_107086_);
		int k = i >> 16 & 255;
		return 240 | k << 16;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new ForceFieldParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, Meshes.FORCE_FIELD, EnderDragonRenderer.CRYSTAL_BEAM_LOCATION);
		}
	}
}