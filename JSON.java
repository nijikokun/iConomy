
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JSON
 *
 * Controls the log files, not an extensive JSON parser or anything. Simple quote exchange.
 *
 * @author Nijiko
 */
public class JSON {

    private String filename;
    private boolean log;

    public JSON(String filename, boolean log) {
	this.filename = filename;
	this.log = log;
    }

    /*
     * Gives the current date formatted nice and neat.
     */
    public static String date() {
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Date now = new Date();
	return format.format(now);
    }

    /*
     * Parses data to JSON format
     *
     * @param data The data to be written.
     */
    public String parse(String input) {
	return input.replace('\'', '"');
    }

    /*
     * Writes the data given out to the log file.
     *
     * @param data The data to be written.
     */
    public void write(String data) {
	if (log) {
	    try {
		FileWriter fstream = new FileWriter(iConomy.main_directory + iConomy.log_directory + filename, true);
		BufferedWriter out = new BufferedWriter(fstream);

		out.write(date() + "|" + parse(data));
		out.newLine();
		out.close();
	    } catch (Exception es) {
		iConomy.log.severe("[Logging] " + es.getMessage());
	    }
	}
    }
}
