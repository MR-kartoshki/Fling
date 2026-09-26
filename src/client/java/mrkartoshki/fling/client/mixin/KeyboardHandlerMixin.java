package mrkartoshki.fling.client.mixin;

import mrkartoshki.fling.client.FlingClientPayload;
import mrkartoshki.fling.network.CatchPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.Util;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	private static final long MAX_CHARGE_NANOS = 2_000_000_000L;

	@Shadow
	private Minecraft minecraft;

	@Unique
	private boolean fling$charging;
	@Unique
	private long fling$chargeStartedAt;
	@Unique
	private boolean fling$wholeStack;

	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	private void fling$keyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
		if (!minecraft.options.keyDrop.matches(event) || minecraft.player == null || minecraft.gui.screen() != null) {
			return;
		}

		if (action == 1 && !fling$charging) {
			fling$charging = true;
			fling$chargeStartedAt = Util.getNanos();
			fling$wholeStack = event.modifiers() != 0;
		} else if (action == 0 && fling$charging) {
			long chargeNanos = Math.min(Util.getNanos() - fling$chargeStartedAt, MAX_CHARGE_NANOS);
			fling$charging = false;
			FlingClientPayload.send((float) chargeNanos / MAX_CHARGE_NANOS, fling$wholeStack);
		}

		ci.cancel();
	}

	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	private void fling$catchOnUseKey(long window, int action, KeyEvent event, CallbackInfo ci) {
		if (!minecraft.options.keyUse.matches(event) || action != 1 || minecraft.player == null || minecraft.gui.screen() != null
			|| !(minecraft.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof ItemEntity item)) {
			return;
		}

		if (ClientPlayNetworking.canSend(CatchPayload.TYPE)) {
			ClientPlayNetworking.send(new CatchPayload(item.getId()));
			ci.cancel();
		}
	}
}
