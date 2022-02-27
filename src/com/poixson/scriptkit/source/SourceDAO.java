package com.poixson.scriptkit.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.utils.FileUtils;
import com.poixson.utils.Utils;


public class SourceDAO {

	public final String pathLoc;
	public final String pathRes;

	public final String file;
	public final boolean isreal;

	public final String code;
	public final long timestamp;



	public static SourceDAO Find(final JavaPlugin plugin,
			final String pathLoc, final String pathRes,
			final String filename)
			throws FileNotFoundException {
		InputStream in = null;
		boolean isreal = false;
		// local file
		{
			final File file = new File(pathLoc, filename);
			if (file.isFile()) {
				in = new FileInputStream(file);
				isreal = true;
			}
		}
		// resource file
		if (in == null) {
			in = plugin.getResource(pathRes + "/" + filename);
		}
		if (in == null) throw new FileNotFoundException(filename);
		final String code = FileUtils.ReadInputStream(in);
		Utils.SafeClose(in);
		final SourceDAO dao =
			new SourceDAO(
				isreal,
				pathLoc, pathRes,
				filename,
				code
			);
		System.out.println(String.format("Loaded %s script: %s", (isreal ? "local" : "resource"), filename));
		return dao;
	}



	public SourceDAO(final boolean isreal,
			final String pathLoc, final String pathRes,
			final String file, final String code) {
		this.pathLoc = pathLoc;
		this.pathRes = pathRes;
		this.file = file;
		this.code = code;
		this.isreal = isreal;
		if (isreal) {
			this.timestamp = Utils.GetMS();
		} else {
			this.timestamp = 0;
		}
	}



	public boolean hasChanged() {
		final File file = new File(this.pathLoc, this.file);
		try {
			final long lastModified = FileUtils.LastModified(file) * 1000L;
			if (!this.isreal)
				return true;
			return (lastModified > this.timestamp);
		} catch (IOException ignore) { }
		return false;
	}



}
