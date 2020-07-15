package af.aib.paymentResponse.log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * LogFormatter class defines the format of the logging.
 * 
 * @author Hizbullah Watandost
 */
public class LogFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {

		SimpleDateFormat logTime = new SimpleDateFormat("--> yyyy-MM-dd HH:mm:ss");
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(record.getMillis());
		return record.getLevel() + logTime.format(cal.getTime()) + " || "
				+ record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf(".") + 1,
						record.getSourceClassName().length())
				+ "." + record.getSourceMethodName() + "() : " + record.getMessage() + "\n";
	}
}
