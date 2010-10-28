import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.logging.Logger;

public final class iData implements Serializable {
    protected static final Logger log = Logger.getLogger("Minecraft");
    private iProperty accounts;
    private int startingBalance;

    // Serial
    private static final long serialVersionUID = -5796481236376288855L;

    // Database
    static boolean mysql = false;
    static String driver = "com.mysql.jdbc.Driver";
    static String user = "root";
    static String pass = "root";
    static String db = "jdbc:mysql://localhost:3306/minecraft";

    public iData(boolean mysql, int balance, String driver, String user, String pass, String db) {
	this.startingBalance = balance;

	// Database
	iData.driver = driver;
	iData.user = user;
	iData.pass = pass;
	iData.db = db;

	if (!mysql) {
	    this.accounts = new iProperty("iConomy/balances.properties");
	} else {
	    // MySQL
	    iData.mysql = true;

	    try {
		Class.forName(driver);
	    } catch (ClassNotFoundException ex) {
		log.severe("[iConomy MySQL] Unable to find driver class " + driver);
	    }
	}
    }

    public Connection MySQL() {
	try {
	    return DriverManager.getConnection(db + "?autoReconnect=true&user=" + user + "&password=" + pass);
	} catch (SQLException ex) {
	    log.severe("[iConomy MySQL] Unable to retreive MySQL connection");
	}

	return null;
    }

    public boolean hasBalance(String playerName) {
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	boolean has = false;

	if (mysql) {
	    try {
		conn = MySQL();
		ps = conn.prepareStatement("SELECT balance FROM iBalances WHERE player = ? LIMIT 1");
		ps.setString(1, playerName);
		rs = ps.executeQuery();

		if (rs.next()) {
		    has = true;
		} else {
		    has = false;
		}
	    } catch (SQLException ex) {
		log.severe("[iConomy] Unable to grab the balance for [" + playerName + "] from database!");
	    } finally {
		try {
		    if (ps != null) {
			ps.close();
		    }
		    if (rs != null) {
			rs.close();
		    }
		    if (conn != null) {
			conn.close();
		    }
		} catch (SQLException ex) {
		}
	    }
	} else {
	    return (this.accounts.getInt(playerName) != 0) ? true : false;
	}

	return has;
    }

    public int getBalance(String playerName) {
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	int balance = this.startingBalance;

	if (mysql) {
	    try {
		conn = MySQL();
		ps = conn.prepareStatement("SELECT balance FROM iBalances WHERE player = ? LIMIT 1");
		ps.setString(1, playerName);
		rs = ps.executeQuery();

		if (rs.next()) {
		    balance = rs.getInt("balance");
		} else {
		    ps = conn.prepareStatement("INSERT INTO iBalances (player, balance) VALUES(?,?)");
		    ps.setString(1, playerName);
		    ps.setInt(2, balance);
		    ps.executeUpdate();
		}
	    } catch (SQLException ex) {
		log.severe("[iConomy] Unable to grab the balance for [" + playerName + "] from database!");
	    } finally {
		try {
		    if (ps != null) {
			ps.close();
		    }
		    if (rs != null) {
			rs.close();
		    }
		    if (conn != null) {
			conn.close();
		    }
		} catch (SQLException ex) {
		}
	    }
	} else {
	    return this.accounts.getInt(playerName, this.startingBalance);
	}

	return balance;
    }

    public void setBalance(String playerName, int balance) {
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;

	if (mysql) {
	    try {
		conn = MySQL();

		if (hasBalance(playerName)) {
		    ps = conn.prepareStatement("UPDATE iBalances SET balance = ? WHERE player = ? LIMIT 1", Statement.RETURN_GENERATED_KEYS);
		    ps.setInt(1, balance);
		    ps.setString(2, playerName);
		    ps.executeUpdate();
		} else {
		    ps = conn.prepareStatement("INSERT INTO iBalances (player, balance) VALUES(?,?)");
		    ps.setString(1, playerName);
		    ps.setInt(2, balance);
		    ps.executeUpdate();
		}
	    } catch (SQLException ex) {
		log.severe("[iConomy] Unable to update or create the balance for [" + playerName + "] from database!");
	    } finally {
		try {
		    if (ps != null) {
			ps.close();
		    }
		    if (rs != null) {
			rs.close();
		    }
		    if (conn != null) {
			conn.close();
		    }
		} catch (SQLException ex) {
		}
	    }
	} else {
	    this.accounts.setInt(playerName, balance);
	}
    }
}
