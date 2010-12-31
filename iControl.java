import java.util.HashMap;

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
