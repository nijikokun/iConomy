import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * iLog
 *	Controls logging of all input/output data for iConomy to be parsed
 *
 * @date 11/17/2010 7:33PM
 * @author Nijiko
 * @copyright CC Nijikokun / DarkGrave, Aslyum Corporation LLC
 */
public class iLog {
	static final Logger log = Logger.getLogger("Minecraft");
	private iConomy p;

	public static String logDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		return format.format(now);
	}

	public void Log(String type, String data) {
		String date = logDate();

		if (type.equalsIgnoreCase("pay") && p.logPay) {
			try {
				FileWriter fstream = new FileWriter(p.directory + p.lDirectory + "pay.log", true);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(date + "|" + data);
				out.newLine();
				out.close();
			} catch (Exception es) {
				log.severe("[iConomy Pay Logging] " + es.getMessage());
			}
		} else if (type.equalsIgnoreCase("buy") && p.logBuy) {
			try {
				FileWriter fstream = new FileWriter(p.directory + p.lDirectory + "buy.log", true);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(date + "|" + data);
				out.newLine();
				out.close();
			} catch (Exception es) {
				log.severe("[iConomy Buy Logging] " + es.getMessage());
			}
		} else if (type.equalsIgnoreCase("sell") && p.logSell) {
			try {
				FileWriter fstream = new FileWriter(p.directory + p.lDirectory + "sell.log", true);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(date + "|" + data);
				out.newLine();
				out.close();
			} catch (Exception es) {
				log.severe("[iConomy Sell Logging] " + es.getMessage());
			}
		} else if (type.equalsIgnoreCase("auction") && p.logAuction) {
			try {
				FileWriter fstream = new FileWriter(p.directory + p.lDirectory + "auction.log", true);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(date + "|" + data);
				out.newLine();
				out.close();
			} catch (Exception es) {
				log.severe("[iConomy Auction Logging] " + es.getMessage());
			}
		} else if (type.equalsIgnoreCase("signs") && p.logAuction) {
			try {
				FileWriter fstream = new FileWriter(p.directory + p.lDirectory + "sign.log", true);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(date + "|" + data);
				out.newLine();
				out.close();
			} catch (Exception es) {
				log.severe("[iConomy Sign Logging] " + es.getMessage());
			}
		} else if (type.equalsIgnoreCase("trade") && p.logAuction) {
			try {
				FileWriter fstream = new FileWriter(p.directory + p.lDirectory + "trade.log", true);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(date + "|" + data);
				out.newLine();
				out.close();
			} catch (Exception es) {
				log.severe("[iConomy Trade Logging] " + es.getMessage());
			}
		}
	}
}
