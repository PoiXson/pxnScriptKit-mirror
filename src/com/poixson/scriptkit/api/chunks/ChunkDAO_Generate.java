package com.poixson.scriptkit.api.chunks;

import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;


public class ChunkDAO_Generate extends ChunkDAO {

	public final WorldInfo worldInfo;
	public final ChunkData chunk;



	public ChunkDAO_Generate(final WorldInfo worldInfo,
			final int chunkX, final int chunkZ,
			final ChunkData chunk) {
		super(null, chunkX, chunkZ);
		if (worldInfo == null) throw new NullPointerException("world");
		this.worldInfo = worldInfo;
		this.chunk     = chunk;
	}



	@Override
	public void setBlock(final int x, final int y, final int z, final BlockData material) {
		this.chunk.setBlock(x, y, z, material);
	}



	@Override
	public int getSeed() {
		return (int) (this.worldInfo.getSeed() % 2147483647L);
	}



}
