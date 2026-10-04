package mrkartoshki.fling.client;

import mrkartoshki.fling.Fling;
import mrkartoshki.fling.FlingConfig;
import mrkartoshki.fling.network.SettingsPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Consumer;

public final class FlingConfigScreen extends Screen {
	private final Screen parent;
	private final FlingConfig draft = new FlingConfig();
	private Component error;

	public FlingConfigScreen(Screen parent) {
		super(Component.translatable("fling.config.title"));
		this.parent = parent;
		FlingConfig config = FlingConfig.get();
		draft.throwing = config.throwing;
		draft.catching = config.catching;
		draft.customRendering = config.customRendering;
		draft.chargeDuration = config.chargeDuration;
		draft.throwStrength = config.throwStrength;
	}

	@Override
	protected void init() {
		int x = width / 2 - 130;
		int y = height / 2 - 78;
		addRenderableWidget(Button.builder(toggleLabel("throwing", draft.throwing), button -> {
			draft.throwing = !draft.throwing;
			button.setMessage(toggleLabel("throwing", draft.throwing));
		}).bounds(x, y, 260, 20).build());
		addRenderableWidget(Button.builder(toggleLabel("catching", draft.catching), button -> {
			draft.catching = !draft.catching;
			button.setMessage(toggleLabel("catching", draft.catching));
		}).bounds(x, y + 24, 260, 20).build());
		addRenderableWidget(Button.builder(toggleLabel("rendering", draft.customRendering), button -> {
			draft.customRendering = !draft.customRendering;
			button.setMessage(toggleLabel("rendering", draft.customRendering));
		}).bounds(x, y + 48, 260, 20).build());
		boolean remote = minecraft.player != null && !minecraft.hasSingleplayerServer();
		var settings = FlingClient.settings(minecraft);
		var duration = addRenderableWidget(new ConfigSlider(x, y + 72, "duration", 0.25F, 10.0F,
			remote ? settings.chargeDuration() : draft.chargeDuration, value -> draft.chargeDuration = value));
		var strength = addRenderableWidget(new ConfigSlider(x, y + 96, "strength", 0.1F, 5.0F,
			remote ? settings.throwStrength() : draft.throwStrength, value -> draft.throwStrength = value));
		duration.active = !remote;
		strength.active = !remote;
		addRenderableWidget(Button.builder(Component.translatable("fling.config.save"), button -> save()).bounds(x, y + 136, 126, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose()).bounds(x + 134, y + 136, 126, 20).build());
	}

	private static Component toggleLabel(String key, boolean enabled) {
		return Component.translatable("fling.config." + key, Component.translatable(enabled ? "options.on" : "options.off"));
	}

	private void save() {
		try {
			FlingConfig.save(draft);
			if (minecraft.hasSingleplayerServer()) {
				var server = minecraft.getSingleplayerServer();
				server.execute(() -> {
					for (var player : server.getPlayerList().getPlayers()) {
						if (ServerPlayNetworking.canSend(player, SettingsPayload.TYPE)) {
							ServerPlayNetworking.send(player, SettingsPayload.current());
						}
					}
				});
			}
			onClose();
		} catch (IOException e) {
			LoggerFactory.getLogger(Fling.MOD_ID).error("Could not save Fling settings", e);
			error = Component.translatable("fling.config.save_error");
		}
	}

	@Override
	public void onClose() {
		minecraft.setScreenAndShow(parent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int y = height / 2 - 78;
		graphics.centeredText(font, title, width / 2, y - 26, 0xFFFFFFFF);
		if (minecraft.player != null && !minecraft.hasSingleplayerServer()) {
			graphics.centeredText(font, Component.translatable("fling.config.server_controls"), width / 2, y + 120, 0xFFAAAAAA);
		}
		if (error != null) {
			graphics.centeredText(font, error, width / 2, y + 162, 0xFFFF5555);
		}
	}

	private static final class ConfigSlider extends AbstractSliderButton {
		private final String key;
		private final float min;
		private final float max;
		private final Consumer<Float> changed;

		private ConfigSlider(int x, int y, String key, float min, float max, float initial, Consumer<Float> changed) {
			super(x, y, 260, 20, Component.empty(), (initial - min) / (max - min));
			this.key = key;
			this.min = min;
			this.max = max;
			this.changed = changed;
			updateMessage();
		}

		private float setting() {
			return Math.clamp(Math.round((min + value * (max - min)) * 100) / 100.0F, min, max);
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.translatable("fling.config." + key, String.format(Locale.ROOT, "%.2f", setting())));
		}

		@Override
		protected void applyValue() {
			changed.accept(setting());
		}
	}
}
