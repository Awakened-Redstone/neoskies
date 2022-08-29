package skylands.util;

import net.minecraft.world.World;
import skylands.Mod;
import skylands.logic.Island;
import skylands.logic.Skylands;

import java.util.Optional;

public class Worlds {

	public static boolean isIsland(World world) {
		var namespace = world.getRegistryKey().getValue().getNamespace();
		return namespace.equals(Mod.MOD_ID) || namespace.equals("nether");
	}

	public static Optional<Island> getIsland(World world) {
		if(isIsland(world)) {
			return Skylands.instance.islandStuck.get(world.getRegistryKey().getValue().getPath());
		}
		return Optional.empty();
	}

}
