import java.io.*;
import java.sql.*;
import java.util.Map;

/**
 * iConomy v1.x
 * Copyright (C) 2010  Nijikokun <nijikokun@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * iData.java
 * <br /><br />
 * internal Data class, controls data coming in and out.
 *
 * @author Nijikokun <nijikokun@gmail.com>
 */
public final class iData implements Serializable {
    /*
     * internal Properties file manager for accounts.
     */
    public static iProperty accounts;

    /*
     * Initial balance of players
     */
    private static int initialBalance;

    /*
     * Serialization of files
     */
    private static final long serialVersionUID = -5796481236376288855L;

    /*
     * Database default values, changed via settings file.
     */
    static boolean mysql = false;
    static String driver = "com.mysql.jdbc.Driver";
    static String user = "root";
    static String pass = "root";
    static String db = "jdbc:mysql://localhost:3306/minecraft";

    public static void setup(boolean mysql, int balance, String driver, String user, String pass, String db) {
	initialBalance = balance;

	// Database
	iData.driver = driver;
	iData.user = user;
	iData.pass = pass;
	iData.db = db;

	if (!mysql) {
	    accounts = new iProperty(iConomy.main_directory + "balances.properties");
	} else {
	    // MySQL
	    iData.mysql = true;

	    try {
		Class.forName(driver);
	    } catch (ClassNotFoundException ex) {
		iConomy.log.severe(Messaging.bracketize(iConomy.name + " MySQL") + " Unable to find driver class: " + driver);
	    }
	}
    }

    public static Connection MySQL() {
	try {
	    return DriverManager.getConnection(db, user, pass);
	} catch (SQLException ex) {
	    iConomy.log.severe(Messaging.bracketize(iConomy.name + " MySQL") + " Unable to retreive MySQL connection");
	}

	return null;
    }

    public static int globalBalance() {
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	int current = 0;

	if (mysql) {
	    try {
		conn = MySQL();
		ps = conn.prepareStatement("SELECT balance FROM iBalances");
		rs = ps.executeQuery();

		while (rs.next()) {
		    current += rs.getInt("balance");
		}

		return current;
	    } catch (SQLException ex) {
		return 0;
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
	    Map balances;

	    try {
		balances = accounts.returnMap();
	    } catch (Exception ex) {
		iConomy.log.severe(Messaging.bracketize(iConomy.name + " FlatFile") + " Listing failed for accounts.");
		return 0;
	    }

	    for (Object key : balances.keySet()) {
		int balance = Integer.parseInt((String) balances.get(key));
		current += balance;
	    }

	    return current;
	}
    }

    public static boolean hasBalance(String player) {
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	boolean has = false;

	if (mysql) {
	    try {
		conn = MySQL();
		ps = conn.prepareStatement("SELECT balance FROM iBalances WHERE player = ? LIMIT 1");
		ps.setString(1, player);
		rs = ps.executeQuery();

		has = (rs.next()) ? true : false;
	    } catch (SQLException ex) {
		iConomy.log.severe(Messaging.bracketize(iConomy.name + " MySQL") + " Unable to grab the balance for [" + player + "] from database!");
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
	    return (accounts.getInt(player) != 0) ? true : false;
	}

	return has;
    }

    public static void removeBalance(String playerName) {
	if (!hasBalance(playerName)) {
	    return;
	}

	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;

	if (mysql) {
	    try {
		conn = MySQL();
		ps = conn.prepareStatement("DELETE FROM iBalances WHERE player = ? LIMIT 1");
		ps.setString(1, playerName);
		ps.executeUpdate();
	    } catch (SQLException ex) {
		iConomy.log.severe(Messaging.bracketize(iConomy.name + " MySQL") + " Unable to grab the balance for [" + playerName + "] from database!");
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
	    accounts.removeKey(playerName);
	}
    }

    public static int getBalance(String player) {
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	int balance = initialBalance;

	if (mysql) {
	    try {
		conn = MySQL();
		ps = conn.prepareStatement("SELECT balance FROM iBalances WHERE player = ? LIMIT 1");
		ps.setString(1, player);
		rs = ps.executeQuery();

		if (rs.next()) {
		    balance = rs.getInt("balance");
		} else {
		    ps = conn.prepareStatement("INSERT INTO iBalances (player, balance) VALUES(?,?)");
		    ps.setString(1, player);
		    ps.setInt(2, balance);
		    ps.executeUpdate();
		}
	    } catch (SQLException ex) {
		iConomy.log.severe(Messaging.bracketize(iConomy.name + " MySQL") + " Unable to grab the balance for [" + player + "] from database!");
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
	    // To work with plugins we must do this.
	    accounts.load();

	    // Return the balance
	    return (hasBalance(player)) ? accounts.getInt(player) : accounts.getInt(player, initialBalance);
	}

	return balance;
    }

    public static void setBalance(String player, int balance) {
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;

	if (mysql) {
	    try {
		conn = MySQL();

		if (hasBalance(player)) {
		    ps = conn.prepareStatement("UPDATE iBalances SET balance = ? WHERE player = ? LIMIT 1");
		    ps.setInt(1, balance);
		    ps.setString(2, player);
		    ps.executeUpdate();
		} else {
		    ps = conn.prepareStatement("INSERT INTO iBalances (player, balance) VALUES(?,?)");
		    ps.setString(1, player);
		    ps.setInt(2, balance);
		    ps.executeUpdate();
		}
	    } catch (SQLException ex) {
		iConomy.log.severe(Messaging.bracketize(iConomy.name + " MySQL") + " Unable to update or create the balance for [" + player + "] from database!");
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
	    accounts.setInt(player, balance);
	}
    }
}
