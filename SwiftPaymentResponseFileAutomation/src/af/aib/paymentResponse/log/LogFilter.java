package af.aib.paymentResponse.log;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * When you will run above java logger example program, you will notice that
 * CONFIG log is not getting printed in file, that is because of MyFilter class.
 * 
 * @author Hizbullah Watandost
 */
public class LogFilter implements Filter {

	@Override
	public boolean isLoggable(LogRecord log) {
		// don't log CONFIG logs in file
		if (log.getLevel() == Level.CONFIG)
			return false;
		return true;
	}
}