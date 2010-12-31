import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 * MySQL.java
 * <br /><br />
 * Controls MySQL Queries & Connections for the plugin, not data.
 *
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class MySQL {

    public MySQL() {
    }

    public static void Rank(String checking, Player viewing, boolean mine) {
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	int i = 1;

	try {
	    conn = iData.MySQL();
	    ps = conn.prepareStatement("SELECT player,balance FROM iBalances ORDER BY balance DESC");
	    rs = ps.executeQuery();

	    while (rs.next()) {
		if (mine) {
		    if (rs.getString("player").equalsIgnoreCase(checking)) {
			Messaging.send(
				viewing,
				iConomy.MoneyTPL.color("tag") +
				iConomy.MoneyTPL.parse(
				"personal-rank",
				new String[]{"+name,+n", "+rank,+r"},
				new String[]{checking, Misc.string(i)}));

			break;
		    }
		} else {
		    if (rs.getString("player").equalsIgnoreCase(checking)) {
			Messaging.send(
				viewing,
				iConomy.MoneyTPL.color("tag") +
				iConomy.MoneyTPL.parse(
				"player-rank",
				new String[]{"+name,+n", "+rank,+r"},
				new String[]{checking, Misc.string(i)}));

			break;
		    }
		}

		i++;
	    }
	} catch (SQLException ex) {
	    iConomy.log.severe(Messaging.bracketize(iConomy.name + " MySQL") + " Unable to grab the ranking from database!");
	} finally {
	    try {
		if (ps != null) {
		    ps.close();
		}

		if (conn != null) {
		    conn.close();
		}
	    } catch (SQLException ex) {
	    }
	}

	return;
    }

    public static void Top(int amount) {
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	int i = 1;

	try {
	    conn = iData.MySQL();
	    ps = conn.prepareStatement("SELECT player,balance FROM iBalances ORDER BY balance DESC LIMIT 0,?");
	    ps.setInt(1, amount);
	    rs = ps.executeQuery();

	    Messaging.send(
		iConomy.MoneyTPL.parse(
		    "top-opening",
		    new String[]{"+amount,+a"},
		    new String[]{Misc.string(amount)}
		)
	    );

	    if (rs != null) {
		while (rs.next()) {
		    Messaging.send(
			iConomy.MoneyTPL.parse(
			    "top-line",
			    new String[]{ "+i,+number", "+name,+n", "+balance,+b" },
			    new String[]{ Misc.string(i), rs.getString("player"), iConomy.Misc.formatCurrency(rs.getInt("balance"), iConomy.currency) }
			)
		    );

		    i++;
		}
	    } else {
		Messaging.send(iConomy.MoneyTPL.color("top-empty"));
	    }
	} catch (SQLException ex) {
	    iConomy.log.severe(Messaging.bracketize(iConomy.name + " MySQL") + " Unable to grab the top players from the database!");
	} finally {
	    try {
		if (ps != null) {
		    ps.close();
		}

		if (conn != null) {
		    conn.close();
		}
	    } catch (SQLException ex) {
	    }
	}

	return;
    }
}
