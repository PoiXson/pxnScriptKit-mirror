package com.poixson.scriptkit.api;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mozilla.javascript.Context;

import com.poixson.commonbukkit.tools.chunks.ChunkDAO;
import com.poixson.commonbukkit.tools.chunks.ChunkDAO_Generate;
import com.poixson.scriptkit.scripting.CraftScript;
import com.poixson.scriptkit.source.ScriptLoader;


public class ScriptChunkGenerator extends ChunkGenerator implements Listener {

	protected final String worldName;

	protected final CraftScript craftscript;



	public ScriptChunkGenerator(final JavaPlugin plugin,
			final String worldName, final String argsStr,
			final String pathLoc, final String pathRes,
			final String filename) {
		this.worldName = worldName;
		final ScriptLoader loader =
			new ScriptLoader(
				plugin,
				pathLoc, pathRes,
				filename
			);
		this.craftscript = new CraftScript(loader);
		// listeners
		final PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(this, plugin);
	}



	@Override
	public void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunkData) {
		final ChunkDAO chunk =
			new ChunkDAO_Generate(
				worldInfo,
				chunkX, chunkZ,
				chunkData
			);
//TODO: find a better way
//worldInfo.getSeed()
		this.craftscript.call(ScriptHookType.GEN.name, chunk);
	}



	// -------------------------------------------------------------------------------



//TODO
//	@Override
//	public List<BlockPopulator> getDefaultPopulators(final World world) {
//		final ArrayList<BlockPopulator> list = new ArrayList<BlockPopulator>();
//		list.add(
//			new BlockPopulator() {
//				@Override
//				public void populate(final WorldInfo worldInfo,
//						final Random random, final int x, final int z,
//						final LimitedRegion region) {
//					final ChunkDAO_Populate chunk =
//						new ChunkDAO_Populate(
//							worldInfo,
//							x,
//							z,
//							region
//						);
//					ScriptChunkGenerator.this.craftscript.call(ScriptHook.POST.name, chunk);
//				}
//			}
//		);
//		return list;
//	}



//	@EventHandler(priority=EventPriority.MONITOR)
//	public void onChunkPopulate(final ChunkPopulateEvent event) {
//		if (event.isAsynchronous())
//			throw new RuntimeException("Async event detected; please notify developer to fix");
//{
//		final Chunk chunk = event.getChunk();
//		final ChunkDAO_Buffered dao =
//			new ChunkDAO_Buffered(
//				event.getWorld(),
//				chunk.getX(),
//				chunk.getZ()
//			);
//		this.postQueue.add(dao);
//System.out.println(String.format("QUEUE    %d  %d  x: %d  z: %d", this.postQueue.size(), this.blocksQueue.size(), dao.absX, dao.absZ));
//}



//	public void processQueue() {
//{
//		int count = 0;
//		for (int i=0; i<10; i++) {
//			final ChunkDAO_Buffered chunk = this.postQueue.poll();
//			if (chunk == null)
//				return;
//			this.craftscript.call(ScriptHook.POST.name, chunk);
//			this.blocksQueue.add(chunk);
//			count++;
//System.out.println(String.format("PROCESS  %d  %d  x: %d  z: %d", this.postQueue.size(), this.blocksQueue.size(), chunk.absX, chunk.absZ));
//		}
//		if (count > 0) {
//			Bukkit.getScheduler().runTaskLater(this.plugin, this, 1L);
//		}
//}

//	@Override
//	public void run() {
//		int count = 0;
//		boolean finished = false;
//		final int size = this.blocksQueue.size();
//		final int imax = Math.max(10, size/10);
//		for (int i=0; i<imax; i++) {
//		for (int i=0; i<size; i++) {
//			final ChunkDAO_Buffered chunk = this.blocksQueue.poll();
//			if (chunk == null) {
//				finished = true;
//				break;
//			}
//			count += chunk.commit();
//		}
//System.out.println("PLACED " + Integer.toString(count) + " BLOCKS");
//		if (!finished)
//		if (this.blocksQueue.size() > 5)
//			Bukkit.getScheduler().runTaskLater(this.plugin, this, 1L);
//	}



//	@Override
//	public List<BlockPopulator> getDefaultPopulators(final World world) {
//		final ArrayList<BlockPopulator> list = new ArrayList<BlockPopulator>();
//		list.add(
//			new BlockPopulator() {
//				@Override
//				public void populate(World world, Random random, Chunk chunk) {
//					if ( chunk.getX() != 0 || chunk.getZ() != 0)
//						return;
//					for ( int x=0; x<16; x++ )
//						for (int z=0; z<16; z++ ) {
//							for ( int y=80; y>0; y-- )
//								chunk.getBlock(x,y,z).setType(Material.STONE);
//							for ( int y=81; y<chunk.getWorld().getMaxHeight(); y++ )
//								chunk.getBlock(x,y,z).setType(Material.AIR);
//					}
//				}
//			}
//		);
//		return list;
//	}



//TODO
//	@Override
//	public List<BlockPopulator> getDefaultPopulators(final World world) {
//		return new ArrayList<BlockPopulator>();
//	}



	@Override
	public boolean canSpawn(final World world, final int x, final int z) {
		final Object result = this.craftscript.call(ScriptHookType.CAN_SPAWN.name, x, z);
		return Context.toBoolean(result);
	}
	public Location getFixedSpawnLocation(final World world, final Random random) {
		final Object result = this.craftscript.call(ScriptHookType.GET_SPAWN.name, world);
		if (result instanceof Location) {
			return (Location) result;
		}
System.out.println();
System.out.println();
System.out.println();
System.out.println();
System.out.println(result.getClass().getName());
System.out.println();
System.out.println();
System.out.println();
System.out.println();
		return null;
	}



	public CraftScript getCraftScript() {
		return this.craftscript;
	}



	@Override
	public boolean isParallelCapable() {
		return true;
	}



}
