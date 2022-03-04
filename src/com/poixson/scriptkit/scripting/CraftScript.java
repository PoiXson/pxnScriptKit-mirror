package com.poixson.scriptkit.scripting;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.poixson.scriptkit.source.ScriptLoader;
import com.poixson.scriptkit.source.SourceDAO;
import com.poixson.tools.CoolDown;
import com.poixson.utils.Utils;


public class CraftScript {

	protected final ConcurrentHashMap<Thread, ScriptInstance> instances =
			new ConcurrentHashMap<Thread, ScriptInstance>();

	protected final ScriptLoader loader;
	protected final CoolDown reloadCool = new CoolDown("5s");

	protected final AtomicReference<Script[]> compiled = new AtomicReference<Script[]>(null);
	protected final AtomicReference<String> filename = new AtomicReference<String>(null);

	protected final Scriptable sharedScope;

	protected final AtomicBoolean failed = new AtomicBoolean(false);



	private static final AtomicBoolean inited = new AtomicBoolean(false);
	static {
		if (inited.compareAndSet(false, true))
				RhinoContextFactory.init();
	}



	public CraftScript(final ScriptLoader loader) {
		this.loader = loader;
		// shared scope
		{
			final Context context = Context.enter();
			context.setOptimizationLevel(9);
			context.setLanguageVersion(Context.VERSION_ES6);
			try {
				this.sharedScope = new ImporterTopLevel(context);
				this.sharedScope.put("out",  this.sharedScope, System.out);
			} finally {
				Utils.SafeClose(context);
			}
		}
	}



	public Object call(final String funcName, final Object...args) {
		final ScriptInstance script = this.getScriptInstance();
		if (script == null)
			return null;
		return script.call(funcName, args);
	}



	public ScriptInstance getScriptInstance() {
		// check files modified
		if (this.reloadCool.again()
		&&  this.loader.hasChanged()) {
			System.out.println("Reloading script: " + this.loader.filename);
			this.reload();
		} else
		if (this.hasFailed())
			return null;
		final Thread thread = Thread.currentThread();
		// existing
		{
			final ScriptInstance script = this.instances.get(thread);
			if (script != null)
				return script;
		}
		// new instance
		{
			SourceDAO[] sources;
			try {
				sources = this.loader.getSources();
			} catch (FileNotFoundException e) {
				this.setFailed();
				e.printStackTrace();
				return null;
			}
			final Script[] compiled = this.getCompiledScripts(sources);
			final ScriptInstance instance =
				new ScriptInstance(
					this,
					this.getFileName(),
					this.sharedScope,
					compiled
				);
			final ScriptInstance existing = this.instances.putIfAbsent(thread, instance);
			if (existing == null) {
				// initial run
				instance.runAll();
				return instance;
			}
			return existing;
		}
	}

	public Script[] getCompiledScripts(final SourceDAO[] sources) {
		if (this.hasFailed())
			return null;
		// existing
		{
			final Script[] compiled = this.compiled.get();
			if (compiled != null)
				return compiled;
		}
		// compile
		{
			final Context context = Context.enter();
			context.setOptimizationLevel(9);
			context.setLanguageVersion(Context.VERSION_ES6);
			try {
				final List<Script> list = new ArrayList<Script>();
				for (final SourceDAO src : sources) {
					final Script script = context.compileString(src.code, src.file, 1, null);
					list.add(script);
				}
				final Script[] compiled = list.toArray(new Script[0]);
				if (this.compiled.compareAndSet(null, compiled))
					return compiled;
			} catch (Exception e) {
				this.setFailed("Failed to compile sources");
				e.printStackTrace();
				return null;
			} finally {
				Utils.SafeClose(context);
			}
		}
		return this.getCompiledScripts(sources);
	}



	public void cleanup() {
		final Set<Thread> remove = new HashSet<Thread>();
		final Iterator<Entry<Thread, ScriptInstance>> it = this.instances.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Thread, ScriptInstance> entry = it.next();
			if (entry.getValue().isStale())
				remove.add(entry.getKey());
		}
		final int count = remove.size();
		if (count == 0)
			return;
		System.out.println(String.format("Cleaned %d stale script instances", count));
		for (final Thread entry : remove) {
			this.instances.remove(entry);
		}
	}



	public boolean hasFailed() {
		return this.failed.get();
	}
	public boolean setFailed(final String msg) {
		System.out.println(msg);
		return this.setFailed();
	}
	public boolean setFailed() {
		final boolean previous = this.failed.getAndSet(true);
		if (!previous) {
			System.out.println(String.format(
				"Script %s failed and has been halted!",
				this.getFileName()
			));
		}
		return previous;
	}



	public String getFileName() {
		// cached
		{
			final String filename = this.filename.get();
			if (filename != null) {
				if (filename.length() == 0)
					return null;
				return filename;
			}
		}
		// find file name
		{
			final SourceDAO[] sources;
			try {
				sources = this.loader.getSources();
			} catch (FileNotFoundException e) {
				this.setFailed();
				e.printStackTrace();
				return null;
			}
			final String filename = (Utils.isEmpty(sources) ? "" : sources[0].file);
			if (this.filename.compareAndSet(null, filename))
				return filename;
		}
		return this.getFileName();
	}



	public void reload() {
		this.reloadCool.reset();
		this.loader.reload();
		this.compiled.set(null);
		this.filename.set(null);
		this.instances.clear();
		this.failed.set(false);
	}



}
