import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.*;
import java.util.HashMap;

/**
 * iConomy
 *	Super controller, controls all variables, enabling, disabling, and initialization
 *
 * @date 11/17/2010 8:36PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public class iConomy extends Plugin {
	protected static final Logger log = Logger.getLogger("Minecraft");

	// Classes reuse
	private iListen l = new iListen();
	private iMoney mo;

	// Debugging
	public boolean debugging = true;

	// Directory
	public String directory = "iConomy/", lDirectory = "logs/";

	// Property Files
	public iProperty settings, prizes, buying, selling, auctions, auctioner, itemNames, sign, signOwners, signLocation, trades;

	// Hashmaps for lottery
	public Map<String, String> hashPrizes;

	// Hashmaps for items
	public BidiMap items = new TreeBidiMap();

	// Data Control
	public iData data;

	// Ranking
	public HashMap<String, Long> lastClick = new HashMap<String, Long>();

	// Money Timer Settings
	public Timer mTime1, mTime2;
	public int moneyGive = 0;
	public int moneyGiveInterval = 0;
	public int moneyTake = 0;
	public int moneyTakeInterval = 0;

	// Money Settings
	public String moneyName;
	public int startingBalance;

	// Permissions
	public String canPay, canCredit, canDebit, canReset, canRank, canTop, canView, canSell, canBuy, canAuction, canBid, canEnd, canLottery, canSign, canSignSell, canSignBuy;
	public String canTrade;

	// Shop Details
	public boolean globalAuction, globalShop, physicalShop, globalLottery, globalSigns, globalStock;

	// Auction settings
	public Timer auctionTimer = new Timer();
	public String auctionName, auctionStarter, auctionCurName;
	public boolean auctionTimerRunning, auctionReserveMet = false;
	public int auctionStartingBid = 0, auctionInterval, auctionItem = 0, auctionAmount = 0, auctionMin = 0;
	public int auctionReserve = 0, auctionCurAmount = 0, auctionCurBid = 0, auctionCurBidCount = 0, auctionCurSecretBid = 0;

	// Buying Template
	public String buyInvalidAmount, buyNotEnough, buyReject, buyGive;

	// Selling Template
	public String sellInvalidAmount, sellReject, sellGiveAmount, sellGive,  sellNone;

	// Lottery Template
	public String lotteryNotEnough, lotteryLoser, lotteryNotAvailable, lotteryWinner, lotteryLimit, lotteryCost;

	// Sign Shop Template
	public String signFailedToParse, signAmountGreaterThan, signValuesNotNumerical, signInvalidItem, signMaxBuySell, signTransactionGreaterThan, signSellCreated, signBuyCreated;
	public String signWait, signOwnerBankrupt, signUpgradeAmount, signUpgradeExists, signSold, signStocked, signNotEnough, signNotInStock,signBoughtAmount,signLeftAmount, signNotEnoughp, signInvalid;
	public String signNotInStocky,signNoExists;

	// Logging
	public boolean logPay, logBuy, logSell, logAuction;

	// Tickets
	public int ticketCost;

	// Sign Shop Data
	public boolean signOwnUse;
	public int signOwnAmount, signOwnUpgrade, signMaxAmount, signWaitAmount;

	// Database
	public boolean mysql;
	public String driver, user, pass, db;

	// Versioning
	public String   version = "0.9.5";
	public String  sversion = "0.8.5";
	public String  aversion = "0.5";
	public String  lversion = "0.3";
	public String ssversion = "0.3a";
	public String  tversion = "0.1a";

	public iConomy() {
		this.settings = null;
		this.data = null;
		this.mTime1 = null;
		this.mTime2 = null;
		this.auctionTimer = null;
	}

	public void enable() {
		if (load()) {
			log.info("[iConomy v" + this.version + "] Plugin Enabled.");

			if(debugging)
				log.info("[iConomy Debugging] Enabled.");

			String latest = getVersion();
			if(latest != null) {
				if(!latest.equals(version)) {
					log.info("[iConomy Update] A new version of iConomy has been released! v"+latest+"!");
				}
			}
		} else {
			log.info("[iConomy v" + this.version + "] Plugin failed to load.");
		}

		etc.getInstance().addCommand("/money", "? - For more information");

		if(this.globalShop)
			etc.getInstance().addCommand("/shop", "? - For more information");

		if(this.globalAuction)
			etc.getInstance().addCommand("/auction", "? - For more information");

		if(this.globalLottery)
			etc.getInstance().addCommand("/lottery", "- Test your luck on the lottery!");

		if(this.globalSigns)
			etc.getInstance().addCommand("/sign", "? - For more information");
	}

	public void initialize() {
		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, l, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.LOGIN, l, this, PluginListener.Priority.LOW);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_CREATED, l, this, PluginListener.Priority.HIGH);
		etc.getLoader().addListener( PluginLoader.Hook.BLOCK_DESTROYED, l, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.COMPLEX_BLOCK_CHANGE, l, this, PluginListener.Priority.MEDIUM);
	}

	public void disable() {
		etc.getInstance().removeCommand("/money");

		if(this.globalShop)
			etc.getInstance().removeCommand("/shop");

		if(this.globalAuction)
			etc.getInstance().removeCommand("/auction");

		if(this.globalLottery)
			etc.getInstance().removeCommand("/lottery");

		if(this.globalSigns)
			etc.getInstance().removeCommand("/sign");

		if (this.mTime1 != null)
			this.mTime1.cancel();

		if (this.mTime2 != null)
			this.mTime2.cancel();

		if (this.auctionTimer != null)
			this.auctionTimer.cancel();

		log.info("[iConomy v" + this.version + "] Plugin Disabled.");
	}

	/**
	 * Update user state in plugin
	 */
	private boolean load() {
		// Create directory if it doesn't exist.
		(new File(directory)).mkdir();
		(new File(directory + lDirectory)).mkdir();

		// File Data
		this.settings = new iProperty(directory + "settings.properties");

		// Switches
		this.mysql = this.settings.getBoolean("use-mysql", false);
		this.globalShop = this.settings.getBoolean("use-shop", true);
		this.globalStock = this.settings.getBoolean("use-stock", true);
		this.globalAuction = this.settings.getBoolean("use-auction", true);
		this.globalLottery = this.settings.getBoolean("use-lottery", true);
		this.globalSigns = this.settings.getBoolean("use-signs", true);

		// Debugging
		this.debugging = this.settings.getBoolean("debugging", false);

		// Money Starting Balance
		this.startingBalance = this.settings.getInt("starting-balance", 0);

		// Lottery Ticket Cost
		this.ticketCost = this.settings.getInt("ticket-cost", 150);

		// Ticker Amounts
		this.moneyGive = this.settings.getInt("money-give", 0);
		this.moneyTake = this.settings.getInt("money-take", 0);

		// Ticker Intervals
		this.moneyGiveInterval = (1000 * this.settings.getInt("money-give-interval", 0));
		this.moneyTakeInterval = (1000 * this.settings.getInt("money-take-interval", 0));

		// Money Name
		this.moneyName = (" " + this.settings.getString("money-name", "coin"));

		// Sign Shop
		this.signMaxAmount = this.settings.getInt("sign-max-amount", 1000);
		this.signOwnUse = this.settings.getBoolean("sign-own-use",true);
		this.signOwnAmount = this.settings.getInt("sign-own-amount", 3);
		this.signOwnUpgrade = this.settings.getInt("sign-upgrade-cost", 300);
		this.signWaitAmount = this.settings.getInt("sign-wait-amount", 2);

		// Groups per Command
		this.canPay = this.settings.getString("can-pay", "*");
		this.canDebit = this.settings.getString("can-debit", "admins,");
		this.canCredit = this.settings.getString("can-credit", "admins,");
		this.canReset = this.settings.getString("can-reset", "admins,");
		this.canRank = this.settings.getString("can-rank", "*");
		this.canTop = this.settings.getString("can-top", "*");
		this.canView = this.settings.getString("can-view-player-balance", "*");
		this.canBuy = this.settings.getString("can-buy", "*");
		this.canSell = this.settings.getString("can-sell", "*");
		this.canAuction = this.settings.getString("can-auction", "*");
		this.canBid = this.settings.getString("can-bid", "*");
		this.canEnd = this.settings.getString("can-end", "admins,");
		this.canLottery = this.settings.getString("can-lottery", "*");
		this.canSign = this.settings.getString("can-make-sign-shops", "*");
		this.canSignBuy = this.settings.getString("can-sign-shop-buy", "*");
		this.canSignSell = this.settings.getString("can-sign-shop-sell", "*");
		this.canTrade = this.settings.getString("can-trade", "*");

		// Buying / Selling logging
		this.logPay = this.settings.getBoolean("log-pay", false);
		this.logBuy = this.settings.getBoolean("log-buy", false);
		this.logSell = this.settings.getBoolean("log-sell", false);
		this.logAuction = this.settings.getBoolean("log-auction", false);

		// Buy / Sell messages
		this.sellGive = this.settings.getString("sell-success", "Your account has been credited with %s!");
		this.sellGiveAmount = this.settings.getString("sell-success-sold", "Sold %d out of %d!");
		this.sellReject = this.settings.getString("sell-rejected", "Sorry, that item is currently unavailable!");
		this.sellNone = this.settings.getString("sell-none", "Whoops, you seem to not have any of that item!");
		this.buyGive = this.settings.getString("buy-success", "Your purchase cost you %s! Here you go :)!");
		this.buyReject = this.settings.getString("buy-rejected", "Sorry, that item is currently unavailable!");
		this.buyNotEnough = this.settings.getString("buy-not-enough", "Sorry, you currently don't have enough to buy that!");
		this.buyInvalidAmount = this.settings.getString("buy-invalid-amount", "Sorry, you must buy these in increments of %d!");
		this.sellInvalidAmount = this.settings.getString("sell-invalid-amount", "Sorry, you must sell these in increments of %d!");

		// Lottery messages
		this.lotteryNotEnough = this.settings.getString("lottery-not-enough", "Sorry, you do not have enough to buy a ticket!");
		this.lotteryWinner = this.settings.getString("lottery-winner", "Congratulations %s won %d %s in the lottery!");
		this.lotteryLoser = this.settings.getString("lottery-loser", "The lady looks at you and shakes her head. Try Again!.");
		this.lotteryNotAvailable = this.settings.getString("lottery-not-available", "Lottery seems to be unavailable this time. Try again!");
		this.lotteryCost = this.settings.getString("lottery-cost", "The lady snatches %s from your hand and gives you a ticket.");

		// Sign Shop messages
		this.signFailedToParse = this.settings.getString("sign-failed-parse","Failed to parse: %s");
		this.signAmountGreaterThan = this.settings.getString("sign-amount-greater-than","Amount must be greater then %d");
		this.signTransactionGreaterThan = this.settings.getString("sign-transaction-greater-than","Transaction amount must be greater then %d");
		this.signValuesNotNumerical = this.settings.getString("sign-values-numerical","Values are not numerical: %s");
		this.signMaxBuySell = this.settings.getString("sign-max-buysell","You may not sell or buy items in amounts over %d");
		this.signInvalidItem = this.settings.getString("sign-invalid-item","Invalid item!");
		this.signWait = this.settings.getString("sign-wait","You need to wait %d seconds before shopping!");
		this.signOwnerBankrupt = this.settings.getString("sign-owner-bankrupt","%s is currently out of %s.");
		this.signSellCreated = this.settings.getString("sign-sell-created","Shop sign created! People can sell %d %s for %s to you.");
		this.signBuyCreated = this.settings.getString("sign-buy-created","Shop sign created! People can buy %d %s for %s from you.");
		this.signUpgradeAmount = this.settings.getString("sign-upgrade-amount","Cannot upgrade! You must have at least %s to upgrade.");
		this.signUpgradeExists = this.settings.getString("sign-upgrade-exists","Sorry! Cannot upgrade, you must create a sign first!");
		this.signSold = this.settings.getString("sign-player-sold","You sold %d %s giving you %s");
		this.signStocked = this.settings.getString("sign-owner-stock", "%d %s has been put into stock!");
		this.signNotEnough = this.settings.getString("sign-owner-not-enough-stock", "%s doesn't have enough %s left in stock!");
		this.signNotInStock = this.settings.getString("sign-owner-no-stock", "%s doesn't have that in stock!");
		this.signNotInStocky = this.settings.getString("sign-you-no-stock", "%s don't have that in stock!");
		this.signBoughtAmount = this.settings.getString("sign-bought-amount","You bought %d of %s, it cost you %s");
		this.signLeftAmount = this.settings.getString("sign-amount-left", "You now have %d left of %s in your stock.");
		this.signNotEnoughp = this.settings.getString("sign-not-enough-player", "You don't have enough %s to do that!");
		this.signInvalid = this.settings.getString("sign-invalid", "Shop no longer exists, resetting text!");
		this.signNoExists = this.settings.getString("sign-non-existant", "You don't have a sign with that item.");

		// To be added
		//this.signUpgraded = this.settings.getString("sign-upgraded", "Successfully upgraded sign shop! You can now use %s signs");

		// Shop Data
		this.auctions = new iProperty(directory + "auction.properties");
		this.auctioner = new iProperty(directory + "auctioner.properties");
		this.trades = new iProperty(directory + "trading.properties");

		if(!this.mysql) {
			this.buying = new iProperty(directory + "buying.properties");
			this.selling = new iProperty(directory + "selling.properties");
			this.prizes = new iProperty(directory + "prizes.properties");
			this.signOwners = new iProperty(directory + "sign-owners.properties");
			this.sign = new iProperty(directory + "signs.properties");
			this.signLocation = new iProperty(directory + "sign-locations.properties");

			try {
				this.hashPrizes = this.prizes.returnMap();
			} catch (Exception ex) {
				log.severe("[iConomy Lottery] Error: " + ex);
			}
		}

		// MySQL
		this.driver = this.settings.getString("driver", "com.mysql.jdbc.Driver");
		this.user = this.settings.getString("user", "root");
		this.pass = this.settings.getString("pass", "root");
		this.db = this.settings.getString("db", "jdbc:mysql://localhost:3306/minecraft");

		// Data
		this.data = new iData(this.mysql, this.startingBalance, this.driver, this.user, this.pass, this.db);

		// Buying
		items.put("1", "stone");
		items.put("2", "grass");
		items.put("3", "dirt");
		items.put("4", "cobblestone");
		items.put("5", "wood");
		items.put("6", "sapling");
		items.put("7", "bedrock");
		items.put("8", "water");
		items.put("9", "still-water");
		items.put("10", "lava");
		items.put("11", "still-lava");
		items.put("12", "sand");
		items.put("13", "gravel");
		items.put("14", "gold-ore");
		items.put("15", "iron-ore");
		items.put("16", "coal-ore");
		items.put("17", "log");
		items.put("18", "leaves");
		items.put("19", "sponge");
		items.put("20", "glass");
		items.put("35", "gray-cloth");
		items.put("37", "yellow-flower");
		items.put("38", "red-rose");
		items.put("39", "brown-mushroom");
		items.put("40", "red-mushroom");
		items.put("41", "gold-block");
		items.put("42", "iron-block");
		items.put("43", "double-step");
		items.put("44", "step");
		items.put("45", "brick");
		items.put("46", "tnt");
		items.put("47", "bookcase");
		items.put("48", "mossy-cobblestone");
		items.put("49", "obsidian");
		items.put("50", "torch");
		items.put("51", "fire");
		items.put("52", "mob-spawner");
		items.put("53", "wooden-stairs");
		items.put("54", "chest");
		items.put("55", "redstone-wire");
		items.put("56", "diamond-ore");
		items.put("57", "diamond-block");
		items.put("58", "workbench");
		items.put("59", "crops");
		items.put("60", "soil");
		items.put("61", "furnace");
		items.put("62", "burning-furnace");
		items.put("63", "sign-post");
		items.put("64", "wooden-door");
		items.put("65", "ladder");
		items.put("66", "mine-cart-tracks");
		items.put("67", "cobblestone-stairs");
		items.put("68", "wall-sign");
		items.put("69", "lever");
		items.put("70", "stone-pressure-plate");
		items.put("71", "iron-door");
		items.put("72", "wooden-pressure-plate");
		items.put("73", "redstone-ore");
		items.put("74", "glowing-redstone-ore");
		items.put("75", "redstone-torch-off");
		items.put("76", "redstone-torch-on");
		items.put("77", "stone-button");
		items.put("78", "snow");
		items.put("79", "ice");
		items.put("80", "snow-block");
		items.put("81", "cactus");
		items.put("82", "clay");
		items.put("83", "reed");
		items.put("84", "jukebox");
		items.put("85", "fence");
		items.put("86", "pumpkin");
		items.put("87", "red-mossy-cobblestone");
		items.put("88", "mud");
		items.put("89", "brittle-gold");
		items.put("90", "portal");
		items.put("91", "jack-o-lantern");
		items.put("256", "iron-spade");
		items.put("257", "iron-pickaxe");
		items.put("258", "iron-axe");
		items.put("259", "steel-and-flint");
		items.put("260", "apple");
		items.put("261", "bow");
		items.put("262", "arrow");
		items.put("263", "coal");
		items.put("264", "diamond");
		items.put("265", "iron-ingot");
		items.put("266", "gold-ingot");
		items.put("267", "iron-sword");
		items.put("268", "wooden-sword");
		items.put("269", "wooden-spade");
		items.put("270", "wooden-pickaxe");
		items.put("271", "wooden-axe");
		items.put("272", "stone-sword");
		items.put("273", "stone-spade");
		items.put("274", "stone-pickaxe");
		items.put("275", "stone-axe");
		items.put("276", "diamond-sword");
		items.put("277", "diamond-spade");
		items.put("278", "diamond-pickaxe");
		items.put("279", "diamond-axe");
		items.put("280", "stick");
		items.put("281", "bowl");
		items.put("282", "mushroom-soup");
		items.put("283", "gold-sword");
		items.put("284", "gold-spade");
		items.put("285", "gold-pickaxe");
		items.put("286", "gold-axe");
		items.put("287", "string");
		items.put("288", "feather");
		items.put("289", "gunpowder");
		items.put("290", "wooden-hoe");
		items.put("291", "stone-hoe");
		items.put("292", "iron-hoe");
		items.put("293", "diamond-hoe");
		items.put("294", "gold-hoe");
		items.put("295", "seeds");
		items.put("296", "wheat");
		items.put("297", "bread");
		items.put("298", "leather-helmet");
		items.put("299", "leather-chestplate");
		items.put("300", "leather-pants");
		items.put("301", "leather-boots");
		items.put("302", "chainmail-helmet");
		items.put("303", "chainmail-chestplate");
		items.put("304", "chainmail-pants");
		items.put("305", "chainmail-boots");
		items.put("306", "iron-helmet");
		items.put("307", "iron-chestplate");
		items.put("308", "iron-pants");
		items.put("309", "iron-boots");
		items.put("310", "diamond-helmet");
		items.put("311", "diamond-chestplate");
		items.put("312", "diamond-pants");
		items.put("313", "diamond-boots");
		items.put("314", "gold-helmet");
		items.put("315", "gold-chestplate");
		items.put("316", "gold-pants");
		items.put("317", "gold-boots");
		items.put("318", "flint");
		items.put("319", "pork");
		items.put("320", "grilled-pork");
		items.put("321", "painting");
		items.put("322", "golden-apple");
		items.put("323", "sign");
		items.put("324", "wooden-door");
		items.put("325", "bucket");
		items.put("326", "water-bucket");
		items.put("327", "lava-bucket");
		items.put("328", "mine-cart");
		items.put("329", "saddle");
		items.put("330", "iron-door");
		items.put("331", "redstone");
		items.put("332", "snowball");
		items.put("333", "boat");
		items.put("334", "leather");
		items.put("335", "milk-bucket");
		items.put("336", "clay-brick");
		items.put("337", "clay-balls");
		items.put("338", "reed");
		items.put("339", "paper");
		items.put("340", "book");
		items.put("341", "slime-ball");
		items.put("342", "storage-mine-cart");
		items.put("343", "powered-mine-cart");
		items.put("344", "egg");
		items.put("345", "compass");
		items.put("346", "fishing-rod");
		items.put("347", "watch");
		items.put("348", "brittle-gold-dust");
		items.put("349", "raw-fish");
		items.put("350", "cooked-fish");
		items.put("2256", "gold-record");
		items.put("2257", "green-record");

		// Setup Timers
		this.mTime1 = new Timer();
		this.mTime2 = new Timer();

		// Start the ticking for giving
		if (this.moneyGiveInterval > 0) {
			this.mTime1.schedule(new TimerTask() {

				public void run() {
					etc.getInstance();
					List<Player> localList = etc.getServer().getPlayerList();
					for (Player localPlayer : localList) {
						mo.deposit(null, localPlayer.getName(), iConomy.this.moneyGive, false);
					}
				}
			}, 0L, this.moneyGiveInterval);
		}

		// Start the ticking for taking
		if (this.moneyTakeInterval > 0) {
			this.mTime2.schedule(new TimerTask() {

				public void run() {
					etc.getInstance();
					List<Player> localList = etc.getServer().getPlayerList();
					for (Player localPlayer : localList) {
						mo.debit(null, localPlayer.getName(), iConomy.this.moneyTake, false);
					}
				}
			}, 0L, this.moneyTakeInterval);
		}

		return true;
	}

	/* Versioning */
	private static String getVersion()
	{
		String content = null;

		// many of these calls can throw exceptions, so i've just
		// wrapped them all in one try/catch statement.
		try
		{
			URL url = new URL("http://mc.nexua.org/plugins/iConomy/?latest");
			URLConnection urlConnection = url.openConnection();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			String line;

			// read from the urlconnection via the bufferedreader
			while ((line = bufferedReader.readLine()) != null){
				content = line;
			}
			bufferedReader.close();
		} catch(Exception e) {
			log.severe("[iConomy Version Check] " + e);
		}

		return content;
	}
}
