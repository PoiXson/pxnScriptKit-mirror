package com.poixson.scriptkit.scripting;

import java.util.concurrent.atomic.AtomicBoolean;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

import com.poixson.tools.Keeper;


public class RhinoContextFactory extends ContextFactory {

	protected static final AtomicBoolean inited = new AtomicBoolean(false);



	public static void init() {
		final RhinoContextFactory factory = new RhinoContextFactory();
		Keeper.add(factory);
		if (inited.compareAndSet(false, true)) {
			ContextFactory.initGlobal(factory);
		}
	}



	@Override
	protected Context makeContext() {
		final Context context = super.makeContext();
		context.setLanguageVersion(Context.VERSION_ES6);
		context.setOptimizationLevel(9);
		return context;
	}



	@Override
	protected boolean hasFeature(final Context context, final int featureIndex) {
		switch (featureIndex) {
			case Context.FEATURE_STRICT_MODE:
			case Context.FEATURE_ENABLE_JAVA_MAP_ACCESS:
			case Context.FEATURE_LOCATION_INFORMATION_IN_ERROR:
				return true;
			default: break;
		}
		return super.hasFeature(context, featureIndex);
	}



}
