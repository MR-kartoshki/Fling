package mrkartoshki.fling.network;

import mrkartoshki.fling.Fling;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ThrowPayload(float charge, boolean wholeStack) implements CustomPacketPayload {
	public static final Type<ThrowPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Fling.MOD_ID, "throw"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ThrowPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, ThrowPayload::charge,
		ByteBufCodecs.BOOL, ThrowPayload::wholeStack,
		ThrowPayload::new
	);

	@Override
	public Type<ThrowPayload> type() {
		return TYPE;
	}
}
