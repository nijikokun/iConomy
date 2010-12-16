import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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
