import java.io.File;
import java.util.logging.Logger;

/*
 * iConomy v1.0 - Official `LightWeight` Version
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
public class iConomy extends Plugin {
    /*
     * Central Data pertaining directly to the plugin name & versioning.
     */
    public static String name = "iConomy";
    public static String version = "1.2";

    /**
     * Grab the logging system to attach to.
     */
    protected static final Logger log = Logger.getLogger("Minecraft");

    /**
     * Listener for the plugin system.
     */
    private static final iListen l = new iListen();

    /**
     * Controller for permissions and security.
     */
    public static iControl Watch = new iControl();

    /**
     * Things the controller needs to watch permissions for
     */
    private final String[] watching = { "access", "payment", "rank", "list", "deposit", "withdraw", "reset" };

    /**
     * Default settings for the permissions
     */
    private final String[] defaults = { "*", "*", "*", "*", "admins,", "admins,", "admins," };
    
    /*
     * Data locations
     */
    public static String temp_directory = "templates/";
    public static String main_directory = "iConomy/";
    public static String log_directory = "logs/";

    /**
     * Internal Data controller
     */
    public static iData i;

    /**
     * MySQL Query Controller
     */
    public static MySQL MySQL = new MySQL();

    /**
     * HTTP Connection, Version Check.
     */
    public static HTTP Version = new HTTP();

    /**
     * JSON Parser, Information Logging.
     */
    public static JSON Info;

    /**
     * Internal Properties controllers
     */
    public static iProperty Settings;

    /**
     * Template object
     */
    public static Template MoneyTPL;

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public static Misc Misc = new Misc();

    /*
     * Variables
     */
    public static String currency;
    public static int initialBalance;

    /*
     * Database default values, changed via settings file.
     */
    static boolean mysql = false;
    static String driver = "com.mysql.jdbc.Driver";
    static String user = "root";
    static String pass = "root";
    static String db = "jdbc:mysql://localhost:3306/minecraft";

    /**
     * Initialize the plugin
     */
    public void initialize() {
	String Check = Version.check();

	PluginLoader loader = etc.getLoader();
	loader.addListener(PluginLoader.Hook.SERVERCOMMAND, l, this, PluginListener.Priority.MEDIUM);
	loader.addListener(PluginLoader.Hook.COMMAND, l, this, PluginListener.Priority.MEDIUM);
	loader.addListener(PluginLoader.Hook.LOGIN, l, this, PluginListener.Priority.MEDIUM);
	loader.addListener(PluginLoader.Hook.DISCONNECT, l, this, PluginListener.Priority.MEDIUM);
	loader.addListener(PluginLoader.Hook.BAN, l, this, PluginListener.Priority.MEDIUM);
	loader.addListener(PluginLoader.Hook.IPBAN, l, this, PluginListener.Priority.MEDIUM);

	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " loaded");

	if(Check != null) {
	    if (!Check.equals("1")) {
		String[] data = Check.split(",");

		// iConomy release data, finally a place to put update info.
		log.info(Messaging.bracketize(name + " Update") + " version " + Messaging.bracketize(data[0]) + " has been released!");
		log.info(Messaging.bracketize(name + " Update") + " " + data[1]);
	    }
	}
    }

    /**
     * Enables data and sets up the necessary variables, objects, etc.
     */
    public void enable() {
	setup();
	setupTemplate();
	setupCommands();
	setupPermissions();
    }

    /**
     * Disable
     */
    public void disable() {

    }

    /**
     * Setup variables, directories, and data.
     */
    public void setup() {
	// Create directory if it doesn't exist.
	(new File(main_directory)).mkdir();
	(new File(main_directory + log_directory)).mkdir();
	(new File(main_directory + temp_directory)).mkdir();

	// File Data
	Settings = new iProperty(main_directory + "settings.properties");

	// Templating
	MoneyTPL = new Template("money.tpl");

	// Logging
	Info = new JSON("pay.log", Settings.getBoolean("log-pay", false));

	// String Variables
	currency = Settings.getString("money-name", "Coin");

	// Integer Variables
	initialBalance = Settings.getInt("starting-balance", 0);

	// MySQL & Flatfile check
	mysql = Settings.getBoolean("use-mysql", false);
	
	// MySQL Variables
	driver = Settings.getString("driver", "com.mysql.jdbc.Driver");
	user = Settings.getString("user", "root");
	pass = Settings.getString("pass", "root");
	db = Settings.getString("db", "jdbc:mysql://localhost:3306/minecraft");

	// internal Data
	iData.setup(mysql, initialBalance, driver, user, pass, db);
    }

    /**
     * Setup Template
     */
    public void setupTemplate() {
	MoneyTPL.raw("tag", "<green>[<white>Money<green>] ");
	MoneyTPL.raw("personal-balance", "<green>Balance: <white>+balance");
	MoneyTPL.raw("player-balance", "<green>+name's Balance: <white>+balance");
	MoneyTPL.raw("personal-rank", "<green>Current rank: <white>+rank");
	MoneyTPL.raw("player-rank", "<green>+name's rank: <white>+rank");
	MoneyTPL.raw("top-opening", "<green>Top <white>+amount<green> Richest Players:");
	MoneyTPL.raw("top-line", "<white>   +i.<green> +name <white>(<green>+balance<white>)");
	MoneyTPL.raw("top-empty", "<white>   Nobody yet!");
	MoneyTPL.raw("payment-to", "<green>You have sent <white>+amount<green> to <white>+name<green>.");
	MoneyTPL.raw("payment-from", "<white>+name<green> has sent you <white>+amount<green>.");
	MoneyTPL.raw("personal-reset", "<rose>Your account has been reset.");
	MoneyTPL.raw("player-reset", "<white>+name's <rose>account has been reset.");
	MoneyTPL.raw("personal-withdraw", "<rose>Your account had <white>+amount<rose> withdrawn.");
	MoneyTPL.raw("player-withdraw", "<white>+name's <rose>account had <white>+amount<rose> withdrawn.");
	MoneyTPL.raw("personal-deposited", "<white>+amount<green> was deposited into your account.");
	MoneyTPL.raw("player-deposited", "<white>+name's <green>account had <white>+amount<green> deposited into it.");
	MoneyTPL.raw("payment-self", "<rose>Sorry, you cannot send money to yourself.");
	MoneyTPL.raw("no-funds", "<rose>Sorry, you do not have enough funds to do that.");
	MoneyTPL.raw("no-account", "<rose>Player does not have account: <white>+name");
   }

    /**
     * Setup Commands
     */
    public void setupCommands() {

    }

    /**
     * Setup Permissions that need to be watched throughout the listener.
     */
    public void setupPermissions() {
	for(int x = 0; x < watching.length; x++) {
	    Watch.add(watching[x], Settings.getString("can-" + watching[x], defaults[x]));
	}
    }
}
