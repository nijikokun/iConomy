import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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
 * HTTP.java
 * <br /><br />
 * Currently controls any outside url connections. Currently it only checks for the latest version.
 * If a version is newer we tell the user.
 *
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class HTTP {

    public HTTP() {

    }

    public String check() {
	String content = null;

	// many of these calls can throw exceptions, so i've just
	// wrapped them all in one try/catch statement.
	try {
	    URL url = new URL("http://iconomy.nexua.org/?latest&current=" + iConomy.version);
	    URLConnection urlConnection = url.openConnection();
	    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

	    String line;

	    // read from the urlconnection via the bufferedreader
	    while ((line = bufferedReader.readLine()) != null) {
		content = line;
	    }

	    bufferedReader.close();
	} catch (Exception e) {
	    iConomy.log.severe("[Version Check] " + e);
	}

	return content;
    }
}
