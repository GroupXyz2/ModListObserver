package redstonedubstep.mods.modlistobserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.authlib.GameProfile;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod(ModListObserver.MODID)
public class ModListObserver {
	public static final String MODID = "modlistobserver";
	private static final Map<GameProfile, Set<String>> ALL_SESSION_MODS = new HashMap<>();
	private static final Map<GameProfile, Set<String>> CURRENT_MODS = new HashMap<>();
	private static final Logger LOGGER = LogManager.getLogger();

	public ModListObserver() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ModListObserverConfig.SERVER_SPEC);
	}

	public void registerCommands(RegisterCommandsEvent event) {
		ModListCommand.register(event.getDispatcher());
	}

	public static Set<String> getAllSessionMods(GameProfile player) {
		ALL_SESSION_MODS.putIfAbsent(player, new HashSet<>());
		return ALL_SESSION_MODS.get(player);
	}

	public static Set<String> getCurrentMods(GameProfile player) {
		CURRENT_MODS.putIfAbsent(player, new HashSet<>());
		return CURRENT_MODS.get(player);
	}

	public static void updateModListOnJoin(Set<String> modList, GameProfile player) {
		CURRENT_MODS.put(player, modList);

		ALL_SESSION_MODS.putIfAbsent(player, modList);
		ALL_SESSION_MODS.get(player).addAll(modList);

		String messageString = String.format("Player %s connected with mods %s", player.getName(), String.join(", ", modList));

		if (ModListObserverConfig.CONFIG.logJoiningModList.get())
			LOGGER.info(messageString);

		if (ModListObserverConfig.CONFIG.broadcastModListOnJoin.get())
			broadcastModList(messageString, player);

	}

	public static void broadcastModList(String messageString, GameProfile player) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

		if (server != null) {
			List<ServerPlayer> playerList = server.getPlayerList().getPlayers();
			int joiningPlayerOpLevel = server.getProfilePermissions(player);

			if (joiningPlayerOpLevel <= ModListObserverConfig.CONFIG.broadcastPermissionLevel.get()) {
				MutableComponent prefix = Component.literal("[").append(Component.literal("ModListObserver").withStyle(ChatFormatting.GRAY)).append("] ");
				Component message = prefix.append(messageString);

				for (ServerPlayer otherPlayer : playerList) {
					if (otherPlayer.hasPermissions(ModListObserverConfig.CONFIG.receiveBroadcastPermissionLevel.get()))
						otherPlayer.sendSystemMessage(message);
				}
			}
		}
	}
}
