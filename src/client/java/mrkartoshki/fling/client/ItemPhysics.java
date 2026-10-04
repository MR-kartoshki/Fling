package mrkartoshki.fling.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mrkartoshki.fling.ItemEntityRotationExt;
import mrkartoshki.fling.client.mixin.EntityAccessor;
import mrkartoshki.fling.client.mixin.ItemStackRenderStateAccessor;
import mrkartoshki.fling.client.mixin.LayerRenderStateAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

public final class ItemPhysics {
	private static final float ROTATE_SPEED = 1.0F;
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private ItemPhysics() {}

	public static void calculateRotation(ItemEntity entity, ItemEntityRenderState state) {
		Vec3 velocity = entity.getDeltaMovement();
		float motion = (float) velocity.length();
		float rotateBy = CLIENT.getDeltaTracker().getRealtimeDeltaTicks() * 0.25F * ROTATE_SPEED * Math.min(1.0F, motion * 2.0F);
		if (CLIENT.isPaused()) {
			return;
		}

		boolean block = ((ItemEntityRenderStateExt) state).fling$isBlock();
		ItemEntityRotationExt rotation = (ItemEntityRotationExt) entity;
		if (entity.onGround() && !entity.isInWater()) {
			rotation.fling$setRotation(0);
			return;
		}
		if (entity.isInWater() && fluidAt(entity) != null) {
			if (velocity.horizontalDistanceSqr() < 0.0001) {
				float angle = rotation.fling$getRotation();
				float step = Math.max(rotateBy * 2.0F, 0.01F);
				rotation.fling$setRotation(Math.abs(angle) <= step ? 0 : angle - Math.copySign(step, angle));
				return;
			}
			float direction = velocity.horizontalDistanceSqr() > 0 ? 1.0F : Math.signum((float) velocity.y);
			rotation.fling$setRotation(rotation.fling$getRotation() + direction * rotateBy * 2.0F);
			return;
		}
		if (motion < 0.005F) {
			return;
		}
		if (velocity.horizontalDistanceSqr() < 0.0025) {
			float direction = Math.signum((float) velocity.y);
			rotation.fling$setRotation(rotation.fling$getRotation() + direction * rotateBy * 2.0F);
			return;
		}

		Vec3 stuck = ((EntityAccessor) entity).fling$getStuckSpeedMultiplier();
		if (stuck != null && stuck.lengthSqr() > 0) {
			rotateBy *= stuck.x * 0.2;
		}

		if (block) {
			if (!entity.onGround()) {
				var fluid = fluidAt(entity);
				if (fluid == null) {
					fluid = fluidAt(entity, true);
				}
				if (fluid != null) {
					rotateBy /= 1.0F + fluid.getTickDelay(entity.level()) / 5.0F;
				}
				rotation.fling$setRotation(rotation.fling$getRotation() + rotateBy * 2);
			}
		} else if (entity.onGround()) {
			rotation.fling$setRotation(0);
		} else {
			var fluid = fluidAt(entity);
			if (fluid != null) {
				rotateBy /= 1.0F + fluid.getTickDelay(entity.level()) / 5.0F;
			}
			rotation.fling$setRotation(rotation.fling$getRotation() + rotateBy * 2);
		}
	}

	private static net.minecraft.world.level.material.Fluid fluidAt(ItemEntity item) {
		return fluidAt(item, false);
	}

	private static net.minecraft.world.level.material.Fluid fluidAt(ItemEntity item, boolean below) {
		var pos = item.blockPosition();
		if (below) {
			pos = pos.below();
		}
		var state = item.level().getFluidState(pos);
		var fluid = state.getType();
		if (below || fluid.getTickDelay(item.level()) == 0) {
			return below ? fluid : null;
		}
		return item.position().y - pos.getY() - 0.2 <= state.getHeight(item.level(), pos) ? fluid : null;
	}

	public static boolean submit(ItemEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, RandomSource random) {
		if (state.ageInTicks < 1 || state.item.isEmpty()) {
			return false;
		}

		pose.pushPose();
		random.setSeed(state.seed);
		int models = modelCount(state.count);
		boolean block = ((ItemEntityRenderStateExt) state).fling$isBlock();
		var transform = ((LayerRenderStateAccessor) ((ItemStackRenderStateAccessor) state.item).callFirstLayer()).getItemTransform();
		pose.rotate(Axis.XP.rotation((float) Math.PI / 2));
		pose.rotate(Axis.ZP.rotation(((ItemEntityRenderStateExt) state).fling$getYRot()));
		pose.translate(0, block ? -0.2 : 0, block ? -0.08 : -0.04);
		double height = transform.scale().y();
		if (block) {
			pose.translate(0, height, 0);
		}
		pose.rotate(Axis.YP.rotation(((ItemEntityRenderStateExt) state).fling$getXRot()));
		if (block) {
			pose.translate(0, -height, 0);
		}

		float sx = transform.scale().x();
		float sy = transform.scale().y();
		float sz = transform.scale().z();
		for (int i = 0; i < models; i++) {
			pose.pushPose();
			if (i > 0 && block) {
				pose.translate((random.nextFloat() * 2 - 1) * sx, (random.nextFloat() * 2 - 1) * sy, (random.nextFloat() * 2 - 1) * sz);
			}
			state.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
			pose.popPose();
			if (!block) {
				pose.translate(0, 0, 0.09375F * sz);
			}
		}
		pose.popPose();
		return true;
	}

	private static int modelCount(int count) {
		if (count > 48) return 5;
		if (count > 32) return 4;
		if (count > 16) return 3;
		return count > 1 ? 2 : 1;
	}
}
