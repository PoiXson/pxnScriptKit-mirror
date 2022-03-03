package com.poixson.scriptkit.scripting;

import java.util.concurrent.atomic.AtomicLong;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.poixson.scriptkit.exceptions.JSFunctionNotFoundException;
import com.poixson.tools.xTime;
import com.poixson.utils.Utils;


public class ScriptInstance {
	public static final long STALE_TIMEOUT = xTime.ParseToLong("3m");

	protected final CraftScript craftscript;

	protected final String filename;
	protected final Script[] compiled;
	protected final Scriptable scope;

	protected final AtomicLong lastUsed = new AtomicLong(0L);



	public ScriptInstance(final CraftScript craftscript, final String filename,
			final Scriptable sharedScope, final Script[] compiled) {
		this.craftscript = craftscript;
		this.compiled    = compiled;
		this.filename    = filename;
		{
			final Context context = Context.enter();
			try {
				this.scope = context.newObject(sharedScope);
				this.scope.setPrototype(sharedScope);
				this.scope.setParentScope(null);
			} finally {
				Utils.SafeClose(context);
			}
		}
	}



	public Object run() {
		if (this.hasFailed())
			return null;
		this.setLastUsed();
		final Context context = Context.enter();
		final Object result;
		try {
			result = this.compiled[0].exec(context, this.scope);
		} catch (Exception e) {
			this.setFailed();
			e.printStackTrace();
			return e;
		} finally {
			Utils.SafeClose(context);
		}
		this.setLastUsed();
		return result;
	}

	public Object runAll() {
		if (this.hasFailed())
			return null;
		this.setLastUsed();
		if (this.compiled == null)
			return null;
		final Context context = Context.enter();
		Object result = null;
		try {
			for (final Script script : this.compiled) {
				final Object r = script.exec(context, this.scope);
				if (r != null) {
					if (result == null)
						result = r;
				}
				this.setLastUsed();
			}
		} catch (Exception e) {
			this.setFailed();
			e.printStackTrace();
			return e;
		} finally {
			Utils.SafeClose(context);
		}
		return result;
	}

	public Object call(final String funcName, final Object[] args) {
		if (this.hasFailed())
			return null;
		if (Utils.isEmpty(funcName)) {
			return this.run();
		}
		this.setLastUsed();
		final Context context = Context.enter();
		final Object result;
		try {
			final Object funcObj = this.scope.get(funcName, this.scope);
			if (funcObj == null)
				throw new JSFunctionNotFoundException(this.filename, funcName, funcObj);
			final Function func;
			try {
				func = (Function) funcObj;
			} catch (Exception e) {
				throw new JSFunctionNotFoundException(this.filename, funcName, funcObj);
			}
			result = func.call(context, this.scope, this.scope, args);
		} catch (Exception e) {
			this.setFailed();
			e.printStackTrace();
			return e;
		} finally {
			Utils.SafeClose(context);
		}
		this.setLastUsed();
		return result;
	}



	protected void setLastUsed() {
		this.setLastUsed(Utils.GetMS());
	}
	protected void setLastUsed(final long current) {
		this.lastUsed.set(current);
	}


	public boolean isStale() {
		return this.isStale(Utils.GetMS());
	}
	public boolean isStale(final long current) {
		final long last = this.lastUsed.get();
		if (last == 0L) {
			this.setLastUsed(current);
			return false;
		}
		return (current > last + STALE_TIMEOUT);
	}



	public boolean hasFailed() {
		return this.craftscript.hasFailed();
	}
	public boolean setFailed(final String msg) {
		return this.craftscript.setFailed(msg);
	}
	public boolean setFailed() {
		return this.craftscript.setFailed();
	}



}
