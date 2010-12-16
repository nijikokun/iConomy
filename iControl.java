
import java.util.HashMap;

/**
 * iControl.java
 *
 * Permission handler
 *
 * @author Nijiko
 */
public class iControl {

    public static HashMap<String, String> permissions = new HashMap<String, String>();

    public iControl() {

    }

    public static void add(String controller, String groups) {
	permissions.put(controller, groups);
    }

    public static boolean permission(String controller, Player player) {
	if (!permissions.containsKey(controller)) {
	    return false;
	}

	String groups = permissions.get(controller);

	if (!groups.equals("*")) {
	    String[] groupies = groups.split(",");

	    for (String group : groupies) {
		if (player.isInGroup(group)) {
		    return true;
		}
	    }

	    return false;
	}

	return true;
    }
}
