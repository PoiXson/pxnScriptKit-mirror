package com.poixson.scriptkit.api.chunks;

import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;


public class ChunkDAO_Populate extends ChunkDAO {

	public final WorldInfo worldInfo;
	public final LimitedRegion region;



	public ChunkDAO_Populate(final WorldInfo worldInfo,
			final int chunkX, final int chunkZ,
			final LimitedRegion region) {
		super(null, chunkX, chunkZ);
		if (region == null) throw new NullPointerException("region");
		this.worldInfo = worldInfo;
		this.region    = region;
	}



	@Override
	public void setBlock(final int x, final int y, final int z, final BlockData material) {
		this.region.setBlockData(x, y, z, material);
	}



	@Override
	public int getSeed() {
		return (int) (this.worldInfo.getSeed() % 2147483647L);
	}



}
