package com.poixson.scriptkit.scripting;

import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.scriptkit.api.ScriptChunkGenerator;
import com.poixson.scriptkit.api.ScriptKitAPI;


public class ScriptCleanupTask extends BukkitRunnable {



	@Override
	public void run() {
		final ScriptKitAPI api = ScriptKitAPI.GetAPI();
		final ScriptChunkGenerator[] generators = api.getGenerators();
		for (final ScriptChunkGenerator gen : generators) {
			gen.getCraftScript().cleanup();
		}
	}



}
