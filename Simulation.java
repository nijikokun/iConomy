import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.collections.MapIterator;

public class Simulation {
	private static HashMap<String,Product> items = new HashMap<String,Product>();
	private static double devaluation;

	public void initilize() {
		MapIterator glob = iConomy.items.mapIterator();
		while (glob.hasNext())
			items.put((String)glob.next(), new Product());
	}

	public static double averageStock() {
		Iterator<Product> working = items.values().iterator();
		double total = 0;
		while(working.hasNext())
	      total += working.next().getStock();
		return total / items.size();
	}

	public static void setDevaluation(double devaluation) {
		Simulation.devaluation = devaluation;
	}

	public static double getDevaluation() {
		return devaluation;
	}



}
