package mrkartoshki.fling;

import mrkartoshki.fling.network.ThrowPayload;
import mrkartoshki.fling.network.CatchPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class Fling implements ModInitializer {
	public static final String MOD_ID = "fling";
	public static final String FLOATING_ITEM_TAG = "fling_floating_item";

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(ThrowPayload.TYPE, ThrowPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(CatchPayload.TYPE, CatchPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ThrowPayload.TYPE, (payload, context) -> {
			if (Float.isFinite(payload.charge())) {
				float charge = Math.clamp(payload.charge(), 0.0F, 1.0F);
				if (charge < 0.075F) {
					ServerPlayer player = context.player();
					ItemStack dropped = player.getInventory().removeFromSelected(payload.wholeStack());
					if (!dropped.isEmpty()) {
						player.containerMenu.broadcastChanges();
						ItemEntity entity = player.drop(dropped, true, Prediction.SERVER_ONLY);
						if (entity != null) {
							entity.addTag(FLOATING_ITEM_TAG);
						}
					}
				} else {
					throwItem(context.player(), charge, payload.wholeStack());
				}
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(CatchPayload.TYPE, (payload, context) -> {
			catchItem(context.player(), payload.entityId());
		});
	}

	private static void catchItem(ServerPlayer player, int entityId) {
		if (player.isSpectator() || !player.isAlive()) {
			return;
		}
		var entity = player.level().getEntity(entityId);
		if (!(entity instanceof ItemEntity item) || item.isRemoved() || item.hasPickUpDelay()
			|| !player.getBoundingBox().inflate(1.0).intersects(item.getBoundingBox())) {
			return;
		}

		ItemStack stack = item.getItem();
		if (stack.isEmpty()) {
			return;
		}
		ItemStack remaining = stack.copy();
		player.getInventory().add(remaining);
		if (remaining.getCount() != stack.getCount()) {
			if (remaining.isEmpty()) {
				item.discard();
			} else {
				item.setItem(remaining);
			}
			player.take(item, stack.getCount() - remaining.getCount());
			player.level().playSound(null, item.getX(), item.getY(), item.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.7F, 1.0F);
		}
	}

	private static void throwItem(ServerPlayer player, float charge, boolean wholeStack) {
		ItemStack held = player.getInventory().getSelectedItem();
		if (held.isEmpty() || player.isSpectator() || !player.isAlive()) {
			return;
		}

		Vec3 look = player.getLookAngle();
		float strength = (float) (1.0 - Math.exp(-2.302585 * charge));
		float speed = 0.15F + 1.5F * strength;
		Vec3 velocity = new Vec3(look.x * speed, look.y * speed - (0.2F * (1.0F - strength)), look.z * speed);
		ItemStack thrown = held.copyWithCount(wholeStack ? held.getCount() : 1);
		Vec3 spawn = player.getEyePosition().add(look.scale(0.55));
		held.shrink(thrown.getCount());
		ItemEntity entity = new ItemEntity(player.level(), spawn.x, spawn.y, spawn.z, thrown);
		entity.setThrower(player);
		entity.addTag(FLOATING_ITEM_TAG);
		entity.setPickUpDelay(10);
		entity.setDeltaMovement(velocity);
		if (player.level().addFreshEntity(entity)) {
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.7F, 0.9F);
		} else {
			player.getInventory().add(thrown);
		}
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
