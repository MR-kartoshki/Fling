package mrkartoshki.fling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FlingConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(Fling.MOD_ID);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fling.json");
	private static volatile FlingConfig current = new FlingConfig();

	public boolean throwing = true;
	public boolean catching = true;
	public boolean customRendering = true;
	public float chargeDuration = 2.0F;
	public float throwStrength = 1.0F;

	public static FlingConfig get() {
		return current;
	}

	public static void load() {
		try {
			if (Files.exists(PATH)) {
				FlingConfig loaded = GSON.fromJson(Files.readString(PATH), FlingConfig.class);
				if (loaded == null) {
					throw new IllegalArgumentException("Config must be a JSON object");
				}
				loaded.validate();
				current = loaded;
			} else {
				save(current);
			}
		} catch (IOException | RuntimeException e) {
			LOGGER.error("Could not load {}. Using defaults; the existing file has been preserved.", PATH, e);
		}
	}

	public static void save(FlingConfig config) throws IOException {
		config.validate();
		Files.createDirectories(PATH.getParent());
		Path temp = Files.createTempFile(PATH.getParent(), "fling-", ".json.tmp");
		try {
			Files.writeString(temp, GSON.toJson(config) + "\n");
			try {
				Files.move(temp, PATH, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temp, PATH, StandardCopyOption.REPLACE_EXISTING);
			}
			current = config;
		} finally {
			Files.deleteIfExists(temp);
		}
	}

	private void validate() {
		if (!Float.isFinite(chargeDuration) || chargeDuration < 0.25F || chargeDuration > 10.0F) {
			throw new IllegalArgumentException("chargeDuration must be between 0.25 and 10 seconds");
		}
		if (!Float.isFinite(throwStrength) || throwStrength < 0.1F || throwStrength > 5.0F) {
			throw new IllegalArgumentException("throwStrength must be between 0.1 and 5");
		}
	}
}
