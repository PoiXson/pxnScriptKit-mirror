package com.poixson.scriptkit.api;


public enum ScriptHook {
	GEN("generate"),
	POST("post_generate"),
	CAN_SPAWN("can_spawn"),
	GET_SPAWN("get_spawn");



	public final String name;



	ScriptHook(final String name) {
		this.name = name;
	}



}
