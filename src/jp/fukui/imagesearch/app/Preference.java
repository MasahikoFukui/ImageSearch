package jp.fukui.imagesearch.app;

import java.io.*;
import java.util.*;

/**
 * 初期設定
 */
public class Preference {
	//------------------------------------------------------------------------------
	// 生成と破棄
	//------------------------------------------------------------------------------

	public Preference() {
		this.prop = new Properties();
		loadProperties();
	}

	public void close() {
		saveProperties();
	}

	//------------------------------------------------------------------------------
	// プロパティ
	//------------------------------------------------------------------------------

	public void setDirectory(File dir)   { setFile("directory", dir); }
	public File getDirectory()           { return(getFile("directory")); }
	public void setSourceImage(File src) { setFile("sourceImage", src); }
	public File getSourceImage()         { return(getFile("sourceImage")); }
	public void setDifferency(int d)     { setInt("differency", d); }
	public int  getDifferency()          { return(getInt("differency", 0)); }

	public void setFile(String key, File f) {
		String path = "";
		if(f != null)
			path = f.getPath();
		setProperty(key, path);
	}

	public File getFile(String key) {
		String path = getProperty(key);
		if(path == null || path.isEmpty())
			return(null);
		return(new File(path));
	}

	public void setInt(String key, int n) {
		setProperty(key, Integer.toString(n));
	}

	public int getInt(String key, int defaultVal) {
		String n = getProperty(key);
		if(n == null || n.isEmpty())
			return(defaultVal);
		return(Integer.parseInt(n));
	}

	//------------------------------------------------------------------------------
	// プロパティファイル参照
	//------------------------------------------------------------------------------

	private Properties prop;

	private String getProperty(String key) {
		return(this.prop.getProperty(key));
	}

	private void setProperty(String key, String val) {
		this.prop.setProperty(key, val);
	}

	private void loadProperties() {
		InputStream in = null;
		try {
			in = new FileInputStream("app.properties");
			this.prop.load(in);
		}
		catch(Exception ex) {
		}
		finally {
			try { in.close(); } catch(Exception ex) { }
		}
	}

	private void saveProperties() {
		OutputStream out = null;
		try {
			out = new FileOutputStream("app.properties");
			this.prop.store(out, "Application preference");
		}
		catch(Exception ex) {
		}
		finally {
			try { out.close(); } catch(Exception ex) { }
		}
	}
}
