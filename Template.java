/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nijiko
 */
public class Template {

    iProperty file;

    public Template(String filename) {
	this.file = new iProperty(iConomy.main_directory + iConomy.temp_directory + filename);
    }

    /**
     * Grab the raw template line by the key, and don't save anything.
     *
     * @param key The template key we wish to grab.
     *
     * @return <code>String</code> - Template line / string.
     */
    public String raw(String key) {
	return file.getString(key);
    }

    /**
     * Grab the raw template line and save data if no key existed.
     *
     * @param key The template key we are searching for.
     * @param line The line to be placed if no key was found.
     * 
     * @return
     */
    public String raw(String key, String line) {
	return file.getString(key, line);
    }

    public void save(String key, String line) {
	file.setString(key, line);
    }

    public String color(String key) {
	return Messaging.parse(Messaging.colorize(file.getString(key)));
    }

    public String parse(String key, String[] argument, String[] points) {
	return Messaging.parse(Messaging.colorize(Messaging.argument(file.getString(key), argument, points)));
    }

    public String parse(String key, String line, String[] argument, String[] points) {
	return Messaging.parse(Messaging.colorize(Messaging.argument(file.getString(key, line), argument, points)));
    }
}
