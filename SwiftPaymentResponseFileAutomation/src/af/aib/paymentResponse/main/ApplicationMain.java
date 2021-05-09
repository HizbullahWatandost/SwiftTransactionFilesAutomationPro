package af.aib.paymentResponse.main;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.config.PathsConfig;
import af.aib.paymentResponse.db.DBServiceImpl;
import af.aib.paymentResponse.db.MSDBConnection;
import af.aib.paymentResponse.etl.FileExtraction;
import af.aib.paymentResponse.etl.FileLoading;
import af.aib.paymentResponse.etl.FileTransformationAndMerging;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.CustomPaymentFile;
import af.aib.paymentResponse.model.RejectedPaymentFile;
import af.aib.paymentResponse.model.ResponseFile;
import af.aib.paymentResponse.robot.AIBSwiftPaymentFileRobot;
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
	private final static String PAYMENT_WARNING_STATUS = "WARNING (The pending ACKs will be sent when received)";

	private static String paymentFilesSrcPath = "";
	private static String paymentAckFilesSrcPath = "";
	private static String destPendingFolder = "";

	private static int xmlIndexPaymentFile;
	private static int xmlIndexAckFile;

	private static CustomPaymentFile customPaymentFile;
	private static RejectedPaymentFile rejectedPaymentFile;
	private static String fName;
	private static ArrayList<ArrayList<CustomPaymentFile>> paymentFiles = null;
	private static ArrayList<ArrayList<RejectedPaymentFile>> rejectedFiles = null;

	public static void main(String[] args) throws IOException, InterruptedException {

		// configuring the path automatically
		PathsConfig.pathsSetup();
		// ArrayList which store all the payment files to get stored in Database
		paymentFiles = new ArrayList<>();
		rejectedFiles = new ArrayList<>();

		// configuring the logging
		ActivityLogger.startLogging();

		loggMsg = "#################### <<<>>> Start of Today's Swift Payment Response File Merger Automation: "
				+ AppCommons.getCurrentDateTime() + " <<<>>> ####################";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);

		Banner.printBanner();

		loggMsg = "<<<<< Start of Fetching the Swift payment response files from Swift Server >>>>>";
		System.out.println(loggMsg);
		ActivityLogger.logActivity("\n" + loggMsg);
		AppCommons.processFiles();

		// Extracting UNDP response files
		FileExtraction.extractResponseFileFromSwift("86154");
		// Checking for UNDP duplicate response files
		FileLoading.duplicateFileHandler("86154");

		// Extracting UNICEF response files
		FileExtraction.extractResponseFileFromSwift("86570");
		// Checking for UNICEF duplicate response files
		FileLoading.duplicateFileHandler("86570");

		// Extracting UNOPS response files
		// Customer one
		FileExtraction.extractResponseFileFromSwift("86702");
		// Checking for UNOPS customer one duplicate response files
		FileLoading.duplicateFileHandler("86702");

		// Customer two
		FileExtraction.extractResponseFileFromSwift("86754");
		// Checking for UNOPS customer two duplicate response files
		FileLoading.duplicateFileHandler("86754");

		// Reading Files properties
		// Reading UNDP file properties
		System.out.println("UNDP files properties");
		ArrayList<ResponseFile> undpFileListProperties = FileExtraction.getListOfAllFilesProperties("86154");

		for (ResponseFile file : undpFileListProperties)
			AppCommons.createTransactionFilesFolder("86154", file.getOrgnlMsgId());
		// Reading UNICEF file properties
		System.out.println("UNICEF files properties");
		ArrayList<ResponseFile> unicefFileListProperties = FileExtraction.getListOfAllFilesProperties("86570");

		for (ResponseFile file : unicefFileListProperties)
			AppCommons.createTransactionFilesFolder("86570", file.getOrgnlMsgId());

		// Reading UNOPS customer one file properties
		System.out.println("UNOPS customer one files properties");
		ArrayList<ResponseFile> unopsCustOneFileListProperties = FileExtraction.getListOfAllFilesProperties("86702");
		for (ResponseFile file : unopsCustOneFileListProperties)
			AppCommons.createTransactionFilesFolder("86702", file.getOrgnlMsgId());
		// Reading UNOPS customer two file properties
		System.out.println("UNOPS customer two files properties");
		ArrayList<ResponseFile> unopsCustTwoFileListProperties = FileExtraction.getListOfAllFilesProperties("86754");
		for (ResponseFile file : unopsCustTwoFileListProperties)
			AppCommons.createTransactionFilesFolder("86754", file.getOrgnlMsgId());

		// Creating pending folders
		AppCommons.createTodayPendingFolder("86154");
		AppCommons.createTodayPendingFolder("86570");
		AppCommons.createTodayPendingFolder("86702");
		AppCommons.createTodayPendingFolder("86754");

		// Extracting UNDP transaction files
		FileExtraction.extractTransactionFilesFromSwift("86154");
		// Extracting UNICEF transaction files
		FileExtraction.extractTransactionFilesFromSwift("86570");
		// Extracting UNOPS customer one transaction files
		FileExtraction.extractTransactionFilesFromSwift("86702");
		// Extracting UNOPS customer two transaction files
		FileExtraction.extractTransactionFilesFromSwift("86754");

		// Checking for rejected or invalid transaction files
		// UNDP transaction files check
		FileLoading.rejectedOrInvalidTransactionFilesCheck("86154");
		// UNICEF transaction files check
		FileLoading.rejectedOrInvalidTransactionFilesCheck("86570");
		// UNOPS customer one transaction files check
		FileLoading.rejectedOrInvalidTransactionFilesCheck("86702");
		// UNOPS customer two transaction files check
		FileLoading.rejectedOrInvalidTransactionFilesCheck("86754");

		// UNDP copying the first transaction file to the source path
		FileTransformationAndMerging.modifyAndCopyFirstTransacitonFileToSrcPath("86154");
		// UNICEF copying the first transaction file to the source path
		FileTransformationAndMerging.modifyAndCopyFirstTransacitonFileToSrcPath("86570");
		// UNOPS customer one copying the first transaction file to the source path
		FileTransformationAndMerging.modifyAndCopyFirstTransacitonFileToSrcPath("86702");
		// UNOPS customer two copying the first transaction file to the source path
		FileTransformationAndMerging.modifyAndCopyFirstTransacitonFileToSrcPath("86754");

		// Merging and moving UNDP transaction files to merged path
		FileTransformationAndMerging.mergingTransactionsFilesAndMovingToMergedPath("86154");
		// Merging and moving UNICEF transaction files to merged path
		FileTransformationAndMerging.mergingTransactionsFilesAndMovingToMergedPath("86570");
		// Merging and moving UNOPS customer one transaction files to merged path
		FileTransformationAndMerging.mergingTransactionsFilesAndMovingToMergedPath("86702");
		// Merging and moving UNOPS customer two transaction files to merged path
		FileTransformationAndMerging.mergingTransactionsFilesAndMovingToMergedPath("86754");

		// Adding closing tags to final UNDP merged transaction files
		FileTransformationAndMerging.addingClosingTagsToTheTransactionFiles("86154");
		// Adding closing tags to final UNICEF merged transaction files
		FileTransformationAndMerging.addingClosingTagsToTheTransactionFiles("86570");
		// Adding closing tags to final UNOPS customer one merged transaction files
		FileTransformationAndMerging.addingClosingTagsToTheTransactionFiles("86702");
		// Adding closing tags to final UNOPS customer two merged transaction files
		FileTransformationAndMerging.addingClosingTagsToTheTransactionFiles("86754");

		// TODO - Robotic Check - check the payment files and transaction files, and
		// find which files should be send and which not and it will be send to Swift
		// automatically

		String paymentLog = "";
		loggMsg = "*****************************************************************************************";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";

		loggMsg = "<<<<< Start of Processing & Checking the Swift payment files by HizWat Robot >>>>>";
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

		paymentFiles.add(AppCommons.collectMatchedFiles(UNDPMatchedFiles));

		String paymentFileName = "", paymentAckFileName = "";

		// Looping through matched files
		for (int i = 0; i < UNDPMatchedFiles.size(); i++) {

			// Looping through each item of the array list which contains the matched files
			// pairs
			for (int j = 0; j < UNDPMatchedFiles.get(i).size(); j++) {

				// Separator of file level and transaction files level
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

				// If the number of payments in payment file level does not match the ACK merged
				// file
			} else {

				int pendingAcks = Integer.parseInt(UNDPMatchedFiles.get(i).get(1))
						- Integer.parseInt(UNDPMatchedFiles.get(i).get(3));
				System.out.print("===> " + PAYMENT_WARNING_STATUS + " -> " + pendingAcks + " payments are pending");
				paymentLog += "===> " + PAYMENT_WARNING_STATUS + " -> " + pendingAcks + " payments are pending";

				destPendingFolder = AppCommons.getTodayPendingFolder("86154");
				xmlIndexPaymentFile = UNDPMatchedFiles.get(i).get(0).lastIndexOf(".xml") + (".xml").length();
				paymentFileName = UNDPMatchedFiles.get(i).get(0).substring(0, xmlIndexPaymentFile);

				xmlIndexAckFile = UNDPMatchedFiles.get(i).get(2).lastIndexOf(".xml") + (".xml").length();
				paymentAckFileName = UNDPMatchedFiles.get(i).get(2).substring(0, xmlIndexAckFile);

				pendingAcks = pendingAcks < 0 ? (-1) * pendingAcks : pendingAcks;

				// If the number of missing/pending payments are more than 1 or 2 then move it
				// to rejected or pending folder
				/*
				 * if (pendingAcks >= 3) {
				 * 
				 * AppCommons.moveFile(false, paymentFilesSrcPath + "\\" + paymentFileName,
				 * destPendingFolder + "\\" + paymentFileName); AppCommons.moveFile(false,
				 * paymentAckFilesSrcPath + "\\merged\\" + paymentAckFileName, destPendingFolder
				 * + "\\" + paymentAckFileName); }
				 */
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

		paymentFiles.add(AppCommons.collectMatchedFiles(UNICEFMatchedFiles));

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
				xmlIndexPaymentFile = UNICEFMatchedFiles.get(i).get(0).lastIndexOf(".xml") + (".xml").length();
				paymentFileName = UNICEFMatchedFiles.get(i).get(0).substring(0, xmlIndexPaymentFile);

				xmlIndexAckFile = UNICEFMatchedFiles.get(i).get(2).lastIndexOf(".xml") + (".xml").length();
				paymentAckFileName = UNICEFMatchedFiles.get(i).get(2).substring(0, xmlIndexAckFile);

				pendingAcks = pendingAcks < 0 ? (-1) * pendingAcks : pendingAcks;

				// If the number of missing/pending payments are more than 1 or 2 then move it
				// to rejected or pending folder
				/*
				 * if (pendingAcks >= 3) {
				 * 
				 * AppCommons.moveFile(false, paymentFilesSrcPath + "\\" + paymentFileName,
				 * destPendingFolder + "\\" + paymentFileName); AppCommons.moveFile(false,
				 * paymentAckFilesSrcPath + "\\merged\\" + paymentAckFileName, destPendingFolder
				 * + "\\" + paymentAckFileName); }
				 */
			}

			System.out.println();
			paymentLog += "\n";
		}

		ActivityLogger.logActivity(paymentLog + "\n");
		System.out.println();

		paymentLog = "";
		loggMsg = "---<<<< UNOPS customer one Payments and ACK confirmation files >>>>---";
		System.out.println(loggMsg);
		paymentLog += "\n" + loggMsg + "\n";

		loggMsg = "---  Payment File Name                    |OrgnlMsgId|No of Payments<|>Response File Name     |OrgnlMsgId|No of Payments Received|Status ---";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";
		ArrayList<ArrayList<String>> UNOPSCustOneMatchedFiles = AIBSwiftPaymentFileRobot
				.matchPaymentFileAndAckFilesProperties("86702");

		paymentFiles.add(AppCommons.collectMatchedFiles(UNOPSCustOneMatchedFiles));

		paymentFilesSrcPath = AppCommons.getTodaysResponseFilesFolder("86702");
		paymentAckFilesSrcPath = AppCommons.getTodaysTransactionFilesFolder("86702");

		for (int i = 0; i < UNOPSCustOneMatchedFiles.size(); i++) {

			for (int j = 0; j < UNOPSCustOneMatchedFiles.get(i).size(); j++) {

				if (j == 2) {

					System.out.print("  <|>");
					paymentLog += "  <|>";
				}

				System.out.print(UNOPSCustOneMatchedFiles.get(i).get(j) + " - ");
				paymentLog += UNOPSCustOneMatchedFiles.get(i).get(j) + " - ";
			}

			if (UNOPSCustOneMatchedFiles.get(i).get(1).equals(UNOPSCustOneMatchedFiles.get(i).get(3))) {

				System.out.print("===> " + PAYMENT_SUCCESS_STATUS);
				paymentLog += "===> " + PAYMENT_SUCCESS_STATUS;

			} else {

				int pendingAcks = Integer.parseInt(UNOPSCustOneMatchedFiles.get(i).get(1))
						- Integer.parseInt(UNOPSCustOneMatchedFiles.get(i).get(3));
				System.out.print("===> " + PAYMENT_WARNING_STATUS + " -> " + pendingAcks + " payments are pending");
				paymentLog += "===> " + PAYMENT_WARNING_STATUS + " -> " + pendingAcks + " payments are pending";

				destPendingFolder = AppCommons.getTodayPendingFolder("86702");
				xmlIndexPaymentFile = UNOPSCustOneMatchedFiles.get(i).get(0).lastIndexOf(".xml") + (".xml").length();
				paymentFileName = UNOPSCustOneMatchedFiles.get(i).get(0).substring(0, xmlIndexPaymentFile);

				xmlIndexAckFile = UNOPSCustOneMatchedFiles.get(i).get(2).lastIndexOf(".xml") + (".xml").length();
				paymentAckFileName = UNOPSCustOneMatchedFiles.get(i).get(2).substring(0, xmlIndexAckFile);

				pendingAcks = pendingAcks < 0 ? (-1) * pendingAcks : pendingAcks;

				// If the number of missing/pending payments are more than 1 or 2 then move it
				// to rejected or pending folder
				/*
				 * if (pendingAcks >= 3) {
				 * 
				 * AppCommons.moveFile(false, paymentFilesSrcPath + "\\" + paymentFileName,
				 * destPendingFolder + "\\" + paymentFileName); AppCommons.moveFile(false,
				 * paymentAckFilesSrcPath + "\\merged\\" + paymentAckFileName, destPendingFolder
				 * + "\\" + paymentAckFileName); }
				 */
			}

			System.out.println();
			paymentLog += "\n";
		}

		ActivityLogger.logActivity(paymentLog + "\n");
		System.out.println();

		paymentLog = "";
		loggMsg = "---<<<< UNOPS customer two Payments and ACK confirmation files >>>>---";
		System.out.println(loggMsg);
		paymentLog += "\n" + loggMsg + "\n";

		loggMsg = "---  Payment File Name                    |OrgnlMsgId|No of Payments<|>Response File Name     |OrgnlMsgId|No of Payments Received|Status ---";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";
		ArrayList<ArrayList<String>> UNOPSCustTwoMatchedFiles = AIBSwiftPaymentFileRobot
				.matchPaymentFileAndAckFilesProperties("86754");

		paymentFiles.add(AppCommons.collectMatchedFiles(UNOPSCustTwoMatchedFiles));

		paymentFilesSrcPath = AppCommons.getTodaysResponseFilesFolder("86754");
		paymentAckFilesSrcPath = AppCommons.getTodaysTransactionFilesFolder("86754");

		for (int i = 0; i < UNOPSCustTwoMatchedFiles.size(); i++) {

			for (int j = 0; j < UNOPSCustTwoMatchedFiles.get(i).size(); j++) {

				if (j == 2) {

					System.out.print("  <|>");
					paymentLog += "  <|>";
				}

				System.out.print(UNOPSCustTwoMatchedFiles.get(i).get(j) + " - ");
				paymentLog += UNOPSCustTwoMatchedFiles.get(i).get(j) + " - ";
			}

			if (UNOPSCustTwoMatchedFiles.get(i).get(1).equals(UNOPSCustTwoMatchedFiles.get(i).get(3))) {

				System.out.print("===> " + PAYMENT_SUCCESS_STATUS);
				paymentLog += "===> " + PAYMENT_SUCCESS_STATUS;

			} else {

				int pendingAcks = Integer.parseInt(UNOPSCustTwoMatchedFiles.get(i).get(1))
						- Integer.parseInt(UNOPSCustTwoMatchedFiles.get(i).get(3));
				System.out.print("===> " + PAYMENT_WARNING_STATUS + " -> " + pendingAcks + " payments are pending");
				paymentLog += "===> " + PAYMENT_WARNING_STATUS + " -> " + pendingAcks + " payments are pending";

				destPendingFolder = AppCommons.getTodayPendingFolder("86754");
				xmlIndexPaymentFile = UNOPSCustTwoMatchedFiles.get(i).get(0).lastIndexOf(".xml") + (".xml").length();
				paymentFileName = UNOPSCustTwoMatchedFiles.get(i).get(0).substring(0, xmlIndexPaymentFile);

				xmlIndexAckFile = UNOPSCustTwoMatchedFiles.get(i).get(2).lastIndexOf(".xml") + (".xml").length();
				paymentAckFileName = UNOPSCustTwoMatchedFiles.get(i).get(2).substring(0, xmlIndexAckFile);

				pendingAcks = pendingAcks < 0 ? (-1) * pendingAcks : pendingAcks;

				// If the number of missing/pending payments are more than 1 or 2 then move it
				// to rejected or pending folder
				/*
				 * if (pendingAcks >= 3) {
				 * 
				 * AppCommons.moveFile(false, paymentFilesSrcPath + "\\" + paymentFileName,
				 * destPendingFolder + "\\" + paymentFileName); AppCommons.moveFile(false,
				 * paymentAckFilesSrcPath + "\\merged\\" + paymentAckFileName, destPendingFolder
				 * + "\\" + paymentAckFileName); }
				 */
			}

			System.out.println();
			paymentLog += "\n";
		}

		loggMsg = "------------------------------------------------------------------------------------------";
		System.out.println(loggMsg);
		paymentLog += loggMsg + "\n";

		ActivityLogger.logActivity(paymentLog);

		// Storing the complete (matched) payment files into database
		AIBSwiftPaymentFileRobot.storePaymentFiles(paymentFiles);

		loggMsg = "\n===============================================================================================\n"
				+ "--- *** Rejected/Pending UNDP Payment Files***---";
		System.out.println(loggMsg);
		paymentLog = loggMsg + "\n";

		// Listing UNDP rejected/pending file levels
		HashMap<String, Integer> UNDPRejectedPayments = AIBSwiftPaymentFileRobot
				.getRejectedAndWarningPaymentFilesProperties("86154");

		rejectedFiles.add(AppCommons.collectRejectedFiles(UNDPRejectedPayments));

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
		rejectedFiles.add(AppCommons.collectRejectedFiles(UNICEFRejectedPayments));

		for (Map.Entry<String, Integer> unicefRejectedFile : UNICEFRejectedPayments.entrySet()) {

			System.out.println(unicefRejectedFile.getKey() + " --> " + unicefRejectedFile.getValue());
			paymentLog += unicefRejectedFile.getKey() + " --> " + unicefRejectedFile.getValue() + "\n";
		}

		ActivityLogger.logActivity(paymentLog);

		loggMsg = "\n---***Rejected/Pending UNOPS customer one Payment Files***--";
		System.out.println(loggMsg);
		paymentLog = loggMsg + "\n";

		// Listing UNOPS customer one rejected/pending file levels
		HashMap<String, Integer> UNOPSCustOneRejectedPayments = AIBSwiftPaymentFileRobot
				.getRejectedAndWarningPaymentFilesProperties("86702");

		rejectedFiles.add(AppCommons.collectRejectedFiles(UNOPSCustOneRejectedPayments));

		for (Map.Entry<String, Integer> unopsCustOneRejectedFile : UNOPSCustOneRejectedPayments.entrySet()) {

			System.out.println(unopsCustOneRejectedFile.getKey() + " --> " + unopsCustOneRejectedFile.getValue());
			paymentLog += unopsCustOneRejectedFile.getKey() + " --> " + unopsCustOneRejectedFile.getValue() + "\n";
		}
		ActivityLogger.logActivity(paymentLog);

		loggMsg = "\n---***Rejected/Pending UNOPS customer two Payment Files***--";
		System.out.println(loggMsg);
		paymentLog = loggMsg + "\n";

		// Listing UNOPS customer one rejected/pending file levels
		HashMap<String, Integer> UNOPSCustTwoRejectedPayments = AIBSwiftPaymentFileRobot
				.getRejectedAndWarningPaymentFilesProperties("86754");

		rejectedFiles.add(AppCommons.collectRejectedFiles(UNOPSCustTwoRejectedPayments));
		for (Map.Entry<String, Integer> unopsCustTwoRejectedFile : UNOPSCustTwoRejectedPayments.entrySet()) {

			System.out.println(unopsCustTwoRejectedFile.getKey() + " --> " + unopsCustTwoRejectedFile.getValue());
			paymentLog += unopsCustTwoRejectedFile.getKey() + " --> " + unopsCustTwoRejectedFile.getValue() + "\n";
		}

		ActivityLogger.logActivity(paymentLog);

		loggMsg = "\n---***Rejected/Pending UNDP Transaction Files***---";
		System.out.println(loggMsg);
		paymentLog = loggMsg + "\n";

		// Listing UNDP rejected/pending ACK levels
		HashMap<String, Integer> UNDPRejectedTxns = AIBSwiftPaymentFileRobot
				.getRejectedAndWarningTxnFilesProperties("86154");

		rejectedFiles.add(AppCommons.collectRejectedFiles(UNDPRejectedTxns));

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

		rejectedFiles.add(AppCommons.collectRejectedFiles(UNICEFRejectedTxns));

		for (Map.Entry<String, Integer> unicefRejectedTxns : UNICEFRejectedTxns.entrySet()) {

			System.out.println(unicefRejectedTxns.getKey() + " --> " + unicefRejectedTxns.getValue());
			paymentLog += unicefRejectedTxns.getKey() + " --> " + unicefRejectedTxns.getValue() + "\n";
		}

		ActivityLogger.logActivity(paymentLog);

		loggMsg = "\n---***Rejected/Pending UNOPS customer one Transaction Files***---";
		System.out.println(loggMsg);
		paymentLog = loggMsg + "\n";

		// Listing UNICEF rejected/pending ACK levels
		HashMap<String, Integer> UNOPSCustOneRejectedTxns = AIBSwiftPaymentFileRobot
				.getRejectedAndWarningTxnFilesProperties("86702");

		rejectedFiles.add(AppCommons.collectRejectedFiles(UNOPSCustOneRejectedTxns));

		for (Map.Entry<String, Integer> unopsCustOneRejectedTxns : UNOPSCustOneRejectedTxns.entrySet()) {

			System.out.println(unopsCustOneRejectedTxns.getKey() + " --> " + unopsCustOneRejectedTxns.getValue());
			paymentLog += unopsCustOneRejectedTxns.getKey() + " --> " + unopsCustOneRejectedTxns.getValue() + "\n";
		}

		ActivityLogger.logActivity(paymentLog);

		loggMsg = "\n---***Rejected/Pending UNOPS customer two Transaction Files***---";
		System.out.println(loggMsg);
		paymentLog = loggMsg + "\n";

		// Listing UNICEF rejected/pending ACK levels
		HashMap<String, Integer> UNOPSCustTwoRejectedTxns = AIBSwiftPaymentFileRobot
				.getRejectedAndWarningTxnFilesProperties("86754");

		rejectedFiles.add(AppCommons.collectRejectedFiles(UNOPSCustTwoRejectedTxns));

		for (Map.Entry<String, Integer> unopsCustTwoRejectedTxns : UNOPSCustTwoRejectedTxns.entrySet()) {
			System.out.println(unopsCustTwoRejectedTxns.getKey() + " --> " + unopsCustTwoRejectedTxns.getValue());
			paymentLog += unopsCustTwoRejectedTxns.getKey() + " --> " + unopsCustTwoRejectedTxns.getValue() + "\n";

		}

		ActivityLogger.logActivity(paymentLog);

		loggMsg = "\n====================================================================================";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);

		// Moving the copied folder to the uploaded folder in each organization folder
		AppCommons.moveFilesToUploadedFolder("received", "86154");
		AppCommons.moveFilesToUploadedFolder("received", "86570");
		AppCommons.moveFilesToUploadedFolder("received", "86702");
		AppCommons.moveFilesToUploadedFolder("received", "86754");
		AppCommons.moveFilesToUploadedFolder("success", "86154");
		AppCommons.moveFilesToUploadedFolder("success", "86570");
		AppCommons.moveFilesToUploadedFolder("success", "86702");
		AppCommons.moveFilesToUploadedFolder("success", "86754");

		// Checking and storing the rejected payment files to database
		AIBSwiftPaymentFileRobot.checkRejectedPaymentFiles(rejectedFiles);

		// Checking for any ignored rejected or warning files
		AppCommons.rejectedOrWarningFileAdvanceSearch("received", "86154");
		AppCommons.rejectedOrWarningFileAdvanceSearch("received", "86570");
		AppCommons.rejectedOrWarningFileAdvanceSearch("received", "86702");
		AppCommons.rejectedOrWarningFileAdvanceSearch("received", "86754");

		AppCommons.rejectedOrWarningFileAdvanceSearch("success", "86154");
		AppCommons.rejectedOrWarningFileAdvanceSearch("success", "86570");
		AppCommons.rejectedOrWarningFileAdvanceSearch("success", "86702");
		AppCommons.rejectedOrWarningFileAdvanceSearch("success", "86754");

		// Moving the FileLevel and incomplete warning ACK files to the related folders
		// to
		// be send through Swift
		AppCommons.rectifyingWarningAndRejectedFiles("86154");
		AppCommons.rectifyingWarningAndRejectedFiles("86570");
		AppCommons.rectifyingWarningAndRejectedFiles("86702");
		AppCommons.rectifyingWarningAndRejectedFiles("86754");

		// Moving the rejected - missing files back to Swift to be rechecked/resent in
		// next day trial
		AppCommons.moveRejectedOrWarningFilesBackToSwift("86154");
		AppCommons.moveRejectedOrWarningFilesBackToSwift("86570");
		AppCommons.moveRejectedOrWarningFilesBackToSwift("86702");
		AppCommons.moveRejectedOrWarningFilesBackToSwift("86754");

		loggMsg = "------------------------------------------------------------------------------------------";
		loggMsg += "\n******** Sending Files To Swift and Updating their status in database ************";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);

		AIBSwiftPaymentFileRobot.verifyingFileSendingToSwift("received", "86154");
		AIBSwiftPaymentFileRobot.verifyingFileSendingToSwift("received", "86570");
		AIBSwiftPaymentFileRobot.verifyingFileSendingToSwift("received", "86702");
		AIBSwiftPaymentFileRobot.verifyingFileSendingToSwift("received", "86754");

		AIBSwiftPaymentFileRobot.verifyingFileSendingToSwift("success", "86154");
		AIBSwiftPaymentFileRobot.verifyingFileSendingToSwift("success", "86570");
		AIBSwiftPaymentFileRobot.verifyingFileSendingToSwift("success", "86702");
		AIBSwiftPaymentFileRobot.verifyingFileSendingToSwift("success", "86754");

		loggMsg = "*****************************************************************************************";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);

		loggMsg = "\n<<<<< End of Check by Robot >>>>>";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);

		loggMsg = "\n====================================================================================";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);

		loggMsg = "\n#################### Important Important Important (please read it) #######################\n";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);

		loggMsg = ">>> Files are processed and sent back to the Swift Server, please check Swift UI interface for the status of the files (should be Network ACKed)";
		System.out.println(loggMsg);
		ActivityLogger.logActivity("\n" + loggMsg);

		loggMsg = ">>> Give Feedback: You know this automation saved you from hours of manual work and headache considering efficiency and accuracy as well\n"
				+ "If you like it, please give a feedback through email with subject title 'AIB Swift Payments Combiner Automation Software'\n"
				+ "and send it to the application team, It hardly takes one minute.\nThank you ):\n"
				+ "******************************************************************************************";
		System.out.println(loggMsg);
		ActivityLogger.logActivity("\n" + loggMsg);

		loggMsg = "#################### <<<>>> End of Today's Swift Response File Merger Automation: "
				+ AppCommons.getCurrentDateTime() + " <<<>>> ####################";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);
	}
}
