package af.aib.paymentResponse.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.util.Banner;

/**
 * The Config class which read the contents of application config file. The
 * config file contains the app configuration details i.e. the source input
 * and output path and other configuraiton detials
 * 
 * @author Hizbullah Watandost
 */
public class AppConfig {

	// Instance of properties
	private static Properties prop = new Properties();
	// Properties file name
	private static String fileName = "config.properties";
	private static InputStream appConfigStream = null;

	// Error handling message
	private static String errorMsg = "";

	/**
	 * This method setup the configuration using the config.properties file
	 * 
	 * @return : true if the configuration is OK, else false in case of invalid
	 *         configuration
	 * @throws InterruptedException 
	 */
	public boolean configSetup() {

		boolean config = false;

		try {
			
			appConfigStream = getClass().getClassLoader().getResourceAsStream(fileName);
			prop.load(appConfigStream);
			
			if(Banner.appConfigCheck()) {
				config = true;
			}
			
		} catch (FileNotFoundException fnfex) {

			errorMsg = "<Config File Not Found> " + "The config file " + fileName
					+ " is either empty or can't be found!";
			System.out.println(errorMsg);
			ActivityLogger.logActivity(errorMsg);
			config = false;

		} catch (IOException exp) {

			config = false;
			errorMsg = "<Configuration Error> Invalid app configuration detected!!!" + exp.getClass().getSimpleName() + "->" + exp.getCause() + "->"
					+ exp.getMessage();
			System.out.println(errorMsg);
			ActivityLogger.logActivity(errorMsg);
		}

		return config;
	}

	public static String getAppName() {
		return (prop.getProperty("app.name"));
	}

	public static String getAppVersion() {
		return (prop.getProperty("app.version"));
	}

	public static String getAppReleaseDate() {
		return (prop.getProperty("app.release.date"));
	}

	public static String getAppMaintainanceDate() {
		return (prop.getProperty("app.maintainance.date"));
	}
	
	public static String getAppOwner() {
		return (prop.getProperty("app.owner.name"));
	}
	
	public static String getAppDeveloperName() {
		return (prop.getProperty("app.developer.name"));
	}
	
	public static String getAppDeveloperEmail() {
		return (prop.getProperty("app.developer.email"));
	}
	
	public static String getAppPoweredBy() {
		return (prop.getProperty("app.poweredby"));
	}
	public static String getAppRootPath() {
		return (prop.getProperty("app_root_path"));
	}

	public static String getAppLoggingPath() {
		return (prop.getProperty("app_logging_path"));
	}
	
	public static String getResponseFileSrcPath() {
		return (prop.getProperty("responsefile_src_path"));
	}
	
	public static String getAppBannerPath() {
		return (prop.getProperty("app_banner_path"));
	}
	
	public static String getDbURL() {
		return (prop.getProperty("DBUrl"));
	}

	public static String getDbUserName() {
		return (prop.getProperty("DBUserName"));
	}

	public static String getDbPassword() {
		return (prop.getProperty("DBPassword"));
	}
}
