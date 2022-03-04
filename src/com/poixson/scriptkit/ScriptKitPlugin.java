package com.poixson.scriptkit;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.scriptkit.api.ChunkGeneratorFactory;
import com.poixson.scriptkit.api.ScriptChunkGenerator;
import com.poixson.scriptkit.api.ScriptKitAPI;
import com.poixson.scriptkit.scripting.ScriptCleanupTask;
import com.poixson.tools.xTime;


public class ScriptKitPlugin extends JavaPlugin implements ChunkGeneratorFactory {
	public static final String LOG_PREFIX  = "[ScriptKit] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + "[ScriptKit] " + ChatColor.WHITE;
	public static final Logger log = Logger.getLogger("Minecraft");

	public static final String GENERATOR_NAME = "ScriptKit";
	public static final long DEFAULT_CLEANUP_TICKS = xTime.ParseToLong("5m") / 50L;

	protected static final AtomicReference<ScriptKitPlugin> instance = new AtomicReference<ScriptKitPlugin>(null);
	protected final ScriptKitAPI api = ScriptKitAPI.GetAPI();

	// listeners
	protected final AtomicReference<ScriptKitCommands> commandListener = new AtomicReference<ScriptKitCommands>(null);


	protected final ScriptCleanupTask cleanup = new ScriptCleanupTask();



	public static ScriptKitPlugin GetPlugin() {
		return instance.get();
	}



	@Override
	public void onEnable() {
		if (!instance.compareAndSet(null, this))
			throw new RuntimeException("Plugin instance already enabled?");
		{
			final File path = new File(this.getDataFolder(), "scripts");
			if (!path.isDirectory()) {
				log.info(CHAT_PREFIX + "Creating directory: " + path.toString());
				path.mkdirs();
			}
		}
		// world generator
		this.api.addGenFactory(GENERATOR_NAME, this);
		// commands listener
		{
			final ScriptKitCommands listener = new ScriptKitCommands(this);
			final ScriptKitCommands previous = this.commandListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// script engine cleanup
		this.cleanup.runTaskTimerAsynchronously(this, DEFAULT_CLEANUP_TICKS, DEFAULT_CLEANUP_TICKS);
	}



	@Override
	public void onDisable() {
		// world generator
		this.api.removeGenFactory(GENERATOR_NAME);
		// commands listener
		{
			final ScriptKitCommands listener = this.commandListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// stop listeners
		HandlerList.unregisterAll(this);
		// stop schedulers
		try {
			Bukkit.getScheduler()
				.cancelTasks(this);
		} catch (Exception ignore) {}
		if (!instance.compareAndSet(this, null))
			throw new RuntimeException("Disable wrong instance of plugin?");
	}



	// -------------------------------------------------------------------------------
	// chunk generator



	// factory
	@Override
	public ScriptChunkGenerator newInstance(final String worldName, final String argsStr) {
		log.info(
			String.format(
				"%sWorld <%s> using generator <%s> %s",
				LOG_PREFIX,
				worldName,
				GENERATOR_NAME,
				argsStr
			)
		);
//TODO
		final String pathLoc = (new File(this.getDataFolder(), "scripts")).toString();
		final String pathRes = "scripts";
		final String filename = "flat.js";
		final ScriptChunkGenerator gen =
			new ScriptChunkGenerator(
				this,
				worldName, argsStr,
				pathLoc, pathRes,
				filename
			);
		return gen;
	}



	// generator
	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String argsStr) {
		log.info(String.format("%sWorld <%s> using generator <%s> %s",
			LOG_PREFIX, worldName, GENERATOR_NAME, argsStr));
		return this.api.getChunkGenerator(GENERATOR_NAME, worldName, argsStr);
	}



}
