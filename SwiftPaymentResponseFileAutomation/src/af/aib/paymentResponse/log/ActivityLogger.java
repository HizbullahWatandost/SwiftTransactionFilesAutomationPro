package af.aib.paymentResponse.log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.*;

import af.aib.paymentResponse.config.AppConfig;

/**
 * ActivityLogger class log all the activity with the application. It points the
 * issue smartly and most of the failure causes have been predicted. In future
 * version we are going to provide direct solution for each failure causes.
 * 
 * @author Hizbullah Watandost
 */
public class ActivityLogger {

	static AppConfig config = new AppConfig();

	private static Logger logger = Logger.getLogger(ActivityLogger.class.getName());
	private static FileHandler fileHandler = null;
	private static String loggingPath;

	private static String errorMsg = "";

	public static void startLogging() throws InterruptedException {

		try {

			LogManager.getLogManager().readConfiguration(ClassLoader.getSystemResourceAsStream("logging.properties"));

		} catch (SecurityException | IOException ex) {

			errorMsg = ex.getClass().getSimpleName() + "->" + ex.getCause() + "->" + ex.getMessage();
			System.out.println("<Logging Error> " + errorMsg);
			ActivityLogger.logActivity("<Logging Error> " + errorMsg);
		}

		logger.setLevel(Level.FINE);
		logger.addHandler(new ConsoleHandler());
		// adding custom handler
		logger.addHandler(new LogHandler());

		try {

			// FileHandler file name with max size and number of log files limit
			SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d HH-mm-ss");

			if (config.configSetup()) {

				loggingPath = AppConfig.getAppLoggingPath();
				fileHandler = new FileHandler(
						loggingPath + "PaymentResponse@" + format.format(Calendar.getInstance().getTime()) + ".log");
				fileHandler.setFormatter(new LogFormatter());
				// setting custom filter for FileHandler
				fileHandler.setFilter(new LogFilter());
				logger.addHandler(fileHandler);

			} else {

				System.out.println("<Config Error> Can not read the config file. Please check config.properties file.");
				ActivityLogger.logActivity(
						"<Config Error> Can not read the config file. Please check config.properties file.");
			}

		} catch (SecurityException | IOException exp) {

			errorMsg = exp.getClass().getSimpleName() + "->" + exp.getCause() + "->" + exp.getMessage();
			System.out.println("<Logging Error> " + errorMsg);
			ActivityLogger.logActivity("<Logging Error> " + errorMsg);
		}
	}

	/**
	 * This method is used to write log in log file
	 * 
	 * @param msg: the log message
	 */
	public static void logActivity(String msg) {
		
		logger.log(Level.FINE, msg);
	}
}
