import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author Nijiko
 */
class ValueComparator implements Comparator {

    Map base;

    public ValueComparator(Map base) {
	this.base = base;
    }

    public int compare(Object a, Object b) {
	int ax = Integer.parseInt((String) base.get(a));
	int bx = Integer.parseInt((String) base.get(b));

	if (ax < bx) {
	    return 1;
	} else if (ax == bx) {
	    return 0;
	} else {
	    return -1;
	}
    }
}
