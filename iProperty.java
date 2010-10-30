import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nijiko
 */
public final class iProperty {
	private static final Logger log = Logger.getLogger("Minecraft");
	private Properties properties;
	private String fileName;

	public iProperty(String fileName) {
		this.fileName = fileName;
		this.properties = new Properties();
		File file = new File(fileName);

		if (file.exists()){
			load();
		} else {
			save();
		}
	}

	public void load() {
		try {
			this.properties.load(new FileInputStream(this.fileName));
		} catch (IOException ex) {
			log.log(Level.SEVERE, "Unable to load " + this.fileName, ex);
		}
	}

	public void save() {
		try {
			this.properties.store(new FileOutputStream(this.fileName), "Minecraft Properties File");
		} catch (IOException ex) {
			log.log(Level.SEVERE, "Unable to save " + this.fileName, ex);
		}
	}

	public void removeKey(String key) {
		this.properties.remove(key);
	}

	public boolean keyExists(String key) {
		return this.properties.containsKey(key);
	}

	public String getString(String key) {
		if (this.properties.containsKey(key)) {
			return this.properties.getProperty(key);
		}

		return "";
	}

	public String getString(String key, String value) {
		if (this.properties.containsKey(key)) {
			return this.properties.getProperty(key);
		}
		setString(key, value);
		return value;
	}

	public void setString(String key, String value) {
		this.properties.setProperty(key, value);
		save();
	}

	public int getInt(String key) {
		if (this.properties.containsKey(key)) {
			return Integer.parseInt(this.properties.getProperty(key));
		}

		return 0;
	}

	public int getInt(String key, int value) {
		if (this.properties.containsKey(key)) {
			return Integer.parseInt(this.properties.getProperty(key));
		}

		setInt(key, value);
		return value;
	}

	public void setInt(String key, int value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}

	public long getLong(String key) {
		if (this.properties.containsKey(key)) {
			return Long.parseLong(this.properties.getProperty(key));
		}

		return 0;
	}

	public long getLong(String key, long value) {
		if (this.properties.containsKey(key)) {
			return Long.parseLong(this.properties.getProperty(key));
		}

		setLong(key, value);
		return value;
	}

	public void setLong(String key, long value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}

	public boolean getBoolean(String key) {
		if (this.properties.containsKey(key)) {
			return Boolean.parseBoolean(this.properties.getProperty(key));
		}

		return false;
	}

	public boolean getBoolean(String key, boolean value) {
		if (this.properties.containsKey(key)) {
			return Boolean.parseBoolean(this.properties.getProperty(key));
		}

		setBoolean(key, value);
		return value;
	}

	public void setBoolean(String key, boolean value) {
		this.properties.setProperty(key, String.valueOf(value));
		save();
	}
}