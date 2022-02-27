package com.poixson.scriptkit.api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.poixson.scriptkit.ScriptKitPlugin;


public class ScriptKitAPI {
	protected static final String LOG_PREFIX  = ScriptKitPlugin.LOG_PREFIX;
	protected static final Logger log = ScriptKitPlugin.log;

	protected static final AtomicReference<ScriptKitAPI> instance = new AtomicReference<ScriptKitAPI>(null);

	// world generators
	protected final ConcurrentHashMap<String, ChunkGeneratorFactory> factories =
			new ConcurrentHashMap<String, ChunkGeneratorFactory>();
	protected final ConcurrentHashMap<String, ScriptChunkGenerator> generators =
			new ConcurrentHashMap<String, ScriptChunkGenerator>();



	public static ScriptKitAPI GetAPI() {
		// existing
		{
			final ScriptKitAPI api = instance.get();
			if (api != null)
				return api;
		}
		// new instance
		{
			final ScriptKitAPI api = new ScriptKitAPI();
			if (instance.compareAndSet(null, api))
				return api;
		}
		return GetAPI();
	}

	public ScriptKitAPI() {
	}



	// -------------------------------------------------------------------------------
	// factories



	public ChunkGeneratorFactory getChunkGenFactory(final String factoryName) {
		final ChunkGeneratorFactory factory = this.factories.get(factoryName);
		if (factory == null)
			throw new RuntimeException("World generator factory not found: " + factoryName);
		return factory;
	}
	public void addGenFactory(final String factoryName, final ChunkGeneratorFactory factory) {
		this.factories.put(factoryName, factory);
	}
	public void removeGenFactory(final String factoryName) {
		this.factories.remove(factoryName);
	}
	public boolean hasGenFactory(final String factoryName) {
		return this.factories.contains(factoryName);
	}



	// -------------------------------------------------------------------------------
	// generators



	public ScriptChunkGenerator[] getGenerators() {
		return this.generators.values().toArray(new ScriptChunkGenerator[0]);
	}

	public boolean hasChunkGenerator(final String worldName) {
		return this.generators.containsKey(worldName);
	}

	public ScriptChunkGenerator getChunkGenerator(final String genName,
			final String worldName, final String argsStr) {
		// existing generator
		{
			final ScriptChunkGenerator gen = this.generators.get(worldName);
			if (gen != null)
				return gen;
		}
		// new generator
		{
			final ChunkGeneratorFactory factory = this.getChunkGenFactory(genName);
			if (factory == null) {
				log.severe(LOG_PREFIX + "World/chunk generator not found: " + genName);
				return null;
			}
			final ScriptChunkGenerator gen = factory.newInstance(worldName, argsStr);
			if (gen == null) {
				log.severe(LOG_PREFIX + "Failed to create a new world/chunk generator: " + genName);
				return null;
			}
			final ScriptChunkGenerator existing =
				this.generators.putIfAbsent(worldName, gen);
			if (existing == null)
				return gen;
			return existing;
		}
	}



}
