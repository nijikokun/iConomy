import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * iShop
 *	Controls global shop system
 *
 * @date 11/17/2010 9:08PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public class iShop {
	static final Logger log = Logger.getLogger("Minecraft");
	private iConomy p = iConomy.getInstance();
	private iLog l = new iLog();
	private iMisc m = new iMisc();
	private iMoney mo = new iMoney();

	public int itemNeedsAmount(String type, String itemId) {
		if (p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			int cost = 0;

			try {
				conn = p.data.MySQL();
				if(type.equals("buy")) {
					ps = conn.prepareStatement("SELECT cost,perbundle FROM iBuy WHERE id = ? LIMIT 1");
				} else {
					ps = conn.prepareStatement("SELECT cost,perbundle FROM iSell WHERE id = ? LIMIT 1");
				}
				ps.setInt(1, Integer.parseInt(itemId));
				rs = ps.executeQuery();

				if (rs.next()) {
					if (rs.getInt("perbundle") > 1) {
						return rs.getInt("perbundle");
					} else {
						if (rs.getInt("cost") == 0) {
							return 0;
						} else {
							return 1;
						}
					}
				} else {
					return 0;
				}
			} catch (SQLException ex) {
				log.severe("[iConomy] Unable to grab the item [" + itemId + "] " + type + " price from database!");
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}

			return cost;
		} else {
			String info = "";

			if (type.equals("buy")) {
				info = p.buying.getString((String) p.items.get(itemId));
			} else if (type.equals("sell")) {
				info = p.selling.getString((String) p.items.get(itemId));
			}

			if (info.equals("")) {
				return 0;
			}

			if (info.contains(",")) {
				String[] item = info.split(",");
				return Integer.parseInt(item[0]);
			} else {
				return 1;
			}
		}
	}

	public boolean itemCan(String type, String itemId, int amount) {
		int itemAmount = this.itemNeedsAmount(type, itemId);

		// Maximum
		if (amount > 6400) {
			return false;
		}

		if (itemAmount == 0) {
			return false;
		} else if (itemAmount > 1) {
			if ((amount * itemAmount) < 6400) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public int itemCost(String type, String itemId, int amount, boolean total) {
		String info = "";
		int perBundle = 0;
		int cost = 0;

		if (p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				if(type.equals("buy")) {
					ps = conn.prepareStatement("SELECT cost,perbundle FROM iBuy WHERE id = ? LIMIT 1");
				} else {
					ps = conn.prepareStatement("SELECT cost,perbundle FROM iSell WHERE id = ? LIMIT 1");
				}
				ps.setInt(1, Integer.parseInt(itemId));
				rs = ps.executeQuery();

				if (rs.next()) {
					if (!this.itemCan(type, itemId, amount)) {
						return 0;
					}

					// Settings
					cost = rs.getInt("cost");
					perBundle = rs.getInt("perbundle");

					if (total) {
						return cost*amount;
					} else {
						return cost;
					}
				} else {
					return 0;
				}
			} catch (SQLException ex) {
				log.severe("[iConomy] Unable to grab the item [" + itemId + "] " + type + " price from database!");
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}

			return cost;
		} else {
			if (type.equals("buy")) {
				info = p.buying.getString((String) p.items.get(itemId));
			} else if (type.equals("sell")) {
				info = p.selling.getString((String) p.items.get(itemId));
			}

			if (info.equals("")) {
				return 0;
			}

			if (info.contains(",")) {
				String[] item = info.split(",");
				perBundle = Integer.parseInt(item[0]);
				cost = Integer.parseInt(item[1]);

				// Check if we can
				if (!this.itemCan(type, itemId, amount)) {
					return 0;
				}

				if (total) {
					return cost*amount;
				} else {
					return cost;
				}
			} else {
				cost = Integer.parseInt(info);

				if (total) {
					return cost*amount;
				} else {
					return cost;
				}
			}
		}
	}

	/* Shop Listing Functions */
	public int listGetRows(String table) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int i = 0;

		try {
			conn = p.data.MySQL();

			if(table.equalsIgnoreCase("ibuy")){
				ps = conn.prepareStatement("SELECT * FROM iBuy");
			} else {
				ps = conn.prepareStatement("SELECT * FROM iSell");
			}

			rs = ps.executeQuery();

			while(rs.next()) { if(rs.getInt("cost") != 0) { i++; } }
		} catch (SQLException ex) {
			log.severe("[iConomy] Unable to count itemlist!");
		} finally {
			try {
				if (ps != null) { ps.close(); }
				if (rs != null) { rs.close(); }
				if (conn != null) { conn.close(); }
			} catch (SQLException ex) { }
		}

		return i;
	}

	public void showSellersList(Player player, int page) {
		List available = new ArrayList();
		Map aList;

		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			int i = 1;

			int amount = page;

			if (amount > 0){
				amount = (amount - 1) * 10;
			} else {
				amount = 0;
			}

			int process = amount+10;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT id,cost,perbundle FROM iSell ORDER BY cost ASC");
				rs = ps.executeQuery();

				int rowCount = this.listGetRows("iSell");

				if(rowCount == 0) {
					player.sendMessage(Colors.Rose +"No items for selling."); return;
				}

				player.sendMessage(Colors.White + "Shop Selling (Page " + Colors.Green  + (page) + Colors.White + " of " + Colors.Green  + (int)Math.ceil(rowCount / 10.0D) + Colors.White + "):");

				i = amount;
				while (rs.next()) {
					if(i < process) {
						if(i > amount) {
							int itemId = rs.getInt("id");
							int cost = rs.getInt("cost");
							int perBundle = rs.getInt("perbundle");
							boolean bundle = false;

							if (cost == 0) {
								continue;
							}

							if(perBundle != 0) {
								bundle = true;
							}

							String name = m.itemName(m.cInt(itemId));

							if(bundle)
								player.sendMessage(Colors.Green + name + Colors.LightGray + " is worth " + Colors.Green + cost + mo.name + Colors.LightGray + " at " + Colors.Green + perBundle + Colors.LightGray + " per bundle.");
							else {
								player.sendMessage(Colors.Green + name + Colors.LightGray + " is worth " + Colors.Green + cost + mo.name + Colors.LightGray + " per item.");
							}
						}
						i++;
					} else {
						break;
					}
				}
			} catch (SQLException ex) {
				log.severe("[iConomy] Unable to grab the itemlist from database!");
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			try {
				aList = p.selling.returnMap();
			} catch (Exception ex) {
				log.info("[iConomy] Listing failed for selling list"); return;
			}

			for (Object key: aList.keySet()) {
				String cost = (String) aList.get(key);
				String name = (String) key;
				Boolean bundle = false;
				int perBundle = 0;

				if(cost.equals("0")) {
					continue;
				} else if(cost.contains(",")) {
					String[] item = cost.split(",");
					perBundle = Integer.parseInt(item[0]);
					cost = item[1];
					bundle = true;
				} else {
					bundle = false;
				}

				if(bundle)
					available.add(Colors.Green + name + Colors.LightGray + " is worth " + Colors.Green + cost + mo.name + Colors.LightGray + " at " + Colors.Green + perBundle + Colors.LightGray + " per bundle.");
				else {
					available.add(Colors.Green + name + Colors.LightGray + " is worth " + Colors.Green + cost + mo.name + Colors.LightGray + " per item.");
				}
			}

			if(available.isEmpty()) {
				player.sendMessage(Colors.Rose +"No items for selling."); return;
			}

			player.sendMessage(Colors.White + "Shop Selling (Page " + Colors.Green  + (page) + Colors.White + " of " + Colors.Green  + (int)Math.ceil(available.size() / 10.0D) + Colors.White + "):");

			try {
				int amount = page;

				if (amount > 0){
					amount = (amount - 1) * 10;
				} else {
					amount = 0;
				}

				for (int i = amount; i < amount + 10; i++)
					if (available.size() > i)
						player.sendMessage((String)available.get(i));
			}
			catch (NumberFormatException ex)
			{
				player.sendMessage("§cNot a valid page number.");
			}
		}
	}

	public void showBuyersList(Player player, int page) {
		List available = new ArrayList();
		Map aList;

		if(p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			int i = 1;

			int amount = page;

			if (amount > 0){
				amount = (amount - 1) * 10;
			} else {
				amount = 0;
			}

			int process = amount+10;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT id,cost,perbundle FROM iBuy ORDER BY cost ASC");
				rs = ps.executeQuery();

				int rowCount = this.listGetRows("iBuy");

				if(rowCount == 0) {
					player.sendMessage(Colors.Rose +"No items for buying."); return;
				}

				player.sendMessage(Colors.White + "Shop Buying (Page " + Colors.Green  + (page) + Colors.White + " of " + Colors.Green  + (int)Math.ceil(rowCount / 10.0D) + Colors.White + "):");

				while (rs.next()) {
					if(i < process) {
						if(i > amount) {
							int itemId = rs.getInt("id");
							int cost = rs.getInt("cost");
							int perBundle = rs.getInt("perbundle");
							boolean bundle = false;

							if (cost == 0) {
								continue;
							}

							if(perBundle != 0) {
								bundle = true;
							}

							String name = m.itemName(m.cInt(itemId));

							if(bundle)
								player.sendMessage(Colors.Green + name + Colors.LightGray + " costs " + Colors.Green + cost + mo.name + Colors.LightGray + " at " + Colors.Green + perBundle + Colors.LightGray + " per bundle.");
							else {
								player.sendMessage(Colors.Green + name + Colors.LightGray + " costs " + Colors.Green + cost + mo.name + Colors.LightGray + " per item.");
							}
						}
						i++;
					} else {
						break;
					}
				}
			} catch (SQLException ex) {
				log.severe("[iConomy] Unable to grab the itemlist from database!");
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}
		} else {
			try {
				aList = p.buying.returnMap();
			} catch (Exception ex) {
				log.info("[iConomy] Listing failed for buying list"); return;
			}

			for (Object key: aList.keySet()) {
				String cost = (String) aList.get(key);
				String name = (String) key;
				Boolean bundle = false;
				int perBundle = 0;

				if(cost.equals("0")) {
					continue;
				} else if(cost.contains(",")) {
					String[] item = cost.split(",");
					perBundle = Integer.parseInt(item[0]);
					cost = item[1];
					bundle = true;
				} else {
					bundle = false;
				}

				if(bundle)
					available.add(Colors.Green + name + Colors.LightGray + " costs " + Colors.Green + cost + mo.name + Colors.LightGray + " at " + Colors.Green + perBundle + Colors.LightGray + " per bundle.");
				else {
					available.add(Colors.Green + name + Colors.LightGray + " costs " + Colors.Green + cost + mo.name + Colors.LightGray + " per item.");
				}
			}

			if(available.isEmpty()) {
				player.sendMessage(Colors.Rose +"No items for buying."); return;
			}

			player.sendMessage(Colors.White + "Shop Buying (Page " + Colors.Green  + (page) + Colors.White + " of " + Colors.Green  + (int)Math.ceil(available.size() / 10.0D) + Colors.White + "):");

			try {
				int amount = page;

				if (amount > 0){
					amount = (amount - 1) * 10;
				} else {
					amount = 0;
				}

				for (int i = amount; i < amount + 10; i++)
					if (available.size() > i)
						player.sendMessage((String)available.get(i));
			}
			catch (NumberFormatException ex)
			{
				player.sendMessage("§cNot a valid page number.");
			}
		}
	}

	/* Shop Functions */
	public boolean doPurchase(Player player, int itemId, int amount) {
		int itemAmount = this.itemCost("buy", m.cInt(itemId), amount, false);
		int needsAmount = this.itemNeedsAmount("buy", m.cInt(itemId));

		if(p.data.getBalance(player.getName()) < itemAmount){
			player.sendMessage(Colors.Rose + p.buyNotEnough);
			log.info("[iConomy Shop] " + "Player " + player.getName() + " attempted to buy more [" + itemId + "] [" + amount + "] than they have in "+mo.name+" [" + itemAmount + "].");
			l.Log("buy", player.getName() + "|0|203|" + itemId + "|" + amount + "|" + itemAmount + mo.name);

			if(p.debugging)
				log.info("[iConomy Debugging] [" + player.getName() + "] ["+itemId+"] ["+amount+"] ["+itemAmount+"] [#20335]");
		}

		if (!this.itemCan("buy", m.cInt(itemId), amount)) {
			player.sendMessage(Colors.Rose + String.format(p.buyInvalidAmount, needsAmount));
			log.info("[iConomy Shop] " + "Player " + player.getName() + " attempted to buy bundle [" + itemId + "] with the offset amount [" + amount + "].");
			l.Log("buy", player.getName() + "|0|202|" + itemId + "|" + amount);

			if(p.debugging)
				log.info("[iConomy Debugging] [" + player.getName() + "] ["+itemId+"] ["+needsAmount+"] ["+amount+"] [#20336]");

			return true;
		}

		if (itemAmount != 0) {
			int total = this.itemCost("buy", m.cInt(itemId), amount, true);
			String totalAmount = total + mo.name;

			if (p.data.getBalance(player.getName()) < total) {
				player.sendMessage(Colors.Rose + p.buyNotEnough);
				return true;
			}

			// Take dat money!
			mo.debit(null, player.getName(), total, true);

			// Total giving
			int totalGive = (needsAmount > 1) ? needsAmount*amount : amount;

			// Give dat item!
			player.giveItem(itemId, totalGive);

			// Send Message
			player.sendMessage(Colors.Green + String.format(p.buyGive, totalAmount));
			log.info("[iConomy Shop] " + "Player " + player.getName() + " bought item [" + itemId + "] amount [" + amount + "] total [" + totalAmount + "].");
			l.Log("buy", player.getName() + "|1|200|" + itemId + "|" + amount + "|" + total);
		} else {
			// Send Message
			player.sendMessage(Colors.Rose + p.buyReject);
			log.info("[iConomy Shop] " + "Player " + player.getName() + " requested to buy an unavailable item: [" + itemId + "].");
			l.Log("buy", player.getName() + "|0|201|" + itemId);

			if(p.debugging)
				log.info("[iConomy Debugging] [" + player.getName() + "] ["+itemId+"] [#20337]");

			return true;
		}

		return false;
	}

	public boolean doSell(Player player, int itemId, int amount) {
		Inventory bag = player.getInventory();
		int needsAmount = this.itemNeedsAmount("sell", m.cInt(itemId));

		if (!this.itemCan("sell", m.cInt(itemId), amount)) {
			player.sendMessage(Colors.Rose + String.format(p.sellInvalidAmount, needsAmount));
			log.info("[iConomy Shop] " + "Player " + player.getName() + " attempted to sell [" + amount + "] bundles of [" + itemId + "].");
			l.Log("sell", player.getName() + "|0|204|" + itemId + "|" + amount);

			if(p.debugging)
				log.info("[iConomy Debugging] [" + player.getName() + "] ["+amount+"] ["+needsAmount*amount+"] ["+itemId+"] [#20338]");

			return true;
		}

		int itemAmount = this.itemCost("sell", m.cInt(itemId), amount, false);

		if (itemAmount != 0) {
			int amt = (needsAmount > 1) ? needsAmount*amount : amount;
			int sold = 0;
			while (amt > 0) {
				if (bag.hasItem(itemId, ((amt > 64) ? 64 : amt), 6400)) {
					sold += ((amt > 64) ? 64 : amt);
					bag.removeItem(new Item(itemId, ((amt > 64) ? 64 : amt)));
					amt -= 64;
				} else {
					break;
				}
			}

			// Really didn't sell anything did we?
			if (sold == 0) {
				player.sendMessage(Colors.Rose + p.sellNone);
				log.info("[iConomy Shop] " + "Player " + player.getName() + " attempted to sell itemId [" + itemId + "] but had none.");
				l.Log("sell", player.getName() + "|0|202|" + itemId);

				if(p.debugging)
					log.info("[iConomy Debugging] [" + player.getName() + "] ["+itemId+"] [#20339]");

				return true;
			} else {
				bag.updateInventory();
			}

			// Total
			int total = this.itemCost("sell", m.cInt(itemId), (sold/needsAmount), true);
			String totalAmount = total + mo.name;

			// Send Message
			player.sendMessage(Colors.LightGray + String.format(p.sellGiveAmount, sold, ((needsAmount > 1) ? needsAmount*amount : amount)));
			player.sendMessage(Colors.Green + String.format(p.sellGive, totalAmount));

			// Take dat money!
			mo.deposit(null, player.getName(), total, false);

			// Show Balance
			mo.showBalance(player.getName(), null, true);

			log.info("[iConomy Shop] " + "Player " + player.getName() + " sold item [" + itemId + "] amount [" + amount + "] total [" + totalAmount + "].");
			l.Log("sell", player.getName() + "|1|200|" + itemId + "|" + amount + "|" + total);
			return true;
		} else {
			// Send Message
			player.sendMessage(Colors.Rose + p.sellReject);
			log.info("[iConomy Shop] " + "Player " + player.getName() + " requested to sell an unsellable item: [" + itemId + "].");
			l.Log("sell", player.getName() + "|0|201|" + itemId);

				if(p.debugging)
					log.info("[iConomy Debugging] [" + player.getName() + "] ["+itemId+"] [#20340]");

			return true;
		}
	}
}
