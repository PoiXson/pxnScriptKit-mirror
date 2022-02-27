package com.poixson.scriptkit.api;


public interface ChunkGeneratorFactory {


	public ScriptChunkGenerator newInstance(final String worldName, final String argsStr);


}
