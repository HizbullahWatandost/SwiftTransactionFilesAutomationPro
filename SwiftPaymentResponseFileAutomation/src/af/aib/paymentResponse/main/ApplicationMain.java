package af.aib.paymentResponse.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.config.PathsConfig;
import af.aib.paymentResponse.etl.FileExtraction;
import af.aib.paymentResponse.etl.FileLoading;
import af.aib.paymentResponse.etl.FileTransformationAndMerging;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.ResponseFile;
import af.aib.paymentResponse.robotcheck.AIBSwiftPaymentFileRobot;
import af.aib.paymentResponse.util.AppCommons;
import af.aib.paymentResponse.util.Banner;

/**
 * This class the main class which runs the application and other methods are
 * called here.
 * 
 * @author Hizbullah Watandost
 *
 */
public class ApplicationMain {

	// Configuration instance
	private static AppConfig config = new AppConfig();

	// Error handling message
	private static String errorMsg = "";

	// Logging message
	private static String loggMsg = "";

	private final static String PAYMENT_SUCCESS_STATUS = "OK (should be sent today)";
	private final static String PAYMENT_WARNING_STATUS = "WARNING (should be sent tomorrow)";
	
	private static String paymentFilesSrcPath = "";
	private static String  paymentAckFilesSrcPath = "";
	private static String  destPendingFolder = "";
	
	private static int xmlIndexPaymentFile;
	private static int xmlIndexAckFile; 
	
	

	public static void main(String[] args) throws IOException, InterruptedException {
		// configuring the path automatically
		
		PathsConfig.pathsSetup();

		// configuring the logging
		ActivityLogger.startLogging();
		
		loggMsg = "#################### <<<>>> Start of Today's Swift Payment Response File Merger Automation: "
				+ AppCommons.getCurrentDateTime() + " <<<>>> ####################";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);
		
		Banner.printBanner();
		
		loggMsg = "<<<<< Start of Fetching the Swift payment response files from Swift Server >>>>>";
		System.out.println(loggMsg);
		ActivityLogger.logActivity("\n"+loggMsg);
		AppCommons.processFiles();
		
		// Extracting UNDP response files
		FileExtraction.extractResponseFileFromSwift("86154");
		// Checking for UNDP duplicate response files
		FileLoading.duplicateFileHandler("86154");

		// Extracting UNICEF response files
		FileExtraction.extractResponseFileFromSwift("86570");
		// Checking for UNICEF duplicate response files
		FileLoading.duplicateFileHandler("86570");

		// Reading Files properties
		// Reading UNDP file properties
		System.out.println("UNDP files properties");
		ArrayList<ResponseFile> undpFileListProperties = FileExtraction.getListOfAllFilesProperties("86154");

		for (ResponseFile file : undpFileListProperties) {

			AppCommons.createTransactionFilesFolder("86154", file.getOrgnlMsgId());
		}

		// Reading UNICEF file properties
		System.out.println("UNICEF files properties");
		ArrayList<ResponseFile> unicefFileListProperties = FileExtraction.getListOfAllFilesProperties("86570");

		for (ResponseFile file : unicefFileListProperties) {

			AppCommons.createTransactionFilesFolder("86570", file.getOrgnlMsgId());
		}

		// Creating pending folders
		AppCommons.createTodayPendingFolder("86154");
		AppCommons.createTodayPendingFolder("86570");
		
		// Extracting UNDP transaction files
		FileExtraction.extractTransactionFilesFromSwift("86154");
		// Extracting UNICEF transaction files
		FileExtraction.extractTransactionFilesFromSwift("86570");

		// Checking for rejected or invalid transaction files
		// UNDP transaction files check
		FileLoading.rejectedOrInvalidTransactionFilesCheck("86154");
		// UNICEF transaction files check
		FileLoading.rejectedOrInvalidTransactionFilesCheck("86570");

		// UNDP copying the first transaction file to the source path
		FileTransformationAndMerging.modifyAndCopyFirstTransacitonFileToSrcPath("86154");
		// UNICEF copying the first transaction file to the source path
		FileTransformationAndMerging.modifyAndCopyFirstTransacitonFileToSrcPath("86570");

		// Merging and moving UNDP transaction files to merged path
		FileTransformationAndMerging.mergingTransactionsFilesAndMovingToMergedPath("86154");
		// Merging and moving UNICEF transaction files to merged path
		FileTransformationAndMerging.mergingTransactionsFilesAndMovingToMergedPath("86570");

		// Adding closing tags to final UNDP merged transaction files
		FileTransformationAndMerging.addingClosingTagsToTheTransactionFiles("86154");
		// Adding closing tags to final UNICEF merged transaction files
		FileTransformationAndMerging.addingClosingTagsToTheTransactionFiles("86570");

		// TODO - Robotic Check - check the payment files and transaction files, and
		// find which files should be send and which not and it will be send to Swift automatically

		String paymentLog = "";
		loggMsg = "*****************************************************************************************";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";

		loggMsg = "<<<<< Start of Processing & Checking the Swift payment files by Hizwat Robot >>>>>";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";
		
		AppCommons.processFiles();

		loggMsg = "-----------------------------------------------------------------------------------------";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";

		loggMsg = "---<<<< UNDP Payments and ACK confirmation files >>>>---";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";

		loggMsg = "---  Payment File Name                 |OrgnlMsgId|No of Payments  <|>Response File Name     |OrgnlMsgId|No of Payments Received|Status ---";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";

		ArrayList<ArrayList<String>> UNDPMatchedFiles = AIBSwiftPaymentFileRobot
				.matchPaymentFileAndAckFilesProperties("86154");
		
		paymentFilesSrcPath = AppCommons.getTodaysResponseFilesFolder("86154");
		paymentAckFilesSrcPath = AppCommons.getTodaysTransactionFilesFolder("86154");
		
		
		String paymentFileName = "", paymentAckFileName = "";

		// Looping through matched files
		for (int i = 0; i < UNDPMatchedFiles.size(); i++) {

			// Looping through each item of the array list which contains the matched files pairs 
			for (int j = 0; j < UNDPMatchedFiles.get(i).size(); j++) {

				// Separator of file level and transaction files leve
				if (j == 2) {

					System.out.print("          <|>");
					paymentLog += "          <|>";
				}
				System.out.print(UNDPMatchedFiles.get(i).get(j) + " - ");
				paymentLog += UNDPMatchedFiles.get(i).get(j) + " - ";
			}

			// If the number of payments in payment file level matches the ACK file
			if (UNDPMatchedFiles.get(i).get(1).equals(UNDPMatchedFiles.get(i).get(3))) {
				
				System.out.print("===> " + PAYMENT_SUCCESS_STATUS);
				paymentLog += "===> " + PAYMENT_SUCCESS_STATUS;
			
			// If the number of payments in payment file level does not match the ACK merged file
			} else {
				
				int pendingAcks = Integer.parseInt(UNDPMatchedFiles.get(i).get(1))
						- Integer.parseInt(UNDPMatchedFiles.get(i).get(3));
				System.out.print("===> " + PAYMENT_WARNING_STATUS + " -> " + pendingAcks + " payments are pending");
				paymentLog += "===> " + PAYMENT_WARNING_STATUS + " -> " + pendingAcks + " payments are pending";
				
				destPendingFolder = AppCommons.getTodayPendingFolder("86154");
				xmlIndexPaymentFile = UNDPMatchedFiles.get(i).get(0).lastIndexOf(".xml")+(".xml").length();
				paymentFileName = UNDPMatchedFiles.get(i).get(0).substring(0, xmlIndexPaymentFile);
				
				xmlIndexAckFile = UNDPMatchedFiles.get(i).get(2).lastIndexOf(".xml")+(".xml").length();
				paymentAckFileName = UNDPMatchedFiles.get(i).get(2).substring(0, xmlIndexAckFile);
				
				pendingAcks = pendingAcks < 0 ? (-1) * pendingAcks : pendingAcks;
				
				// If the number of missing/pending payments are more than 1 or 2 then move it to rejected or pending folder
				if(pendingAcks >= 3) {
					
					AppCommons.moveFile(false,paymentFilesSrcPath+"\\"+paymentFileName, destPendingFolder+"\\"+paymentFileName);
					AppCommons.moveFile(false,paymentAckFilesSrcPath+"\\merged\\"+paymentAckFileName, destPendingFolder+"\\"+paymentAckFileName);
				}	
			}

			System.out.println();
			paymentLog += "\n";
		}

		ActivityLogger.logActivity(paymentLog + "\n");
		System.out.println();

		paymentLog = "";
		loggMsg = "---<<<< UNICEF Payments and ACK confirmation files >>>>---";
		System.out.println(loggMsg);
		paymentLog += "\n" + loggMsg + "\n";

		loggMsg = "---  Payment File Name                    |OrgnlMsgId|No of Payments<|>Response File Name     |OrgnlMsgId|No of Payments Received|Status ---";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";
		ArrayList<ArrayList<String>> UNICEFMatchedFiles = AIBSwiftPaymentFileRobot
				.matchPaymentFileAndAckFilesProperties("86570");
		
		paymentFilesSrcPath = AppCommons.getTodaysResponseFilesFolder("86570");
		paymentAckFilesSrcPath = AppCommons.getTodaysTransactionFilesFolder("86570");
		

		for (int i = 0; i < UNICEFMatchedFiles.size(); i++) {

			for (int j = 0; j < UNICEFMatchedFiles.get(i).size(); j++) {

				if (j == 2) {

					System.out.print("  <|>");
					paymentLog += "  <|>";
				}

				System.out.print(UNICEFMatchedFiles.get(i).get(j) + " - ");
				paymentLog += UNICEFMatchedFiles.get(i).get(j) + " - ";
			}

			if (UNICEFMatchedFiles.get(i).get(1).equals(UNICEFMatchedFiles.get(i).get(3))) {
				
				System.out.print("===> " + PAYMENT_SUCCESS_STATUS);
				paymentLog += "===> " + PAYMENT_SUCCESS_STATUS;
				
			} else {
				
				int pendingAcks = Integer.parseInt(UNICEFMatchedFiles.get(i).get(1))
						- Integer.parseInt(UNICEFMatchedFiles.get(i).get(3));
				System.out.print("===> " + PAYMENT_WARNING_STATUS + " -> " + pendingAcks + " payments are pending");
				paymentLog += "===> " + PAYMENT_WARNING_STATUS + " -> " + pendingAcks + " payments are pending";
				
				destPendingFolder = AppCommons.getTodayPendingFolder("86570");
				xmlIndexPaymentFile = UNICEFMatchedFiles.get(i).get(0).lastIndexOf(".xml")+(".xml").length();
				paymentFileName = UNICEFMatchedFiles.get(i).get(0).substring(0, xmlIndexPaymentFile);
				
				xmlIndexAckFile = UNICEFMatchedFiles.get(i).get(2).lastIndexOf(".xml")+(".xml").length();
				paymentAckFileName = UNICEFMatchedFiles.get(i).get(2).substring(0, xmlIndexAckFile);
				
				pendingAcks = pendingAcks < 0 ? (-1) * pendingAcks : pendingAcks;
				
				// If the number of missing/pending payments are more than 1 or 2 then move it to rejected or pending folder
				if(pendingAcks >= 3) {
					
					AppCommons.moveFile(false, paymentFilesSrcPath+"\\"+paymentFileName, destPendingFolder+"\\"+paymentFileName);
					AppCommons.moveFile(false, paymentAckFilesSrcPath+"\\merged\\"+paymentAckFileName, destPendingFolder+"\\"+paymentAckFileName);
				}
			}

			System.out.println();
			paymentLog += "\n";
		}

		loggMsg = "------------------------------------------------------------------------------------------";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";

		ActivityLogger.logActivity(paymentLog);

		loggMsg = "\n===============================================================================================\n"
				+ "--- *** Rejected/Pending UNDP Payment Files***---";
		System.out.println(loggMsg);
		paymentLog = loggMsg + "\n";

		// Listing UNDP rejected/pending file levels
		HashMap<String, Integer> UNDPRejectedPayments = AIBSwiftPaymentFileRobot
				.getRejectedAndWarningPaymentFilesProperties("86154");

		for (Map.Entry<String, Integer> undpRejectedFile : UNDPRejectedPayments.entrySet()) {
			System.out.println(undpRejectedFile.getKey() + " --> " + undpRejectedFile.getValue());
			paymentLog += undpRejectedFile.getKey() + " --> " + undpRejectedFile.getValue() + "\n";
		}

		ActivityLogger.logActivity(paymentLog);

		loggMsg = "\n---***Rejected/Pending UNICEF Payment Files***--";
		System.out.println(loggMsg);
		paymentLog = loggMsg + "\n";

		// Listing UNICEF rejected/pending file levels
		HashMap<String, Integer> UNICEFRejectedPayments = AIBSwiftPaymentFileRobot
				.getRejectedAndWarningPaymentFilesProperties("86570");

		for (Map.Entry<String, Integer> unicefRejectedFile : UNICEFRejectedPayments.entrySet()) {
			System.out.println(unicefRejectedFile.getKey() + " --> " + unicefRejectedFile.getValue());
			paymentLog += unicefRejectedFile.getKey() + " --> " + unicefRejectedFile.getValue() + "\n";
		}
		ActivityLogger.logActivity(paymentLog);

		loggMsg = "\n---***Rejected/Pending UNDP Transaction Files***---";
		System.out.println(loggMsg);
		paymentLog = loggMsg + "\n";

		// Listing UNDP rejected/pending ACK levels
		HashMap<String, Integer> UNDPRejectedTxns = AIBSwiftPaymentFileRobot
				.getRejectedAndWarningTxnFilesProperties("86154");

		for (Map.Entry<String, Integer> undpRejectedTxns : UNDPRejectedTxns.entrySet()) {
			System.out.println(undpRejectedTxns.getKey() + " --> " + undpRejectedTxns.getValue());
			paymentLog += undpRejectedTxns.getKey() + " --> " + undpRejectedTxns.getValue() + "\n";
		}

		ActivityLogger.logActivity(paymentLog);
		loggMsg = "\n---***Rejected/Pending UNICEF Transaction Files***---";
		System.out.println(loggMsg);
		paymentLog = loggMsg + "\n";

		// Listing UNICEF rejected/pending ACK levels
		HashMap<String, Integer> UNICEFRejectedTxns = AIBSwiftPaymentFileRobot
				.getRejectedAndWarningTxnFilesProperties("86570");

		for (Map.Entry<String, Integer> unicefRejectedTxns : UNICEFRejectedTxns.entrySet()) {
			System.out.println(unicefRejectedTxns.getKey() + " --> " + unicefRejectedTxns.getValue());
			paymentLog += unicefRejectedTxns.getKey() + " --> " + unicefRejectedTxns.getValue() + "\n";
		}

		ActivityLogger.logActivity(paymentLog);

		loggMsg = "\n====================================================================================";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);

		loggMsg = "\n<<<<< End of Check by Robot >>>>>";
		System.out.println(loggMsg);
		paymentLog = loggMsg;
		ActivityLogger.logActivity(paymentLog);

		loggMsg = "*****************************************************************************************";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);
		
		loggMsg = ">>> Files are processed and sent back to the Swift Server, please check Swift UI interface for the status of the files (should be Network ACKed)";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);

		loggMsg = "#################### <<<>>> End of Today's Swift Response File Merger Automation: "
				+ AppCommons.getCurrentDateTime() + " <<<>>> ####################";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);
		
		// Delete the source directory folder's files after processing it
		AppCommons.deleteFilesOfDirectory("received", "86154");
		AppCommons.deleteFilesOfDirectory("received", "86570");
		AppCommons.deleteFilesOfDirectory("success", "86154");
		AppCommons.deleteFilesOfDirectory("success", "86570");
		
		// Move back the rejected/pending files to the source folder to be processed and send next day
		AppCommons.movingWarningAndRejectedFilesBack("received", "86154");
		AppCommons.movingWarningAndRejectedFilesBack("received", "86570");
		
		AppCommons.movingWarningAndRejectedFilesBack("success", "86154");
		AppCommons.movingWarningAndRejectedFilesBack("success", "86570");
	}
}
