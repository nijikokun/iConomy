/**
 * iHelp
 *	Controls help output and help documentation
 *
 * @date 11/17/2010 7:33PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public final class iHelp extends iConomy {
	private iConomy p = iConomy.getInstance();
	private iCan c = new iCan();

	public void help(Player player, String type) {
		if (type.equals("money")) {
			player.sendMessage(Colors.Rose + "iConomy [Money] v" + p.version + " - by Nijikokun");
			player.sendMessage(Colors.Rose + "---------------");
			player.sendMessage(Colors.Rose + "<p> = player, <a> = amount");
			player.sendMessage(Colors.Rose + "---------------");
			player.sendMessage(Colors.Rose + "/money - Shows your balance");

			if (c.Can(player, "view")) {
				player.sendMessage(Colors.Rose + "/money <p> - Shows player balance");
			}

			if (c.Can(player, "pay")) {
				player.sendMessage(Colors.Rose + "/money pay <p> <a> - Pay a player money");
			}

			if (c.Can(player, "credit")) {
				player.sendMessage(Colors.Rose + "/money credit <p> <a> - Give a player money");
			}

			if (c.Can(player, "debit")) {
				player.sendMessage(Colors.Rose + "/money debit <p> <a> - Take a players money");
			}

			if (c.Can(player, "rank")) {
				player.sendMessage(Colors.Rose + "/money rank <p> - Show your rank or another players");
			}

			if (c.Can(player, "reset")) {
				player.sendMessage(Colors.Rose + "/money reset <p> - Reset a players account balance.");
			}

			if (c.Can(player, "top")) {
				player.sendMessage(Colors.Rose + "/money top - Shows top 5");
				player.sendMessage(Colors.Rose + "/money top <a> - Shows top <a> richest players");
			}

			player.sendMessage(Colors.Rose + "/money help|? - Displays this.");
		} else if (type.equals("shop")) {
			player.sendMessage(Colors.Rose + "iConomy [Shop] v" + p.sversion + " - by Nijikokun");
			player.sendMessage(Colors.Rose + "---------------");
			player.sendMessage(Colors.Rose + "<i> = item, <a> = amount, [] = optional");
			player.sendMessage(Colors.Rose + "---------------");
			player.sendMessage(Colors.Rose + "/shop <i> - Shows amount per item for sell/buy");
			player.sendMessage(Colors.Rose + "/shop <i> <a> - Shows amount per <a> for sell/buy");
			player.sendMessage(Colors.Rose + "/shop stock [page #] - Shows stock");
			player.sendMessage(Colors.Rose + "/shop list buy [page #] - Shows buying list");
			player.sendMessage(Colors.Rose + "/shop list sell [page #] - Shows selling list");

			if (c.Can(player, "buy")) {
				player.sendMessage(Colors.Rose + "/shop buy <i> - Purchase 1 item");
				player.sendMessage(Colors.Rose + "/shop buy <i> <a> - Purchase multiple items");
			}

			if (c.Can(player, "sell")) {
				player.sendMessage(Colors.Rose + "/shop sell <i> - Sell 1 item");
				player.sendMessage(Colors.Rose + "/shop sell <i> <a> - Sell multiple items");
			}

			player.sendMessage(Colors.Rose + "/shop help|? - Displays this.");
		} else if (type.equals("auction")) {
			player.sendMessage(Colors.Rose + "iConomy [Auction] v" + p.aversion + " - by Nijikokun");
			player.sendMessage(Colors.Rose + "---------------");
			player.sendMessage(Colors.Rose + "<i> Item, <a> Amount, <s> Secret bid");
			player.sendMessage(Colors.Rose + "---------------");
			player.sendMessage(Colors.Rose + "/auction - Shows current auction details or auction running information.");

			if(c.Can(player, "auction"))
				player.sendMessage(Colors.Rose + "/auction start <time-seconds> <item> <amount> <start-bid>");
				player.sendMessage(Colors.Rose + "    Optional after <start-bid>: [min-bid] [max-bid]");
				player.sendMessage(Colors.Rose + "    Desc: Starts the auction with name for concurrent bids");

			if(c.Can(player, "bid"))
				player.sendMessage(Colors.Rose + "/auction bid <a> - bid on the current auction");
				player.sendMessage(Colors.Rose + "/auction bid <a> <s> - bid with a secret amount");

			player.sendMessage(Colors.Rose + "/auction end - ends the current auction");
			player.sendMessage(Colors.Rose + "/auction ? - help documentation");
		} else if (type.equals("lottery")) {
			player.sendMessage(Colors.Rose + "iConomy [Lottery] v" + p.lversion + " - by Nijikokun");
			player.sendMessage(Colors.Rose + "---------------");
			player.sendMessage(Colors.Rose + "/lottery - Try your luck at winning the lottery!");
			player.sendMessage(Colors.Rose + "/lottery ? - help documentation");
		} else if (type.equals("sign")) {
			player.sendMessage(Colors.Rose + "iConomy [Sign Shop] v" + p.ssversion + " - by Nijikokun & Graniz");
			player.sendMessage(Colors.Rose + "---------------");
			player.sendMessage(Colors.Rose + "/sign - Check your sign stock");
			player.sendMessage(Colors.Rose + "/sign stock <item> <amount> - Add stock to an item");
			player.sendMessage(Colors.Rose + "/sign empty <item> - Empty sign stock for item");
			player.sendMessage(Colors.Rose + "/sign ? - help documentation");
		}
	}
}
