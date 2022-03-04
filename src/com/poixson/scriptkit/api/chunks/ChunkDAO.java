package com.poixson.scriptkit.api.chunks;

import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;

import com.poixson.commonbukkit.utils.BukkitUtils;
import com.poixson.utils.StringUtils;


public class ChunkDAO {

	public final World world;
	public final int chunkX, chunkZ;
	public final int absX, absZ;



	public ChunkDAO(final World world, final int chunkX, final int chunkZ) {
		this.world  = world;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.absX   = chunkX * 16;
		this.absZ   = chunkZ * 16;
	}



	public static int jsToInt(final Object value) {
		if (value instanceof Integer) return ((Integer) value).intValue();
		if (value instanceof Long)    return ((Long)    value).intValue();
		if (value instanceof Double)  return ((Double)  value).intValue();
		if (value instanceof Float)   return ((Float)   value).intValue();
		throw new ClassCastException();
	}



	public void setBlockJS(final Object x, final Object y, final Object z, final Object material) {
		if (material == null)
			return;
		final int xx = jsToInt(x);
		final int yy = jsToInt(y);
		final int zz = jsToInt(z);
		if (material instanceof BlockData) {
			this.setBlock(xx, yy, zz, (BlockData)material);
			return;
		}
		if (material instanceof String) {
			this.setBlock(xx, yy, zz, StringUtils.ToString(material));
			return;
		}
		if (material instanceof ConsString) {
			this.setBlock(xx, yy, zz, Context.toString(material));
			return;
		}
		throw new RuntimeException("Unknown block object type: " + material.getClass().getName());
	}

	public void setBlocksJS(final Object blocks) {
		final List<?> list = (List<?>) blocks;
		for (final Object entry : list) {
			if (entry == null) continue;
			@SuppressWarnings("unchecked")
			final Map<String, Object> map = (Map<String, Object>) entry;
			final Object type = map.get("type");
			final Object x    = map.get("x");
			final Object y    = map.get("y");
			final Object z    = map.get("z");
			this.setBlockJS(x, y, z, type);
		}
	}



	public void setBlock(final int x, final int y, final int z, final String matStr) {
		final BlockData material = BukkitUtils.ParseBlockType(matStr);
		if (material == null)
			throw new RuntimeException("Unknown block type: " + matStr);
		this.setBlock(x, y, z, material);
	}

	// override in gen/pop child classes
	public void setBlock(final int x, final int y, final int z, final BlockData material) {
		if (this.world == null) throw new NullPointerException("world");
		this.world.setBlockData(this.absX+x, y, this.absZ+z, material);
	}



	public int getSeed() {
		return (int) (this.world.getSeed() % 2147483647L);
	}



}
