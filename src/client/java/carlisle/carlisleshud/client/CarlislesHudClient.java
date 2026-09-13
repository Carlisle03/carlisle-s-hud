package carlisle.carlisleshud.client;

import carlisle.carlisleshud.CarlislesHud;
import carlisle.carlisleshud.client.HudConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.Math;
import java.util.UUID;

public class CarlislesHudClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(CarlislesHud.MOD_ID);
	public static Minecraft client = Minecraft.getInstance();
	public static final File configFile = FabricLoader.getInstance().getConfigDir().resolve("carlisles-hud.json").toFile();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static HudConfig config = new HudConfig();

	@Override
	public void onInitializeClient() {
		getConfig();
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(CarlislesHud.MOD_ID, "before_chat"), CarlislesHudClient::extract);
	}

	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
		Font font = client.font;
		Player player = client.player;
		Identifier icon = Identifier.fromNamespaceAndPath(CarlislesHud.MOD_ID, "textures/icon.png");
		// hud background
		int x1 = 10;
		int x2 = 150;
		int y1 = 10;
		int y2 = 95;
		if (2 == config.hudPos) {
			x1 = 310;
			x2 = 450;
        }
		graphics.fill(x1, y1, x2, y2, 0x80000000);


		// text
        Component fpsCounter = Component.literal("FPS: " + client.getFps());
		Component pingCounter = Component.literal("Ping: " + playerPing(player.getUUID()));
		Component coordinates = Component.literal("X: " + Math.round(player.getX()) + " Y: " + Math.round(player.getY()) + " Z: " + Math.round(player.getZ()));
		Component sprintStatus;
		if (!client.player.isSprinting()) { sprintStatus = Component.literal("Walking"); } else { sprintStatus = Component.literal("Sprinting"); }
		if (client.getSingleplayerServer() != null) { pingCounter = Component.literal("Ping: N/A (singleplayer)"); }
		graphics.text(font, fpsCounter, x1+10, y1+10, 0xFFFFFFFF);
		graphics.text(font, pingCounter, x1+10, y1+20, 0xFFFFFFFF);
		graphics.text(font, coordinates, x1+10, y1+30, 0xFFFFFFFF);
		graphics.text(font, sprintStatus, x1+10, y1+40, 0xFFFFFFFF);

		// icon
		graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x2/2+(x1/2), y2-20, 0,0,16,16,16,16);
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
				config = GSON.fromJson(reader, HudConfig.class);
				if (config == null) {
					config = new HudConfig();
				}
            } catch (IOException e) {
				LOGGER.error("Failed to load the config, fallback to defaults.", e);
            }

		} else {
			saveConfig();
		}
	}

	public static void saveConfig(){
		try (FileWriter writer = new FileWriter(configFile)) {
			GSON.toJson(config, writer);
		} catch (IOException e) {
			LOGGER.error("Could not write or save configuration files.", e);
		}
	}
}