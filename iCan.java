/**
 * iCan
 *	Controls permissions for each sub-command on iConomy
 *
 * @date 11/17/2010 7:15PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public class iCan {
	iConomy p;

	public boolean Do(String can, Player player) {
	    if (!can.equals("*")) {
		    String[] groupies = can.split(",");
		    for (String group : groupies){ if(player.isInGroup(group)){return true;}}
		    return false;
	    }
	    return true;
	}

	public boolean Can(Player player, String command) {
		if (command.equals("pay")) {
			return this.Do(p.canPay, player);
		} else if (command.equals("debit")) {
			return this.Do(p.canDebit, player);
		} else if (command.equals("credit")) {
			return this.Do(p.canCredit, player);
		} else if (command.equals("reset")) {
			return this.Do(p.canReset, player);
		} else if (command.equals("rank")) {
			return this.Do(p.canRank, player);
		} else if (command.equals("view")) {
			return this.Do(p.canView, player);
		} else if (command.equals("top")) {
			return this.Do(p.canTop, player);
		} else if (command.equals("sell")) {
			return this.Do(p.canSell, player);
		} else if (command.equals("buy")) {
			return this.Do(p.canBuy, player);
		} else if (command.equals("bid")) {
			return this.Do(p.canBid, player);
		} else if (command.equals("auction")) {
			return this.Do(p.canAuction, player);
		} else if (command.equals("end")) {
			return this.Do(p.canEnd, player);
		} else if (command.equals("lottery")) {
			return this.Do(p.canLottery, player);
		} else if (command.equals("sign")) {
			return this.Do(p.canSign, player);
		} else if (command.equals("signBuy")) {
			return this.Do(p.canSignBuy, player);
		} else if (command.equals("signSell")) {
			return this.Do(p.canSignSell, player);
		} else if (command.equals("trade")) {
			return this.Do(p.canTrade, player);
		}

		return false;
	}
}
