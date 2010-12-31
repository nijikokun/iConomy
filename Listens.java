import java.util.HashMap;

/**
 * Listens for hook requests / calls
 * 
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class Listens {
    static PluginInterface iSettings = new iSettings();
    static PluginInterface iBalance = new iBalance();
    static PluginInterface iLogging = new iLogging();
    static PluginInterface iTemplate = new iTemplate();
    static HashMap<String, JSON> Loggers = new HashMap<String, JSON>();
    static HashMap<String, Template> Templates = new HashMap<String, Template>();

    public Listens() { }

    public static class iSettings implements PluginInterface {

	public iSettings() { }

	public String getName() {
	    return "iSettings";
	}

	public int getNumParameters() {
	    return 1;
	}

	public String checkParameters(Object[] os) {
	    if(os.length < 1 || os.length > 1) {
		return "Invalid amount of parameters.";
	    } else {
		return null;
	    }
	}

	public Object run(Object[] os) {
	    String command = (String)os[0];

	    if(iConomy.debugging) {
		iConomy.log.info("[Hook: iSettings] Command: "+command);
	    }

	    if(command.equalsIgnoreCase("starting-balance")) {
		return iConomy.initialBalance;
	    }

	    if(command.equalsIgnoreCase("mysql")) {
		return iConomy.mysql;
	    }

	    if(command.equalsIgnoreCase("mysql-driver")) {
		return iConomy.driver;
	    }

	    if(command.equalsIgnoreCase("mysql-user")) {
		return iConomy.user;
	    }

	    if(command.equalsIgnoreCase("mysql-pass")) {
		return iConomy.pass;
	    }

	    if(command.equalsIgnoreCase("mysql-db")) {
		return iConomy.db;
	    }

	    if(command.equalsIgnoreCase("currency")) {
		return iConomy.currency;
	    }

	    if(command.equalsIgnoreCase("directory")) {
		return iConomy.main_directory;
	    }

	    if(command.equalsIgnoreCase("debugging")) {
		return iConomy.debugging;
	    }

	    if(command.equalsIgnoreCase("version")) {
		return iConomy.version;
	    }

	    if(command.equalsIgnoreCase("codename")) {
		return iConomy.codename;
	    }

	    if(command.equalsIgnoreCase("loaded")) {
		return true;
	    }

	    return false;
	}
    }

    public static class iLogging implements PluginInterface {
	public iLogging() { }

	public String getName() {
	    return "iLogging";
	}

	public int getNumParameters() {
	    return 3;
	}

	public String checkParameters(Object[] os) {
	    if(os.length < 1 || os.length > getNumParameters()) {
		return "Invalid amount of parameters.";
	    } else {
		return null;
	    }
	}

	public Object run(Object[] os) {
	    String command = (String)os[0];
	    String variable = (String)os[1];
	    String log = "";

	    if(os.length >= getNumParameters()) {
		log = (String)os[2];
	    }

	    if(iConomy.debugging) {
		iConomy.log.info("[Hook: iLogging] Command: "+command+", variable: "+variable);
	    }

	    if(command.equalsIgnoreCase("enabled")) {
		return iConomy.Logging.getBoolean("log-"+variable, false);
	    }

	    if(command.equalsIgnoreCase("enable")) {
		iConomy.Logging.setBoolean("log-"+variable, true);
	    }

	    if(command.equalsIgnoreCase("disable")) {
		iConomy.Logging.setBoolean("log-"+variable, true);
	    }

	    if(command.equalsIgnoreCase("setup")) {
		boolean enabled = iConomy.Logging.getBoolean("log-"+variable);
		JSON logger = new JSON(variable+".log", enabled);
		Listens.Loggers.put(variable, logger);
		logger.write(log);
	    }

	    if(command.equalsIgnoreCase("log")) {
		boolean enabled = iConomy.Logging.getBoolean("log-"+variable);
		if(enabled) {
		    if(Listens.Loggers == null || Listens.Loggers.isEmpty()) {
			JSON logger = new JSON(variable+".log", enabled);
			logger.write(log);
			Listens.Loggers.put(variable, logger);
		    }

		    if(Listens.Loggers.containsKey(variable)) {
			(Listens.Loggers.get(variable)).write(log);
		    } else {
			JSON logger = new JSON(variable+".log", enabled);
			Listens.Loggers.put(variable, logger);
			logger.write(log);
		    }
		} else {
		    return false;
		}
	    }

	    return false;
	}
    }

    public static class iTemplate implements PluginInterface {
	public iTemplate() { }

	public String getName() {
	    return "iTemplate";
	}

	public int getNumParameters() {
	    return 5;
	}

	public String checkParameters(Object[] os) {
	    if(os.length < 1 || os.length > getNumParameters()) {
		return "Invalid amount of parameters.";
	    } else {
		return null;
	    }
	}

	public Object run(Object[] os) {
	    String[] arguments = new String[]{};
	    String[] values = new String[]{};
	    String command = (String)os[0];
	    String variable = (String)os[1];
	    String key = "";
	    String value = "";

	    if(os.length >= getNumParameters()-2) {
		key = (String)os[2];
	    }

	    if(os.length >= getNumParameters()-1) {
		if(command.equalsIgnoreCase("set")) {
		    value = (String)os[3];
		} else if(command.equalsIgnoreCase("parse")) {
		    arguments = (String[])os[3];
		}
	    }

	    if(os.length >= getNumParameters()) {
		values = (String[])os[4];
	    }

	    if(iConomy.debugging) {
		iConomy.log.info("[Hook: iTemplate] Command: "+command+", template: "+variable+", key:"+key);
	    }

	    if(command.equalsIgnoreCase("set")) {
		if(Listens.Loggers.containsKey(variable)) {
		    (Listens.Templates.get(variable)).raw(key, value);
		} else {
		    Template template = new Template(variable + ".tpl");
		    Listens.Templates.put(variable, template);
		    template.raw(key, value);
		}
	    }

	    if(command.equalsIgnoreCase("parse")) {
		if(Listens.Loggers.containsKey(variable)) {
		    return (Listens.Templates.get(variable)).parse(key, arguments, values);
		} else {
		    Template template = new Template(variable + ".tpl");
		    Listens.Templates.put(variable, template);
		    return template.parse(key, arguments, values);
		}
	    }

	    if(command.equalsIgnoreCase("color")) {
		if(Listens.Loggers.containsKey(variable)) {
		    return (Listens.Templates.get(variable)).color(key);
		} else {
		    Template template = new Template(variable + ".tpl");
		    Listens.Templates.put(variable, template);
		    return template.color(key);
		}
	    }

	    if(command.equalsIgnoreCase("setup")) {
		Template template = new Template(variable + ".tpl");
		Listens.Templates.put(variable, template);
	    }

	    return false;
	}
    }

    public static class iBalance implements PluginInterface {

	public iBalance() { }

	public String getName() {
	    return "iBalance";
	}

	public int getNumParameters() {
	    return 3;
	}

	public String checkParameters(Object[] os) {
	    if(os.length < 1 || os.length > getNumParameters()) {
		return "Invalid amount of parameters.";
	    } else {
		return null;
	    }
	}

	public Object run(Object[] os) {
	    String command = (String)os[0];
	    String player = (String)os[1];
	    int amount = 0;

	    if(os.length >= getNumParameters()) {
		amount = (Integer)os[2];
	    }

	    if(iConomy.debugging) {
		iConomy.log.info("[Hook: iBalance] Command: "+command+", player: "+player+", amount: " + amount);
	    }

	    if(command.equalsIgnoreCase("balance")) {
		return iData.getBalance(player);
	    }

	    if(command.equalsIgnoreCase("show")) {
		if(etc.getServer().getPlayer(player) != null) {
		    int balance = iData.getBalance(player);
		    Messaging.send(
			etc.getServer().getPlayer(player),
			iConomy.MoneyTPL.color("tag") +
			iConomy.MoneyTPL.parse(
			    "personal-balance",
			    new String[]{ "+balance,+b" },
			    new String[]{ Misc.formatCurrency(balance, iConomy.currency) }
			)
		    );
		}
	    }

	    if(command.equalsIgnoreCase("check")) {
		return iData.hasBalance(player);
	    }

	    if(command.equalsIgnoreCase("set")) {
		if(amount < 0) {
		    return false;
		}

		iData.setBalance(player, amount);
		return true;
	    }

	    if(command.equalsIgnoreCase("deposit")) {
		if(amount < 0) {
		    return false;
		}

		int balance = (iData.getBalance(player) + amount);
		iData.setBalance(player, balance);
		return true;
	    }

	    if(command.equalsIgnoreCase("withdraw")) {
		if(amount < 0) {
		    return false;
		}

		int balance = iData.getBalance(player);

		if(amount > balance) {
		    amount = balance;
		}

		iData.setBalance(player, (balance-amount));
		return true;
	    }

	    return false;
	}
    }
}
