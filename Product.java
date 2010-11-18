
public class Product {
	public static final double INDEX  = 10000;
	private double stack; // volume of trade, stacks etc.
	private double stock;
	
	public Product() {
	}
	
	public double value(int quantity, boolean sell) {
		double currencyIndex = iData.globalBalance() / INDEX;
		double stockPivot = (1 + (INDEX * Simulation.averageStock()));
		double value = Math.ceil((stockPivot/(stock+1)) * currencyIndex *stack);
		if (sell)
			value *= Simulation.getDevaluation();
		return value;
	}
	
	/*
	=CEILING((($I$6/(D2+1))*$I$3*E2),1)
	
	Currency.inCirculation()/INDEX
	
	*/

	public void setStock(double stock) {
		this.stock = stock;
	}

	public double getStock() {
		return stock;
	}

}
