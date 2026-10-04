package mrkartoshki.fling.client.mixin;

import mrkartoshki.fling.client.FlingClient;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
	@Shadow
	private Minecraft minecraft;

	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	private void fling$keyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
		if (window != minecraft.getWindow().handle()) {
			return;
		}
		if (minecraft.options.keyDrop.matches(event) && FlingClient.handleDrop(minecraft, action, event.modifiers())) {
			ci.cancel();
		} else if (minecraft.options.keyUse.matches(event) && FlingClient.handleCatch(minecraft, action)) {
			ci.cancel();
		}
	}
}
