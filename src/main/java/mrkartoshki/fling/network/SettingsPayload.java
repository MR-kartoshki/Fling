package mrkartoshki.fling.network;

import mrkartoshki.fling.Fling;
import mrkartoshki.fling.FlingConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SettingsPayload(boolean throwing, boolean catching, float chargeDuration, float throwStrength) implements CustomPacketPayload {
	public static final Type<SettingsPayload> TYPE = new Type<>(Fling.id("settings"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SettingsPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL, SettingsPayload::throwing,
		ByteBufCodecs.BOOL, SettingsPayload::catching,
		ByteBufCodecs.FLOAT, SettingsPayload::chargeDuration,
		ByteBufCodecs.FLOAT, SettingsPayload::throwStrength,
		SettingsPayload::new
	);

	public static SettingsPayload current() {
		FlingConfig config = FlingConfig.get();
		return new SettingsPayload(config.throwing, config.catching, config.chargeDuration, config.throwStrength);
	}

	@Override
	public Type<SettingsPayload> type() {
		return TYPE;
	}
}
