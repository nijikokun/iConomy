import java.util.Comparator;
import java.util.Map;

/**
 * ValueComparator - Compares integers to get the organized result
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
