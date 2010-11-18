import java.util.Comparator;
import java.util.Map;

/**
 * iValue
 *	Comparator for values and creates a asc / descending sorted view.
 *
 * @date 11/17/2010 7:33PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public class iValue implements Comparator {
	Map base;
	public iValue(Map base) {
		this.base = base;
	}

	public int compare(Object a, Object b) {
		int ax = Integer.parseInt((String)base.get(a));
		int bx = Integer.parseInt((String)base.get(b));

		if(ax < bx) {
			return 1;
		} else if(ax == bx) {
			return 0;
		} else {
			return -1;
		}
	}
}