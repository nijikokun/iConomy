import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Logger;

/**
 * iLottery
 *	Controls global lottery system
 *
 * @date 11/17/2010 7:33PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public class iLottery {
	static final Logger log = Logger.getLogger("Minecraft");
	private iConomy p;
	private iMisc m;
	private iMoney mo;

	// Lottery Tag
	public String lotteryTag = Colors.White + "["+Colors.Green+"Lottery"+Colors.White+"]";

	public String lotteryPrize() {
		if (p.mysql) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = p.data.MySQL();
				ps = conn.prepareStatement("SELECT * FROM `iPrizes` ORDER BY RAND() LIMIT 1");
				rs = ps.executeQuery();

				if (rs.next()) {
					// Settings
					int percent = rs.getInt("percent");
					int item = rs.getInt("item");
					int amount = rs.getInt("amount");

					return ""+percent+";"+item+";"+amount+";";
				} else {
					return "";
				}
			} catch (SQLException ex) {
				log.severe("[iConomy Lottery] Unable to grab a prize!");
			} finally {
				try {
					if (ps != null) { ps.close(); }
					if (rs != null) { rs.close(); }
					if (conn != null) { conn.close(); }
				} catch (SQLException ex) { }
			}

			return "";
		} else {
			Random generator = new Random();
			Object[] values = p.hashPrizes.values().toArray();

			if(values.length < 1) {
				return "";
			}

			String randomValue = (String) values[generator.nextInt(values.length)];
			return randomValue;
		}
	}

	public void lottery(Player player) {
		if(p.data.getBalance(player.getName()) < p.ticketCost) {
			player.sendMessage(this.lotteryTag + Colors.Rose + " You do not have enough to purchase a ticket!");
			player.sendMessage(this.lotteryTag + Colors.Rose + " Ticket cost: "+ p.ticketCost);

			if(p.debugging)
				log.info("[iConomy Debugging] [" + player + "] ["+p.data.getBalance(player.getName())+"] [#20358]");

			 return;
		} else {
			String prize = this.lotteryPrize();
			Random generator = new Random();

			// Do lottery Ticket
			mo.debit(null, player.getName(), p.ticketCost, false);
			player.sendMessage(this.lotteryTag + Colors.LightGray + " " + String.format(p.lotteryCost, p.ticketCost + p.moneyName));

			// Do lottery checks
			if(prize.equals("")) {
				player.sendMessage(this.lotteryTag + Colors.Rose + " " + p.lotteryNotAvailable); return;
			}

			String[] data = prize.split(";");
			int percent = Integer.parseInt(data[0]);
			int item = Integer.parseInt(data[1]);
			int amount = Integer.parseInt(data[2]);
			int chance = generator.nextInt(100);
			int amountGiven = (amount > 1) ? generator.nextInt(amount) : 1;
			String itemName = m.itemName(m.cInt(item));
			itemName.replace("-", " ");

			if(chance < percent) {
				if(amountGiven != 0) {
					player.giveItem(item, amountGiven);
					m.broadcast(this.lotteryTag + Colors.Gold + " " + String.format(p.lotteryWinner, player.getName(), amountGiven, itemName));
					// player.sendMessage(this.lotteryTag + Colors.Gold + " Enjoy your items :)!");
				} else {
					player.sendMessage(this.lotteryTag + Colors.LightGray + " " + p.lotteryLoser);
					// this.broadcast(this.lotteryTag + Colors.LightGray + " " + this.lotteryLoser);
				}
			} else {
				player.sendMessage(this.lotteryTag + Colors.LightGray + " " + p.lotteryLoser);
			}

			if(p.debugging)
				log.info("[iConomy Debugging] [" + player + "] ["+p.data.getBalance(player.getName())+"] ["+amount+"] ["+amountGiven+"] ["+item+"] ["+percent+"] ["+chance+"] ["+itemName+"] [#20359]");

		}
	}
}
