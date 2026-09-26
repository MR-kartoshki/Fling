package mrkartoshki.fling.client.mixin;

import mrkartoshki.fling.client.ItemEntityRenderStateExt;
import mrkartoshki.fling.client.ItemPhysics;
import mrkartoshki.fling.ItemEntityRotationExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(net.minecraft.client.renderer.entity.state.ItemEntityRenderState.class)
public abstract class ItemEntityRenderStateMixin implements ItemEntityRenderStateExt {
	@Unique
	private boolean fling$isBlock;
	@Unique
	private float fling$xRot;
	@Unique
	private float fling$yRot;

	@Override
	public boolean fling$isBlock() {
		return fling$isBlock;
	}

	@Override
	public float fling$getXRot() {
		return fling$xRot;
	}

	@Override
	public float fling$getYRot() {
		return fling$yRot;
	}

	@Override
	public void fling$extractPhysics(net.minecraft.world.entity.item.ItemEntity entity) {
		var state = (net.minecraft.client.renderer.entity.state.ItemEntityRenderState) (Object) this;
		fling$isBlock = state.item.usesBlockLight();
		ItemPhysics.calculateRotation(entity, state);
		fling$xRot = ((ItemEntityRotationExt) entity).fling$getRotation();
		fling$yRot = (float) Math.toRadians(entity.getYRot());
	}
}
