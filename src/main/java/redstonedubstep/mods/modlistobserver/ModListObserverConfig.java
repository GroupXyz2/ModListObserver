package redstonedubstep.mods.modlistobserver;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class ModListObserverConfig {
	public static final ForgeConfigSpec SERVER_SPEC;
	public static final Config CONFIG;

	static {
		final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);

		SERVER_SPEC = specPair.getRight();
		CONFIG = specPair.getLeft();
	}

	public static class Config {
		public BooleanValue logJoiningModList;
		public BooleanValue logServerMods;
		public BooleanValue allowlistEnabled;
		public BooleanValue broadcastModListOnJoin;
		public IntValue modlistCommandPermissionLevel;
		public IntValue broadcastPermissionLevel;
		public IntValue receiveBroadcastPermissionLevel;
		public ForgeConfigSpec.ConfigValue<List<String>> allowlist;

		Config(ForgeConfigSpec.Builder builder) {
			logJoiningModList = builder
					.comment(" --- ModListObserver Config File --- ", "Should the current mod list of joining players be printed in the server console?")
					.define("logJoiningModList", true);
			logServerMods = builder
					.comment("Should mods of a joining player that are also present on the server be logged?")
					.define("logServerMods", true);
			allowlistEnabled = builder
					.comment("Should only mods that are not allowlisted (via the \"allowlist\" config) be recorded and logged?")
					.define("allowlistEnabled", false);
			broadcastModListOnJoin = builder
					.comment("Should the current mod list of a joining player be broadcast in chat to online players?")
					.define("broadcastModListOnJoin", false);
			modlistCommandPermissionLevel = builder
					.comment("What op permission level should be the requirement for being able to execute /modlist?")
					.defineInRange("modlistCommandPermissionLevel", 3, 0, 4);
			broadcastPermissionLevel = builder
					.comment("What's the maximum (inclusive) op permission level of joining players that should have their mod list broadcast in chat to eligible players?", "For example: With a value of 0, the mod list of joining non-admins will be broadcast, while the mod list of joining admins (op permission level > 0) will not be broadcast.")
					.defineInRange("broadcastPermissionLevel", 0, 0, 4);
			receiveBroadcastPermissionLevel = builder
					.comment("What op permission level should be the requirement for receiving in-game broadcasts of mod lists of joining players?")
					.defineInRange("receiveBroadcastPermissionLevel", 1, 0, 4);
			allowlist = builder
					.comment("A comma-separated list of mod ids that are allowed by the server and thus shouldn't be recorded and logged. Example: [\"jei\", \"neat\"]")
					.define("allowlist", new ArrayList<>());
		}
	}
}