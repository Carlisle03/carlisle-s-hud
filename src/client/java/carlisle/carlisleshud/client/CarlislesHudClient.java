package carlisle.carlisleshud.client;

import carlisle.carlisleshud.CarlislesHud;
import carlisle.carlisleshud.client.config.ConfigDefaults;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.Math;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CarlislesHudClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(CarlislesHud.MOD_ID);
	public static Minecraft client = Minecraft.getInstance();
	public static final File configFile = FabricLoader.getInstance().getConfigDir().resolve("carlisles-hud.json").toFile();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static ConfigDefaults config = new ConfigDefaults();

	@Override
	public void onInitializeClient() {
		getConfig();
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(CarlislesHud.MOD_ID, "before_chat"), CarlislesHudClient::extract);
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker tick) {
		Font font = client.font;
		Player player = client.player;

		if (!client.getDebugOverlay().showDebugScreen()) {
			// hud background
			int tx1 = 10;
			int ty1 = 10;
			if (1 == config.hudPos) {
				tx1 = 10;
			} else if (2 == config.hudPos) {
				tx1 = client.getWindow().getGuiScaledWidth() - 150;
			}

			// text
			Component fpsCounter = Component.literal("FPS: " + client.getFps());
			Component pingCounter = Component.literal("Ping: " + playerPing(player.getUUID()));
			Component coordinates = Component.literal("X: " + Math.round(player.getX()) + " Y: " + Math.round(player.getY()) + " Z: " + Math.round(player.getZ()));
			Component sprintStatus;
			Component minecraftVersion = Component.literal("Minecraft " + FabricLoader.getInstance().
					getModContainer("minecraft")
					.map(container -> container.getMetadata().getVersion().getFriendlyString())
					.orElse("Unknown"));

			if (!client.player.isSprinting()) {
				sprintStatus = Component.literal("Walking");
			} else {
				sprintStatus = Component.literal("Sprinting");
			}
			if (client.getSingleplayerServer() != null) {
				pingCounter = Component.literal("Ping: N/A (singleplayer)");
			}

			graphics.text(font, minecraftVersion, tx1 + 10, ty1 + 10, 0xFFFFFFFF);
			graphics.text(font, fpsCounter, tx1 + 10, ty1 + 20, 0xFFFFFFFF);
			graphics.text(font, pingCounter, tx1 + 10, ty1 + 30, 0xFFFFFFFF);
			graphics.text(font, coordinates, tx1 + 10, ty1 + 40, 0xFFFFFFFF);
			graphics.text(font, sprintStatus, tx1 + 10, ty1 + 50, 0xFFFFFFFF);
			graphics.text(font, getTime(), tx1 + 10, ty1 + 60, 0xFFFFFFFF);

			// waila
			HitResult hit = client.hitResult;
			Component block = Component.literal("");
			ItemStack item = ItemStack.EMPTY;
			if (hit != null && hit.getType() != HitResult.Type.ENTITY) {
				BlockHitResult blockHit = (BlockHitResult) hit;
				BlockPos pos = blockHit.getBlockPos();
				BlockState blockState = client.level.getBlockState(pos);
				block = blockState.getBlock().getName();
				item = blockState.getBlock().asItem().getDefaultInstance();
			}
			int bx = client.getWindow().getGuiScaledWidth() / 2;
			int by = 10;
			if (config.isWAILAEnabled){
				graphics.text(font, block, bx, by + 10, 0xFFFFFFFF);
				graphics.fakeItem(item, bx - 15, by);
			}
		}
	}

	public static int playerPing(UUID playerUUID) {

		PlayerInfo info = client.getConnection().getPlayerInfo(playerUUID);
		if (info != null) {
			return info.getLatency();
		} else {
			return 0;
		}
	}

	public static void getConfig() {
		if (configFile.exists()) {
			try (FileReader reader = new FileReader(configFile)) {
				config = GSON.fromJson(reader, ConfigDefaults.class);
				if (config == null) {
					config = new ConfigDefaults();
				}
			} catch (IOException e) {
				LOGGER.error("Failed to load the config, fallback to defaults.", e);
			}

		} else {
			saveConfig();
		}
	}

	public static void saveConfig() {
		try (FileWriter writer = new FileWriter(configFile)) {
			GSON.toJson(config, writer);
		} catch (IOException e) {
			LOGGER.error("Could not write or save configuration files.", e);
		}
	}

	public static String getTime() {
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss a ");
		if (config.is12HourFormat) {
			timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a");
		}
		return ZonedDateTime.now(ZoneId.systemDefault()).format(timeFormat);
	}
}
