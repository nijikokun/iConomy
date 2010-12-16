import java.util.Map;
import java.util.TreeMap;

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
 * iListen.java
 * <br /><br />
 * Listens for calls from hMod, and reacts accordingly.
 * 
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class iListen extends PluginListener {
    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public Misc Misc = new Misc();

    /**
     * Sends simple condensed help lines to the current player
     */
    private void showSimpleHelp() {
	Messaging.send("&cUsage: &f/money &c[&fcommand&c|&fplayer&c] &c[&fparameter&c] &c[&fparameter&c]");
	Messaging.send("&c    Commands: &fpay&c, &ftop&c, &frank&c, &fwithdraw&c, &fdeposit&c, &freset");
	Messaging.send("&cAlt&f-&cCommands: &f-p&c, &f-t&c, &f-r&c, &f-w&c, &f-d&c, &f-x");
    }

    /**
     * Shows the balance to the requesting player.
     *
     * @param name The name of the player we are viewing
     * @param viewing The player who is viewing the account
     * @param mine Is it the player who is trying to view?
     */
    private void showBalance(String name, Player viewing, boolean mine) {
	int balance = 0;

	if (mine) {
	    balance = iData.getBalance(viewing.getName());
	    Messaging.send(viewing, iConomy.MoneyTPL.color("tag") + iConomy.MoneyTPL.parse("personal-balance", new String[]{"+balance,+b"}, new String[]{Misc.formatCurrency(balance, iConomy.currency)}));
	} else {
	    balance = iData.getBalance(name);
	    Messaging.send(viewing, iConomy.MoneyTPL.color("tag") + iConomy.MoneyTPL.parse("player-balance", new String[]{"+balance,+b", "+name,+n"}, new String[]{Misc.formatCurrency(balance, iConomy.currency), name}));
	}
    }

    /**
     * Shows the rank of the given player. If the player is not there.. don't show at all.
     *
     * @param checking The player we are checking the rank of.
     * @param viewing The player who is viewing the rank.
     * @param mine Is it our rank?
     */
    private void showRank(String checking, Player viewing, boolean mine) {
	int i = 1;

	if (iConomy.mysql) {
	    iConomy.MySQL.Rank(checking, viewing, mine);

	    return;
	} else {
	    Map accounts;
	    TreeMap<String, Integer> sortedAccounts = null;
	    ValueComparator bvc = null;

	    try {
		accounts = iData.accounts.returnMap();
		bvc = new ValueComparator(accounts);
		sortedAccounts = new TreeMap(bvc);
		sortedAccounts.putAll(accounts);
	    } catch (Exception ex) {
		iConomy.log.severe(Messaging.bracketize(iConomy.name + " FlatFile") + " Exception while mapping accounts during ranking: " + ex);
	    }

	    for (Object key : sortedAccounts.keySet()) {
		String name = (String) key;

		if (mine) {
		    if (name.equalsIgnoreCase(checking)) {
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
		    if (name.equalsIgnoreCase(checking)) {
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
	}
    }

    /**
     * Shows the top [amount] of players, MySQL & Flatfile
     *
     * @param amount The number of players we are attempting to view
     */
    private void showTop(int amount) {
	int i = 1;

	if (iConomy.mysql) {
	    iConomy.MySQL.Top(amount);

	    return;
	} else {
	    Map accounts;
	    TreeMap<String, Integer> sortedAccounts = null;
	    ValueComparator bvc = null;

	    try {
		accounts = iData.accounts.returnMap();
		bvc = new ValueComparator(accounts);
		sortedAccounts = new TreeMap(bvc);
		sortedAccounts.putAll(accounts);
	    } catch (Exception ex) {
		iConomy.log.severe(Messaging.bracketize(iConomy.name + " FlatFile") + " Unable to retrieve array of balances!");
	    }

	    Messaging.send(
		iConomy.MoneyTPL.parse(
		    "top-opening",
		    new String[]{"+amount,+a"},
		    new String[]{Misc.string(amount)}
		)
	    );

	    if (sortedAccounts.size() < 1) {
		Messaging.send(iConomy.MoneyTPL.color("top-empty"));
		return;
	    }

	    if (amount > sortedAccounts.size()) {
		amount = sortedAccounts.size();
	    }

	    for (Object key : sortedAccounts.keySet()) {
		String name = (String) key;
		int balance = Integer.valueOf("" + sortedAccounts.get(name));

		if (i <= amount) {
		    Messaging.send(
			iConomy.MoneyTPL.parse(
			    "top-line",
			    new String[]{ "+i,+number", "+name,+n", "+balance,+b" },
			    new String[]{ Misc.string(i), name, Misc.formatCurrency(balance, iConomy.currency) }
			)
		    );
		} else {
		    break;
		}

		i++;
	    }
	}
    }

    /*
     * Reset a players account easily.
     *
     * @param resetting The player being reset. Cannot be null.
     * @param by The player resetting the account. Cannot be null.
     * @param notify Do we want to show the updates to each player?
     */
    public void showPayment(String from, String to, int amount) {
	Player paymentFrom = Misc.player(from);
	Player paymentTo = Misc.player(to);
	int balanceFrom = iData.getBalance(from);
	int balanceTo = iData.getBalance(to);

	if (from.equals(to)) {
	    if (paymentFrom != null) {
		Messaging.send(paymentFrom, iConomy.MoneyTPL.color("pay-self"));
	    }

	    iConomy.Info.write("{'action': 'self', 'player': '" + from + "', 'local': '" + to + "', 'executed': 0, 'data': { 'amount': " + amount + " }}");
	} else if (amount > balanceFrom) {
	    if (paymentFrom != null) {
		Messaging.send(paymentFrom, iConomy.MoneyTPL.color("no-funds"));
	    }

	    iConomy.Info.write("{'action': 'low balance', 'player': '" + from + "', 'local': '" + to + "', 'executed': 0, 'data': { 'amount': " + amount + " }}");
	} else {
	    balanceFrom -= amount;
	    balanceTo += amount;
	    iData.setBalance(from, balanceFrom);
	    iData.setBalance(to, balanceTo);

	    if (paymentFrom != null) {
		Messaging.send(
		    paymentFrom,
		    iConomy.MoneyTPL.color("tag") +
		    iConomy.MoneyTPL.parse(
			"payment-to",
			new String[] { "+name,+n", "+amount,+a" },
			new String[] { to, Misc.formatCurrency(amount, iConomy.currency) }
		    )
		);
	    }

	    if (paymentTo != null) {
		Messaging.send(
		    paymentTo,
		    iConomy.MoneyTPL.color("tag") +
		    iConomy.MoneyTPL.parse(
			"payment-from",
			new String[] { "+name,+n", "+amount,+a" },
			new String[] { from, Misc.formatCurrency(amount, iConomy.currency) }
		    )
		);
	    }

	    iConomy.Info.write("{'action': 'success', 'player': '" + from + "', 'local': '" + to + "', 'executed': 1, 'data': { 'amount': " + amount + " }}");

	    if (paymentFrom != null) {
		showBalance(from, paymentFrom, true);
	    }

	    if (paymentTo != null) {
		showBalance(to, paymentTo, true);
	    }
	}
    }

    /**
     * Reset a players account, accessable via Console & In-Game
     *
     * @param account The account we are resetting.
     * @param controller If set to null, won't display messages.
     * @param console Is it sent via console?
     */
    private void showReset(String account, Player controller, boolean console) {
	Player player = Misc.player(account);
	iData.setBalance(account, iConomy.initialBalance);

	if (player != null) {
	    Messaging.send(player, iConomy.MoneyTPL.color("personal-reset"));
	}

	if (controller != null) {
	    Messaging.send(
		iConomy.MoneyTPL.parse(
		    "player-reset",
		    new String[]{ "+name,+n" },
		    new String[]{ account }
		)
	    );
	}

	if (console) {
	    iConomy.log.info("Player " + account + "'s account has been reset.");
	} else {
	    iConomy.log.info(Messaging.bracketize(iConomy.name) + "Player " + account + "'s account has been reset by " + controller.getName() + ".");
	}
    }

    /**
     *
     * @param account
     * @param controller If set to null, won't display messages.
     * @param amount
     * @param console Is it sent via console?
     */
    private void showWithdraw(String account, Player controller, int amount, boolean console) {
	Player online = etc.getServer().getPlayer(account);
	int balance = iData.getBalance(account);
	balance -= (amount > balance) ? balance : amount;
	iData.setBalance(account, balance);

	if (online != null) {
	    Messaging.send(online,
		iConomy.MoneyTPL.color("tag") +
		iConomy.MoneyTPL.parse(
		    "personal-withdraw",
		    new String[]{ "+by", "+amount,+a" },
		    new String[]{ (console) ? "console" : controller.getName(), Misc.formatCurrency(amount, iConomy.currency) }
		)
	    );

	    showBalance(account, online, true);
	}

	if (controller != null) {
	    Messaging.send(
		iConomy.MoneyTPL.color("tag") +
		iConomy.MoneyTPL.parse(
		    "player-withdraw",
		    new String[]{"+name,+n", "+amount,+a"},
		    new String[]{ account, Misc.formatCurrency(amount, iConomy.currency) }
		)
	    );
	}

	if (console) {
	    iConomy.log.info("Player " + account + "'s account had " + amount + " withdrawn.");
	} else {
	    iConomy.log.info(Messaging.bracketize(iConomy.name) + "Player " + account + "'s account had " + amount + " withdrawn by " + controller.getName() + ".");
	}
    }

    /**
     *
     * @param account
     * @param controller If set to null, won't display messages.
     * @param amount
     * @param console Is it sent via console?
     */
    private void showDeposit(String account, Player controller, int amount, boolean console) {
	Player online = etc.getServer().getPlayer(account);
	int balance = iData.getBalance(account);
	balance += amount;
	iData.setBalance(account, balance);

	if (online != null) {
	    Messaging.send(online,
		iConomy.MoneyTPL.color("tag") +
		iConomy.MoneyTPL.parse(
		    "personal-deposited",
		    new String[]{ "+by", "+amount,+a" },
		    new String[]{ (console) ? "console" : controller.getName(), Misc.formatCurrency(amount, iConomy.currency) }
		)
	    );

	    showBalance(account, online, true);
	}

	if (controller != null) {
	    Messaging.send(
		iConomy.MoneyTPL.color("tag") +
		iConomy.MoneyTPL.parse(
		    "player-deposited",
		    new String[]{"+name,+n", "+amount,+a"},
		    new String[]{account, Misc.formatCurrency(amount, iConomy.currency)}
		)
	    );
	}

	if (console) {
	    iConomy.log.info("Player " + account + "'s account had " + amount + " deposited into it.");
	} else {
	    iConomy.log.info(Messaging.bracketize(iConomy.name) + "Player " + account + "'s account had " + amount + " deposited into it by " + controller.getName() + ".");
	}
    }

    /**
     * Commands sent from console to us.
     *
     * @param split The input line split by spaces.
     * @return <code>boolean</code> - True denotes that the command existed, false the command doesn't.
     */
    public boolean onConsoleCommand(String[] split) {
	String base = split[0];

	if (Misc.is(base, "help")) {
	    iConomy.log.info("iConomy Console Commands:");
	    iConomy.log.info("       reset [player]                Resets players account to initial balance");
	    iConomy.log.info("     deposit [amount(%)]             Deposit an [amount] or [percent%] to all players online (must include % to be a percent)");
	    iConomy.log.info("     deposit [player] [amount]       Deposit an [amount] to a specific player");
	    iConomy.log.info("    withdraw [amount(%)]             Withdraw an [amount] or [percent%] from all players online (must include % to be a percent)");
	    iConomy.log.info("    withdraw [player] [amount]       Withdraw an [amount] from a specific player");

	    return false;
	}

	if (Misc.is(base, "reset")) {
	    if (Misc.arguments(split, 0)) {
		iConomy.log.info("Invalid usage: reset [player]");
		return true;
	    }

	    if (Misc.arguments(split, 1)) {
		Player viewable = etc.getServer().getPlayer(split[1]);

		if (viewable == null) {
		    if (iData.hasBalance(split[1])) {
			showReset(split[1], null, true);
		    } else {
			iConomy.log.info("Sorry.. Player " + split[1] + " does not exist.");
		    }
		    return true;

		} else {
		    showReset(viewable.getName(), null, true);
		    return true;
		}
	    }

	    return true;
	}

	if (Misc.is(base, "withdraw")) {
	    if (Misc.arguments(split, 0)) {
		iConomy.log.info("Invalid usage: withdraw [player] [amount] or withdraw [amount] (takes from all players online)");
	    }

	    if(Misc.arguments(split, 1)) {
		boolean isPercent = false;
		int percent = 0;

		if(split[1].contains("%")) {
		    String[] data = split[1].split("%");
		    percent = Integer.valueOf(data[0]);
		    isPercent = true;

		    if(percent < 0) {
			iConomy.log.info("Percentage cannot be lower than 0");

			return true;
		    } else if(percent > 100) {
			iConomy.log.info("Percentage cannot be greater than 100");

			return true;
		    }
		}

		for (Player p : etc.getServer().getPlayerList()) {
		    if(p == null) { continue; }

		    int balance = 0;
		    int amount = 0;

		    if(isPercent) {
			balance = iData.getBalance(p.getName());

			if(balance > 0) {
			    amount = Math.round((percent*balance)/100);
			} else {
			    amount = 0;
			}
		    } else {
			amount = Integer.valueOf(split[1]);

			if(amount < 0) {
			    iConomy.log.info("Amount cannot be lower than 0");
			    return true;
			}
		    }

		    showWithdraw(p.getName(), null, amount, true);
		}

		return true;
	    }

	    if (Misc.arguments(split, 2)) {
		Player viewable = etc.getServer().getPlayer(split[1]);

		if (viewable == null) {
		    if (iData.hasBalance(split[1])) {
			showWithdraw(split[1], null, Integer.valueOf(split[2]), true);
		    } else {
			iConomy.log.info("Sorry.. Player " + split[1] + " does not exist.");
		    }
		    return true;

		} else {
		    showWithdraw(viewable.getName(), null, Integer.valueOf(split[2]), true);
		    return true;
		}
	    }

	    return true;
	}

	if (Misc.is(base, "deposit")) {
	    if (Misc.arguments(split, 0)) {
		iConomy.log.info("Invalid usage: deposit [player] [amount] or deposit [amount] (takes from all players online)");
	    }

	    if(Misc.arguments(split, 1)) {
		boolean isPercent = false;
		int percent = 0;

		if(split[1].contains("%")) {
		    String[] data = split[1].split("%");
		    percent = Integer.valueOf(data[0]);
		    isPercent = true;

		    if(percent < 0) {
			iConomy.log.info("Percentage cannot be lower than 0");

			return true;
		    } else if(percent > 100) {
			iConomy.log.info("Percentage cannot be greater than 100");

			return true;
		    }
		}

		for (Player p : etc.getServer().getPlayerList()) {
		    if(p == null) { continue; }
		    
		    int balance = 0;
		    int amount = 0;

		    if(isPercent) {
			balance = iData.getBalance(p.getName());

			if(balance > 0) {
			    amount = Math.round((percent*balance)/100);
			} else {
			    amount = 0;
			}
		    } else {
			amount = Integer.valueOf(split[1]);

			if(amount < 0) {
			    iConomy.log.info("Amount cannot be lower than 0");
			    return true;
			}
		    }

		    showDeposit(p.getName(), null, amount, true);
		}

		return true;
	    }

	    if (Misc.arguments(split, 2)) {
		Player viewable = etc.getServer().getPlayer(split[1]);

		if (viewable == null) {
		    if (iData.hasBalance(split[1])) {
			showDeposit(split[1], null, Integer.valueOf(split[2]), true);
		    } else {
			iConomy.log.info("Sorry.. Player " + split[1] + " does not exist.");
		    }
		    return true;

		} else {
		    showDeposit(viewable.getName(), null, Integer.valueOf(split[2]), true);
		    return true;
		}
	    }

	    return true;
	}

	return false;
    }

    /**
     * Commands sent from in game to us.
     *
     * @param player The player who sent the command.
     * @param split The input line split by spaces.
     * @return <code>boolean</code> - True denotes that the command existed, false the command doesn't.
     */
    public boolean onCommand(Player player, String[] split) {
	Messaging.save(player);
	String base = split[0];

	if (!player.canUseCommand(base)) {
	    iConomy.log.info("[iConomy] [" + player.getName() + " - " + split[0] + "] [cannot use command]");
	    return false;
	}

	if (Misc.is(base, "/money")) {
	    if (Misc.arguments(split, 0)) {
		showBalance("", player, true);
		return true;
	    }

	    if (Misc.arguments(split, 1)) {
		if (Misc.isEither(split[1], "rank", "-r")) {
		    if (!iConomy.Watch.permission("rank", player)) {
			return false;
		    }

		    showRank(player.getName(), player, true);
		    return true;
		}

		if (Misc.isEither(split[1], "top", "-t")) {
		    if (!iConomy.Watch.permission("list", player)) {
			return false;
		    }

		    showTop(5);
		    return true;
		}

		if (Misc.isEither(split[1], "help", "?")
			|| Misc.isEither(split[1], "deposit", "-d")
			|| Misc.isEither(split[1], "credit", "-c")
			|| Misc.isEither(split[1], "withdraw", "-w")
			|| Misc.isEither(split[1], "debit", "-b")
			|| Misc.isEither(split[1], "pay", "-p")) {
		    showSimpleHelp();
		    return true;
		}

		// Check another players account
		if (!iConomy.Watch.permission("access", player)) {
		    return false;
		}

		Player viewable = etc.getServer().matchPlayer(split[1]);

		if (viewable == null) {
		    if (iData.hasBalance(split[1])) {
			showBalance(split[1], player, false);
		    } else {
			Messaging.send(iConomy.MoneyTPL.parse("no-account", new String[]{"+name,+n"}, new String[]{split[1]}));
		    }
		    return true;

		} else {
		    showBalance(viewable.getName(), player, false);
		    return true;
		}
	    }

	    if (Misc.arguments(split, 2)) {
		if (Misc.isEither(split[1], "reset", "-x")) {
		    if (!iConomy.Watch.permission("reset", player)) {
			return false;
		    }

		    Player viewable = etc.getServer().getPlayer(split[2]);

		    if (viewable == null) {
			if (iData.hasBalance(split[2])) {
			    showReset(split[2], player, false);
			} else {
			    Messaging.send(iConomy.MoneyTPL.parse("no-account", new String[]{"+name,+n"}, new String[]{split[2]}));
			}
			return true;

		    } else {
			showReset(viewable.getName(), player, false);
			return true;
		    }
		}

		if (Misc.isEither(split[1], "rank", "-r")) {
		    if (!iConomy.Watch.permission("rank", player)) {
			return false;
		    }

		    Player viewable = etc.getServer().matchPlayer(split[2]);

		    if (viewable == null) {
			if (iData.hasBalance(split[2])) {
			    showRank(split[2], player, false);
			} else {
			    Messaging.send(iConomy.MoneyTPL.parse("no-account", new String[]{"+name,+n"}, new String[]{split[2]}));
			}
			return true;

		    } else {
			showRank(viewable.getName(), player, false);
			return true;
		    }
		}

		if (Misc.isEither(split[1], "top", "-t")) {
		    if (!iConomy.Watch.permission("list", player)) {
			return false;
		    }

		    showTop((Integer.parseInt(split[2]) < 0) ? 5 : Integer.parseInt(split[2]));
		    return true;
		}

		showSimpleHelp();
		return true;
	    }

	    if (Misc.arguments(split, 3)) {
		if (Misc.isEither(split[1], "pay", "-p")) {
		    if (!iConomy.Watch.permission("pay", player)) {
			return false;
		    }

		    Player viewable = etc.getServer().matchPlayer(split[2]);
		    String name = "";
		    int amount = 0;

		    if (viewable == null) {
			if (iData.hasBalance(split[2])) {
			    name = split[2];
			} else {
			    Messaging.send(iConomy.MoneyTPL.parse("no-account", new String[]{"+name,+n"}, new String[]{split[2]}));
			    return true;
			}
		    } else {
			name = viewable.getName();
		    }

		    try {
			amount = Integer.parseInt(split[3]);

			if (amount < 1) {
			    throw new NumberFormatException();
			}
		    } catch (NumberFormatException ex) {
			Messaging.send("&cInvalid amount: &f" + amount);
			Messaging.send("&cUsage: &f/money &c[&f-p&c|&fpay&c] <&fplayer&c> &c<&famount&c>");
			return true;
		    }

		    // Pay amount
		    showPayment(player.getName(), name, amount);
		    return true;
		}

		if (Misc.isEither(split[1], "deposit", "-d") || Misc.isEither(split[1], "credit", "-c")) {
		    Player viewable = etc.getServer().getPlayer(split[2]);

		    if (viewable == null) {
			if (iData.hasBalance(split[2])) {
			    showDeposit(split[2], player, Integer.valueOf(split[3]), true);
			} else {
			    Messaging.send(iConomy.MoneyTPL.parse("no-account", new String[]{"+name,+n"}, new String[]{split[2]}));
			}
			return true;

		    } else {
			showDeposit(viewable.getName(), player, Integer.valueOf(split[3]), false);
			return true;
		    }
		}

		if (Misc.isEither(split[1], "withdraw", "-w") || Misc.isEither(split[1], "debit", "-b")) {
		    Player viewable = etc.getServer().getPlayer(split[2]);

		    if (viewable == null) {
			if (iData.hasBalance(split[2])) {
			    showWithdraw(split[2], player, Integer.valueOf(split[3]), true);
			} else {
			    Messaging.send(iConomy.MoneyTPL.parse("no-account", new String[]{"+name,+n"}, new String[]{split[2]}));
			}
			return true;
		    } else {
			showWithdraw(viewable.getName(), player, Integer.valueOf(split[3]), false);
			return true;
		    }
		}

		showSimpleHelp();
		return true;
	    }
	}

	return false;
    }

    /**
     * Checks whenever a player physically logs into the server.
     * <br /><br />
     * We create a players balance based on the initial balance if they don't have one.
     *
     * @param player
     */
    public void onLogin(Player player) {
	if (!iData.hasBalance(player.getName())) {
	    iData.setBalance(player.getName(), iConomy.initialBalance);
	}
    }
}
