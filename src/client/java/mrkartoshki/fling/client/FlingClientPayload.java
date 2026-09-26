package mrkartoshki.fling.client;

import mrkartoshki.fling.network.ThrowPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FlingClientPayload {
	private FlingClientPayload() {}

	public static void send(float charge, boolean wholeStack) {
		if (ClientPlayNetworking.canSend(ThrowPayload.TYPE)) {
			ClientPlayNetworking.send(new ThrowPayload(charge, wholeStack));
		}
	}
}
