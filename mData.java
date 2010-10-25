import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public final class mData implements Serializable {
	protected static final Logger log = Logger.getLogger("Minecraft");
	private PropertiesFile accounts;
	private Hashtable balances;
	private int startingBalance;
	private static final long serialVersionUID = -5796481236376288855L;

	public mData(String file, int balance) {
		this.startingBalance = balance;

		// Check the properties file
		File pFile = new File("iConomy-balances.properties");
		if (!pFile.exists()) {
			// State we are starting.
			log.severe("[iConomy] Older version found, Starting conversion of .data to .properties file!");

			// Convert to our latest type.
			if(this.convert(file)){
			    log.severe("[iConomy] Conversion to .properties file complete!");
			} else {
			    log.severe("[iConomy] Conversion could not be completed. Please post any errors to Nijikokun if applicable.");
			}
		} else {
			this.accounts = new PropertiesFile("iConomy-balances.properties");
		}
	}

	public boolean convert(String filename) {
		mData localmData = null;

		// Initiate file
		this.accounts = new PropertiesFile("iConomy-balances.properties");

		try {
			FileInputStream localFileInputStream = new FileInputStream(filename);
			ObjectInputStream localObjectInputStream = new ObjectInputStream(localFileInputStream);
			localmData = (mData)localObjectInputStream.readObject();
			localObjectInputStream.close();
			localFileInputStream.close();
		} catch (Exception localException) {
			// Nothing to convert
			log.severe(localException.getMessage());
			return false;
		}

		if (localmData == null){
			log.severe("[iConomy] No .data file found.");
			return false;
		}

		this.balances = localmData.balances;

		Enumeration e = this.balances.keys();

		while (e.hasMoreElements()) {
			String key = e.nextElement().toString();
			int value = ((Integer)this.balances.get(key)).intValue();

			// Push the new values
			this.accounts.setInt(key, value);
		}

		return true;
	}

	public int getBalance(String playerName) {
		return this.accounts.getInt(playerName, this.startingBalance);
	}

	public void setBalance(String playerName, int balance) {
		this.accounts.setInt(playerName, balance);
	}
}