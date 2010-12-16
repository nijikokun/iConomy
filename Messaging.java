/**
 * Messaging.java
 * <br /><br />
 * Lets us do fancy pantsy things with colors, messages, and broadcasting :D!
 *
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class Messaging {

    public static Player player = null;

    /**
     * Converts a list of arguments into points.
     *
     * @param original The original string necessary to convert inside of.
     * @param arguments The list of arguments, multiple arguments are seperated by commas for a single point.
     * @param points The point used to alter the argument.
     *
     * @return <code>String</code> - The parsed string after converting arguments to variables (points)
     */
    public static String argument(String original, String[] arguments, String[] points) {
       for (int i = 0; i < arguments.length; i++) {
	    if(arguments[i].contains(",")) {
		for(String arg : arguments[i].split(",")) {
		    original = original.replace(arg, points[i]);
		}
	    } else {
		original = original.replace(arguments[i], points[i]);
	   }
        }

       return original;
    }

    /**
     * Parses the original string against color specific codes. This one converts &[code] to §[code]
     * <br /><br />
     * Example:
     * <blockquote<pre>
     * Messaging.parse("Hello &2world!"); // returns: Hello §2world!
     * </pre></blockquote>
     *
     * @param original The original string used for conversions.
     *
     * @return <code>String</code> - The parsed string after conversion.
     */
    public static String parse(String original) {
	return original.replaceAll("(&([a-z0-9]))", "§$2").replace("\\\\\u00A7", "&");
    }

    /**
     * Converts color codes into the simoleon code. Sort of a HTML format color code tag.
     * <br /><br />
     * Color codes allowed: black, navy, green, teal, red, purple, gold, silver, gray, blue, lime, aqua, rose, pink, yellow, white.
     * <br /><br />
     * Example:
     * <blockquote<pre>
     * Messaging.colorize("Hello &lt;green>world!"); // returns: Hello §2world!
     * </pre></blockquote>
     *
     * @param original Original string to be parsed against group of color names.
     *
     * @return <code>String</code> - The parsed string after conversion.
     */
    public static String colorize(String original) {
	return original.replace("<black>", "§0").replace("<navy>", "§1").replace("<green>", "§2").replace("<teal>", "§3").replace("<red>", "§4").replace("<purple>", "§5").replace("<gold>", "§6").replace("<silver>", "§7").replace("<gray>", "§8").replace("<blue>", "§9").replace("<lime>", "§a").replace("<aqua>", "§b").replace("<rose>", "§c").replace("<pink>", "§d").replace("<yellow>", "§e").replace("<white>", "§f");
    }

    /**
     * Helper function to assist with making brackets. Why? Dunno, lazy.
     *
     * @param message The message inside of brackets.
     *
     * @return <code>String</code> - The message inside [brackets]
     */
    public static String bracketize(String message) {
	return "[" + message + "]";
    }

    /**
     * Save the player to be sent messages later. Ease of use sending messages.
     * <br /><br />
     * Example:
     * <blockquote><pre>
     * Messaging.save(player);
     * Messaging.send("This will go to the player saved.");
     * </pre></blockquote>
     *
     * @param player The player we wish to save for later.
     */
    public static void save(Player player) {
	Messaging.player = player;
    }

    /**
     * Sends a message to a specific player.
     * <br /><br />
     * Example:
     * <blockquote><pre>
     * Messaging.send(player, "This will go to the player saved.");
     * </pre></blockquote>
     *
     * @param player Player we are sending the message to.
     * @param message The message to be sent.
     */
    public static void send(Player player, String message) {
	player.sendMessage(parse(message));
    }

    /**
     * Sends a message to the stored player.
     *
     * @param message The message to be sent.
     * @see Messaging#save(Player)
     */
    public static void send(String message) {
	if(Messaging.player != null)
	    player.sendMessage(parse(message));
    }

    /**
     * Brodcast a message to every player online.
     *
     * @param message - The message to be sent.
     */
    public static void broadcast(String message) {
	for (Player p : etc.getServer().getPlayerList()) {
	    p.sendMessage(parse(message));
	}
    }
}
