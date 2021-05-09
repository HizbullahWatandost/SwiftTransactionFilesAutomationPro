package af.aib.paymentResponse.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.CustomPaymentFile;

/**
 * This class is used to handle CRUD operation
 * 
 * @author hizwat
 *
 */
public class DBServiceImpl {

	private static AppConfig config = new AppConfig();
	private static String errMsg = "";
	private static String logMsg = "";

	// Database connectivity url
	private static String url = null;
	// Database username
	private static String username = null;
	// Database password
	private static String password = null;

	private static MSDBConnection msdbConnection = new MSDBConnection();

	public static void savePaymentFile(CustomPaymentFile cpf) {

		if (msdbConnection.dbConnect()) {

			String msConnUrl = AppConfig.getDbURL() + ";" + "user=" + AppConfig.getDbUserName() + ";" + "password="
					+ AppConfig.getDbPassword();
			try (Connection msConn = DriverManager.getConnection(msConnUrl)) {

				String q = "INSERT INTO PaymentResponseFileTbl(" + "payment_batch_ref," + "batch_file_name,"
						+ "no_of_txn," + "ack_merged_file_name," + "no_of_sc_txn," + "no_of_pd_txn," + "ins_date,"
						+ "is_batch_file_sent," + "is_ack_file_sent," + "last_update_date, "
						+ "is_completed) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

				PreparedStatement prepStmt = msConn.prepareStatement(q);
				prepStmt.setString(1, cpf.getPaymentRef());
				prepStmt.setString(2, cpf.getBatchFileName());
				prepStmt.setInt(3, cpf.getNoOfTxn());
				prepStmt.setString(4, cpf.getAckMergedFileName());
				prepStmt.setInt(5, cpf.getNoOfScTxn());
				prepStmt.setInt(6, cpf.getNoOfPdTxn());
				prepStmt.setString(7, cpf.getInsDate());
				prepStmt.setInt(8, ((cpf.isBatchFileSent() == true) ? 1 : 0));
				prepStmt.setInt(9, ((cpf.isAckFileSent() == true) ? 1 : 0));
				prepStmt.setString(10, cpf.getLastUpdateDate());
				prepStmt.setInt(11, (cpf.getNoOfTxn() == cpf.getNoOfScTxn()) ? 1 : 0);

				if (prepStmt.executeUpdate() > 0) {
					logMsg = "<Storing Payment File> The payment with reference " + cpf.getPaymentRef()
							+ " has been saved to database successfully!";
					System.out.println(logMsg);
					ActivityLogger.logActivity(logMsg);

				} else {
					logMsg = "<Storing Payment File> The payment with reference " + cpf.getPaymentRef()
							+ " failed to be saved to database!";
					System.out.println(logMsg);
					ActivityLogger.logActivity(logMsg);

				}

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
	}

	public static CustomPaymentFile getPaymentFile(String batchRef) {
		CustomPaymentFile customPaymentFile = null;

		if (msdbConnection.dbConnect()) {

			String msConnUrl = AppConfig.getDbURL() + ";" + "user=" + AppConfig.getDbUserName() + ";" + "password="
					+ AppConfig.getDbPassword();
			try (Connection msConn = DriverManager.getConnection(msConnUrl)) {

				String q = "SELECT * FROM PaymentResponseFileTbl WHERE payment_batch_ref = ?";

				PreparedStatement prepStmt = msConn.prepareStatement(q);
				prepStmt.setString(1, batchRef);

				ResultSet rs = prepStmt.executeQuery();
				while (rs.next()) {
					customPaymentFile = new CustomPaymentFile();
					customPaymentFile.setPaymentRef(batchRef);
					customPaymentFile.setBatchFileName(rs.getString("batch_file_name"));
					customPaymentFile.setNoOfTxn(rs.getInt("no_of_txn"));
					customPaymentFile.setAckMergedFileName(rs.getString("ack_merged_file_name"));
					customPaymentFile.setNoOfScTxn(rs.getInt("no_of_sc_txn"));
					customPaymentFile.setNoOfPdTxn(rs.getInt("no_of_pd_txn"));
					customPaymentFile.setInsDate(rs.getString("ins_date"));
					customPaymentFile.setBatchFileSent(rs.getInt("is_batch_file_sent") == 1 ? true : false);
					customPaymentFile.setAckFileSent(rs.getInt("is_ack_file_sent") == 1 ? true : false);
					customPaymentFile.setLastUpdateDate(rs.getString("last_update_date"));
					customPaymentFile.setCompleted(rs.getInt("is_completed") == 1 ? true : false);

				}

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

		return customPaymentFile;
	}

	public static void updatePaymentFile(String paymentRef, CustomPaymentFile cpf) {

		if (msdbConnection.dbConnect()) {

			String msConnUrl = AppConfig.getDbURL() + ";" + "user=" + AppConfig.getDbUserName() + ";" + "password="
					+ AppConfig.getDbPassword();
			try (Connection msConn = DriverManager.getConnection(msConnUrl)) {

				String q = "UPDATE PaymentResponseFileTbl SET batch_file_name = ?," + "no_of_txn = ?, "
						+ "ack_merged_file_name =?, " + "no_of_sc_txn =?, " + "no_of_pd_txn = ?, " + "ins_date = ?,"
						+ "is_batch_file_sent =?, " + "is_ack_file_sent =? ,"
						+ "last_update_date =?, is_completed = ? WHERE payment_batch_ref = ?";

				PreparedStatement prepStmt = msConn.prepareStatement(q);
				prepStmt.setString(1, cpf.getBatchFileName());
				prepStmt.setInt(2, cpf.getNoOfTxn());
				prepStmt.setString(3, cpf.getAckMergedFileName());
				prepStmt.setInt(4, cpf.getNoOfScTxn());
				prepStmt.setInt(5, cpf.getNoOfPdTxn());

				prepStmt.setString(6, cpf.getInsDate());
				prepStmt.setInt(7, ((cpf.isBatchFileSent() == true) ? 1 : 0));
				prepStmt.setInt(8, ((cpf.isAckFileSent() == true) ? 1 : 0));
				prepStmt.setString(9, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));
				prepStmt.setInt(10, (cpf.getNoOfTxn() == cpf.getNoOfScTxn()) ? 1 : 0);
				prepStmt.setString(11, paymentRef);
				prepStmt.executeUpdate();

				if (prepStmt.executeUpdate() > 0) {

					logMsg = "<Payment File Update> The payment file with reference " + paymentRef
							+ " has been updated successfully!";
					System.out.println(logMsg);
					ActivityLogger.logActivity(logMsg);
				} else {

					logMsg = "<Payment File Update> The payment  file with referecne " + paymentRef + " update failed!";
					System.out.println(logMsg);
					ActivityLogger.logActivity(logMsg);

				}

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
	}

	public static void deletePaymentFile(String paymentRef) {

		if (msdbConnection.dbConnect()) {

			String msConnUrl = AppConfig.getDbURL() + ";" + "user=" + AppConfig.getDbUserName() + ";" + "password="
					+ AppConfig.getDbPassword();
			try (Connection msConn = DriverManager.getConnection(msConnUrl)) {

				String q = "DELETE FROM PaymentResponseFileTbl WHERE payment_batch_ref = ?";
				PreparedStatement prepStmt = msConn.prepareStatement(q);
				prepStmt.setString(1, paymentRef);
				if (prepStmt.executeUpdate() > 0) {
					logMsg = "<Deleting Payment File> The payment file with reference " + paymentRef
							+ " has been deleted successfully!";
					System.out.println(logMsg);
					ActivityLogger.logActivity(logMsg);
				} else {
					logMsg = "<Deleting Payment File> The payment file with reference " + paymentRef
							+ " failed to be deleted!";
					System.out.println(logMsg);
					ActivityLogger.logActivity(logMsg);

				}

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
	}

}
