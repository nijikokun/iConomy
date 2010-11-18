import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 *
 * @author Nijiko
 */
public class iMoney {
	static final Logger log = Logger.getLogger("Minecraft");
	private iConomy p = iConomy.getInstance();
	private iMisc m = new iMisc();
	private iLog l = new iLog();

	// Money Name
	public String name = p.moneyName;

	public void deposit(String pdata1, String pdata2, int amount, boolean really) {
		// Check
		Player player1 = m.getPlayer(pdata1);
		Player player2 = m.getPlayer(pdata2);

		// Balance
		int i = p.data.getBalance(pdata2);

		i += amount;
		p.data.setBalance(pdata2, i);

		if (really && player2 != null) {
			player2.sendMessage(Colors.Green + "You received " + amount + this.name);
			showBalance(pdata2, null, true);

			if (player1 != null) {
				player1.sendMessage(Colors.Green + amount + this.name + " deposited into " + pdata2 + "'s account");
			}

		}

		if(p.debugging)
			log.info("[iConomy Debugging] [" + player1 + "] [" + player2 + "] ["+amount+"] [" + really + "] [#20341]");
	}

	public void debit(String pdata1, String pdata2, int amount, boolean really) {
		// Check
		Player player1 = m.getPlayer(pdata1);
		Player player2 = m.getPlayer(pdata2);

		// Balance
		int i = p.data.getBalance(pdata2);

		if (amount > i) {
			amount = i;
		}

		i -= amount;
		p.data.setBalance(pdata2, i);

		if (really && player2 != null) {
			player2.sendMessage(Colors.Green + amount + this.name + " was deducted from your account.");
			showBalance(pdata2, null, true);

			if (player1 != null) {
				player1.sendMessage(Colors.Green + amount + this.name + " removed from " + pdata2 + "'s account");
			}

		}

		if(p.debugging)
			log.info("[iConomy Debugging] [" + player1 + "] [" + player2 + "] ["+amount+"] [" + really + "] [#20342]");
	}

	public void reset(String pdata, Player local, boolean notify) {
		// Check
		Player player = m.getPlayer(pdata);

		// Reset
		p.data.setBalance(pdata, p.startingBalance);

		// Notify
		if (notify) {
			if (player != null) {
				player.sendMessage(Colors.Green + "Your account has been reset.");
			}
		}

		// Notify the resetter and server regardless.
		local.sendMessage(Colors.Rose + pdata + "'s account has been reset.");
		log.info("[iConomy Money] " + pdata + "'s account has been reset by " + local.getName());

		if(p.debugging)
			log.info("[iConomy Debugging] [" + player + "] [" + local + "] [" + notify + "] [#20343]");
	}

	public void pay(String pdata1, String pdata2, int amount) {
		// Check
		Player player1 = m.getPlayer(pdata1);
		Player player2 = m.getPlayer(pdata2);

		// Balances
		int i = p.data.getBalance(pdata1);
		int j = p.data.getBalance(pdata2);

		if (pdata1.equals(pdata2)) {
			if(player1 != null)
				player1.sendMessage(Colors.Rose + "You cannot send yourself money");

			if(p.debugging)
				log.info("[iConomy Debugging] [" + pdata1 + "] [" + pdata2 + "] [" + amount + "] [#20344]");
		} else if (amount > i) {
			if(player1 != null)
				player1.sendMessage(Colors.Rose + "You do not have enough money.");

			if(p.debugging)
				log.info("[iConomy Debugging] [" + pdata1 + "] [" + pdata2 + "] [" + amount + "] [#20345]");
		} else {
			// Update player one balance
			i -= amount;
			p.data.setBalance(pdata1, i);

			// Update player two balance
			j += amount;
			p.data.setBalance(pdata2, j);

			// Send messages
			if(player1 != null)
				player1.sendMessage(Colors.LightGray + "You have sent " + Colors.Green + amount + this.name + Colors.LightGray + " to " + Colors.Green + pdata2);

			if(player2 != null)
				player2.sendMessage(Colors.Green + pdata1 + Colors.LightGray + " has sent you " + Colors.Green + amount + this.name);

			// Log
			l.Log("pay", pdata1 + "|"+pdata2+"|1|200|" + amount);

			if(p.debugging)
				log.info("[iConomy Debugging] [" + pdata1 + "] [" + pdata2 + "] [" + amount + "] [#20346]");

			// Show each balance
			if(player1 != null)
				showBalance(pdata1, null, true);

			if(player2 != null)
				showBalance(pdata2, null, true);
		}
	}

	public void showBalance(String name, Player local, boolean isMe) {
		int i = p.data.getBalance(name);
		if (isMe) {
			Player player = m.getPlayer(name);
			player.sendMessage(Colors.LightGray + "Balance: " + Colors.Green + i + this.name);
		} else {
			local.sendMessage(Colors.LightGray + name + "'s Balance: " + Colors.Green + i + this.name);
		}
	}

	public void rank(String pdata1, String pdata2, boolean isMe) {
		Player player = m.getPlayer(pdata1);

		if (p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			int i = 1;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT player,balance FROM iBalances ORDER BY balance DESC");
				rs = ps.executeQuery();

				while (rs.next()) {
					if (isMe) {
						if (rs.getString("player").equalsIgnoreCase(pdata1)) {
							player.sendMessage(Colors.LightGray + "Your rank is " + Colors.Green + i); break;
						}
					} else {
						if (rs.getString("player").equalsIgnoreCase(pdata2)) {
							player.sendMessage(Colors.Green + rs.getString("player") + Colors.LightGray + " rank is " + Colors.Green + i); break;
						}
					}
					i++;
				}
			} catch (SQLException ex) {
				log.severe("[iConomy Money] Unable to grab the sqlrank for from database!");
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			Map accounts;
			TreeMap<String,Integer> sorted_accounts = null;
			iValue bvc = null;
			int i = 1;

			try {
				accounts = p.data.accounts.returnMap();
				bvc =  new iValue(accounts);
				sorted_accounts = new TreeMap(bvc);
				sorted_accounts.putAll(accounts);
			} catch (Exception ex) {
				log.severe("[iConomy Money] Unable to retrieve array of balances!");
			}

			for (Object key : sorted_accounts.keySet()) {
				String name = (String) key;

				if (isMe) {
					if (name.equalsIgnoreCase(pdata1)) {
						player.sendMessage(Colors.LightGray + "Your rank is " + Colors.Green + i); break;
					}
				} else {
					if (name.equalsIgnoreCase(pdata2)) {
						player.sendMessage(Colors.Green + name + Colors.LightGray + " rank is " + Colors.Green + i); break;
					}
				}

				i++;
			}
		}
	}

	public void top(Player player, int amount) {
		if (p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			int i = 1;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT player,balance FROM iBalances ORDER BY balance DESC LIMIT 0,?");
				ps.setInt(1, amount);
				rs = ps.executeQuery();
				player.sendMessage(Colors.LightGray + "Top " + Colors.Green + amount + Colors.LightGray + " Richest People:");

				while (rs.next()) {
					player.sendMessage(Colors.LightGray + "   " + i + ". " + Colors.Green + rs.getString("player") + Colors.LightGray + " - " + Colors.Green + rs.getInt("balance") + this.name);
					i++;
				}
			} catch (SQLException ex) {
				log.severe("[iConomy] Unable to grab the sqltop from database!");
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}

		} else {
			Map accounts;
			TreeMap<String,Integer> sorted_accounts = null;
			iValue bvc = null;
			int i = 1;

			try {
				accounts = p.data.accounts.returnMap();
				bvc =  new iValue(accounts);
				sorted_accounts = new TreeMap(bvc);
				sorted_accounts.putAll(accounts);
			} catch (Exception ex) {
				log.severe("[iConomy Money] Unable to retrieve array of balances!");
			}

			player.sendMessage(Colors.LightGray + "Top " + Colors.Green + amount + Colors.LightGray + " Richest People:");

			if (sorted_accounts.size() < 1) {
				player.sendMessage(Colors.LightGray + "   Nobody Yet!"); return;
			}

			if (amount > sorted_accounts.size()) {
				amount = sorted_accounts.size();
			}

			for (Object key : sorted_accounts.keySet()) {
				String name = (String) key;
				String balance = "" + sorted_accounts.get(name);

				if(i <= amount) {
					// Send top players
					player.sendMessage(Colors.LightGray + "   " + i + ". " + Colors.Green + name + Colors.LightGray + " - " + Colors.Green + balance + this.name);
				} else {
					break;
				}

				i++;
			}
		}
	}
}
