package mrkartoshki.fling.network;

import mrkartoshki.fling.Fling;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CatchPayload(int entityId) implements CustomPacketPayload {
	public static final Type<CatchPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Fling.MOD_ID, "catch"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CatchPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, CatchPayload::entityId, CatchPayload::new
	);

	@Override
	public Type<CatchPayload> type() {
		return TYPE;
	}
}
