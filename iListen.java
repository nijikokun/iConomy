import java.util.Map;
import java.util.logging.Logger;

/**
 * iListen
 *	Controls all listener events and uses mostly all other classes
 *
 * @date 11/17/2010 7:33PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public class iListen extends PluginListener {
	static final Logger log = Logger.getLogger("Minecraft");
	private iConomy p;
	private iCan c;
	private iHelp h;
	private iMisc m;
	private iShop s;
	private iSign ss;
	private iMoney mo;
	private iAuction a;
	private iLottery lo;

	public void onLogin(Player player) {
		if(a.wonAuction(player.getName())) {
			a.auctionItems(player);
		}

		if(a.hasAuctions(player.getName())){
			a.auctionerItems(player);
		}
	}

	public boolean onCommand(Player player, String[] split) {
		if (!player.canUseCommand(split[0])) {

			if(p.debugging)
				log.info("[iConomy Debugging] ["+split[0]+"] [cannot use command] [#20360]");

			return false;
		}

		// Player stuff
		Player localPlayer;
		int i;

		/*
		 *  iConomy [Money]
		 *
		 *  @author: Nijikokun
		 *  @description: Allows a player to interact with an imaginary currency
		 */
		if (split[0].equalsIgnoreCase("/money")) {
			// Level 1, /money
			if ((split.length < 2)) {
				//-------------------------------------------------------------
				// TIER 1 [SELF MONEY CHECK
				//-------------------------------------------------------------
				mo.showBalance(player.getName(), null, true);
				return true;
			}

			// Level 2, [player], top, rank
			if ((split.length < 3)) {
				if (split[1].equalsIgnoreCase("-p") || split[1].equalsIgnoreCase("pay")) {
					//-------------------------------------------------------------
					// TIER 2 [PAY]
					//-------------------------------------------------------------
					if (!c.Can(player, "pay")) {
						return false;
					}

					player.sendMessage(Colors.Rose + "Invalid Usage: /money [-p|pay] <player> <amount>");
					return true;
				} else if (split[1].equalsIgnoreCase("-c") || split[1].equalsIgnoreCase("credit")) {
					if (!c.Can(player, "credit")) {
						return false;
					}

					player.sendMessage(Colors.Rose + "Invalid Usage: /money [-c|credit] <player> <amount>");
					return true;
				} else if (split[1].equalsIgnoreCase("-d") || split[1].equalsIgnoreCase("debit")) {
					if (!c.Can(player, "debit")) {
						return false;
					}

					player.sendMessage(Colors.Rose + "Invalid Usage: /money [-d|debit] <player> <amount>");
					return true;
				} else if (split[1].equalsIgnoreCase("-x") || split[1].equalsIgnoreCase("reset")) {
					//-------------------------------------------------------------
					// TIER 2 [RESET]
					//-------------------------------------------------------------
					if (!c.Can(player, "reset")) {
						return false;
					}

					player.sendMessage(Colors.Rose + "Invalid Usage: /money [-x|reset] <player> <notify(y|n)>");
					return true;
				} else if (split[1].equalsIgnoreCase("-t") || split[1].equalsIgnoreCase("top")) {
					if (!c.Can(player, "top")) {
						return false;
					}

					mo.top(player, 5);
					return true;
				} else if (split[1].equalsIgnoreCase("-r") || split[1].equalsIgnoreCase("rank")) {
					if (!c.Can(player, "rank")) {
						return false;
					}

					mo.rank(player.getName(), null, true);
					return true;
				} else if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "money");
					return true;
				} else {
					//-------------------------------------------------------------
					// TIER 2 [PLAYER MONEY CHECK]
					//-------------------------------------------------------------
					String pName = "";

					if (!c.Can(player, "view")) {
						return false;
					}

					localPlayer = m.getPlayer(split[1]);

					if (localPlayer == null) {
						if(p.data.hasBalance(split[2])) {
							pName = split[2];
						} else {
							player.sendMessage(Colors.Rose + "Player does not have account: " + split[2]);
							return true;
						}
					} else {
						pName = localPlayer.getName();
					}

					// Show another players balance
					mo.showBalance(pName, player, false);
					return true;
				}
			}

			// Level 3, top [amount], rank [amount], debit [amount] (self)
			if ((split.length < 4)) {
				if (split[1].equalsIgnoreCase("-p") || split[1].equalsIgnoreCase("pay")) {
					//-------------------------------------------------------------
					// TIER 3 [PAY]
					//-------------------------------------------------------------
					if (!c.Can(player, "pay")) {
						return false;
					}

					player.sendMessage(Colors.Rose + "Invalid Usage: /money [-p|pay] <player> <amount>");
					return true;
				} else if (split[1].equalsIgnoreCase("-c") || split[1].equalsIgnoreCase("credit")) {
					//-------------------------------------------------------------
					// TIER 3 [CREDIT]
					//-------------------------------------------------------------
					if (!c.Can(player, "credit")) {
						return false;
					}

					player.sendMessage(Colors.Rose + "Invalid Usage: /money [-c|credit] <player> <amount>");
					return true;
				} else if (split[1].equalsIgnoreCase("-d") || split[1].equalsIgnoreCase("debit")) {
					//-------------------------------------------------------------
					// TIER 3 [DEBIT]
					//-------------------------------------------------------------
					if (!c.Can(player, "debit")) {
						return false;
					}

					i = 0;

					try {
						i = Integer.parseInt(split[2]);
						if (i < 1) {
							throw new NumberFormatException();
						}
					} catch (NumberFormatException localNumberFormatException2) {
						player.sendMessage(Colors.Rose + "Invalid amount: " + i);
						player.sendMessage(Colors.Rose + "Usage: /money [-d|debit] <player> <amount>");
						return true;
					}

					// Show this amount!
					mo.debit(player.getName(), player.getName(), Integer.parseInt(split[2]), true);
					return true;
				} else if (split[1].equalsIgnoreCase("-x") || split[1].equalsIgnoreCase("reset")) {
					//-------------------------------------------------------------
					// TIER 3 [RESET]
					//-------------------------------------------------------------
					String pName = "";

					if (!c.Can(player, "reset")) {
						return false;
					}

					localPlayer = m.getPlayer(split[2]);

					if (localPlayer == null) {
						if(p.data.hasBalance(split[2])) {
							pName = split[2];
						} else {
							player.sendMessage(Colors.Rose + "Player does not have account: " + split[2]);
							return true;
						}
					} else {
						pName = localPlayer.getName();
					}

					mo.reset(pName, player, true);
					return true;
				} else if (split[1].equalsIgnoreCase("-t") || split[1].equalsIgnoreCase("top")) {
					//-------------------------------------------------------------
					// TIER 3 [TOP]
					//-------------------------------------------------------------
					if (!c.Can(player, "top")) {
						return false;
					}

					i = 0;

					try {
						i = Integer.parseInt(split[2]);
						if (i < 1) {
							throw new NumberFormatException();
						}
					} catch (NumberFormatException localNumberFormatException2) {
						player.sendMessage(Colors.Rose + "Invalid amount: " + i);
						return true;
					}

					// Show this amount!
					mo.top(player, i);
					return true;
				} else if (split[1].equalsIgnoreCase("-r") || split[1].equalsIgnoreCase("rank")) {
					//-------------------------------------------------------------
					// TIER 3 [RANK]
					//-------------------------------------------------------------
					String pName = "";

					if (!c.Can(player, "rank")) {
						return false;
					}

					localPlayer = m.getPlayer(split[2]);

					if (localPlayer == null) {
						if(p.data.hasBalance(split[2])) {
							pName = split[2];
						} else {
							player.sendMessage(Colors.Rose + "Player does not have account: " + split[2]);
							return true;
						}
					} else {
						pName = localPlayer.getName();
					}

					// Show another players rank
					mo.rank(pName, player.getName(), false);
					return true;
				} else if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "money");
					return true;
				} else {
					player.sendMessage(Colors.Rose + "Usage: /money [command|player] [parameter] [parameter]");
					player.sendMessage(Colors.Rose + "    Commands: pay, credit, debit, top, rank");
					player.sendMessage(Colors.Rose + "Alt-Commands: -p, -c, -d, -t, -r");
					return true;
				}
			}

			// Level 4
			if ((split.length < 5)) {
				if (split[1].equalsIgnoreCase("-p") || split[1].equalsIgnoreCase("pay")) {
					//-------------------------------------------------------------
					// TIER 4 [PAY]
					//-------------------------------------------------------------
					String pName = "";

					if (!c.Can(player, "pay")) {
						return false;
					}

					localPlayer = m.getPlayer(split[2]);

					if (localPlayer == null) {
						if(p.data.hasBalance(split[2])) {
							pName = split[2];
						} else {
							player.sendMessage(Colors.Rose + "Player does not have account: " + split[2]);
							return true;
						}
					} else {
						pName = localPlayer.getName();
					}

					i = 0;

					try {
						i = Integer.parseInt(split[3]);
						if (i < 1) {
							throw new NumberFormatException();
						}
					} catch (NumberFormatException localNumberFormatException2) {
						player.sendMessage(Colors.Rose + "Invalid amount: " + i);
						player.sendMessage(Colors.Rose + "Usage: /money [-p|pay] <player> <amount>");
						return true;
					}

					// Pay amount
					mo.pay(player.getName(), pName, Integer.parseInt(split[3]));
					return true;
				} else if (split[1].equalsIgnoreCase("-c") || split[1].equalsIgnoreCase("credit")) {
					//-------------------------------------------------------------
					// TIER 4 [CREDIT]
					//-------------------------------------------------------------
					String pName = "";

					if (!c.Can(player, "credit")) {
						return false;
					}

					localPlayer = m.getPlayer(split[2]);

					if (localPlayer == null) {
						if(p.data.hasBalance(split[2])) {
							pName = split[2];
						} else {
							player.sendMessage(Colors.Rose + "Player does not have account: " + split[2]);
							return true;
						}
					} else {
						pName = localPlayer.getName();
					}

					i = 0;

					try {
						i = Integer.parseInt(split[3]);
						if (i < 1) {
							throw new NumberFormatException();
						}
					} catch (NumberFormatException localNumberFormatException2) {
						player.sendMessage(Colors.Rose + "Invalid amount: " + i);
						player.sendMessage(Colors.Rose + "Usage: /money [-c|credit] <player> <amount>");
						return true;
					}

					// Credit amount
					mo.deposit(player.getName(), pName, Integer.parseInt(split[3]), true);
					return true;
				} else if (split[1].equalsIgnoreCase("-d") || split[1].equalsIgnoreCase("debit")) {
					//-------------------------------------------------------------
					// TIER 4 [DEBIT]
					//-------------------------------------------------------------
					String pName = "";

					if (!c.Can(player, "debit")) {
						return false;
					}

					localPlayer = m.getPlayer(split[2]);

					if (localPlayer == null) {
						if(p.data.hasBalance(split[2])) {
							pName = split[2];
						} else {
							player.sendMessage(Colors.Rose + "Player does not have account: " + split[2]);
							return true;
						}
					} else {
						pName = localPlayer.getName();
					}

					i = 0;

					try {
						i = Integer.parseInt(split[3]);
						if (i < 1) {
							throw new NumberFormatException();
						}
					} catch (NumberFormatException localNumberFormatException2) {
						player.sendMessage(Colors.Rose + "Invalid amount: " + i);
						player.sendMessage(Colors.Rose + "Usage: /money [-d|debit] <player> <amount>");
						return true;
					}

					// Show this amount!
					mo.debit(player.getName(), pName, Integer.parseInt(split[3]), true);
					return true;
				} else if (split[1].equalsIgnoreCase("-x") || split[1].equalsIgnoreCase("reset")) {
					//-------------------------------------------------------------
					// TIER 4 [RESET]
					//-------------------------------------------------------------
					String pName = "";

					if (!c.Can(player, "reset")) {
						return false;
					}

					localPlayer = m.getPlayer(split[2]);

					if (localPlayer == null) {
						if(p.data.hasBalance(split[2])) {
							pName = split[2];
						} else {
							player.sendMessage(Colors.Rose + "Player does not have account: " + split[2]);
							return true;
						}
					} else {
						pName = localPlayer.getName();
					}

					if (split[3].equalsIgnoreCase("y") || split[3].equalsIgnoreCase("yes")) {
						mo.reset(pName, player, true);
						return true;
					} else if (split[3].equalsIgnoreCase("n") || split[3].equalsIgnoreCase("no")) {
						mo.reset(pName, player, false);
						return true;
					} else {
						player.sendMessage(Colors.Rose + "Invalid Parameter[3] for /shop reset. must be y/n");
						return true;
					}
				} else if (split[1].equalsIgnoreCase("-t") || split[1].equalsIgnoreCase("top")) {
					//-------------------------------------------------------------
					// TIER 4 [TOP]
					//-------------------------------------------------------------
					if (!c.Can(player, "top")) {
						return false;
					}

					i = 0;

					try {
						i = Integer.parseInt(split[2]);
						if (i < 1) {
							throw new NumberFormatException();
						}
					} catch (NumberFormatException localNumberFormatException2) {
						player.sendMessage(Colors.Rose + "Invalid amount: " + i);
						player.sendMessage(Colors.Rose + "Usage: /money [-t|top] <amount>");
						return true;
					}

					// Show this amount!
					mo.top(player, Integer.parseInt(split[2]));
					return true;
				} else if (split[1].equalsIgnoreCase("-r") || split[1].equalsIgnoreCase("rank")) {
					//-------------------------------------------------------------
					// TIER 4 [RANK]
					//-------------------------------------------------------------
					String pName = "";

					if (!c.Can(player, "rank")) {
						return false;
					}

					localPlayer = m.getPlayer(split[2]);

					if (localPlayer == null) {
						if(p.data.hasBalance(split[2])) {
							pName = split[2];
						} else {
							player.sendMessage(Colors.Rose + "Player does not have account: " + split[2]);
							return true;
						}
					} else {
						pName = localPlayer.getName();
					}

					// Show another players rank
					mo.rank(pName, player.getName(), false);
					return true;
				} else if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "money");
					return true;
				} else {
					player.sendMessage(Colors.Rose + "Usage: /money [command|player] [parameter] [parameter]");
					player.sendMessage(Colors.Rose + "    Commands: pay, credit, debit, top, rank");
					player.sendMessage(Colors.Rose + "Alt-Commands: -p, -c, -d, -t, -r");
					return true;
				}
			}
			return true;
		}

		/*
		 *  iConomy [Shop] [Basic 1.0]
		 *
		 *  @author: Nijikokun
		 *  @description: Creates a basic shop!
		 *
		 *  @commands:
		 *	/auction -s|start <time-seconds> <item> <amount> <start-bid> [min-bid] [max-bid] - starts the auction with name for concurrent bids
		 *	/auction -s 30 stone 20
		 *	/auction -b|bid <a> - bid on the current auction
		 *	/auction -e|end - end the auction
		 *	/auction ?|help - help documentation
		 *	/auction - Lists currenct auction details
		 */
		if (split[0].equalsIgnoreCase("/auction") && p.globalAuction) {
			if ((split.length < 2)) {
				if(p.auctionTimerRunning) {
					String itemName = (String) p.items.get(m.cInt(p.auctionItem));
					itemName.replace("-", " ");
					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+ Colors.Yellow + " Started by " + Colors.Gold + p.auctionStarter + Colors.Yellow + " Currently Winning: " + Colors.Gold + ((p.auctionCurName == null) ? "Nobody" : p.auctionCurName));
					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Gold + player.getName() + Colors.Yellow + " Item: ["+Colors.LightGray + p.auctionAmount + Colors.Yellow + "] " + Colors.LightGray + itemName);
					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Gold + player.getName() + Colors.Yellow + " Starting Bid: " + Colors.LightGray + p.auctionStartingBid);
					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Gold + player.getName() + Colors.Yellow + " Current Balance: " + Colors.LightGray + p.auctionCurAmount);

				} else {
					player.sendMessage(Colors.Rose + "No Auction currently in progress! Use "+Colors.White+"/auction ?"+Colors.Rose+" to learn more!");
				}

				return true;
			}

			// Level 2
			if ((split.length < 3)) {
				if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "auction");
					return true;
				} else if (split[1].equalsIgnoreCase("-b") || split[1].equalsIgnoreCase("bid")) {
					if(!p.auctionTimerRunning) {
						player.sendMessage(Colors.Rose + "No Auction currently in progress! Use "+Colors.White+"/auction"+Colors.Rose+" to learn more!");
						return true;
					}

					if(player.getName().equals(p.auctionStarter)) {
						player.sendMessage(Colors.Rose + "Cannot bid on your own auction!");
					} else if (c.Can(player, "bid")) {
						player.sendMessage(Colors.Rose + "Usage: /auction bid <amount>");
						player.sendMessage(Colors.Rose + "Alt-Commands: -b");
					} else {
						return false;
					}

					return true;
				} else if (split[1].equalsIgnoreCase("-e") || split[1].equalsIgnoreCase("end")) {
					if(!p.auctionTimerRunning) {
						player.sendMessage(Colors.Rose + "No Auction currently in progress! Use "+Colors.White+"/auction"+Colors.Rose+" to learn more!");
						return true;
					}

					if(player.getName().equals(p.auctionStarter)) {
						a.endAuction();
					} else if (c.Can(player, "end")) {
						a.endAuction();
					} else {
						return false;
					}

					return true;
				} else if (split[1].equalsIgnoreCase("-s") || split[1].equalsIgnoreCase("start")) {
					player.sendMessage(Colors.Rose + "Usage: /auction start <time-seconds> <item> <amount> <start-bid>");
					player.sendMessage(Colors.Rose + "    Optional after start-bid: min-bid, max-bid");
					player.sendMessage(Colors.Rose + "Alt-Commands: -s");
					return true;
				}

				return true;
			}

			// Level 3
			if ((split.length < 4)) {
				if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "auction");
					return true;
				} else if (split[1].equalsIgnoreCase("-b") || split[1].equalsIgnoreCase("bid")) {
					if(!p.auctionTimerRunning) {
						player.sendMessage(Colors.Rose + "No Auction currently in progress! Use "+Colors.White+"/auction"+Colors.Rose+" to learn more!");
						return true;
					}

					if(player.getName().equals(p.auctionStarter)) {
						player.sendMessage(Colors.Rose + "Cannot bid on your own auction!");
					} else if (c.Can(player, "bid")) {
						int amount = Integer.parseInt(split[2]);

						if(amount < p.auctionCurAmount) {
							player.sendMessage(Colors.Rose + "You must bid at least over "+p.auctionCurAmount+mo.name+"!");
							return true;
						}

						if(amount > p.data.getBalance(player.getName())) {
							player.sendMessage(Colors.Rose + "You cannot bid more than you have!");
							mo.showBalance(player.getName(), player, true);
							return true;
						}

						a.bidAuction(player, amount, 0);

					} else {
						return false;
					}

					return true;
				} else if (split[1].equalsIgnoreCase("-e") || split[1].equalsIgnoreCase("end")) {
					if(!p.auctionTimerRunning) {
						player.sendMessage(Colors.Rose + "No Auction currently in progress! Use "+Colors.White+"/auction"+Colors.Rose+" to learn more!");
						return true;
					}

					if(player.getName().equals(p.auctionStarter)) {
						a.endAuction();
					} else if (c.Can(player, "end")) {
						a.endAuction();
					} else {
						return false;
					}

					return true;
				} else if (split[1].equalsIgnoreCase("-s") || split[1].equalsIgnoreCase("start")) {
					player.sendMessage(Colors.Rose + "Usage: /auction start <time-seconds> <item> <amount> <start-bid>");
					player.sendMessage(Colors.Rose + "    Optional after start-bid: min-bid, max-bid");
					player.sendMessage(Colors.Rose + "Alt-Commands: -s");
					return true;
				}

				return true;
			}

			if ((split.length < 10)) {
				if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "auction");
				} else if (split[1].equalsIgnoreCase("-b") || split[1].equalsIgnoreCase("bid")) {
					if(!p.auctionTimerRunning) {
						player.sendMessage(Colors.Rose + "No Auction currently in progress! Use "+Colors.White+"/auction"+Colors.Rose+" to learn more!");
						return true;
					}

					if(player.getName().equals(p.auctionStarter)) {
						player.sendMessage(Colors.Rose + "Cannot bid on your own auction!");
					} else if (c.Can(player, "bid")) {
						int amount = Integer.parseInt(split[2]);
						int secret = 0;

						if(amount < p.auctionCurAmount) {
							player.sendMessage(Colors.Rose + "You must bid at least over "+p.auctionCurAmount+mo.name+"!");
							return true;
						}

						if(amount > p.data.getBalance(player.getName())) {
							player.sendMessage(Colors.Rose + "You cannot bid more than you have!");
							mo.showBalance(player.getName(), player, true);
							return true;
						}

						if(split.length < 5) {
							secret = Integer.parseInt(split[3]);
						}

						a.bidAuction(player, amount, secret);

					} else {
						return false;
					}

					return true;
				} else if (split[1].equalsIgnoreCase("-e") || split[1].equalsIgnoreCase("end")) {
					if(!p.auctionTimerRunning) {
						player.sendMessage(Colors.Rose + "No Auction currently in progress! Use "+Colors.White+"/auction"+Colors.Rose+" to learn more!");
						return true;
					}

					if(player.getName().equals(p.auctionStarter)) {
						a.endAuction();
					} else if (c.Can(player, "end")) {
						a.endAuction();
					} else {
						return false;
					}

					return true;
				} else if (split[1].equalsIgnoreCase("-s") || split[1].equalsIgnoreCase("start")) {
					int max = 0;
					int min = 0;
					int start = 0;
					int amount = 0;
					int itemID = 0;
					int interval = 0;
					String itemName = "";

					if (!c.Can(player, "auction")) {
						return false;
					}

					if(p.auctionTimerRunning) {
						player.sendMessage(Colors.Rose + "Auction currently in progress! Use "+Colors.White+"/auction"+Colors.Rose+" to learn more!");
						return true;
					}

					if(split.length < 5) {
						player.sendMessage(Colors.Rose + "Usage: /auction start <time-seconds> <item> <amount> <start-bid>");
						player.sendMessage(Colors.Rose + "    Optional after start-bid: min-bid, max-bid");
						player.sendMessage(Colors.Rose + "Alt-Commands: -s");
						return true;
					}

					// 6
					if(split.length > 5) {
						interval = Integer.parseInt(split[2]);
						amount = Integer.parseInt(split[4]);
						start = Integer.parseInt(split[5]);

						if(interval < 11) {
							player.sendMessage(Colors.Rose + "Interval must be above 10 seconds!");
							return true;
						}

						try {
							itemID = Integer.parseInt(split[3]);
						} catch (NumberFormatException n) {
							itemID = etc.getDataSource().getItem(split[3]);
						}

						if(itemID < 0) {
							player.sendMessage(Colors.Rose + "Invalid item!");
							return true;
						}

						if (!Item.isValidItem(itemID)) {
							if(p.items.getKey(split[3]) != null) {
								itemID = Integer.parseInt(p.items.getKey(split[3]).toString());
							} else {
								player.sendMessage(Colors.Rose + "Invalid item!");
								return true;
							}
						}

						itemName = (String) p.items.get(m.cInt(itemID));
						itemName.replace("-", " ");

						if(amount < 1) {
							player.sendMessage(Colors.Rose + "Amount cannot be lower than 1!");
							return true;
						}
					}

					// 7
					if(split.length >= 7) {
						min = Integer.parseInt(split[8]);

						if(min < 3) {
							player.sendMessage(Colors.Rose + "Min cannot be lower than 2!");
							return true;
						}
					}

					// 9
					if(split.length >= 8) {
						max = Integer.parseInt(split[7]);

						if(max < 4) {
							player.sendMessage(Colors.Rose + "Max cannot be lower than 3!");
							return true;
						} else if(max < min) {
							player.sendMessage(Colors.Rose + "Max cannot be lower than minimum bid!");
							return true;
						}
					}

					if(p.debugging)
						log.info("[iConomy Debugging] ["+player+"] ["+interval+"] ["+itemID+"] ["+amount+"] ["+start+"] ["+min+"] ["+max+"] [#20361]");

					if(a.startAuction(player, interval, itemID, amount, start, min, max)) {
						player.sendMessage(Colors.Yellow + "Auction has begun.");
						m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Gold + player.getName() + Colors.Yellow + " started a new auction.");
						m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Gold + player.getName() + Colors.Yellow + " Item: ["+Colors.LightGray + amount + Colors.Yellow + "] " + Colors.LightGray + itemName);
						m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Gold + player.getName() + Colors.Yellow + " Starting Bid: " + Colors.LightGray + start);
						return true;
					} else {
						return true;
					}
				}

				return true;
			}
		}

		/*
		 *  iConomy [Lottery]
		 *
		 *  @author: Nijikokun
		 *  @description: Creates a basic lottery!
		 *
		 *  @commands:
		 *	/lottery - Lets do a lottery
		 */
		if (split[0].equalsIgnoreCase("/lottery") && p.globalLottery) {
			if ((split.length < 2)) {
				if (!c.Can(player, "lottery")) {
					return false;
				}

				// Do the lottery!
				lo.lottery(player); return true;
			}
			// Level 2
			if ((split.length < 3)) {
				if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "lottery");
					return true;
				}
			}
		}

		/*
		 *  iConomy [SignShops] [Basic 1.0]
		 *
		 *  @author: Nijikokun
		 *  @description: Creates a basic shop!
		 *
		 *  @commands:
		 *	/sign - Displays your current stock!
		 */
		if (split[0].equalsIgnoreCase("/sign") && p.globalSigns) {
			if ((split.length < 2)) {
				if(p.signOwnUse)
					player.sendMessage("You currently own " + ss.ownSign(player.getName()) + " out of " + ss.canOwn(player.getName()) + " possible shops.");

				ss.signStock(player);
				return true;
			}
			// Level 2
			if ((split.length < 3)) {
				if (split[1].equalsIgnoreCase("upgrade") || split[1].equalsIgnoreCase("-u")) {
					ss.upgradeOwn(player.getName()); return true;
				}
				if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "sign");
					return true;
				}
			}
			// Level 3
			if ((split.length < 4)) {
				if (split[1].equalsIgnoreCase("stock") || split[1].equalsIgnoreCase("-s")) {
					player.sendMessage(Colors.Rose + "Usage: /sign stock <item> <amount>");
					player.sendMessage(Colors.Rose + "Alt-Commands: -s");
					return true;
				}

				if (split[1].equalsIgnoreCase("empty") || split[1].equalsIgnoreCase("-e")) {
					int itemID = 0;

					try {
						itemID = Integer.parseInt(split[2]);
					} catch (NumberFormatException n) {
						itemID = etc.getDataSource().getItem(split[2]);
					}

					if (!Item.isValidItem(itemID)) {
						if(p.items.getKey(split[2]) != null) {
							itemID = Integer.parseInt(p.items.getKey(split[2]).toString()); return true;
						} else {
							player.sendMessage(p.signInvalidItem); return true;
						}
					}

					if(ss.existsSign(player.getName(), itemID)) {
						int curStock = ss.signCurStock(player.getName(), itemID);
						ss.signPull(player, player.getName(), itemID, curStock, true, 0); return true;
					} else {
						player.sendMessage(p.signNoExists); return true;
					}
				}

				if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "sign");
					return true;
				}
			}

			// Level 3
			if ((split.length < 5)) {
				if (split[1].equalsIgnoreCase("stock") || split[1].equalsIgnoreCase("-s")) {
					int itemID = 0;
					int amount = 0;

					try {
						itemID = Integer.parseInt(split[2]);
					} catch (NumberFormatException n) {
						itemID = etc.getDataSource().getItem(split[2]);
					}

					if (!Item.isValidItem(itemID)) {
						if(p.items.getKey(split[2]) != null) {
							itemID = Integer.parseInt(p.items.getKey(split[2]).toString()); return true;
						} else {
							player.sendMessage(p.signInvalidItem); return true;
						}
					}

					try {
						amount = Integer.parseInt(split[3]);
					} catch (NumberFormatException n) { }

					if(ss.existsSign(player.getName(), itemID) && amount > 0) {
						ss.signPush(player, player.getName(), itemID, amount, true, 0, null); return true;
					} else {
						player.sendMessage(p.signNoExists); return true;
					}
				}

				if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "sign");
					return true;
				}
			}
		}

		/*
		 *  iConomy [Shop] [Basic 1.0]
		 *
		 *  @author: Nijikokun
		 *  @description: Creates a basic shop!
		 *
		 *  @commands:
		 *	/shop <i> - Displays amount per 1 item
		 *	/shop <i> <a> - Displays amount per <a> items
		 *	/shop -b|buy <i> <a> - Purchase the item
		 *	/shop -s|sell <i> <a> - Sell an item
		 */
		if (split[0].equalsIgnoreCase("/shop") && p.globalShop) {
			// Level 1
			if ((split.length < 2)) {
				player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
				player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
				player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
				return true;
			}

			// Level 2
			if ((split.length < 3)) {
				if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "shop");
					return true;
				} else {
					int itemID = 0;

					try {
						itemID = Integer.parseInt(split[1]);
					} catch (NumberFormatException n) {
						itemID = etc.getDataSource().getItem(split[1]);
					}

					if(itemID < 0) {
						player.sendMessage(Colors.Rose + "Invalid item!");
						return true;
					}

					if (!Item.isValidItem(itemID)) {
						if(p.items.getKey(split[1]) != null) {
							itemID = Integer.parseInt(p.items.getKey(split[1]).toString());
						} else {
							player.sendMessage(Colors.Rose + "Invalid item!");
							return true;
						}
					}

					if (itemID != 0) {
						int bNA = s.itemNeedsAmount("buy", m.cInt(itemID));
						int sNA = s.itemNeedsAmount("sell", m.cInt(itemID));

						int buying = s.itemCost("buy", m.cInt(itemID), bNA, false);
						int selling = s.itemCost("sell", m.cInt(itemID), sNA, false);

						if (buying != 0) {
							if (bNA > 1) {
								player.sendMessage(" " + Colors.Green + "Must be " + Colors.Green + "bought" + Colors.LightGray + " in bundles of " + Colors.Green + bNA + Colors.LightGray + " for " + Colors.Green + buying + mo.name + Colors.LightGray + ".");
							} else {
								player.sendMessage(Colors.LightGray + "Can be " + Colors.Green + "bought" + Colors.LightGray + " for " + Colors.Green + buying + mo.name + Colors.LightGray + ".");
							}
						} else {
							player.sendMessage(Colors.Rose + "Currently not for purchasing.");
						}

						if (selling != 0) {
							if (sNA > 1) {
								player.sendMessage(Colors.LightGray + "Must be " + Colors.Green + "sold" + Colors.LightGray + " in bundles of " + Colors.Green + sNA + Colors.LightGray + " for " + Colors.Green + selling + mo.name + ".");
							} else {
								player.sendMessage(Colors.LightGray + "Can be " + Colors.Green + "sold" + Colors.LightGray + " for " + Colors.Green + selling + mo.name + Colors.LightGray + ".");
							}
						} else {
							player.sendMessage(Colors.Rose + "Currently cannot be sold.");
						}

						return true;
					} else {
						player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
						player.sendMessage(Colors.Rose + "    Commands: list, buy, sell, help");
						player.sendMessage(Colors.Rose + "Alt-Commands: -l, -b, -s, ?");
						return true;
					}
				}
			}

			// Level 3
			if ((split.length < 4)) {
				if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "shop");
					return true;
				} else if(split[1].equalsIgnoreCase("-l") || split[1].equalsIgnoreCase("list")) {
					String type = split[2];

					if(type.equalsIgnoreCase("-b") || type.equalsIgnoreCase("buy")) {
						s.showBuyersList(player, 1);
					} else if(type.equalsIgnoreCase("-s") || type.equalsIgnoreCase("sell")) {
						s.showSellersList(player, 1);
					} else {
						player.sendMessage(Colors.Rose + "Invalid Usage: /shop list [buy|sell] <page>");
						player.sendMessage(Colors.Rose + "Alt-Usage: -l, -b|-s");
					}

					return true;
				} else if (split[1].equalsIgnoreCase("-b") || split[1].equalsIgnoreCase("buy")) {
					int itemID = 0;

					if (!c.Can(player, "buy")) {
						return false;
					}

					try {
						itemID = Integer.parseInt(split[2]);
					} catch (NumberFormatException n) {
						itemID = etc.getDataSource().getItem(split[2]);
					}

					if(itemID < 0) {
						player.sendMessage(Colors.Rose + "Invalid item!");
						return true;
					}

					if (!Item.isValidItem(itemID)) {
						if(p.items.getKey(split[2]) != null) {
							itemID = Integer.parseInt(p.items.getKey(split[2]).toString());
						} else {
							player.sendMessage(Colors.Rose + "Invalid item!");
							return true;
						}
					}

					if (itemID != 0) {
						int buying = s.itemNeedsAmount("buy", m.cInt(itemID));

						if (buying != 0) {
							s.doPurchase(player, itemID, 1);
						} else {
							player.sendMessage(Colors.Rose + "Item currently not for purchasing.");
						}

						return true;
					} else {
						player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
						player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
						player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
						return true;
					}
				} else if (split[1].equalsIgnoreCase("-s") || split[1].equalsIgnoreCase("sell")) {
					int itemID = 0;

					if (!c.Can(player, "sell")) {
						return false;
					}

					try {
						itemID = Integer.parseInt(split[2]);
					} catch (NumberFormatException n) {
						itemID = etc.getDataSource().getItem(split[2]);
					}

					if(itemID < 0) {
						player.sendMessage(Colors.Rose + "Invalid item!");
						return true;
					}

					if (!Item.isValidItem(itemID)) {
						if(p.items.getKey(split[2]) != null) {
							itemID = Integer.parseInt(p.items.getKey(split[2]).toString());
						} else {
							player.sendMessage(Colors.Rose + "Invalid item!");
							return true;
						}
					}

					if (itemID != 0) {
						int selling = s.itemNeedsAmount("sell", m.cInt(itemID));

						if (selling != 0) {
							s.doSell(player, itemID, 1);
						} else {
							player.sendMessage(Colors.Rose + "Currently cannot be sold.");
						}

						return true;
					} else {
						player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
						player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
						player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
						return true;
					}
				} else if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "shop");
					return true;
				} else {
					int itemID = 0;
					int amount = Integer.parseInt(split[2]);

					try {
						itemID = Integer.parseInt(split[1]);
					} catch (NumberFormatException n) {
						itemID = etc.getDataSource().getItem(split[1]);
					}

					if(itemID < 0) {
						player.sendMessage(Colors.Rose + "Invalid item!");
						return true;
					}

					if (!Item.isValidItem(itemID) && amount == 0) {
						player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
						player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
						player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
						return true;
					} else if(!Item.isValidItem(itemID)) {
						if(p.items.getKey(split[1]) != null) {
							itemID = Integer.parseInt(p.items.getKey(split[1]).toString());
						} else {
							player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
							player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
							player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
							return true;
						}
					}

					if (itemID != 0) {
						int bNA = s.itemNeedsAmount("buy", m.cInt(itemID));
						int sNA = s.itemNeedsAmount("sell", m.cInt(itemID));

						int buying = s.itemCost("buy", m.cInt(itemID), amount, false);
						int selling = s.itemCost("sell", m.cInt(itemID), amount, false);
						int totalBuying = s.itemCost("buy", m.cInt(itemID), amount, true);
						int totalSelling = s.itemCost("sell", m.cInt(itemID), amount, true);

						if (buying != 0) {
							if (bNA > 1) {
								player.sendMessage(Colors.Green + amount + Colors.LightGray + " bundles will cost " + Colors.Green + totalBuying + mo.name + Colors.LightGray + ".");
							} else {
								player.sendMessage(Colors.Green + amount + Colors.LightGray + " will cost " + Colors.Green + totalBuying + mo.name + Colors.LightGray + ".");
							}
						} else {
							player.sendMessage(Colors.Rose + "Invalid amount or not for purchasing!");
						}

						if (selling != 0) {
							if (sNA > 1) {
								player.sendMessage(Colors.Green + amount + Colors.LightGray + " bundles will sell for " + Colors.Green + totalSelling + mo.name + Colors.LightGray + ".");
							} else {
								player.sendMessage(Colors.Green + amount + Colors.LightGray + " can be sold for " + Colors.Green + totalSelling + mo.name + Colors.LightGray + ".");
							}
						} else {
							player.sendMessage(Colors.Rose + "Invalid Amount or not for selling!");
						}

						return true;
					} else {
						player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
						player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
						player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
						return true;
					}
				}
			}

			// Level 4
			if ((split.length < 5)) {
				if (split[1].equalsIgnoreCase("-b") || split[1].equalsIgnoreCase("buy")) {
					int itemID = 0;

					if (!c.Can(player, "buy")) {
						return false;
					}

					try {
						itemID = Integer.parseInt(split[2]);
					} catch (NumberFormatException n) {
						itemID = etc.getDataSource().getItem(split[2]);
					}

					if(itemID < 0) {
						player.sendMessage(Colors.Rose + "Invalid item!");
						return true;
					}

					if (!Item.isValidItem(itemID)) {
						if(p.items.getKey(split[2]) != null) {
							itemID = Integer.parseInt(p.items.getKey(split[2]).toString());
						} else {
							player.sendMessage(Colors.Rose + "Invalid item!");
							return true;
						}
					}

					int amount = Integer.parseInt(split[3]);

					if (amount < 0 || amount == 0) {
						player.sendMessage(Colors.Rose + "Invalid amount!");
						return true;
					}

					if (itemID != 0) {
						int buying = s.itemNeedsAmount("buy", m.cInt(itemID));

						if (buying != 0) {
							s.doPurchase(player, itemID, amount);
						} else {
							player.sendMessage(Colors.Rose + "Item currently not for purchasing.");
						}

						return true;
					} else {
						player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
						player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
						player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
						return true;
					}
				} else if(split[1].equalsIgnoreCase("-l") || split[1].equalsIgnoreCase("list")) {
					String type = split[2];

					int amount = Integer.parseInt(split[3]);

					if (amount < 2 || amount == 1) {
						amount = 1;
					}

					if(type.equalsIgnoreCase("-b") || type.equalsIgnoreCase("buy")) {
						s.showBuyersList(player, amount);
					} else if(type.equalsIgnoreCase("-s") || type.equalsIgnoreCase("sell")) {
						s.showSellersList(player, amount);
					} else {
						player.sendMessage(Colors.Rose + "Invalid Usage: /shop list [buy|sell] <page>");
						player.sendMessage(Colors.Rose + "Alt-Usage: -l, -b|-s");
					}

					return true;
				} else if (split[1].equalsIgnoreCase("-s") || split[1].equalsIgnoreCase("sell")) {
					int itemID = 0;

					if (!c.Can(player, "sell")) {
						return false;
					}

					try {
						itemID = Integer.parseInt(split[2]);
					} catch (NumberFormatException n) {
						itemID = etc.getDataSource().getItem(split[2]);
					}

					if(itemID < 0) {
						player.sendMessage(Colors.Rose + "Invalid item!");
						return true;
					}

					if (!Item.isValidItem(itemID)) {
						if(p.items.getKey(split[2]) != null) {
							itemID = Integer.parseInt(p.items.getKey(split[2]).toString());
						} else {
							player.sendMessage(Colors.Rose + "Invalid item!");
							return true;
						}
					}

					int amount = Integer.parseInt(split[3]);

					if (amount < 0 || amount == 0) {
						player.sendMessage(Colors.Rose + "Invalid amount!");
						return true;
					}

					if (itemID != 0) {
						int selling = s.itemNeedsAmount("sell", m.cInt(itemID));

						if (selling != 0) {
							s.doSell(player, itemID, amount);
						} else {
							player.sendMessage(Colors.Rose + "Item currently not for selling.");
						}

						return true;
					} else {
						player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
						player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
						player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
						return true;
					}
				} else if (split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")) {
					h.help(player, "shop");
					return true;
				} else {
					player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
					player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
					player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
					return true;
				}
			}
		}
		return false;
	}

	public boolean onComplexBlockChange(Player player, ComplexBlock block) {
		if(!p.globalSigns)
			return false;

		if (!c.Can(player, "sign") || !c.Can(player, "trade")) {
			return false;
		}

		if (!(block instanceof Sign)) {
			return false;
		}

		Sign sign = (Sign) block;

		// Trading
		if(sign.getText(1).equalsIgnoreCase("[trade]")) {
			sign.setText(2, "Setup Chest");
			sign.setText(3, "Reclick Sign");
			sign.update();
			return false;
		}

		// Selling / Buying
		if (!(sign.getText(1).equalsIgnoreCase("sell") || sign.getText(1).equalsIgnoreCase("buy"))) {
			return false;
		}

		if(p.signOwnUse){
			if(ss.ownSign(player.getName())+1 > ss.canOwn(player.getName())){
				log.info("[iConomy SS] Cannot create shop Own:"+ss.ownSign(player.getName())+" Can:"+ss.canOwn(player.getName()));
				return false;
			}
		}

		String[] split = sign.getText(2).split(" ");

		if (split.length != 2) {
			player.sendMessage(String.format(p.signFailedToParse, sign.getText(2)));
			sign.setText(0, ""); return false;
		}

		int amount, perTransaction, item = Integer.parseInt(split[0]);

		try {
			amount = Integer.parseInt(split[1]);

			if (amount <= 0) {
				player.sendMessage(String.format(p.signAmountGreaterThan, 0));
				sign.setText(0, ""); return false;
			}
		} catch (Exception ex) {
			player.sendMessage(String.format(p.signValuesNotNumerical, sign.getText(2))); return false;
		}

		if (!Item.isValidItem(item)) {
			if(p.items.getKey(split[0]) != null) {
				item = Integer.parseInt(p.items.getKey(split[0]).toString());
			} else {
				player.sendMessage(String.format(p.signInvalidItem)); return true;
			}
		}

		if (amount > 64) {
			player.sendMessage(String.format(p.signMaxBuySell, 64)); return false;
		}

		try {
			perTransaction = Integer.parseInt(sign.getText(3));

			if (perTransaction <= 0) {
				player.sendMessage(String.format(p.signTransactionGreaterThan, 0));
				sign.setText(0, ""); return false;
			}
		} catch (Exception ex) {
			player.sendMessage(String.format(p.signValuesNotNumerical, sign.getText(2))); return false;
		}

		sign.setText(0, player.getName());

		if(sign.getText(1).equalsIgnoreCase("sell")) {
			player.sendMessage(String.format(p.signSellCreated, amount, m.itemName(String.valueOf(item)), sign.getText(3) + mo.name));
		} else {
			player.sendMessage(String.format(p.signBuyCreated, amount, m.itemName(String.valueOf(item)), sign.getText(3) + mo.name));
		}

		ss.setSign(player.getName(), sign.getX(), sign.getY(), sign.getZ(), item, 0);
		return false;
	}

	public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked, int itemInHand) {

		if (itemInHand != -1) {
			return false;
		}

		if(!p.globalSigns)
			return false;

		if (!c.Can(player, "signBuy") || !c.Can(player, "signSell") || !c.Can(player, "trade")) {
			return false;
		}

		ComplexBlock theblock = etc.getServer().getComplexBlock(blockClicked.getX(), blockClicked.getY(), blockClicked.getZ());

		if (!(theblock instanceof Sign)) {
			return false;
		} else {
			Sign sign = (Sign) theblock;

			// Trading
			if(sign.getText(1).equalsIgnoreCase("[trade]")) {
				Chest chest = null;
				Item item = null;
				Map traders = null;

				String name = "";
				int amount = 0, give = 0, toGive = 0, itemId = 0, remainder = 0, factor = 0, items = 0, i = 0;
				int x, y, z;

				//Coordinates of the sign
				x = blockClicked.getX();
				y = blockClicked.getY();
				z = blockClicked.getZ();

				ComplexBlock theOBlock = etc.getServer().getComplexBlock(x,y-1,z);

				if(theOBlock instanceof Chest)
					chest = (Chest) etc.getServer().getComplexBlock(x, y-1, z);

				//Is there something in the first slot?
				if (chest.getEmptySlot() != 0 ) {
					item = chest.getItemFromSlot(0);
					amount = item.getAmount();

					factor = 0;
					items = 0;
					i = 1;

					try {
						traders = p.trades.returnMap();
					} catch (Exception ex) {
						log.info("[iConomy] Mapping failed for trades"); return false;
					}

					for (Object key: traders.keySet()) {
						String data = (String) traders.get(key);
						String tradeData[] = data.split(",");

						if(Integer.parseInt(tradeData[0]) == item.getItemId()) {
							factor = Integer.parseInt(tradeData[1]);
							name = (String) key;
							toGive = Integer.parseInt(tradeData[2]);

							if(tradeData.length == 4)
								itemId = Integer.parseInt(tradeData[3]);

							items = i;
							break;
						}

						i++;
					}

					player.sendMessage("§6[Trade]§f Trading: " + name + " - Amount: " + amount + ".");

					//Calculates the amount of gold (or the currency) to give the player
					if (factor != 0) {
						if (amount >= factor) {
							give = amount / factor;
							remainder = amount % factor;

							//Send the "money" to the player
							if(itemId != 0)
								player.giveItem(itemId, give * toGive);
							else {
								mo.deposit(null, player.getName(), give * toGive, true);
							}

							//Removes the content's of the chest and add the remainder, if any
							item.setAmount(remainder);
							chest.removeItem(0);
							chest.addItem(item);

							//Let you and everybody know what happened...
							if(itemId != 0)
								player.sendMessage("§6[Trade]§f You received " + (give * toGive) + " " + m.itemName(m.cInt(itemId)) + ".");
							else {
								player.sendMessage("§6[Trade]§f You received " + (give * toGive) + mo.name + ".");
							}

							player.sendMessage("§6[Trade]§f There are " + remainder + " " + name + " left in the chest.");
							if(itemId != 0)
								m.broadcast("§6[Trade] " + player.getName() + " got " + (give * toGive) + " " + m.itemName(m.cInt(itemId)) + " from trade.");
							else {
								m.broadcast("§6[Trade] " + player.getName() + " got " + (give * toGive) + mo.name + " from trade.");
							}
						} else {
							player.sendMessage("§6[Trade]§f Not enough items (" + amount + "/" + factor + ") for trade.");
						}
					} else {
						player.sendMessage("§6[Trade]§f This item cannot be used for trade.");
					}

					//Update the chest content
					chest.update();
				} else {
					player.sendMessage("§6[Trade]§f Add the items into the first slot.");
				}

				return false;
			}

			// Trade Recursive Information~
			if((sign.getText(1).equalsIgnoreCase("[rates]"))) {
				Map traders;
				player.sendMessage("§6[Trade]§f Trading rates are currently:");

				try {
					traders = p.trades.returnMap();
				} catch (Exception ex) {
					log.info("[iConomy] Mapping failed for trades"); return false;
				}

				for (Object key: traders.keySet()) {
					String data = (String) traders.get(key);
					String tradeData[] = data.split(",");

					if(tradeData.length == 4)
						player.sendMessage("    §6-§f " + tradeData[1] + "x " + (String) key + " for " + tradeData[2] + " " + m.itemName(tradeData[3]));
					else {
						player.sendMessage("    §6-§f " + tradeData[1] + "x " + (String) key + " for " + tradeData[2] + mo.name);
					}
				}
			}

			// Selling / Buying
			if (!(sign.getText(1).equalsIgnoreCase("sell") || sign.getText(1).equalsIgnoreCase("buy"))) {
				return false;
			}

			player.addGroup("groupname");


			if (!ss.waitedEnough(player.getName())) {
				player.sendMessage(String.format(p.signWait, p.signWaitAmount));
				return false;
			}

			ss.updateClick(player.getName());

			String[] split = sign.getText(2).split(" ");
			int i = Integer.parseInt(split[0]);
			int amount = Integer.parseInt(split[1]);
			int price = Integer.parseInt(sign.getText(3));

			if (amount <= 0) {
				player.sendMessage(String.format(p.signAmountGreaterThan, 0));
				sign.setText(0, ""); sign.setText(1, ""); sign.setText(2, ""); sign.setText(3, "");
				sign.update();
				return false;
			}

			if(!ss.existsSign(sign.getText(0), i)) {
				player.sendMessage(p.signInvalid);
				sign.setText(0, ""); sign.setText(1, ""); sign.setText(2, ""); sign.setText(3, "");
				sign.update();
				return false;
			}

			int curStock = ss.signCurStock(sign.getText(0), i);

			if (player.getName().equalsIgnoreCase(sign.getText(0))) {
				if (sign.getText(1).equalsIgnoreCase("sell")) {
					ss.signPush(player, sign.getText(0), i, amount, true, 0, blockClicked);
				} else {
					if(curStock < 1) {
						player.sendMessage(String.format(p.signNotInStocky, "You", m.itemName(m.cInt(i))));
						return false;
					}

					ss.signPull(player, sign.getText(0), i, amount, true, 0);
				}
			} else {
				if (sign.getText(1).equalsIgnoreCase("sell")) {
					if (!m.canAfford(sign.getText(0), price)) {
						player.sendMessage(String.format(p.signOwnerBankrupt, "Owner", mo.name));
						return false;
					} else {
						ss.signPush(player, sign.getText(0), i, amount, false, price, blockClicked);
					}
				} else {
					if (!m.canAfford(player.getName(), price)) {
						player.sendMessage(String.format(p.signNotEnoughp, mo.name));
						return false;
					} else {

						if(curStock < 1) {
							player.sendMessage(String.format(p.signNotInStock, sign.getText(0), m.itemName(m.cInt(i))));
							return false;
						}

						ss.signPull(player, sign.getText(0), i, amount, false, price);
					}
				}

			}
		}
		return false;
	}

	public boolean onBlockDestroy(Player player, Block block) {

		if(!p.globalSigns)
			return false;

		ComplexBlock theblock = etc.getServer().getComplexBlock(block.getX(), block.getY(), block.getZ());

		if (!(theblock instanceof Sign)) {
			return false;
		} else {
			Sign sign = (Sign) theblock;

			if (!(sign.getText(1).equalsIgnoreCase("sell") || sign.getText(1).equalsIgnoreCase("buy"))) {
				return false;
			}

			String[] split = sign.getText(2).split(" ");
			int i = Integer.parseInt(split[0]);

			if(!ss.existsSign(sign.getText(0), i)) {
				return false;
			}

			if(!sign.getText(0).equalsIgnoreCase(player.getName())){
				if(block.getStatus() == 0 || block.getStatus() == 1 || block.getStatus() == 3) {
					player.sendMessage("This sign is protected.");
					return true;
				}
			} else {
				if(block.getStatus() == 3){
					ss.deleteSign(player.getName(), i);
					sign.setText(0, ""); sign.setText(1, ""); sign.setText(2, ""); sign.setText(3, "");
					sign.update();
					return false;
				}
			}
		}

		return false;
	}
}
