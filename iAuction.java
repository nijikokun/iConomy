import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * iAuction
 *	Controls auction system
 *
 * @date 11/17/2010 8:34PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public class iAuction {
	static final Logger log = Logger.getLogger("Minecraft");
	private iConomy p;
	private iLog l;
	private iMisc m;
	private iMoney mo;

	public boolean startAuction(Player player, int inter, int itemId, int itemAmount, int startingBid, int minBid, int maxBid) {
		Inventory bag = player.getInventory();
		int amt = itemAmount;
		int sold = 0;
		while (amt > 0) {
			if (bag.hasItem(itemId, ((amt > 64) ? 64 : amt), 6400)) {
				sold = sold + ((amt > 64) ? 64 : amt);
				bag.removeItem(new Item(itemId, (amt > 64 ? 64 : amt)));
				amt -= 64;
			} else {
				m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Yellow + "Auctioner has attempted to cheat!");
				m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Yellow + "Specified items are not in his inventory!");

				if(p.debugging)
					log.info("[iConomy Debugging] [" + player + "] [" + inter + "] [" + itemId + "] [" + startingBid + "] [" + minBid + "] [" + maxBid + "] [#20348]");

				return false;
			}
		}

		// Really didn't sell anything did we?
		if (sold == 0) {
			m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Yellow + "Auctioner has attempted to cheat!");
			m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Yellow + "Specified items are not in his inventory!");

			if(p.debugging)
				log.info("[iConomy Debugging] [" + player + "] [" + inter + "] [" + itemId + "] [" + startingBid + "] [" + minBid + "] [" + maxBid + "] [#20349]");


			l.Log("auction", player.getName() + "|null|0|205|"+itemId+"|"+inter+"|"+startingBid+"|"+minBid+"|"+maxBid);

			return false;
		} else {
			bag.updateInventory();
		}

		p.auctionTimerRunning = true;
		p.auctionItem = itemId;
		p.auctionAmount = itemAmount;
		p.auctionStarter = player.getName();
		p.auctionCurBid = 0;
		p.auctionCurSecretBid = 0;
		p.auctionCurAmount = startingBid;
		p.auctionStartingBid = startingBid;
		p.auctionCurBidCount = 0;
		p.auctionMin = minBid;
		p.auctionReserve = maxBid;

		l.Log("auction", player.getName() + "|null|1|205|"+p.auctionItem+"|"+p.auctionAmount+"|"+p.auctionCurAmount+"|"+p.auctionCurBidCount+"|"+p.auctionCurBid+"|"+p.auctionCurSecretBid+"|"+p.auctionReserveMet+"|"+p.auctionReserve);

		// Setup finals
		final iAuction a = this;
		final iMisc mn = m;
		final int interval = inter;



		// The timer whoops.
		p.auctionTimer = new Timer();

		// Start
		p.auctionTimer.scheduleAtFixedRate(new TimerTask() {
		    int i = interval;
		    iAuction p = a;
		    iMisc mo = mn;
		    public void run() {
			this.i--;
			if (i == 10) {
				this.mo.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Green + "10" + Colors.LightGray + " seconds left to bid!");
			}

			if (i < 6 && i > 1) {
				this.mo.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Green + i + Colors.LightGray + " seconds left to bid!");
			}

			if(i == 1) {
				this.mo.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Green + i + Colors.LightGray + " second left to bid!");
			}

			if (i < 1) { this.p.endAuction(); }
		    }
		}, 0L, 1000);

		return true;
	}

	public void bidAuction(Player player, int amount, int secret) {
		Boolean outbid = true;
		if(p.auctionCurAmount < amount) {
			if(p.auctionCurBid != 0 && p.auctionCurAmount != p.auctionStartingBid) {
				if(amount < p.auctionCurSecretBid) {
					if(secret != 0 && secret > p.auctionCurSecretBid) {
						if(secret == p.auctionCurSecretBid) {
							amount = secret;
							secret = 0;
						} else {
							amount = p.auctionCurSecretBid+1;
						}
					} else {
						outbid = false;
					}

					if(!outbid) {
						p.auctionCurBid = amount+1;
						p.auctionCurAmount = amount+1;
						p.auctionCurBidCount += 1;

						if(p.auctionCurBid == p.auctionCurSecretBid) {
							Player previous = m.getPlayer(p.auctionCurName);

							if(previous != null) {
								previous.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Yellow + "has reached your secret amount!");
							}
						}

						m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] has been raised to " + Colors.Green + p.auctionCurAmount + mo.name + Colors.LightGray + "!");
						player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Yellow + "You have been outbid by "+ Colors.Green + p.auctionCurName + Colors.LightGray + " secret bid!");

						if(p.debugging)
							log.info("[iConomy Debugging] [" + player + "] [" + amount + "] [" + secret + "] ["+p.auctionCurAmount+"] ["+p.auctionCurBidCount+"] ["+p.auctionCurBid+"] ["+p.auctionCurSecretBid+"] [#20350]");

						l.Log("auction", player.getName() + "|"+p.auctionCurName+"|1|200|" + amount + "|"+secret+"|"+p.auctionItem+"|"+p.auctionAmount+"|"+p.auctionCurAmount+"|"+p.auctionCurBidCount+"|"+p.auctionCurBid+"|"+p.auctionCurSecretBid+"|"+p.auctionReserveMet+"|"+p.auctionReserve);

					} else {
						Player previous = m.getPlayer(p.auctionCurName);

						if(previous != null) {
							previous.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Yellow + "You have been outbid!");
						}
					}
				} else {
					Player previous = m.getPlayer(p.auctionCurName);

					if(previous != null) {
						previous.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Yellow + "You have been outbid!");
					}
				}
			}

			if(outbid) {
				p.auctionCurBid = amount;
				p.auctionCurSecretBid = (secret == 0) ? amount : secret;
				p.auctionCurAmount = amount;
				p.auctionCurName = player.getName();
				p.auctionCurBidCount += 1;

				if(p.debugging)
					log.info("[iConomy Debugging] [" + player + "] [" + amount + "] [" + secret + "] ["+p.auctionCurAmount+"] ["+p.auctionCurBidCount+"] ["+p.auctionCurBid+"] ["+p.auctionCurSecretBid+"] [#20351]");

				l.Log("auction", player.getName() + "|"+p.auctionCurName+"|1|201|" + amount + "|"+secret+"|"+p.auctionItem+"|"+p.auctionAmount+"|"+p.auctionCurAmount+"|"+p.auctionCurBidCount+"|"+p.auctionCurBid+"|"+p.auctionCurSecretBid+"|"+p.auctionReserveMet+"|"+p.auctionReserve);
			}

			if(p.auctionCurAmount >= p.auctionReserve && p.auctionReserve != 0 && !p.auctionReserveMet) {
				m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Green + p.auctionCurName + Colors.LightGray + " reserve has been met!");
				p.auctionReserveMet = true;

				if(p.debugging)
					log.info("[iConomy Debugging] [" + player + "] [" + amount + "] [" + secret + "] ["+p.auctionCurAmount+"] ["+p.auctionReserve+"] ["+p.auctionReserveMet+"] [#20352]");

				l.Log("auction", player.getName() + "|"+p.auctionCurName+"|1|202|" + amount + "|"+secret+"|"+p.auctionItem+"|"+p.auctionAmount+"|"+p.auctionCurAmount+"|"+p.auctionCurBidCount+"|"+p.auctionCurBid+"|"+p.auctionCurSecretBid+"|"+p.auctionReserveMet+"|"+p.auctionReserve);
			} else {
				if(outbid) {
					m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Green + p.auctionCurName + Colors.LightGray + " is now in the lead!");
					m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.LightGray + "Auction currently stands at " + Colors.Green + p.auctionCurAmount + mo.name + Colors.LightGray + "!");
				}
			}
		} else {
			player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Yellow + "You must bid over the auction amount!");

			if(p.debugging)
				log.info("[iConomy Debugging] [" + player + "] [" + amount + "] [" + secret + "] ["+p.auctionCurAmount+"] ["+p.auctionCurBidCount+"] ["+p.auctionCurBid+"] ["+p.auctionCurSecretBid+"] [#20351]");

			l.Log("auction", player.getName() + "|"+p.auctionCurName+"|0|203|" + amount + "|"+secret+"|"+p.auctionItem+"|"+p.auctionAmount+"|"+p.auctionCurAmount+"|"+p.auctionCurBidCount+"|"+p.auctionCurBid+"|"+p.auctionCurSecretBid+"|"+p.auctionReserveMet+"|"+p.auctionReserve);
		}
	}

	public void endAuction() {
		p.auctionTimer.cancel();
		p.auctionTimerRunning = false;

		if(p.auctionCurBid != 0 && p.auctionCurAmount != p.auctionStartingBid) {
			if(p.auctionCurAmount > p.auctionMin) {
				m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Green + p.auctionCurName + Colors.LightGray + " has won the auction at " + Colors.Green + p.auctionCurAmount + mo.name + Colors.LightGray + "!");

				Player player = m.getPlayer(p.auctionCurName);
				Player auctioner = m.getPlayer(p.auctionStarter);

				if(p.debugging)
					log.info("[iConomy Debugging] [" + player + "] ["+auctioner+"] ["+p.auctionItem+"] ["+p.auctionAmount+"] ["+p.auctionCurAmount+"] ["+p.auctionCurBidCount+"] ["+p.auctionCurBid+"] ["+p.auctionCurSecretBid+"] [#20352]");

				l.Log("auction", player.getName() + "|"+p.auctionStarter+"|1|204|"+p.auctionItem+"|"+p.auctionAmount+"|"+p.auctionCurAmount+"|"+p.auctionCurBidCount+"|"+p.auctionCurBid+"|"+p.auctionCurSecretBid+"|"+p.auctionReserveMet+"|"+p.auctionReserve);

				if(player == null && auctioner == null) {
					p.auctions.setString(p.auctionCurName, p.auctionItem + "," + p.auctionAmount + "," + p.auctionCurAmount);
					p.auctioner.setInt(p.auctionStarter, p.auctionCurAmount);
				} else if (player == null && auctioner != null) {
					p.auctions.setString(p.auctionCurName, p.auctionItem + "," + p.auctionAmount + "," + p.auctionCurAmount);

					// Weee~
					mo.deposit(null, auctioner.getName(), p.auctionCurAmount, false);
					auctioner.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.LightGray + "Auction Over!");
					auctioner.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + p.auctionCurAmount + mo.name + Colors.LightGray + " has been credited to your account!");
				} else if (player != null && auctioner == null) {
					mo.debit(null, player.getName(), p.auctionCurAmount, false);
					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.LightGray + "You Won! ");
					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + p.auctionCurAmount + mo.name + Colors.LightGray + " has been debited from your account!");
					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + "Enjoy your item(s)!");
					player.giveItem(p.auctionItem, p.auctionAmount);

					p.auctioner.setInt(p.auctionStarter, p.auctionCurAmount);
				} else {
					mo.debit(null, player.getName(), p.auctionCurAmount, false);
					mo.deposit(null, auctioner.getName(), p.auctionCurAmount, false);

					auctioner.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.LightGray + "Auction Over!");
					auctioner.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + p.auctionCurAmount + mo.name + Colors.LightGray + " has been credited to your account!");

					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.LightGray + "You Won! ");
					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + p.auctionCurAmount + mo.name + Colors.LightGray + " has been debited from your account!");
					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + "Enjoy your item(s)!");

					// Give items! :D
					player.giveItem(p.auctionItem, p.auctionAmount);
				}
			} else {
				Player player = m.getPlayer(p.auctionStarter);

				if(player != null) {
					player.giveItem(p.auctionItem, p.auctionAmount);
					player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + "Item(s) have been returned to you!");
				}

				if(p.debugging)
					log.info("[iConomy Debugging] [" + player + "] ["+p.auctionCurName+"] ["+p.auctionItem+"] ["+p.auctionAmount+"] ["+p.auctionCurAmount+"] ["+p.auctionCurBidCount+"] ["+p.auctionCurBid+"] ["+p.auctionCurSecretBid+"] [#20353]");

				l.Log("auction", player.getName() + "|"+p.auctionStarter+"|0|204|"+p.auctionItem+"|"+p.auctionAmount+"|"+p.auctionCurAmount+"|"+p.auctionCurBidCount+"|"+p.auctionCurBid+"|"+p.auctionCurSecretBid+"|"+p.auctionReserveMet+"|"+p.auctionReserve);
				m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Yellow + " Has ended. No winner as the minimum bid was not met.");
			}
		} else {
			Player player = m.getPlayer(p.auctionStarter);

			if(player != null) {
				player.giveItem(p.auctionItem, p.auctionAmount);
				player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + "Item(s) have been returned to you!");
			}


			if(p.debugging)
				log.info("[iConomy Debugging] [" + player + "] ["+p.auctionCurName+"] ["+p.auctionItem+"] ["+p.auctionAmount+"] ["+p.auctionCurAmount+"] ["+p.auctionCurBidCount+"] ["+p.auctionCurBid+"] ["+p.auctionCurSecretBid+"] [#20354]");


			l.Log("auction", player.getName() + "|"+p.auctionStarter+"|-1|204|"+p.auctionItem+"|"+p.auctionAmount+"|"+p.auctionCurAmount+"|"+p.auctionCurBidCount+"|"+p.auctionCurBid+"|"+p.auctionCurSecretBid+"|"+p.auctionReserveMet+"|"+p.auctionReserve);
			m.broadcast(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Yellow + " Has ended with no bids.");
		}

		p.auctionItem = 0;
		p.auctionAmount = 0;
		p.auctionStarter = "";
		p.auctionCurBid = 0;
		p.auctionCurSecretBid = 0;
		p.auctionCurAmount = 0;
		p.auctionStartingBid = 0;
		p.auctionCurBidCount = 0;
		p.auctionMin = 0;
		p.auctionReserve = 0;
	}

	public boolean wonAuction(String name) {
		return p.auctions.keyExists(name);
	}

	public boolean realAuction(String name) {
		return p.auctions.getString(name).contains(",");
	}

	public String[] parseAuction(String name) {
		String pauction = p.auctions.getString(name);
		return pauction.split(",");
	}

	public boolean hasAuctions(String name) {
		return p.auctioner.keyExists(name);
	}

	public boolean auctionFailed(String name) {
		return p.auctioner.getString(name).contains(",");
	}

	public String[] parseAuctioner(String name) {
		String auction = p.auctioner.getString(name);
		return auction.split(",");
	}

	public int auctionTotal(String name) {
		return p.auctioner.getInt(name);
	}

	public void auctionItems(Player player) {
		String name = player.getName();
		if(!realAuction(name)){
			return;
		}
		String[] pauction = parseAuction(name);
		int itemId = Integer.parseInt(pauction[0]);
		int itemAmount = Integer.parseInt(pauction[1]);
		int cost = Integer.parseInt(pauction[2]);
		mo.debit(null, name, cost, false);
		player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.LightGray + "You Won the auction!");
		player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + cost + mo.name + Colors.LightGray + " has been debited from your account!");
		player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + "Enjoy your item(s)!");
		player.giveItem(itemId, itemAmount);

		if(p.debugging)
			log.info("[iConomy Debugging] [" + player + "] ["+itemId+"] ["+itemAmount+"] ["+cost+"] [#20355]");

		p.auctions.removeKey(name);
	}

	public void auctionerItems(Player player) {
		String name = player.getName();
		if(auctionFailed(name)){
			String[] pauction = parseAuctioner(name);
			player.giveItem(Integer.parseInt(pauction[0]), Integer.parseInt(pauction[1]));
			player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + "Item(s) have been returned to you!");
			player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] " + Colors.Yellow + " Had ended with no bids or your min/max was not met.");
			p.auctioner.removeKey(name);

			if(p.debugging)
				log.info("[iConomy Debugging] [" + player + "] ["+pauction[0]+"] ["+pauction[1]+"] [#20356]");

			return;
		}
		int total = auctionTotal(name);
		mo.deposit(null, name, total, false);
		player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.LightGray + "Auction Ended!");
		player.sendMessage(Colors.White +"["+ Colors.Gold +"Auction"+ Colors.White +"] "+Colors.Green + total + mo.name + Colors.LightGray + " has been credited to your account!");

		if(p.debugging)
			log.info("[iConomy Debugging] [" + player + "] ["+total+"] [#20357]");

		p.auctioner.removeKey(name);
	}
}
