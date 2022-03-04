package com.poixson.scriptkit.api;


public enum ScriptHookType {
	GEN("generate"),
	POST("post_generate"),
	CAN_SPAWN("can_spawn"),
	GET_SPAWN("get_spawn");



	public final String name;



	ScriptHookType(final String name) {
		this.name = name;
	}



}
