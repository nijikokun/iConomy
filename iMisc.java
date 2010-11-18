/**
 * iMisc
 *	Miscellaneous functions that are used commonly throughout iConomy
 *
 * @date 11/17/2010 9:08PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public class iMisc {
	private iConomy p = iConomy.getInstance();

	public String itemName(String id) {
		String name = (String) p.items.get(id);
		if (name != null) {
			return name;
		}

		return "";
	}

	public String cInt(int i) {
		return Integer.toString(i);
	}

	public boolean hasItems(Player player, int item, int amount) {
		Inventory inv = player.getInventory();

		if (inv.hasItem(item, amount, 6400)) {
			return true;
		} else {
			return false;
		}
	}

	public void removeItems(Player player, int material, int amount) {
		Inventory inv = player.getInventory();
		inv.removeItem(new Item(material, amount));
		inv.updateInventory();
	}

	public boolean canAfford(String name, int cost) {
		return (cost <= p.data.getBalance(name)) ? true : false;
	}

	public Player getPlayer(String name) {
		etc.getInstance();
		return etc.getServer().getPlayer(name);
	}

	public void broadcast(String message)
	{
		for (Player e : etc.getServer().getPlayerList())
			e.sendMessage(message);
	}
}
