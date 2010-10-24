import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;


public class iConomy extends Plugin
{
	protected static final Logger log = Logger.getLogger("Minecraft");
	private Listener l = new Listener(this);
	private PropertiesFile props;
	private PropertiesFile buying;
	private PropertiesFile selling;
	HashMap<String, String> sell = new HashMap(178);
	HashMap<String, String> buy = new HashMap(178);
	public mData data;
	private LinkedList<String> rankedList;
	private int moneyGive = 0;
	private int moneyGiveInterval = 0;
	private int moneyTake = 0;
	private int moneyTakeInterval = 0;
	private Timer mTime1;
	private Timer mTime2;
	public String moneyName;
	public String canPay;
	public String canCredit;
	public String canDebit;
	public String canRank;
	public String canTop;
	public String canView;
	public String canSell;
	public String canBuy;
	public String buyInvalidAmount;
	public String buyNotEnough;
	public String buyReject;
	public String buyGive;
	public String sellInvalidAmount;
	public String sellReject;
	public String sellGive;
	public String sellNone;
	public boolean logBuy;
	public boolean logSell;
	private double version = 0.8;
	private double sversion = 0.3;

	public iConomy() {
		this.props = null;
		this.data = null;
		this.mTime1 = null;
		this.mTime2 = null;
	}

	public void enable() {
		if (load()) {
			log.info("[iConomy v" + this.version + "] Plugin Enabled.");
		} else {
			log.info("[iConomy v" + this.version + "] Plugin failed to load.");
		}

		etc.getInstance().addCommand("/money", "help|? - For more information");
	}

	public void initialize() {
		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, l, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.LOGIN, l, this, PluginListener.Priority.LOW);
	}

	public void disable() {
		etc.getInstance().removeCommand("/money");
		if (this.mTime1 != null) { this.mTime1.cancel(); }
		if (this.mTime2 != null) { this.mTime2.cancel(); }
		log.info("[iConomy v" + this.version + "] Plugin Disabled.");
	}

	private void halp(Player player, String type) {
	    if(type.equals("money")) {
		player.sendMessage(Colors.Rose + "iConomy [Money] v" + this.version + " - by Nijikokun");
		player.sendMessage(Colors.Rose + "---------------");
		player.sendMessage(Colors.Rose + "<p> = player, <a> = amount");
		player.sendMessage(Colors.Rose + "---------------");
		player.sendMessage(Colors.Rose + "/money - Shows your balance");

		if(this.can(player, "view")) {
			player.sendMessage(Colors.Rose + "/money <p> - Shows player balance");
		}

		if(this.can(player, "pay")) {
			player.sendMessage(Colors.Rose + "/money -p|pay <p> <a> - Pay a player money");
		}

		if(this.can(player, "credit")) {
			player.sendMessage(Colors.Rose + "/money -c|credit <a> <p> - Give a player money");
		}

		if(this.can(player, "debit")) {
			player.sendMessage(Colors.Rose + "/money -d|debit <a> <p> - Take a players money");
		}

		if(this.can(player, "rank")) {
			player.sendMessage(Colors.Rose + "/money -r|rank <p> - Show your rank or another players");
		}

		if(this.can(player, "top")) {
			player.sendMessage(Colors.Rose + "/money -t|top - Shows top 5");
			player.sendMessage(Colors.Rose + "/money -t|top <a> - Shows top <a> richest players");
		}

		player.sendMessage(Colors.Rose + "/money help|? - Displays this.");
	    } else if(type.equals("shop")) {
		player.sendMessage(Colors.Rose + "iConomy [Shop] v" + this.sversion + " - by Nijikokun");
		player.sendMessage(Colors.Rose + "---------------");
		player.sendMessage(Colors.Rose + "<i> = item, <a> = amount");
		player.sendMessage(Colors.Rose + "---------------");
		player.sendMessage(Colors.Rose + "/shop <i> - Shows amount per item for sell/buy");
		player.sendMessage(Colors.Rose + "/shop <i> <a> - Shows amount per <a> for sell/buy");

		if(this.can(player, "buy")) {
			player.sendMessage(Colors.Rose + "/shop -b|buy <i> - Purchase 1 item");
			player.sendMessage(Colors.Rose + "/shop -b|buy <i> <a> - Purchase multiple items");
		}

		if(this.can(player, "sell")) {
			player.sendMessage(Colors.Rose + "/shop -s|sell <i> - Sell 1 item");
			player.sendMessage(Colors.Rose + "/shop -s|sell <i> <a> - Sell multiple items");
		}

		player.sendMessage(Colors.Rose + "/shop help|? - Displays this.");
	    }
	}

	/**
	* Update user state in plugin
	*/
	public void updateState(Player player, boolean write){
		String str = player.getName();
		boolean remove = this.rankedList.remove(str);

		if(remove){ }

		insertIntoRankedList(str);

		if (write) {
			this.data.write();
		}
	}

	/**
	* Load the plugin
	*
	*/
	private boolean load() {
		// File Data
		this.props = new PropertiesFile("iConomy.properties");
		this.buying = new PropertiesFile("iConomy-buying.properties");
		this.selling = new PropertiesFile("iConomy-selling.properties");

		// Data
		String str = this.props.getString("dataFile", "iConomy.data");

		// Money Starting Balance
		int i = this.props.getInt("starting-balance", 0);

		// Ticker Amounts
		this.moneyGive = this.props.getInt("money-give", 0);
		this.moneyTake = this.props.getInt("money-take", 0);

		// Ticker Intervals
		this.moneyGiveInterval = (1000 * this.props.getInt("money-give-interval", 0));
		this.moneyTakeInterval = (1000 * this.props.getInt("money-take-interval", 0));

		// Money Name
		this.moneyName = (" " + this.props.getString("money-name", "coin"));

		// Groups per Command
		this.canPay = this.props.getString("can-pay", "*");
		this.canDebit = this.props.getString("can-debit", "admins,");
		this.canCredit = this.props.getString("can-credit", "admins,");
		this.canRank = this.props.getString("can-rank", "*");
		this.canTop = this.props.getString("can-top", "*");
		this.canView = this.props.getString("can-view-player-balance", "*");
		this.canBuy = this.props.getString("can-buy", "*");
		this.canSell = this.props.getString("can-sell", "*");

		// Buying / Selling logging
		this.logBuy = this.props.getBoolean("log-buy", false);
		this.logSell = this.props.getBoolean("log-sell", false);

		// Buy / Sell messages
		this.sellGive = this.props.getString("sell-success", "Your account has been credited with %s!");
		this.sellReject = this.props.getString("sell-rejected", "Sorry, that item is currently unavailable!");
		this.sellNone = this.props.getString("sell-none", "Whoops, you seem to not have any of that item!");
		this.buyGive = this.props.getString("buy-success", "Your purchase cost you %s! Here you go :)!");
		this.buyReject = this.props.getString("buy-rejected", "Sorry, that item is currently unavailable!");
		this.buyNotEnough = this.props.getString("buy-not-enough", "Sorry, you currently don't have enough to buy that!");
		this.buyInvalidAmount = this.props.getString("buy-invalid-amount", "Sorry, you must buy these in increments of %d!");
		this.sellInvalidAmount = this.props.getString("sell-invalid-amount", "Sorry, you must sell these in increments of %d!");

		try {
			this.data = new mData(str, i);
		} catch (Exception localException) {
			log.severe("[iConomy v" + this.version + "] Critical error while loading data:");
			localException.printStackTrace();
		}

		// Selling
		buy.put("1", this.buying.getString("stone", "0"));
		sell.put("1", this.selling.getString("stone", "0"));
		buy.put("2", this.buying.getString("grass", "0"));
		sell.put("2", this.selling.getString("grass", "0"));
		buy.put("3", this.buying.getString("dirt", "0"));
		sell.put("3", this.selling.getString("dirt", "0"));
		buy.put("4", this.buying.getString("cobblestone", "0"));
		sell.put("4", this.selling.getString("cobblestone", "0"));
		buy.put("5", this.buying.getString("wood", "0"));
		sell.put("5", this.selling.getString("wood", "0"));
		buy.put("6", this.buying.getString("sapling", "0"));
		sell.put("6", this.selling.getString("sapling", "0"));
		buy.put("7", this.buying.getString("bedrock", "0"));
		sell.put("7", this.selling.getString("bedrock", "0"));
		buy.put("8", this.buying.getString("water", "0"));
		sell.put("8", this.selling.getString("water", "0"));
		buy.put("9", this.buying.getString("still-water", "0"));
		sell.put("9", this.selling.getString("still-water", "0"));
		buy.put("10", this.buying.getString("lava", "0"));
		sell.put("10", this.selling.getString("lava", "0"));
		buy.put("11", this.buying.getString("still-lava", "0"));
		sell.put("11", this.selling.getString("still-lava", "0"));
		buy.put("12", this.buying.getString("sand", "0"));
		sell.put("12", this.selling.getString("sand", "0"));
		buy.put("13", this.buying.getString("gravel", "0"));
		sell.put("13", this.selling.getString("gravel", "0"));
		buy.put("14", this.buying.getString("gold-ore", "0"));
		sell.put("14", this.selling.getString("gold-ore", "0"));
		buy.put("15", this.buying.getString("iron-ore", "0"));
		sell.put("15", this.selling.getString("iron-ore", "0"));
		buy.put("16", this.buying.getString("coal-ore", "0"));
		sell.put("16", this.selling.getString("coal-ore", "0"));
		buy.put("17", this.buying.getString("log", "0"));
		sell.put("17", this.selling.getString("log", "0"));
		buy.put("18", this.buying.getString("leaves", "0"));
		sell.put("18", this.selling.getString("leaves", "0"));
		buy.put("19", this.buying.getString("sponge", "0"));
		sell.put("19", this.selling.getString("sponge", "0"));
		buy.put("20", this.buying.getString("glass", "0"));
		sell.put("20", this.selling.getString("glass", "0"));
		buy.put("36", this.buying.getString("white-cloth", "0"));
		sell.put("36", this.selling.getString("white-cloth", "0"));
		buy.put("37", this.buying.getString("yellow-flower", "0"));
		sell.put("37", this.selling.getString("yellow-flower", "0"));
		buy.put("38", this.buying.getString("red-rose", "0"));
		sell.put("38", this.selling.getString("red-rose", "0"));
		buy.put("39", this.buying.getString("brown-mushroom", "0"));
		sell.put("39", this.selling.getString("brown-mushroom", "0"));
		buy.put("40", this.buying.getString("red-mushroom", "0"));
		sell.put("40", this.selling.getString("red-mushroom", "0"));
		buy.put("41", this.buying.getString("gold-block", "0"));
		sell.put("41", this.selling.getString("gold-block", "0"));
		buy.put("42", this.buying.getString("iron-block", "0"));
		sell.put("42", this.selling.getString("iron-block", "0"));
		buy.put("43", this.buying.getString("double-step", "0"));
		sell.put("43", this.selling.getString("double-step", "0"));
		buy.put("44", this.buying.getString("step", "0"));
		sell.put("44", this.selling.getString("step", "0"));
		buy.put("45", this.buying.getString("brick", "0"));
		sell.put("45", this.selling.getString("brick", "0"));
		buy.put("46", this.buying.getString("tnt", "0"));
		sell.put("46", this.selling.getString("tnt", "0"));
		buy.put("47", this.buying.getString("bookcase", "0"));
		sell.put("47", this.selling.getString("bookcase", "0"));
		buy.put("48", this.buying.getString("mossy-cobblestone", "0"));
		sell.put("48", this.selling.getString("mossy-cobblestone", "0"));
		buy.put("49", this.buying.getString("obsidian", "0"));
		sell.put("49", this.selling.getString("obsidian", "0"));
		buy.put("50", this.buying.getString("torch", "0"));
		sell.put("50", this.selling.getString("torch", "0"));
		buy.put("51", this.buying.getString("fire", "0"));
		sell.put("51", this.selling.getString("fire", "0"));
		buy.put("52", this.buying.getString("mob-spawner", "0"));
		sell.put("52", this.selling.getString("mob-spawner", "0"));
		buy.put("53", this.buying.getString("wooden-stairs", "0"));
		sell.put("53", this.selling.getString("wooden-stairs", "0"));
		buy.put("54", this.buying.getString("chest", "0"));
		sell.put("54", this.selling.getString("chest", "0"));
		buy.put("55", this.buying.getString("redstone-wire", "0"));
		sell.put("55", this.selling.getString("redstone-wire", "0"));
		buy.put("56", this.buying.getString("diamond-ore", "0"));
		sell.put("56", this.selling.getString("diamond-ore", "0"));
		buy.put("57", this.buying.getString("diamond-block", "0"));
		sell.put("57", this.selling.getString("diamond-block", "0"));
		buy.put("58", this.buying.getString("workbench", "0"));
		sell.put("58", this.selling.getString("workbench", "0"));
		buy.put("59", this.buying.getString("crops", "0"));
		sell.put("59", this.selling.getString("crops", "0"));
		buy.put("60", this.buying.getString("soil", "0"));
		sell.put("60", this.selling.getString("soil", "0"));
		buy.put("61", this.buying.getString("furnace", "0"));
		sell.put("61", this.selling.getString("furnace", "0"));
		buy.put("62", this.buying.getString("burning-furnace", "0"));
		sell.put("62", this.selling.getString("burning-furnace", "0"));
		buy.put("63", this.buying.getString("sign-post", "0"));
		sell.put("63", this.selling.getString("sign-post", "0"));
		buy.put("64", this.buying.getString("wooden-door", "0"));
		sell.put("64", this.selling.getString("wooden-door", "0"));
		buy.put("65", this.buying.getString("ladder", "0"));
		sell.put("65", this.selling.getString("ladder", "0"));
		buy.put("66", this.buying.getString("mine-cart-tracks", "0"));
		sell.put("66", this.selling.getString("mine-cart-tracks", "0"));
		buy.put("67", this.buying.getString("cobblestone-stairs", "0"));
		sell.put("67", this.selling.getString("cobblestone-stairs", "0"));
		buy.put("68", this.buying.getString("wall-sign", "0"));
		sell.put("68", this.selling.getString("wall-sign", "0"));
		buy.put("69", this.buying.getString("lever", "0"));
		sell.put("69", this.selling.getString("lever", "0"));
		buy.put("70", this.buying.getString("stone-pressure-plate", "0"));
		sell.put("70", this.selling.getString("stone-pressure-plate", "0"));
		buy.put("71", this.buying.getString("iron-door", "0"));
		sell.put("71", this.selling.getString("iron-door", "0"));
		buy.put("72", this.buying.getString("wooden-pressure-plate", "0"));
		sell.put("72", this.selling.getString("wooden-pressure-plate", "0"));
		buy.put("73", this.buying.getString("redstone-ore", "0"));
		sell.put("73", this.selling.getString("redstone-ore", "0"));
		buy.put("74", this.buying.getString("glowing-redstone-ore", "0"));
		sell.put("74", this.selling.getString("glowing-redstone-ore", "0"));
		buy.put("75", this.buying.getString("redstone-torch-off", "0"));
		sell.put("75", this.selling.getString("redstone-torch-off", "0"));
		buy.put("76", this.buying.getString("redstone-torch-on", "0"));
		sell.put("76", this.selling.getString("redstone-torch-on", "0"));
		buy.put("77", this.buying.getString("stone-button", "0"));
		sell.put("77", this.selling.getString("stone-button", "0"));
		buy.put("78", this.buying.getString("snow", "0"));
		sell.put("78", this.selling.getString("snow", "0"));
		buy.put("79", this.buying.getString("ice", "0"));
		sell.put("79", this.selling.getString("ice", "0"));
		buy.put("80", this.buying.getString("snow-block", "0"));
		sell.put("80", this.selling.getString("snow-block", "0"));
		buy.put("81", this.buying.getString("cactus", "0"));
		sell.put("81", this.selling.getString("cactus", "0"));
		buy.put("82", this.buying.getString("clay", "0"));
		sell.put("82", this.selling.getString("clay", "0"));
		buy.put("83", this.buying.getString("reed", "0"));
		sell.put("83", this.selling.getString("reed", "0"));
		buy.put("84", this.buying.getString("jukebox", "0"));
		sell.put("84", this.selling.getString("jukebox", "0"));
		buy.put("85", this.buying.getString("fence", "0"));
		sell.put("85", this.selling.getString("fence", "0"));
		buy.put("256", this.buying.getString("iron-spade", "0"));
		sell.put("256", this.selling.getString("iron-spade", "0"));
		buy.put("257", this.buying.getString("iron-pickaxe", "0"));
		sell.put("257", this.selling.getString("iron-pickaxe", "0"));
		buy.put("258", this.buying.getString("iron-axe", "0"));
		sell.put("258", this.selling.getString("iron-axe", "0"));
		buy.put("259", this.buying.getString("steel-and-flint", "0"));
		sell.put("259", this.selling.getString("steel-and-flint", "0"));
		buy.put("260", this.buying.getString("apple", "0"));
		sell.put("260", this.selling.getString("apple", "0"));
		buy.put("261", this.buying.getString("bow", "0"));
		sell.put("261", this.selling.getString("bow", "0"));
		buy.put("262", this.buying.getString("arrow", "0"));
		sell.put("262", this.selling.getString("arrow", "0"));
		buy.put("263", this.buying.getString("coal", "0"));
		sell.put("263", this.selling.getString("coal", "0"));
		buy.put("264", this.buying.getString("diamond", "0"));
		sell.put("264", this.selling.getString("diamond", "0"));
		buy.put("265", this.buying.getString("iron-ingot", "0"));
		sell.put("265", this.selling.getString("iron-ingot", "0"));
		buy.put("266", this.buying.getString("gold-ingot", "0"));
		sell.put("266", this.selling.getString("gold-ingot", "0"));
		buy.put("267", this.buying.getString("iron-sword", "0"));
		sell.put("267", this.selling.getString("iron-sword", "0"));
		buy.put("268", this.buying.getString("wooden-sword", "0"));
		sell.put("268", this.selling.getString("wooden-sword", "0"));
		buy.put("269", this.buying.getString("wooden-spade", "0"));
		sell.put("269", this.selling.getString("wooden-spade", "0"));
		buy.put("270", this.buying.getString("wooden-pickaxe", "0"));
		sell.put("270", this.selling.getString("wooden-pickaxe", "0"));
		buy.put("271", this.buying.getString("wooden-axe", "0"));
		sell.put("271", this.selling.getString("wooden-axe", "0"));
		buy.put("272", this.buying.getString("stone-sword", "0"));
		sell.put("272", this.selling.getString("stone-sword", "0"));
		buy.put("273", this.buying.getString("stone-spade", "0"));
		sell.put("273", this.selling.getString("stone-spade", "0"));
		buy.put("274", this.buying.getString("stone-pickaxe", "0"));
		sell.put("274", this.selling.getString("stone-pickaxe", "0"));
		buy.put("275", this.buying.getString("stone-axe", "0"));
		sell.put("275", this.selling.getString("stone-axe", "0"));
		buy.put("276", this.buying.getString("diamond-sword", "0"));
		sell.put("276", this.selling.getString("diamond-sword", "0"));
		buy.put("277", this.buying.getString("diamond-spade", "0"));
		sell.put("277", this.selling.getString("diamond-spade", "0"));
		buy.put("278", this.buying.getString("diamond-pickaxe", "0"));
		sell.put("278", this.selling.getString("diamond-pickaxe", "0"));
		buy.put("279", this.buying.getString("diamond-axe", "0"));
		sell.put("279", this.selling.getString("diamond-axe", "0"));
		buy.put("280", this.buying.getString("stick", "0"));
		sell.put("280", this.selling.getString("stick", "0"));
		buy.put("281", this.buying.getString("bowl", "0"));
		sell.put("281", this.selling.getString("bowl", "0"));
		buy.put("282", this.buying.getString("mushroom-soup", "0"));
		sell.put("282", this.selling.getString("mushroom-soup", "0"));
		buy.put("283", this.buying.getString("gold-sword", "0"));
		sell.put("283", this.selling.getString("gold-sword", "0"));
		buy.put("284", this.buying.getString("gold-spade", "0"));
		sell.put("284", this.selling.getString("gold-spade", "0"));
		buy.put("285", this.buying.getString("gold-pickaxe", "0"));
		sell.put("285", this.selling.getString("gold-pickaxe", "0"));
		buy.put("286", this.buying.getString("gold-axe", "0"));
		sell.put("286", this.selling.getString("gold-axe", "0"));
		buy.put("287", this.buying.getString("string", "0"));
		sell.put("287", this.selling.getString("string", "0"));
		buy.put("288", this.buying.getString("feather", "0"));
		sell.put("288", this.selling.getString("feather", "0"));
		buy.put("289", this.buying.getString("gunpowder", "0"));
		sell.put("289", this.selling.getString("gunpowder", "0"));
		buy.put("290", this.buying.getString("wooden-hoe", "0"));
		sell.put("290", this.selling.getString("wooden-hoe", "0"));
		buy.put("291", this.buying.getString("stone-hoe", "0"));
		sell.put("291", this.selling.getString("stone-hoe", "0"));
		buy.put("292", this.buying.getString("iron-hoe", "0"));
		sell.put("292", this.selling.getString("iron-hoe", "0"));
		buy.put("293", this.buying.getString("diamond-hoe", "0"));
		sell.put("293", this.selling.getString("diamond-hoe", "0"));
		buy.put("294", this.buying.getString("gold-hoe", "0"));
		sell.put("294", this.selling.getString("gold-hoe", "0"));
		buy.put("295", this.buying.getString("seeds", "0"));
		sell.put("295", this.selling.getString("seeds", "0"));
		buy.put("296", this.buying.getString("wheat", "0"));
		sell.put("296", this.selling.getString("wheat", "0"));
		buy.put("297", this.buying.getString("bread", "0"));
		sell.put("297", this.selling.getString("bread", "0"));
		buy.put("298", this.buying.getString("leather-helmet", "0"));
		sell.put("298", this.selling.getString("leather-helmet", "0"));
		buy.put("299", this.buying.getString("leather-chestplate", "0"));
		sell.put("299", this.selling.getString("leather-chestplate", "0"));
		buy.put("300", this.buying.getString("leather-pants", "0"));
		sell.put("300", this.selling.getString("leather-pants", "0"));
		buy.put("301", this.buying.getString("leather-boots", "0"));
		sell.put("301", this.selling.getString("leather-boots", "0"));
		buy.put("302", this.buying.getString("chainmail-helmet", "0"));
		sell.put("302", this.selling.getString("chainmail-helmet", "0"));
		buy.put("303", this.buying.getString("chainmail-chestplate", "0"));
		sell.put("303", this.selling.getString("chainmail-chestplate", "0"));
		buy.put("304", this.buying.getString("chainmail-pants", "0"));
		sell.put("304", this.selling.getString("chainmail-pants", "0"));
		buy.put("305", this.buying.getString("chainmail-boots", "0"));
		sell.put("305", this.selling.getString("chainmail-boots", "0"));
		buy.put("306", this.buying.getString("iron-helmet", "0"));
		sell.put("306", this.selling.getString("iron-helmet", "0"));
		buy.put("307", this.buying.getString("iron-chestplate", "0"));
		sell.put("307", this.selling.getString("iron-chestplate", "0"));
		buy.put("308", this.buying.getString("iron-pants", "0"));
		sell.put("308", this.selling.getString("iron-pants", "0"));
		buy.put("309", this.buying.getString("iron-boots", "0"));
		sell.put("309", this.selling.getString("iron-boots", "0"));
		buy.put("310", this.buying.getString("diamond-helmet", "0"));
		sell.put("310", this.selling.getString("diamond-helmet", "0"));
		buy.put("311", this.buying.getString("diamond-chestplate", "0"));
		sell.put("311", this.selling.getString("diamond-chestplate", "0"));
		buy.put("312", this.buying.getString("diamond-pants", "0"));
		sell.put("312", this.selling.getString("diamond-pants", "0"));
		buy.put("313", this.buying.getString("diamond-boots", "0"));
		sell.put("313", this.selling.getString("diamond-boots", "0"));
		buy.put("314", this.buying.getString("gold-helmet", "0"));
		sell.put("314", this.selling.getString("gold-helmet", "0"));
		buy.put("315", this.buying.getString("gold-chestplate", "0"));
		sell.put("315", this.selling.getString("gold-chestplate", "0"));
		buy.put("316", this.buying.getString("gold-pants", "0"));
		sell.put("316", this.selling.getString("gold-pants", "0"));
		buy.put("317", this.buying.getString("gold-boots", "0"));
		sell.put("317", this.selling.getString("gold-boots", "0"));
		buy.put("318", this.buying.getString("flint", "0"));
		sell.put("318", this.selling.getString("flint", "0"));
		buy.put("319", this.buying.getString("pork", "0"));
		sell.put("319", this.selling.getString("pork", "0"));
		buy.put("320", this.buying.getString("grilled-pork", "0"));
		sell.put("320", this.selling.getString("grilled-pork", "0"));
		buy.put("321", this.buying.getString("painting", "0"));
		sell.put("321", this.selling.getString("painting", "0"));
		buy.put("322", this.buying.getString("golden-apple", "0"));
		sell.put("322", this.selling.getString("golden-apple", "0"));
		buy.put("323", this.buying.getString("sign", "0"));
		sell.put("323", this.selling.getString("sign", "0"));
		buy.put("324", this.buying.getString("wooden-door", "0"));
		sell.put("324", this.selling.getString("wooden-door", "0"));
		buy.put("325", this.buying.getString("bucket", "0"));
		sell.put("325", this.selling.getString("bucket", "0"));
		buy.put("326", this.buying.getString("water-bucket", "0"));
		sell.put("326", this.selling.getString("water-bucket", "0"));
		buy.put("327", this.buying.getString("lava-bucket", "0"));
		sell.put("327", this.selling.getString("lava-bucket", "0"));
		buy.put("328", this.buying.getString("mine-cart", "0"));
		sell.put("328", this.selling.getString("mine-cart", "0"));
		buy.put("329", this.buying.getString("saddle", "0"));
		sell.put("329", this.selling.getString("saddle", "0"));
		buy.put("330", this.buying.getString("iron-door", "0"));
		sell.put("330", this.selling.getString("iron-door", "0"));
		buy.put("331", this.buying.getString("redstone", "0"));
		sell.put("331", this.selling.getString("redstone", "0"));
		buy.put("332", this.buying.getString("snowball", "0"));
		sell.put("332", this.selling.getString("snowball", "0"));
		buy.put("333", this.buying.getString("boat", "0"));
		sell.put("333", this.selling.getString("boat", "0"));
		buy.put("334", this.buying.getString("leather", "0"));
		sell.put("334", this.selling.getString("leather", "0"));
		buy.put("335", this.buying.getString("milk-bucket", "0"));
		sell.put("335", this.selling.getString("milk-bucket", "0"));
		buy.put("336", this.buying.getString("clay-brick", "0"));
		sell.put("336", this.selling.getString("clay-brick", "0"));
		buy.put("337", this.buying.getString("clay-balls", "0"));
		sell.put("337", this.selling.getString("clay-balls", "0"));
		buy.put("338", this.buying.getString("reed", "0"));
		sell.put("338", this.selling.getString("reed", "0"));
		buy.put("339", this.buying.getString("paper", "0"));
		sell.put("339", this.selling.getString("paper", "0"));
		buy.put("340", this.buying.getString("book", "0"));
		sell.put("340", this.selling.getString("book", "0"));
		buy.put("341", this.buying.getString("slime-ball", "0"));
		sell.put("341", this.selling.getString("slime-ball", "0"));
		buy.put("342", this.buying.getString("storage-mine-cart", "0"));
		sell.put("342", this.selling.getString("storage-mine-cart", "0"));
		buy.put("343", this.buying.getString("powered-mine-cart", "0"));
		sell.put("343", this.selling.getString("powered-mine-cart", "0"));
		buy.put("344", this.buying.getString("egg", "0"));
		sell.put("344", this.selling.getString("egg", "0"));
		buy.put("345", this.buying.getString("compass", "0"));
		sell.put("345", this.selling.getString("compass", "0"));
		buy.put("346", this.buying.getString("fishing-rod", "0"));
		sell.put("346", this.selling.getString("fishing-rod", "0"));
		buy.put("2556", this.buying.getString("gold-record", "0"));
		sell.put("2556", this.selling.getString("gold-record", "0"));
		buy.put("2557", this.buying.getString("green-record", "0"));
		sell.put("2557", this.selling.getString("green-record", "0"));

		// Setup Listing
		this.rankedList = new LinkedList();

		// Setup Timers
		this.mTime1 = new Timer();
		this.mTime2 = new Timer();

		// Start the ticking for giving
		if (this.moneyGiveInterval > 0){
			this.mTime1.schedule(new TimerTask() {
				public void run() {
					etc.getInstance(); List<Player> localList = etc.getServer().getPlayerList();
					for (Player localPlayer : localList){
						iConomy.this.deposit(null, localPlayer, iConomy.this.moneyGive, false);
					}
				}
			}, 0L, this.moneyGiveInterval);
		}

		// Start the ticking for taking
		if (this.moneyTakeInterval > 0){
			this.mTime2.schedule(new TimerTask() {
				public void run() {
					etc.getInstance(); List<Player> localList = etc.getServer().getPlayerList();
					for (Player localPlayer : localList){
						iConomy.this.debit(null, localPlayer, iConomy.this.moneyTake, false);
					}
				}
			}, 0L, this.moneyTakeInterval);
		}

		return true;
	}

	public static String logDate() {
	    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date now = new Date();
	    return format.format(now);
	}

	public void shopLog(String type, String data) {
	   String date = logDate();
	   if(type.equalsIgnoreCase("buy") && this.logBuy) {
		try{
			FileWriter fstream = new FileWriter("iConomy-iBuy.log", true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(date + "|" + data);
			out.newLine();
			out.close();
		}catch (Exception es){
			log.severe("[iCurrency Buy Logging] " + es.getMessage());
		}
	   } else if(type.equalsIgnoreCase("sell") && this.logSell) {
		try{
			FileWriter fstream = new FileWriter("iConomy-iSell.log", true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(date + "|" + data);
			out.newLine();
			out.close();
		}catch (Exception es){
			log.severe("[iCurrency Sell Logging] " + es.getMessage());
		}
	   }
	}

	public static String cInt(int i) {
		return Integer.toString(i);
	}

	public int itemNeedsAmount(String type, String itemId) {
	    String info = "";

	    if(type.equals("buy")){
		info = this.buy.get(itemId);
	    } else if(type.equals("sell")) {
		info = this.sell.get(itemId);
	    }

	    if(info.equals("")) {
		return 0;
	    }

	    if(info.contains(",")) {
		String[] item = info.split(",");
		return Integer.parseInt(item[0]);
	    } else {
		return 1;
	    }
	}

	public boolean itemCan(String type, String itemId, int amount) {
	    int itemAmount = this.itemNeedsAmount(type, itemId);

	    // Maximum
	    if(amount > 6400) {
		return false;
	    }

	    if(itemAmount == 0) {
		return false;
	    } else if(itemAmount > 1) {
		if((amount%itemAmount) == 0) {
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

	    if(type.equals("buy")){
		info = this.buy.get(itemId);
	    } else if(type.equals("sell")) {
		info = this.sell.get(itemId);
	    }

	    if(info.equals("")) {
		return 0;
	    }

	    if(info.contains(",")) {
		String[] item = info.split(",");
		int itemAmount = Integer.parseInt(item[0]);
		int itemCost = Integer.parseInt(item[1]);

		// Check if we can
		if(!this.itemCan(type, itemId, amount)) {
		    return 0;
		}

		if(total) {
		    return itemCost*(amount/itemAmount);
		} else {
		    return itemCost;
		}
	    } else {
		if(total) {
		    return Integer.parseInt(info)*amount;
		} else {
		    return Integer.parseInt(info);
		}
	    }
	}

	public boolean doPurchase(Player player, int itemId, int amount) {
		int itemAmount = this.itemCost("buy", cInt(itemId), amount, false);
		int needsAmount = this.itemNeedsAmount("buy", cInt(itemId));

		if(!this.itemCan("buy",cInt(itemId),amount)){
		    player.sendMessage(Colors.Rose + String.format(this.buyInvalidAmount, needsAmount));
		    log.info("[iConomy Shop] " + "Player "+ player.getName() +" attempted to buy bundle ["+ itemId +"] with the offset amount [" + amount + "].");
		    this.shopLog("buy", player.getName() + "|0|202|"+ itemId +"|"+amount);
		    return true;
		}

		if(itemAmount != 0) {
			int total = this.itemCost("buy", cInt(itemId), amount, true);
			String totalAmount = total + this.moneyName;

			if(this.data.getBalance(player.getName())<total){
			    player.sendMessage(Colors.Rose + this.buyNotEnough);
			    return true;
			}

			// Take dat money!
			this.debit(null, player, total, true);

			// Give dat item!
			player.giveItem(itemId, amount);

			// Send Message
			player.sendMessage(Colors.Green + String.format(this.buyGive, totalAmount));
			log.info("[iConomy Shop] " + "Player "+ player.getName() +" bought item ["+ itemId +"] amount [" + amount + "] total ["+totalAmount+"].");
			this.shopLog("buy", player.getName() + "|1|200|"+ itemId +"|"+amount+"|"+total);
		} else {
			// Send Message
			player.sendMessage(Colors.Rose + this.buyReject);
			log.info("[iConomy Shop] " + "Player "+ player.getName() +" requested to buy an unavailable item: ["+ itemId +"].");
			this.shopLog("buy", player.getName() + "|0|201|"+ itemId);
			return true;
		}

		return false;
	}

	public boolean doSell(Player player, int itemId, int amount) {
		Inventory bag = player.getInventory();
		int needsAmount = this.itemNeedsAmount("sell", cInt(itemId));

		if(!this.itemCan("sell",cInt(itemId),amount)){
		    player.sendMessage(Colors.Rose + String.format(this.sellInvalidAmount, needsAmount));
		    log.info("[iConomy Shop] " + "Player "+ player.getName() +" attempted to sell bundle ["+ itemId +"] with the offset amount [" + amount + "].");
		    this.shopLog("sell", player.getName() + "|0|203|" + itemId + "|" + amount);
		    return true;
		}

		if(bag.hasItem(itemId, amount, 6400)){
			int itemAmount = this.itemCost("sell", cInt(itemId), amount, false);

			if(itemAmount != 0){
				int amt = amount;

				while (amt > 0) {
				    bag.removeItem(new Item(itemId, (amt > 64 ? 64 : amt)));
				    amt -= 64;
				}

				bag.updateInventory();

				// Total
				int total = this.itemCost("sell", cInt(itemId), amount, true);
				String totalAmount = total + this.moneyName;

				// Take dat money!
				this.deposit(null, player, total, true);

				// Send Message
				player.sendMessage(Colors.Green + String.format(this.sellGive, totalAmount));

				log.info("[iConomy Shop] " + "Player "+ player.getName() +" sold item ["+ itemId +"] amount [" + amount + "] total ["+totalAmount+"].");
				this.shopLog("sell", player.getName() + "|1|200|" + itemId + "|" + amount + "|" + total);
				return true;
			} else {
				// Send Message
				player.sendMessage(Colors.Rose + this.sellReject);
				log.info("[iConomy Shop] " + "Player "+ player.getName() +" requested to sell an unsellable item: ["+ itemId +"].");
				this.shopLog("sell", player.getName() + "|0|201|"+ itemId);
				return true;
			}
		} else {
			// Send Message
			player.sendMessage(Colors.Rose + this.sellNone);
			log.info("[iConomy Shop] " + "Player "+ player.getName() +" attempted to sell itemId ["+ itemId +"] but had none.");
			this.shopLog("sell", player.getName() + "|0|202|"+ itemId);
			return true;
		}
    }

	/**
	* Gives money to player 2 idk what the really boolean is all about.
	*/
	public void deposit(Player player1, Player player2, int amount, boolean really){
		String pdata = player2.getName();
		int i = this.data.getBalance(pdata);
		i += amount;
		this.data.setBalance(pdata, i);

		if (really){
			player2.sendMessage(Colors.Green + "You received " + amount + this.moneyName);
			showBalance(player2, null, true);

			if (player1 != null){
				player1.sendMessage(Colors.Green + amount + this.moneyName + " deposited into " + player2.getName() + "'s account");
			}

		}

		updateState(player2, true);
	}

	/**
	* Takes money from player 2
	* really = ticker usage
	*/
	public void debit(Player player1, Player player2, int amount, boolean really) {
		String pdata = player2.getName();
		int i = this.data.getBalance(pdata);

		if (amount > i) {
			amount = i;
		}

		i -= amount;
		this.data.setBalance(pdata, i);

		if (really) {
			player2.sendMessage(Colors.Green + amount + this.moneyName + " was deducted from your account.");
			showBalance(player2, null, true);

			if (player1 != null){
				player1.sendMessage(Colors.Green + amount + this.moneyName + " removed from " + player2.getName() + "'s account");
			}

		}

		updateState(player2, true);
	}

	/**
	* Takes money from player1, and gives it to player2 determined by amount given.
	*/
	public void pay(Player player1, Player player2, int amount) {
		// Playerdata
		String pdata1 = player1.getName();
		String pdata2 = player2.getName();

		// Balances
		int i = this.data.getBalance(pdata1);
		int j = this.data.getBalance(pdata2);

		if (player1.getName().equals(player2.getName())) {
			player1.sendMessage(Colors.Rose + "You cannot send yourself money");
		} else if (amount > i) {
			player1.sendMessage(Colors.Rose + "You do not have enough money.");
		} else {
			// Update player one balance
			i -= amount;
			this.data.setBalance(pdata1, i);

			// Update player two balance
			j += amount;
			this.data.setBalance(pdata2, j);

			// Send messages
			player1.sendMessage(Colors.Green + "You have sent " + amount + this.moneyName + " to " + pdata2);
			player2.sendMessage(Colors.Green + pdata1 + " has sent you " + amount + this.moneyName);

			// Show each balance
			showBalance(player1, null, true);
			showBalance(player2, null, true);

			// Update each players state
			updateState(player1, false);
			updateState(player2, true);
		}
	}

	// Return that shit yo :D
	public int getBalance(Player player) {
	    return this.data.getBalance(player.getName());
	}

	/**
	* Gets and displays rank of a player
	*
	* If not you @isMe is false, if you @isMe is true.
	*/
	public void showBalance(Player player, Player local, boolean isMe){
		int i = this.data.getBalance(player.getName());
		if(isMe) {
			player.sendMessage(Colors.Green + "Balance: " + i + this.moneyName);
		} else {
			local.sendMessage(Colors.Green + player.getName() + "'s Balance: " + i + this.moneyName);
		}
	}

	/**
	* Gets and displays rank of a player
	*
	* If not you isme is false, if you isme is true.
	*/
	public void rank(Player player, Player local, boolean isMe){
		if (!this.rankedList.contains(player.getName())){
			insertIntoRankedList(player.getName());
		}

		int i = this.rankedList.indexOf(player.getName()) + 1;

		if(isMe) {
			player.sendMessage(Colors.Green + "Your rank is " + i);
		} else {
			local.sendMessage(Colors.Green + player.getName() + " rank is " + i);
		}
	}

	/**
	* Iterates through the top amount of richest players
	*
	* Amount shown is determined by... well the amount given.
	*/
	public void top(Player player, int amount) {
		player.sendMessage(Colors.Green + "Top " + amount + " Richest People:");

		if(this.rankedList.size() < 1) {
			player.sendMessage(Colors.Green + "   Nobody Yet!");
		}

		if(amount > this.rankedList.size()) {
			amount = this.rankedList.size();
		}

		for (int i = 0; (i < amount) && (i < this.rankedList.size()); i++) {
			String rankedPlayer = (String)this.rankedList.get(i);
			int j = i + 1;

			// Send top players
			player.sendMessage(Colors.Green + "   " + j + ". " + rankedPlayer + " - " + this.data.getBalance(rankedPlayer) + this.moneyName);
		}
	}

	/**
	* Retrieves player data
	*/
	private Player getPlayer(String name){
		etc.getInstance(); return etc.getServer().getPlayer(name);
	}

	/**
	* Update list ranking
	*/
	private void insertIntoRankedList(String name){
		int i = this.data.getBalance(name);
		int j = 0;
		for (String player : this.rankedList)
		{
			if (i > this.data.getBalance(player)) {
				break;
			}
			j++;
		}

		this.rankedList.add(j, name);
	}

	public boolean can(Player player, String command) {
		if(command.equals("pay")) {
			if(!this.canPay.equals("*")) {
				String[] groups = this.canPay.split(",");
				for (String group : groups) {
					if(player.isInGroup(group)){
						return true;
					}
				}

				return false;
			}

			return true;
		} else if(command.equals("debit")) {
			if(!this.canDebit.equals("*")) {
				String[] groups = this.canDebit.split(",");
				for (String group : groups) {
					if(player.isInGroup(group)){
						return true;
					}
				}

				return false;
			}

			return true;
		} else if(command.equals("credit")) {
			if(!this.canCredit.equals("*")) {
				String[] groups = this.canCredit.split(",");
				for (String group : groups) {
					if(player.isInGroup(group)){
						return true;
					}
				}

				return false;
			}

			return true;
		} else if(command.equals("rank")) {
			if(!this.canRank.equals("*")) {
				String[] groups = this.canRank.split(",");
				for (String group : groups) {
					if(player.isInGroup(group)){
						return true;
					}
				}

				return false;
			}

			return true;
		} else if(command.equals("view")) {
			if(!this.canView.equals("*")) {
				String[] groups = this.canView.split(",");
				for (String group : groups) {
					if(player.isInGroup(group)){
						return true;
					}
				}

				return false;
			}

			return true;
		} else if(command.equals("top")) {
			if(!this.canTop.equals("*")) {
				String[] groups = this.canTop.split(",");
				for (String group : groups) {
					if(player.isInGroup(group)){
						return true;
					}
				}

				return false;
			}

			return true;
		} else if(command.equals("sell")) {
			if(!this.canSell.equals("*")) {
				String[] groups = this.canSell.split(",");
				for (String group : groups) {
					if(player.isInGroup(group)){
						return true;
					}
				}

				return false;
			}

			return true;
		} else if(command.equals("buy")) {
			if(!this.canBuy.equals("*")) {
				String[] groups = this.canBuy.split(",");
				for (String group : groups) {
					if(player.isInGroup(group)){
						return true;
					}
				}

				return false;
			}

			return true;
		}

		return false;
	}

	public class Listener extends PluginListener {
		iConomy p;

		Listener(iConomy plugin) {
			p = plugin;
		}

		public void onLogin(Player player) {
			updateState(player, true);
		}

		public boolean onCommand(Player player, String[] split) {
			if (!player.canUseCommand(split[0])) {
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
					p.showBalance(player, null, true);
					return true;
				}

				// Level 2, [player], top, rank
				if ((split.length < 3)) {
					if(split[1].equalsIgnoreCase("-p") || split[1].equalsIgnoreCase("pay")){
						//-------------------------------------------------------------
						// TIER 2 [PAY]
						//-------------------------------------------------------------
						if(!p.can(player, "pay")){
							return false;
						}

						player.sendMessage(Colors.Rose + "Invalid Usage: /money [-p|pay] <player> <amount>");
						return true;
					} else if(split[1].equalsIgnoreCase("-c") || split[1].equalsIgnoreCase("credit")){
						//-------------------------------------------------------------
						// TIER 2 [CREDIT]
						//-------------------------------------------------------------
						if(!p.can(player, "credit")){
							return false;
						}

						player.sendMessage(Colors.Rose + "Invalid Usage: /money [-c|credit] <player> <amount>");
						return true;
					} else if(split[1].equalsIgnoreCase("-d") || split[1].equalsIgnoreCase("debit")){
						//-------------------------------------------------------------
						// TIER 2 [DEBIT]
						//-------------------------------------------------------------
						if(!p.can(player, "debit")){
							return false;
						}

						player.sendMessage(Colors.Rose + "Invalid Usage: /money [-d|debit] <player> <amount>");
						return true;
					} else if(split[1].equalsIgnoreCase("-t") || split[1].equalsIgnoreCase("top")){
						//-------------------------------------------------------------
						// TIER 2 [TOP]
						//-------------------------------------------------------------
						if(!p.can(player, "top")){
							return false;
						}

						p.top(player, 5);
						return true;
					} else if(split[1].equalsIgnoreCase("-r") || split[1].equalsIgnoreCase("rank")){
						//-------------------------------------------------------------
						// TIER 2 [RANK]
						//-------------------------------------------------------------
						if(!p.can(player, "rank")){
							return false;
						}

						p.rank(player, null, true);
						return true;
					} else if(split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")){
						p.halp(player, "money");
						return true;
					} else {
						//-------------------------------------------------------------
						// TIER 2 [PLAYER MONEY CHECK]
						//-------------------------------------------------------------
						if(!p.can(player, "view")){
							return false;
						}

						localPlayer = p.getPlayer(split[1]);

						if (localPlayer == null) {
							player.sendMessage(Colors.Rose + "Invalid Usage: /money [player|command]");
							return true;
						}

						// Show another players balance
						p.showBalance(localPlayer, player, false);
						return true;
					}
				}

				// Level 3, top [amount], rank [amount], debit [amount] (self)
				if ((split.length < 4)) {
					if(split[1].equalsIgnoreCase("-p") || split[1].equalsIgnoreCase("pay")){
						//-------------------------------------------------------------
						// TIER 3 [PAY]
						//-------------------------------------------------------------
						if(!p.can(player, "pay")){
							return false;
						}

						player.sendMessage(Colors.Rose + "Invalid Usage: /money [-p|pay] <player> <amount>");
						return true;
					} else if(split[1].equalsIgnoreCase("-c") || split[1].equalsIgnoreCase("credit")){
						//-------------------------------------------------------------
						// TIER 3 [CREDIT]
						//-------------------------------------------------------------
						if(!p.can(player, "credit")){
							return false;
						}

						player.sendMessage(Colors.Rose + "Invalid Usage: /money [-c|credit] <player> <amount>");
						return true;
					} else if(split[1].equalsIgnoreCase("-d") || split[1].equalsIgnoreCase("debit")){
						//-------------------------------------------------------------
						// TIER 3 [DEBIT]
						//-------------------------------------------------------------
						if(!p.can(player, "debit")){
							return false;
						}

						i = 0;

						try {
							i = Integer.parseInt(split[2]);
							if (i < 1) { throw new NumberFormatException(); }
						} catch (NumberFormatException localNumberFormatException2) {
							player.sendMessage(Colors.Rose + "Invalid amount: " + i);
							player.sendMessage(Colors.Rose + "Usage: /money [-d|debit] <player> <amount>");
							return true;
						}

						// Show this amount!
						p.debit(player, player, Integer.parseInt(split[2]), true);
						return true;
					} else if(split[1].equalsIgnoreCase("-t") || split[1].equalsIgnoreCase("top")){
						//-------------------------------------------------------------
						// TIER 3 [TOP]
						//-------------------------------------------------------------
						if(!p.can(player, "top")){
							return false;
						}

						i = 0;

						try {
							i = Integer.parseInt(split[2]);
							if (i < 1) { throw new NumberFormatException(); }
						} catch (NumberFormatException localNumberFormatException2) {
							player.sendMessage(Colors.Rose + "Invalid amount: " + i);
							return true;
						}

						// Show this amount!
						p.top(player, i);
						return true;
					} else if(split[1].equalsIgnoreCase("-r") || split[1].equalsIgnoreCase("rank")){
						//-------------------------------------------------------------
						// TIER 3 [RANK]
						//-------------------------------------------------------------
						if(!p.can(player, "rank")){
							return false;
						}

						localPlayer = p.getPlayer(split[2]);

						if (localPlayer == null) {
							player.sendMessage(Colors.Rose + "Invalid Usage: /money [-r|rank] <player>");
							return true;
						}

						// Show another players rank
						p.rank(localPlayer, player, false);
						return true;
					} else if(split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")){
						p.halp(player, "money");
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
					if(split[1].equalsIgnoreCase("-p") || split[1].equalsIgnoreCase("pay")){
						//-------------------------------------------------------------
						// TIER 4 [PAY]
						//-------------------------------------------------------------
						if(!p.can(player, "pay")){
							return false;
						}

						localPlayer = p.getPlayer(split[2]);

						if (localPlayer == null){
							player.sendMessage(Colors.Rose + "Player not found: " + split[2]);
							return true;
						}

						i = 0;

						try {
							i = Integer.parseInt(split[3]);
							if (i < 1) { throw new NumberFormatException(); }
						} catch (NumberFormatException localNumberFormatException2) {
							player.sendMessage(Colors.Rose + "Invalid amount: " + i);
							player.sendMessage(Colors.Rose + "Usage: /money [-p|pay] <player> <amount>");
							return true;
						}

						// Pay amount
						p.pay(player, localPlayer, Integer.parseInt(split[3]));
						return true;
					} else if(split[1].equalsIgnoreCase("-c") || split[1].equalsIgnoreCase("credit")){
						//-------------------------------------------------------------
						// TIER 4 [CREDIT]
						//-------------------------------------------------------------
						if(!p.can(player, "credit")){
							return false;
						}

						localPlayer = p.getPlayer(split[2]);

						if (localPlayer == null){
							player.sendMessage(Colors.Rose + "Player not found: " + split[2]);
							return true;
						}

						i = 0;

						try {
							i = Integer.parseInt(split[3]);
							if (i < 1) { throw new NumberFormatException(); }
						} catch (NumberFormatException localNumberFormatException2) {
							player.sendMessage(Colors.Rose + "Invalid amount: " + i);
							player.sendMessage(Colors.Rose + "Usage: /money [-c|credit] <player> <amount>");
							return true;
						}

						// Credit amount
						p.deposit(player, localPlayer, Integer.parseInt(split[3]), true);
						return true;
					} else if(split[1].equalsIgnoreCase("-d") || split[1].equalsIgnoreCase("debit")){
						//-------------------------------------------------------------
						// TIER 4 [DEBIT]
						//-------------------------------------------------------------
						if(!p.can(player, "debit")){
							return false;
						}

						localPlayer = p.getPlayer(split[2]);

						if (localPlayer == null){
							player.sendMessage(Colors.Rose + "Player not found: " + split[2]);
							return true;
						}

						i = 0;

						try {
							i = Integer.parseInt(split[3]);
							if (i < 1) { throw new NumberFormatException(); }
						} catch (NumberFormatException localNumberFormatException2) {
							player.sendMessage(Colors.Rose + "Invalid amount: " + i);
							player.sendMessage(Colors.Rose + "Usage: /money [-d|debit] <player> <amount>");
							return true;
						}

						// Show this amount!
						p.debit(player, localPlayer, Integer.parseInt(split[3]), true);
						return true;
					} else if(split[1].equalsIgnoreCase("-t") || split[1].equalsIgnoreCase("top")){
						//-------------------------------------------------------------
						// TIER 4 [TOP]
						//-------------------------------------------------------------
						if(!p.can(player, "top")){
							return false;
						}

						i = 0;

						try {
							i = Integer.parseInt(split[2]);
							if (i < 1) { throw new NumberFormatException(); }
						} catch (NumberFormatException localNumberFormatException2) {
							player.sendMessage(Colors.Rose + "Invalid amount: " + i);
							player.sendMessage(Colors.Rose + "Usage: /money [-t|top] <amount>");
							return true;
						}

						// Show this amount!
						p.top(player, Integer.parseInt(split[2]));
						return true;
					} else if(split[1].equalsIgnoreCase("-r") || split[1].equalsIgnoreCase("rank")){
						//-------------------------------------------------------------
						// TIER 4 [RANK]
						//-------------------------------------------------------------
						if(!p.can(player, "rank")){
							return false;
						}

						localPlayer = p.getPlayer(split[2]);

						if (localPlayer == null) {
							player.sendMessage(Colors.Rose + "Invalid Usage: /money [-r|rank] <player>");
							return true;
						}

						// Show another players rank
						rank(localPlayer, player, false);
						return true;
					} else if(split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")){
						p.halp(player, "money");
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
			*	/shop <i> - Displays amount per 1 item
			*	/shop <i> <a> - Displays amount per <a> items
			*	/shop -b|buy <i> <a> - Purchase the item
			*	/shop -s|sell <i> <a> - Sell an item
			*/
			if (split[0].equalsIgnoreCase("/shop")) {
				// Level 1
				if ((split.length < 2)) {
					player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
					player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
					player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
					return true;
				}

				// Level 2
				if ((split.length < 3)) {
					if(split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")){
						p.halp(player, "shop");
						return true;
					} else {
					    int itemID = 0;

					    try {
						    itemID = Integer.parseInt(split[1]);
					    } catch (NumberFormatException n) {
						    itemID = etc.getDataSource().getItem(split[1]);
					    }

					    if(!Item.isValidItem(itemID)) {
						player.sendMessage(Colors.Rose + "Invalid item!");
						return true;
					    }

					    if(itemID != 0) {
						    int bNA = p.itemNeedsAmount("buy", cInt(itemID));
						    int sNA = p.itemNeedsAmount("sell", cInt(itemID));

						    int buying = p.itemCost("buy", cInt(itemID), bNA, false);
						    int selling = p.itemCost("sell", cInt(itemID), sNA, false);

						    if(buying != 0){
							    if(bNA > 1) {
								player.sendMessage(Colors.Green + "Must be bought in bundles of "+bNA+" for "+ buying + p.moneyName +".");
							    } else {
								player.sendMessage(Colors.Green + "Can be bought for "+ buying + p.moneyName +".");
							    }
						    } else {
							    player.sendMessage(Colors.Rose + "Currently not for purchasing.");
						    }

						    if(selling != 0){
							    if(sNA > 1) {
								player.sendMessage(Colors.Green + "Must be sold in bundles of "+sNA+" for "+ selling + p.moneyName +".");
							    } else {
								player.sendMessage(Colors.Green + "Can be sold for "+ selling + p.moneyName +".");
							    }
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
				      }
				}

				// Level 3
				if ((split.length < 4)) {
					if(split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")){
						p.halp(player, "shop");
						return true;
					} else if(split[1].equalsIgnoreCase("-b") || split[1].equalsIgnoreCase("buy")){
						int itemID = 0;

						if(!p.can(player, "buy")){
							return false;
						}

						try {
							itemID = Integer.parseInt(split[2]);
						} catch (NumberFormatException n) {
							itemID = etc.getDataSource().getItem(split[2]);
						}

						if(!Item.isValidItem(itemID)) {
						    player.sendMessage(Colors.Rose + "Invalid item!");
						    return true;
						}

						if(itemID != 0) {
							int buying = p.itemNeedsAmount("buy", cInt(itemID));

							if(buying != 0){
								if(buying > 1) {
								    p.doPurchase(player, itemID, buying);
								} else {
								    p.doPurchase(player, itemID, 1);
								}
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
					} else if(split[1].equalsIgnoreCase("-s") || split[1].equalsIgnoreCase("sell")){
						int itemID = 0;

						if(!p.can(player, "sell")){
							return false;
						}

						try {
							itemID = Integer.parseInt(split[2]);
						} catch (NumberFormatException n) {
							itemID = etc.getDataSource().getItem(split[2]);
						}

						if(!Item.isValidItem(itemID)) {
						    player.sendMessage(Colors.Rose + "Invalid item!");
						    return true;
						}

						if(itemID != 0) {
							int selling = p.itemNeedsAmount("sell", cInt(itemID));

							if(selling != 0){
								if(selling > 1) {
								    p.doSell(player, itemID, selling);
								} else {
								    p.doSell(player, itemID, 1);
								}
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
					} else if(split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")){
						p.halp(player, "shop");
						return true;
					} else {
						int itemID = 0;
						int amount = Integer.parseInt(split[2]);

						try {
							itemID = Integer.parseInt(split[1]);
						} catch (NumberFormatException n) {
							itemID = etc.getDataSource().getItem(split[1]);
						}

						if(!Item.isValidItem(itemID) || amount == 0) {
						    player.sendMessage(Colors.Rose + "Usage: /shop [command|item|itemID] [item] [amount]");
						    player.sendMessage(Colors.Rose + "    Commands: buy, sell, help");
						    player.sendMessage(Colors.Rose + "Alt-Commands: -b, -s, ?");
						    return true;
						}

						if(itemID != 0) {
							int bNA = p.itemNeedsAmount("buy", cInt(itemID));
							int sNA = p.itemNeedsAmount("sell", cInt(itemID));

							int buying = p.itemCost("buy", cInt(itemID), amount, false);
							int selling = p.itemCost("sell", cInt(itemID), amount, false);
							int totalBuying = p.itemCost("buy", cInt(itemID), amount, true);
							int totalSelling = p.itemCost("sell", cInt(itemID), amount, true);

							if(buying != 0){
								if(bNA > 1) {
								    player.sendMessage(Colors.Green + (amount/bNA) + " bundles will cost "+ totalBuying + p.moneyName +".");
								} else {
								    player.sendMessage(Colors.Green + amount + " will cost "+ totalBuying + p.moneyName +".");
								}
							} else {
								player.sendMessage(Colors.Rose + "Invalid amount or not for purchasing!");
							}

							if(selling != 0){
								if(sNA > 1) {
								    player.sendMessage(Colors.Green + (amount/sNA) + " bundles will sell for "+ totalSelling + p.moneyName +".");
								} else {
								    player.sendMessage(Colors.Green + amount + " can be sold for "+ totalSelling + p.moneyName +".");
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
					if(split[1].equalsIgnoreCase("-b") || split[1].equalsIgnoreCase("buy")){
						int itemID = 0;

						if(!p.can(player, "buy")){
							return false;
						}

						try {
							itemID = Integer.parseInt(split[2]);
						} catch (NumberFormatException n) {
							itemID = etc.getDataSource().getItem(split[2]);
						}

						if(!Item.isValidItem(itemID)) {
						    player.sendMessage(Colors.Rose + "Invalid item!");
						    return true;
						}

						int amount = Integer.parseInt(split[3]);

						if(amount < 0 || amount == 0) {
						    player.sendMessage(Colors.Rose + "Invalid amount!");
						    return true;
						}

						if(itemID != 0) {
							int buying = p.itemNeedsAmount("buy", cInt(itemID));

							if(buying != 0){
								p.doPurchase(player, itemID, amount);
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
					} else if(split[1].equalsIgnoreCase("-s") || split[1].equalsIgnoreCase("sell")){
						int itemID = 0;

						if(!p.can(player, "sell")){
							return false;
						}

						try {
							itemID = Integer.parseInt(split[2]);
						} catch (NumberFormatException n) {
							itemID = etc.getDataSource().getItem(split[2]);
						}

						if(!Item.isValidItem(itemID)) {
						    player.sendMessage(Colors.Rose + "Invalid item!");
						    return true;
						}

						int amount = Integer.parseInt(split[3]);

						if(amount < 0 || amount == 0) {
						    player.sendMessage(Colors.Rose + "Invalid amount!");
						    return true;
						}

						if(itemID != 0) {
							int selling = p.itemNeedsAmount("sell", cInt(itemID));

							if(selling != 0){
								p.doSell(player, itemID, amount);
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
					} else if(split[1].equalsIgnoreCase("?") || split[1].equalsIgnoreCase("help")){
						p.halp(player, "shop");
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
	}
}