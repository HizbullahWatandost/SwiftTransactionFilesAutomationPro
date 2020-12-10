package af.aib.paymentResponse.robotcheck;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.ResponseFile;
import af.aib.paymentResponse.model.TransactionFile;
import af.aib.paymentResponse.util.AppCommons;

/**
 * This class the main class which runs the application and other methods are called here.
 * @author Hizbullah Watandost
 *
 */
public class AIBSwiftPaymentFileRobot {
	
	// Configuration instance
		private static AppConfig config = new AppConfig();

		// Error handling message
		private static String errorMsg = "";

		// Logging message
		private static String loggMsg = "";
		
		private static String pKeyFileOrgnlMsgId = "";
		private static ArrayList<String> fileProps;
		private static String tKeyFileOrgnlMsgId = "";
		
		public static HashMap<String, Integer> CheckNoOfPaymentsInFile(String org) {
			
			// It stores no of payments in key:value pair
			HashMap<String, Integer> noOfPaymentsInFile = new HashMap<String, Integer>();
			
			if (config.configSetup()) {

				String dir = AppCommons.getTodaysResponseFilesFolder(org);
				File srcDir = new File(dir);

				if (srcDir.exists()) {

					// Get list of the files inside the directory
					File[] listOfFiles = srcDir.listFiles();

					// If the directory is not empty , then...
					if (listOfFiles != null && listOfFiles.length > 0) {

						ResponseFile responseFile = new ResponseFile();

						for (File file : listOfFiles) {

							if (AppCommons.isXMLFile(file.getName())) {

								try {

									responseFile = AppCommons.getFileProperties(file.toString());
									noOfPaymentsInFile.put(responseFile.getFileName() + " | "+responseFile.getOrgnlMsgId(), responseFile.getOrgnlNbOfTxs());

								} catch (Exception exp) {
									errorMsg = "<File Content and Directory Error>" + exp.getClass().getSimpleName() + "->"
											+ exp.getCause() + "->" + exp.getMessage();
									System.out.println(errorMsg);
									ActivityLogger.logActivity(errorMsg);
								}
							}

						}

					} else {
						loggMsg = "<Empty Directory> No file found in " + dir + " directory.";
						System.out.println(loggMsg);
						ActivityLogger.logActivity(loggMsg);
					}

				}
			}
			
			return noOfPaymentsInFile;
		}
		
		
		public static HashMap<String, Integer> CheckNoOfTxnInAckFile(String org) {
			
			// It stores no of payments in key:value pair
			HashMap<String, Integer> noOfTxnInAckFile = new HashMap<String, Integer>();
			
			if (config.configSetup()) {

				String dir = AppCommons.getTodaysTransactionFilesMergedFolder(org);
				File srcDir = new File(dir);

				if (srcDir.exists()) {

					// Get list of the files inside the directory
					File[] listOfFiles = srcDir.listFiles();

					// If the directory is not empty , then...
					if (listOfFiles != null && listOfFiles.length > 0) {

						TransactionFile transactionFile = new TransactionFile();

						for (File file : listOfFiles) {

							if (AppCommons.isXMLFile(file.getName())) {

								try {

									transactionFile = AppCommons.getTransactionFileProperties(file.toString());
									noOfTxnInAckFile.put(transactionFile.getFileName() + " | "+transactionFile.getOrgnlMsgId(), transactionFile.getOrgnlNbOfTxs());
									
								} catch (Exception exp) {
									errorMsg = "<File Content and Directory Error>" + exp.getClass().getSimpleName() + "->"
											+ exp.getCause() + "->" + exp.getMessage();
									System.out.println(errorMsg);
									ActivityLogger.logActivity(errorMsg);
								}
							}

						}

					} else {
						loggMsg = "<Empty Directory> No file found in " + dir + " directory.";
						System.out.println(loggMsg);
						ActivityLogger.logActivity(loggMsg);
					}

				}
			}
			
			return noOfTxnInAckFile;
		}
		
		public static ArrayList<ArrayList<String>> matchPaymentFileAndAckFilesProperties(String org) {
			
			ArrayList<ArrayList<String>> matchedFilesProps = new ArrayList<ArrayList<String>>();
			
			HashMap<String, Integer> paymentFiles = CheckNoOfPaymentsInFile(org);
			HashMap<String, Integer> ackFiles = CheckNoOfTxnInAckFile(org);
			
			paymentFiles.forEach((pKey, pValue) -> {
				
				pKeyFileOrgnlMsgId = "";
				
				if(pKey.contains("|")) {

					pKeyFileOrgnlMsgId = pKey.substring(pKey.indexOf("|")+("|").length()+1);
				}
				
				
				ackFiles.forEach((tKey, tValue) -> {
					
					fileProps = new ArrayList<String>();
					tKeyFileOrgnlMsgId = "";
					
					tKeyFileOrgnlMsgId = tKey.substring(tKey.indexOf("|")+("|").length()+1);
					
					if(pKeyFileOrgnlMsgId.equals(tKeyFileOrgnlMsgId)) {
						
						fileProps.add(pKey);
						fileProps.add(pValue.toString());
						fileProps.add(tKey);
						fileProps.add(tValue.toString());
						
						matchedFilesProps.add(fileProps);
						
					}
				
				});
				
			});
			
			return matchedFilesProps;
		}
}
