package af.aib.paymentResponse.config;

import java.io.File;
import java.util.ArrayList;

import af.aib.paymentResponse.log.ActivityLogger;

/**
 * This class automatically configures the path in case they have not been
 * created manually. So, when you run this software, it checks for the path, in
 * case it couldn't found any it creates the required paths for data import,
 * transformation or temporary path and output path
 * 
 * @author Hizbullah Watandost
 *
 */
public class PathsConfig {

	static AppConfig config = new AppConfig();

	// Error handling message
	private static String errorMsg = "";

	// Logging message
	private static String loggMsg = "";

	/**
	 * This method setup the path when the application runs in the first time.
	 * Configuration paths means creating the required paths which can be used by
	 * the application to fetch and store data.
	 */
	public static void pathsSetup(){

		// If configuration is OK
		if (config.configSetup()) {

			// Listing the paths
			ArrayList<File> paths = new ArrayList<File>();
			paths.add(new File(AppConfig.getAppRootPath()));
			paths.add(new File(AppConfig.getAppLoggingPath()));

			// Checking the paths
			for (File path : paths) {
				// If the path doesn't exist then
				if (!path.exists()) {

					loggMsg = "Creating Directory -> " + path.toString() + " ...";
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);

					try {
						// Create the path
						if (path.mkdirs()) {

							loggMsg = "<Directory Creation> The above directory has been created successfully!";
							System.out.println(loggMsg);
							ActivityLogger.logActivity(loggMsg);
						}

					} catch (Exception exp) {

						errorMsg = "<Directory Creation Error>" + exp.getClass().getSimpleName() + "->" + exp.getCause()
								+ "->" + exp.getMessage();
						System.out.println(errorMsg);
						ActivityLogger.logActivity(errorMsg);
					}
				}
			}

		} else {

			loggMsg = "<Configuration Error> Can not read the config file. Please check app.config file.";
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
		}
	}
}
