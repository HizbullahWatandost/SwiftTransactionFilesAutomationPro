package af.aib.paymentResponse.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.log.ActivityLogger;

/**
 * Connection to SQL server database
 * 
 * @author hizwat
 *
 */
public class MSDBConnection implements IDBConnection {

	static AppConfig config = new AppConfig();
	private static String errMsg = "";

	@Override
	public boolean dbConnect() {
		// TODO Auto-generated method stub

		boolean connection = false;

		if (config.configSetup()) {

			String url = AppConfig.getDbURL();
			String userName = AppConfig.getDbUserName();
			String password = AppConfig.getDbPassword();

			String connectionUrl = url + ";" + "user=" + userName + ";" + "password=" + password;
			try (Connection conn = DriverManager.getConnection(connectionUrl)) {
				connection = conn != null;
			} catch (SQLException exp) {
				errMsg = "<Database Server (SQL Server) Connection Error> " + exp.getClass().getSimpleName() + "->"
						+ exp.getCause() + "->" + exp.getMessage();
				System.out.println(errMsg);
				ActivityLogger.logActivity(errMsg);
			}

		} else {
			errMsg = "<SQL Server Invalid Configuration Properties> the SQL server connection parameters are invalid.";
			System.out.println(errMsg);
			ActivityLogger.logActivity(errMsg);
		}

		return connection;
	}

}
