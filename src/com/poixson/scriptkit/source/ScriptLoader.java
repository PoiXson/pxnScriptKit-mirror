package com.poixson.scriptkit.source;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.plugin.java.JavaPlugin;


public class ScriptLoader {

	protected final JavaPlugin plugin;

	public final String pathLoc;
	public final String pathRes;
	public final String filename;

	protected final AtomicBoolean realfile = new AtomicBoolean(false);

	protected final AtomicReference<SourceDAO[]> sources = new AtomicReference<SourceDAO[]>();



	public ScriptLoader(final JavaPlugin plugin,
			final String pathLoc, final String pathRes,
			final String filename) {
		this.plugin   = plugin;
		this.pathLoc  = pathLoc;
		this.pathRes  = pathRes;
		this.filename = filename;
	}



	public SourceDAO[] getSources()
			throws FileNotFoundException {
		// existing
		{
			final SourceDAO[] sources = this.sources.get();
			if (sources != null)
				return sources;
		}
		// load sources
		{
			final List<SourceDAO> list = new ArrayList<SourceDAO>();
			this.loadSourcesRecursive(list, this.filename);
			final SourceDAO[] sources = list.toArray(new SourceDAO[0]);
			if (this.sources.compareAndSet(null, sources))
				return sources;
		}
		return this.getSources();
	}

	// load sources recursively
	protected void loadSourcesRecursive(final List<SourceDAO> list, final String filename)
			throws FileNotFoundException {
		// find local or resource file
		final SourceDAO found =
			SourceDAO.Find(
				this.plugin,
				this.pathLoc,
				this.pathRes,
				filename
			);
		if (found == null) throw new RuntimeException("Failed to find script file");
		list.add(found);
		// parse header
		if (found.code.startsWith("//#")){
			String code = found.code;
			String line;
			int pos;
			LINES_LOOP:
			while (code.startsWith("//#")) {
				pos = code.indexOf('\n');
				if (pos == -1) {
					line = code;
					code = "";
				} else {
					line = code.substring(0, pos);
					code = code.substring(pos + 1);
				}
				line = line.substring(3).trim();
				if (line.length() == 0)
					continue LINES_LOOP;
				pos = line.indexOf('=');
				// no =
				if (pos == -1) {
					switch (line) {
//TODO
//					case "":
					default:
						System.out.println(String.format("Unknown statement: %s in file: %s", line, filename));
						break;
					}
				// contains =
				} else {
					final String[] parts = line.split("=", 2);
					final String key = parts[0].trim();
					switch (key) {
					case "include":
						this.loadSourcesRecursive(list, parts[1].trim());
						break;
					default:
						System.out.println(String.format("Unknown statement: %s in file: %s", line, filename));
						break;
					}
				}
			} // end LINES_LOOP
		}
	}



	public void reload() {
		this.sources.set(null);
	}



	public boolean hasChanged() {
		final SourceDAO[] sources = this.sources.get();
		if (sources != null) {
			for (final SourceDAO src : sources) {
				if (src.hasChanged())
					return true;
			}
		}
		return false;
	}



	public boolean isRealFile() {
		return this.realfile.get();
	}



}
