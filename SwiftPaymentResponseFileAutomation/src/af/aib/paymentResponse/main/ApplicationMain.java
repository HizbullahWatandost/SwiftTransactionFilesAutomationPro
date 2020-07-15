package af.aib.paymentResponse.main;

import java.io.IOException;
import java.util.ArrayList;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.config.PathsConfig;
import af.aib.paymentResponse.etl.FileExtraction;
import af.aib.paymentResponse.etl.FileLoading;
import af.aib.paymentResponse.etl.FileTransformationAndMerging;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.ResponseFile;
import af.aib.paymentResponse.util.AppCommons;

/**
 * This class the main class which runs the application and other methods are called here.
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

	public static void main(String[] args) throws IOException {
		// configuring the path automatically
		PathsConfig.pathsSetup();

		// configuring the logging
		ActivityLogger.startLogging();

		loggMsg = "#################### <<<>>> Start of Today's Swift Response File Merger Automation: "
				+ AppCommons.getCurrentDateTime() + " <<<>>> ####################";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);

		// Extracting UNDP response files
		FileExtraction.extractResponseFileFromSwift("86154");
		// Checking for UNDP duplicate response files
		FileLoading.duplicateFileHandler("86154");
		// TODO- removing schema tag from UNDP response files

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
		
		// TODO - Robotic Check - Check the files - The number of files should match 
		/*
		 * In case of 3 response file, their should be 3 transaction merged files in merged folder 
		 * Check whether the number of transactions in response files matches with the number of 
		 * transaction in transaction files or not. 
		 * Check the transaction codes with the response file and the transaction merged files matches or not
		 * Check which transactions are missing and display warning message.
		 * 
		 * Checks whether the response file or transactions already uploaded in Swift or not. 
		 * Read data directly from swift 
		 * 
		 * At the end as final output, move all the response files and transaction files in final_output folder
		 * of each organization. 
		 */

		loggMsg = "#################### <<<>>> End of Today's Swift Response File Merger Automation: "
				+ AppCommons.getCurrentDateTime() + " <<<>>> ####################";
		System.out.println(loggMsg);
		ActivityLogger.logActivity(loggMsg);
	}

}
