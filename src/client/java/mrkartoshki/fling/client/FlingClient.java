package mrkartoshki.fling.client;

import mrkartoshki.fling.Fling;
import mrkartoshki.fling.FlingConfig;
import mrkartoshki.fling.network.CatchPayload;
import mrkartoshki.fling.network.SettingsPayload;
import mrkartoshki.fling.network.ThrowPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

public class FlingClient implements ClientModInitializer {
	private static SettingsPayload serverSettings = new SettingsPayload(true, true, 2.0F, 1.0F);
	private static boolean charging;
	private static long chargeStartedAt;
	private static boolean wholeStack;
	private static int selectedSlot;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (!canInteract(client) || !FlingConfig.get().throwing || !settings(client).throwing()
				|| client.player.getInventory().getSelectedSlot() != selectedSlot) {
				charging = false;
			}
		});
		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			charging = false;
			serverSettings = new SettingsPayload(true, true, 2.0F, 1.0F);
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> charging = false);
		ClientPlayNetworking.registerGlobalReceiver(SettingsPayload.TYPE, (payload, context) -> {
			if (Float.isFinite(payload.chargeDuration()) && payload.chargeDuration() >= 0.25F && payload.chargeDuration() <= 10.0F
				&& Float.isFinite(payload.throwStrength()) && payload.throwStrength() >= 0.1F && payload.throwStrength() <= 5.0F) {
				serverSettings = payload;
				charging = false;
			}
		});
		HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, Fling.id("charge"), (graphics, delta) -> {
			Minecraft client = Minecraft.getInstance();
			if (!charging || !canInteract(client) || !client.options.getCameraType().isFirstPerson()) {
				return;
			}
			int x = graphics.guiWidth() / 2;
			int y = graphics.guiHeight() / 2 + 12;
			float charge = charge(client);
			int color = wholeStack ? 0xFFFFC857 : 0xFF70D6FF;
			graphics.fill(x - 21, y - 1, x + 21, y + 5, 0xCC000000);
			graphics.fill(x - 20, y, x - 20 + Math.round(40 * charge), y + 4, color);
			graphics.centeredText(client.font, Component.translatable(wholeStack ? "fling.charge.stack" : "fling.charge.single", Math.round(charge * 100)), x, y + 7, color);
		});
	}

	public static SettingsPayload settings(Minecraft client) {
		return client.hasSingleplayerServer() ? SettingsPayload.current() : serverSettings;
	}

	private static float charge(Minecraft client) {
		return Math.clamp((Util.getNanos() - chargeStartedAt) / (settings(client).chargeDuration() * 1_000_000_000.0F), 0.0F, 1.0F);
	}

	private static boolean canInteract(Minecraft client) {
		return client.player != null && client.player.isAlive() && !client.player.isSpectator()
			&& client.gui.screen() == null && client.isWindowActive() && !client.isPaused();
	}

	public static boolean handleDrop(Minecraft client, int action, int modifiers) {
		if (!canInteract(client) || !FlingConfig.get().throwing || !settings(client).throwing()
			|| !ClientPlayNetworking.canSend(ThrowPayload.TYPE)) {
			charging = false;
			return false;
		}
		if (action == 1 && !charging && !client.player.getInventory().getSelectedItem().isEmpty()) {
			charging = true;
			chargeStartedAt = Util.getNanos();
			wholeStack = (modifiers & 0x0002) != 0;
			selectedSlot = client.player.getInventory().getSelectedSlot();
		} else if (action == 0 && charging) {
			charging = false;
			if (client.player.getInventory().getSelectedSlot() == selectedSlot) {
				FlingClientPayload.send(charge(client), wholeStack);
			}
		}
		return true;
	}

	public static boolean handleCatch(Minecraft client, int action) {
		if (action != 1 || !canInteract(client) || !FlingConfig.get().catching || !settings(client).catching()
			|| !ClientPlayNetworking.canSend(CatchPayload.TYPE)) {
			return false;
		}
		Vec3 start = client.player.getEyePosition();
		Vec3 end = start.add(client.player.getLookAngle().scale(3.0));
		double distance = start.distanceToSqr(client.player.pick(3.0, 1.0F, false).getLocation());
		ItemEntity target = null;
		for (ItemEntity item : client.level.getEntitiesOfClass(ItemEntity.class, client.player.getBoundingBox().inflate(1.0))) {
			if (item.hasPickUpDelay() || item.isRemoved()) {
				continue;
			}
			var hit = item.getBoundingBox().inflate(0.1).clip(start, end);
			if (hit.isPresent() && start.distanceToSqr(hit.get()) < distance) {
				distance = start.distanceToSqr(hit.get());
				target = item;
			}
		}
		if (target == null) {
			return false;
		}
		ClientPlayNetworking.send(new CatchPayload(target.getId()));
		return true;
	}
}
