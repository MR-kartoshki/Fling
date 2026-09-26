package mrkartoshki.fling.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mrkartoshki.fling.client.ItemEntityRenderStateExt;
import mrkartoshki.fling.client.ItemPhysics;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity, ItemEntityRenderState> {
	@Shadow
	@Final
	private RandomSource random;

	protected ItemEntityRendererMixin(EntityRendererProvider.Context context) {
		super(context);
	}

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
	private void fling$extractPhysics(ItemEntity entity, ItemEntityRenderState state, float partialTick, CallbackInfo ci) {
		((ItemEntityRenderStateExt) state).fling$extractPhysics(entity);
	}

	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
	private void fling$submitPhysics(ItemEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
		if (ItemPhysics.submit(state, pose, collector, camera, random)) {
			super.submit(state, pose, collector, camera);
			ci.cancel();
		}
	}
}
