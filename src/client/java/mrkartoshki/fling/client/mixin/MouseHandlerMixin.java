package mrkartoshki.fling.client.mixin;

import mrkartoshki.fling.client.FlingClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
	private void fling$onButton(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
		if (window != minecraft.getWindow().handle()) {
			return;
		}
		MouseButtonEvent event = new MouseButtonEvent(0, 0, info);
		if (minecraft.options.keyDrop.matchesMouse(event) && FlingClient.handleDrop(minecraft, action, info.modifiers())) {
			ci.cancel();
		} else if (minecraft.options.keyUse.matchesMouse(event) && FlingClient.handleCatch(minecraft, action)) {
			ci.cancel();
		}
	}
}
