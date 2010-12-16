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
 * Misc.java
 * <br /><br />
 * Miscellaneous functions / variables, and other things that really don't go anywhere else.
 *
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class Misc {

    public Misc() { }

    /**
     * Checks the length of an array against the amount plus one,
     * Allowing us to know the true amount of numbers.
     * <br /><br />
     * Example:
     * <blockquote><pre>
     * arguments({0,1,2}, 2); // < 4 length = [0,1,2] = true. It does end at 2.
     * </pre></blockquote>
     *
     * @param array The array we are checking the length of
     * @param amount The amount necessary to continue onwards.
     *
     * @return <code>Boolean</code> - True or false based on length.
     */

    public static Boolean arguments(String[] array, int amount) {
	return (array.length < (amount + 2)) ? true : false;
    }

    /**
     * Checks text against one variables.
     *
     * @param text The text that we were provided with.
     * @param against The first variable that needs to be checked against
     *
     * @return <code>Boolean</code> - True or false based on text.
     */
    public static Boolean is(String text, String against) {
	return (text.equalsIgnoreCase(against)) ? true : false;
    }

    /**
     * Checks text against two variables, if it equals at least one returns true.
     *
     * @param text The text that we were provided with.
     * @param against The first variable that needs to be checked against
     * @param or The second variable that it could possibly be.
     *
     * @return <code>Boolean</code> - True or false based on text.
     */
    public static Boolean isEither(String text, String against, String or) {
	return (text.equalsIgnoreCase(against) || text.equalsIgnoreCase(or)) ? true : false;
    }

    /**
     * Basic formatting on Currency
     *
     * @param Balance The player balance or amount being payed.
     * @param currency The iConomy currency name
     *
     * @return <code>String</code> - Formatted with commas & currency
     */
    public String formatCurrency(int Balance, String currency) {
	return insertCommas(String.valueOf(Balance)) + " " + currency;
    }

    /**
     * Basic formatting for commas.
     *
     * @param str The string we are attempting to format
     *
     * @return <code>String</code> - Formatted with commas
     */
    public static String insertCommas(String str) {
	if (str.length() < 4) {
	    return str;
	}

	return insertCommas(str.substring(0, str.length() - 3)) + "," + str.substring(str.length() - 3, str.length());
    }

    /**
     * Convert int to string
     *
     * @return <code>String</code>
     */
    public static String string(int i) {
	return String.valueOf(i);
    }

    /**
     * Get the player from the server
     */
    public static Player player(String name) {
	return etc.getServer().getPlayer(name);
    }

    /**
     * Get the player from the server (matched)
     */
    public static Player playerMatch(String name) {
	return etc.getServer().matchPlayer(name);
    }
}
