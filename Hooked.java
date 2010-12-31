/**
 * Hooked.java
 * <br><br>
 * Controls custom hook information by easily helping you figure out what is what without recieving errors on the matter.
 *
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class Hooked {

    public Hooked() {

    }

    /**
     * Silently place a call to the custom hook, does not return any information.
     *
     * @param listener
     * @param args
     */
    public static void silent(String listener, Object[] args) {
	etc.getLoader().callCustomHook(listener, args);
	return;
    }

    /**
     * Publicly call the hook and return any information recieved.
     *
     * @param listener
     * @param args
     * @return Object
     */
    public static Object call(String listener, Object[] args) {
	return etc.getLoader().callCustomHook(listener, args);
    }

    /**
     * Make a public call and return an integer if one exists, if not return 0.
     *
     * @param listener
     * @param args
     * @return integer, 0 if none was ever returned.
     */
    public static int getInt(String listener, Object[] args) {
	Object result = call(listener, args);
	return (Integer)result;
    }

    /**
     * Make a public call and return a String if one exists, if not return an empty string.
     *
     * @param listener
     * @param args
     * @return String - Empty if none was ever returned.
     */
    public static String getString(String listener, Object[] args) {
	Object result = call(listener, args);
	return (String)result;
    }

    /**
     * Make a public call and return an boolean if one exists, if not return false.
     *
     * @param listener
     * @param args
     * @return Boolean - false if none was ever returned.
     */
    public static boolean getBoolean(String listener, Object[] args) {
	Object result = call(listener, args);
	return (Boolean)result;
    }

    /**
     * Make a public call and return an Double if one exists, if not return 0.0.
     *
     * @param listener
     * @param args
     * @return Double - 0.0 if none was ever returned.
     */
    public static double getDouble(String listener, Object[] args) {
	Object result = call(listener, args);
	return (Double)result;
    }

    /**
     * Make a public call and return an Long if one exists, if not return 0L.
     *
     * @param listener
     * @param args
     * @return Long - 0L if none was ever returned.
     */
    public static long getLong(String listener, Object[] args) {
	Object result = call(listener, args);
	return (Long)result;
    }

}
