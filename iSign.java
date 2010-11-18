import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

/**
 * iSign
 *	Controls sign shops access
 *
 * @date 11/17/2010 9:08PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public class iSign {
	static final Logger log = Logger.getLogger("Minecraft");
	private iConomy p = iConomy.getInstance();
	private iMisc m = new iMisc();
	private iMoney mo = new iMoney();

	public void updateClick(String name) {
		p.lastClick.put(name, System.currentTimeMillis() / 1000L);
	}

	public boolean waitedEnough(String name) {
		if (p.lastClick.containsKey(name)) {
			long previous = p.lastClick.get(name);
			if ((System.currentTimeMillis() / 1000L) - previous > p.signWaitAmount) {
				return true;
			}
		} else {
			return true;
		}
		return false;
	}

	public void upgradeOwn(String name) {
		Player player = m.getPlayer(name);

		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT usable FROM iSignOwners WHERE owner=? LIMIT 1");
				ps.setString(1, name);
				rs = ps.executeQuery();

				if(rs.next()) {
					int currentAmount = rs.getInt("usable");
					int nextAmount = (currentAmount/p.signOwnAmount)*p.signOwnUpgrade;

					if(p.data.getBalance(name) < nextAmount){
						if(player != null)
							player.sendMessage(String.format(p.signUpgradeAmount, nextAmount + p.moneyName));

					} else {
						mo.debit(null, name, nextAmount, true);
						conn = p.data.MySQL();

						ps = conn.prepareStatement("UPDATE iSignOwners SET usable=usable+? WHERE owner=? LIMIT 1");
						ps.setInt(1, p.signOwnAmount);
						rs = ps.executeQuery();
					}
				} else {
					if(player != null)
						player.sendMessage(p.signUpgradeExists);
				}
			} catch (Exception ex) {
				log.severe("[iConomy] Upgrading failed!" + ex);
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			int currentAmount = p.signOwners.getInt(name);

			if(currentAmount == 0) {
				if(player != null)
					player.sendMessage(p.signUpgradeExists);
			}

			int nextAmount = (currentAmount/p.signOwnAmount)*p.signOwnUpgrade;

			if(p.data.getBalance(name) < nextAmount){
				if(player != null)
					player.sendMessage(String.format(p.signUpgradeAmount, nextAmount + p.moneyName));

			} else {
				mo.debit(null, name, nextAmount, true);
				p.signOwners.setInt(name, currentAmount+p.signOwnAmount);
			}
		}
	}

	public int canOwn(String name) {
		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT usable FROM iSignOwners WHERE owner=? LIMIT 1");
				ps.setString(1, name);
				rs = ps.executeQuery();

				return (rs.next()) ? rs.getInt("usable") : p.signOwnAmount;
			} catch (Exception ex) {
				log.severe("[iConomy] Listing failed for buying list" + ex);
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			return (p.signOwners.getInt(name) != 0) ? p.signOwners.getInt(name) : p.signOwnAmount;
		}

		return 0;
	}

	public void signStock(Player player) {
		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT item,amount FROM iSign WHERE owner=? ORDER BY item ASC");
				ps.setString(1, player.getName());
				rs = ps.executeQuery();

				player.sendMessage(Colors.White + "Shop stock ["+ Colors.Green + "item"+ Colors.White + " - "+ Colors.Green + "amount"+ Colors.White + "]");

				while (rs.next()) {
					player.sendMessage(m.itemName(rs.getString("item")) + " - " + rs.getString("amount"));
				}

			} catch (Exception ex) {
				log.severe("[iConomy] Listing failed for buying list" + ex);
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			Map stockList = null;

			try {
				stockList = p.sign.returnMap();
			} catch (Exception ex) {
				log.info("[iConomy] Failed to create stock list" + ex);
			}

			for (Object key: stockList.keySet()) {
				String shop = (String) key;
				int stock = Integer.parseInt((String) stockList.get(key));
				String[] data = shop.split("-");
				String name = data[0];
				String item = data[1];

				if(name.equalsIgnoreCase(player.getName())){
					player.sendMessage(m.itemName(item) + " - " + stock);
				}
			}
		}
	}

	public int signCurStock(String name, int i) {
		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT amount FROM iSign WHERE owner=? AND item=? LIMIT 1");
				ps.setString(1, name);
				ps.setInt(2, i);
				rs = ps.executeQuery();


				if(rs.next()) {
					return rs.getInt("amount");
				} else {
					return 0;
				}

			} catch (Exception ex) {
				log.severe("[iConomy] Listing failed for buying list" + ex);
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			return p.sign.getInt(name+"-"+i);
		}

		return 0;
	}

	public int ownSign(String name) {
		int i = 0;

		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT owner FROM iSign WHERE owner=?");
				ps.setString(1, name);
				rs = ps.executeQuery();

				while(rs.next()) {
					if(rs.getString("owner").equalsIgnoreCase(name)) {
						i++;
					}
				}
			} catch (SQLException ex) {
				log.info("[iConomy] Failed to grab sign data: " + ex);
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			Map signShopsList = null;

			try {
				signShopsList = p.sign.returnMap();
			} catch (Exception ex) {
				log.info("[iConomy] Failed to create list");
			}

			for (Object key: signShopsList.keySet()) {
				String shop = (String) key;
				String[] data = shop.split("-");
				String owner = data[0];

				if(owner.equalsIgnoreCase(name)){
					i++;
				}
			}
		}

		return i;
	}

	public void deleteSign(String name, int i) {
		if(!this.existsSign(name,i))
			return;

		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("DELETE FROM iSign WHERE owner = ? AND item=? LIMIT 1");
				ps.setString(1, name); ps.setInt(2, i); ps.executeUpdate();

				ps = conn.prepareStatement("DELETE FROM iSignLocation WHERE owner = ? AND item=?");
				ps.setString(1, name); ps.setInt(2, i); ps.executeUpdate();
			} catch (SQLException ex) {
				log.info("[iConomy] Failed to delete Sign Shop: " + ex);
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			p.sign.removeKey(name+"-"+i);
			p.signLocation.removeKey(name+"-"+i);
		}
	}

	public void updateSign(String name, int i, int s) {
		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("UPDATE iSign SET amount=? WHERE owner=? AND item=? LIMIT 1");
				ps.setInt(1, s);
				ps.setString(2, name);
				ps.setInt(3, i);
				ps.executeUpdate();
			} catch (SQLException ex) {
				log.info("[iConomy] Failed to update Sign Shop: " + ex);
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {

			p.sign.setInt(name+"-"+i, s);
		}
	}

	public Boolean existsSign(String name, int i) {
		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT id FROM iSign WHERE owner=? AND item=? LIMIT 1");
				ps.setString(1, name);
				ps.setInt(2, i);
				rs = ps.executeQuery();

				return (rs.next()) ? true : false;
			} catch (SQLException ex) {
				log.info("[iConomy] Failed to check Sign Shop: " + ex);
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			return (p.sign.getString(name+"-"+i).isEmpty()) ? false : true;
		}

		return false;
	}

	public void setSign(String name, int x, int y, int z, int i, int s) {
		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();

				if(!this.existsSign(name, i)) {
					ps = conn.prepareStatement("INSERT IGNORE INTO iSign (owner,item,amount) VALUES(?,?,?)");
					ps.setString(1, name);
					ps.setInt(2, i);
					ps.setInt(3, s);
					ps.executeUpdate();
				}

				if(p.signOwnUse)
					ps = conn.prepareStatement("INSERT IGNORE INTO iSignOwners(owner,usable) VALUES(?,?)");
					ps.setString(1, name);
					ps.setInt(2, p.signOwnAmount);
					ps.executeUpdate();

				ps = conn.prepareStatement("INSERT INTO iSignLocation (owner,item,x,y,z) VALUES(?,?,?,?,?)");
				ps.setString(1, name);
				ps.setInt(2, i);
				ps.setInt(3, x);
				ps.setInt(4, y);
				ps.setInt(5, z);
				ps.executeUpdate();
			} catch (SQLException ex) {
				log.info("[iConomy] Failed to create Sign Shop: " + ex);
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			if(p.sign.getInt(name+"-"+i) == 0 || p.sign.getInt(name+"-"+i) == s)
				p.sign.setInt(name+"-"+i, s);

			if(p.signOwners.getString(name) == null ? "" == null : p.signOwners.getString(name).equals("") && p.signOwnUse)
				p.signOwners.setInt(name, p.signOwnAmount);

			p.signLocation.setString(name+"-"+i, p.signLocation.getString(name+"-"+i) + x +";"+y+";"+z+";|");
		}
	}

	public void signPush(Player player, String owner, int i, int amount, boolean isOwner, int price, Block blockClicked) {
		if (!m.hasItems(player, i, amount)) {
			player.sendMessage("You dont have enough" + m.itemName(m.cInt(i)));
			return;
		}

		int stock = signCurStock(owner, i);

		if (amount + stock > p.signMaxAmount) {
			if (isOwner) {
				player.sendMessage("Sorry dude, your stock is full.");
			} else {
				player.sendMessage("Sorry, " + owner + "s stock is currently full.");
			}
			return;
		} else {
			if(this.existsSign(owner, i)) {
				this.updateSign(owner, i, amount+stock);
			} else {
				this.setSign(owner, blockClicked.getX(), blockClicked.getY(), blockClicked.getZ(), i, amount+stock);
			}

			this.updateSigns(owner, i, (amount + stock));

			if (isOwner) {
				m.removeItems(player, i, amount);
				player.sendMessage(String.format(p.signStocked, amount, m.itemName(m.cInt(i))));
			} else {
				m.removeItems(player, i, amount);
				mo.debit(null, owner, price, true);
				mo.deposit(null, player.getName(), price, true);
				player.sendMessage(String.format(p.signSold, amount, m.itemName(m.cInt(i)), price+p.moneyName));
			}
		}

	}

	public void signPull(Player player, String owner, int i, int amount, boolean isOwner, int price) {
		int stock = signCurStock(owner, i);
		if(stock != 0) {
			if (amount <= stock) {
				this.updateSign(owner, i, (stock-amount));

				if (isOwner) {
					player.sendMessage(String.format(p.signLeftAmount, (stock-amount), m.itemName(m.cInt(i))));
				}

				player.giveItem(i, amount);

				this.updateSigns(owner, i, (stock-amount));

				if (!isOwner) {
					mo.deposit(null, owner, price, true);
					mo.debit(null, player.getName(), price, true);

					player.sendMessage(String.format(p.signBoughtAmount, amount, m.itemName(m.cInt(i)), price+p.moneyName));
				}

			} else {
				player.sendMessage(String.format(p.signNotEnough, owner, m.itemName(m.cInt(i))));
			}

		} else {
			player.sendMessage(owner + " does not have that item in stock");
		}
	}

	public void updateSigns(String name, int i, int stock) {
		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT * FROM iSignLocation WHERE owner=? AND item=?");
				ps.setString(1, name);
				ps.setInt(2, i);
				rs = ps.executeQuery();

				while(rs.next()) {
					if(p.debugging)
						log.info("[iConomy Debugging] "+ rs.getInt("x") + " - "+ rs.getInt("y") + " - "+ rs.getInt("z"));

					int x = rs.getInt("x");
					int y = rs.getInt("y");
					int z = rs.getInt("z");

					if(this.updateSignLocation(x, y, z, 0, 1, 0, i, stock))
						continue;

					if(this.updateSignLocation(x, y, z, 0, -1, 0, i, stock))
						continue;

					if(this.updateSignLocation(x, y, z, 1, 0, 0, i, stock))
						continue;

					if(this.updateSignLocation(x, y, z, -1, 0, 0, i, stock))
						continue;

					if(this.updateSignLocation(x, y, z, 0, 0, 1, i, stock))
						continue;

					if(this.updateSignLocation(x, y, z, 0, 0, -1, i, stock))
						continue;
				}
			} catch (SQLException ex) {
				log.info("[iConomy] Failed to grab sign data: " + ex);
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			Map signShopsList = null;

			try {
				signShopsList = p.sign.returnMap();
			} catch (Exception ex) {
				log.info("[iConomy] Failed to create list");
			}

			for (Object key: signShopsList.keySet()) {
				String shop = (String) key;
				String[] data = shop.split("-");
				String owner = data[0];
				int item = Integer.parseInt(data[1]);

				if(owner.equalsIgnoreCase(name) && item == i){
					String sLocations = p.signLocation.getString(owner+"-"+item);
					String[] shopData = sLocations.split("\\|");

					if(p.debugging)
						log.info("[iConomy Debugging] " + Arrays.toString(shopData));

					for (String row : shopData) {
						if(row.contains(";") && !row.equals(";")) {
							String[] shopCData = row.split(";");

							if(p.debugging)
								log.info("[iConomy Debugging] " + row);

							if(shopCData[0] == null ? "" == null : shopCData[0].equals("") || shopCData[0].equals("|"))
								continue;

							int x = Integer.parseInt(shopCData[0]);
							int y = Integer.parseInt(shopCData[1]);
							int z = Integer.parseInt(shopCData[2]);

							if(p.debugging)
								log.info(x + " - " + y + " - " + z);

							if(this.updateSignLocation(x, y, z, 0, 1, 0, i, stock))
								continue;

							if(this.updateSignLocation(x, y, z, 0, -1, 0, i, stock))
								continue;

							if(this.updateSignLocation(x, y, z, 1, 0, 0, i, stock))
								continue;

							if(this.updateSignLocation(x, y, z, -1, 0, 0, i, stock))
								continue;

							if(this.updateSignLocation(x, y, z, 0, 0, 1, i, stock))
								continue;

							if(this.updateSignLocation(x, y, z, 0, 0, -1, i, stock))
								continue;
						}
					}
				}
			}
		}
	}

	public boolean updateSignLocation(int x, int y, int z, int a, int b, int c, int i, int stock){
		Sign theSign;
		ComplexBlock theblock = etc.getServer().getComplexBlock((x+a), (y+b), (z+c));

		if (!(theblock instanceof Sign)) {
			if(p.debugging)
				log.info("No sign");

			return false;
		} else {
			theSign = (Sign) theblock;

			if ((theSign.getText(1).equalsIgnoreCase("sell") || theSign.getText(1).equalsIgnoreCase("buy"))) {
				if(p.debugging)
					log.info("sign equaled sell or buy");

				return false;
			}

			theSign.setText(1, m.itemName(m.cInt(i)));
			theSign.setText(3, "In stock: " + stock);
			theSign.update();

			if(p.debugging)
				log.info("I updated at "+ (x+a) + "-" + (y+b) + "-" + (z+c));

			return true;
		}
	}
}
