package af.aib.paymentResponse.etl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.ResponseFile;
import af.aib.paymentResponse.util.AppCommons;

/**
 * This class is used to load files from a source directory to another directory
 * 
 * @author Hizbullah Watandost
 *
 */
public class FileLoading {

	private static AppConfig config = new AppConfig();

	// Error handling
	private static String errorMsg = "";

	// Logging message
	private static String loggMsg = "";

	/**
	 * This method handles duplicate file , in case it finds duplicate file , it
	 * creates a folder namely duplicate and moves moves it in that folder
	 * 
	 * @param org: the organization code i.e. undp or unicef folder code
	 */
	public static void duplicateFileHandler(String org) {

		String responseFileSrcDirectory = AppConfig.getAppRootPath() + AppCommons.getCurrentDate() + "\\" + "received"
				+ "\\" + org + "\\";

		File srcDir = new File(responseFileSrcDirectory);

		if (srcDir.exists()) {

			// Get list of response files from the received folder based on the organizatin
			File[] listOfFiles = srcDir.listFiles();

			// if the folder is not empty then
			if (listOfFiles != null && listOfFiles.length > 0) {

				// loop through the files
				for (int i = 0; i < listOfFiles.length; i++) {

					String file1Content = AppCommons.readFileAllContent(listOfFiles[i].toString());
					String fileName = listOfFiles[i].getName();

					// Check if the file is duplicate
					if (AppCommons.isXMLFile(fileName)) {

						// We find duplicate based on same MsgId, OrgnlMsgId, OrgnNbOfTxs and OrgCtrlSum
						String fileOneMsgIdTag = "";
						String fileOneOrgnlMsgIdTag = "";
						String fileOneOrgnlNbOfTxsTag = "";
						String fileOneOrgnlCtrlSumTag = "";

						// Get the file MsgId
						if (file1Content.contains("<MsgId>")) {
							fileOneMsgIdTag = file1Content.substring(
									file1Content.indexOf("<MsgId>") + "<MsgId>".length(),
									file1Content.indexOf("</MsgId>"));
						}
						// Get the file OrgnlMsgId
						if (file1Content.contains("<OrgnlMsgId>")) {
							fileOneOrgnlMsgIdTag = file1Content.substring(
									file1Content.indexOf("<OrgnlMsgId>") + "<OrgnlMsgId>".length(),
									file1Content.indexOf("</OrgnlMsgId>"));
						}
						// Get the file OrgnlMbOfTxs
						if (file1Content.contains("<OrgnlNbOfTxs>")) {
							fileOneOrgnlNbOfTxsTag = file1Content.substring(
									file1Content.indexOf("<OrgnlNbOfTxs>") + "<OrgnlNbOfTxs>".length(),
									file1Content.indexOf("</OrgnlNbOfTxs>"));
						}
						// Get the file OrgnlCtrlSum
						if (file1Content.contains("<OrgnlCtrlSum>")) {
							fileOneOrgnlCtrlSumTag = file1Content.substring(
									file1Content.indexOf("<OrgnlCtrlSum>") + "<OrgnlCtrlSum>".length(),
									file1Content.indexOf("</OrgnlCtrlSum>"));
						}

						// Second loop to match each file with other files within the same folder
						for (int j = i + 1; j < listOfFiles.length; j++) {

							String file2Content = AppCommons.readFileAllContent(listOfFiles[j].toString());

							fileName = listOfFiles[j].getName();

							if (AppCommons.isXMLFile(fileName)) {

								String fileTwoMsgIdTag = "";
								String fileTwoOrgnlMsgIdTag = "";
								String fileTwoOrgnlNbOfTxsTag = "";
								String fileTwoOrgnlCtrlSumTag = "";

								// Get each file attributes as above
								if (file2Content.contains("<MsgId>")) {
									fileTwoMsgIdTag = file2Content.substring(
											file2Content.indexOf("<MsgId>") + "<MsgId>".length(),
											file2Content.indexOf("</MsgId>"));
								}
								if (file2Content.contains("<OrgnlMsgId>")) {
									fileTwoOrgnlMsgIdTag = file2Content.substring(
											file2Content.indexOf("<OrgnlMsgId>") + "<OrgnlMsgId>".length(),
											file2Content.indexOf("</OrgnlMsgId>"));
								}
								if (file2Content.contains("<OrgnlNbOfTxs>")) {
									fileTwoOrgnlNbOfTxsTag = file2Content.substring(
											file2Content.indexOf("<OrgnlNbOfTxs>") + "<OrgnlNbOfTxs>".length(),
											file2Content.indexOf("</OrgnlNbOfTxs>"));
								}
								if (file2Content.contains("<OrgnlCtrlSum>")) {
									fileTwoOrgnlCtrlSumTag = file2Content.substring(
											file2Content.indexOf("<OrgnlCtrlSum>") + "<OrgnlCtrlSum>".length(),
											file2Content.indexOf("</OrgnlCtrlSum>"));
								}

								// check for duplicate file now based on above file attributes
								// If the file is duplicate
								if (fileOneMsgIdTag.equals(fileTwoMsgIdTag)
										&& fileOneOrgnlMsgIdTag.equals(fileTwoOrgnlMsgIdTag)
										&& fileOneOrgnlNbOfTxsTag.equals(fileTwoOrgnlNbOfTxsTag)
										&& fileOneOrgnlCtrlSumTag.equals(fileTwoOrgnlCtrlSumTag)) {

									loggMsg = "<Duplicate Response File Found> " + "File: " + listOfFiles[i].getName()
											+ " and file: " + listOfFiles[j].getName() + " are duplicate files";
									System.out.println(loggMsg);
									ActivityLogger.logActivity(loggMsg);

									// Create a folder for duplicate files in case the duplicate file exist
									AppCommons.createFolderForDuplicateResponseFiles(org);
									String duplicatePath = AppCommons.getDuplicateResponseFileDirectory(org);

									try {

										// Move the duplicate file inside the duplicate folder
										AppCommons.moveFile(true,listOfFiles[i].toString(),
												duplicatePath + "\\" + listOfFiles[i].getName());

									} catch (Exception exp) {

										errorMsg = "<File Content and Directory Error>" + exp.getClass().getSimpleName()
												+ "->" + exp.getCause() + "->" + exp.getMessage();
										System.out.println(errorMsg);
										ActivityLogger.logActivity(errorMsg);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This method is used to move the invalid or rejected transaction files inside
	 * the rejected/pending folder NOTE: the rejected or invalid transaction files are the
	 * files which their transaction code or id doesn't match to any response files.
	 * 
	 * @param org: the organization code for which we check the invalid transaction
	 *             files
	 */
	public static void rejectedOrInvalidTransactionFilesCheck(String org) {

		if (config.configSetup()) {

			// Getting all the files from success from source path -- swift
			String dir = AppConfig.getResponseFileSrcPath() + "success\\" + org + "\\";
			File srcDir = new File(dir);

			if (srcDir.exists()) {

				// Get list of the transaction files
				File[] listOfFiles = srcDir.listFiles();
				FileWriter fileWriterPath = null;
				BufferedWriter bufferedWriter = null;

				// Get the response files properties
				ArrayList<ResponseFile> fileListProperties = FileExtraction.getListOfAllFilesProperties(org);

				// ArrayList to hold the files OrgnlMsgIds
				ArrayList<String> filesOrgnlMsgIds = new ArrayList<>();

				// Get each file OrgnlMsgId and put in filesOrgnlMsgIds list
				for (ResponseFile file : fileListProperties) {
					filesOrgnlMsgIds.add(file.getOrgnlMsgId());
				}

				AppCommons.createTodayPendingFolder(org);
				String pendingTransactionFilesDir = AppCommons.getTodayPendingFolder(org);
				
				File rejectedDir = new File(dir);

				if (listOfFiles != null && listOfFiles.length > 0) {

					for (File file : listOfFiles) {

						if (AppCommons.isXMLFile(file.getName())) {

							try {

								// Reading the file and removing the schema tag if it is UNDP file
								String fileContent = AppCommons.removeSchemaTagFromUNDPFiles(file.toString());
								String orgnlMsgId = AppCommons.getTransactionOrgnlMsgId(file.toString());

								// If the file OrgnlMsgId doesn't not match to the list response file
								// OrgnlsMsgIds
								// Then move that file in rejected folder
								if (!filesOrgnlMsgIds.contains(orgnlMsgId)) {

									if (rejectedDir.exists()) {

										fileWriterPath = new FileWriter(
												pendingTransactionFilesDir + "\\" + file.getName());
										bufferedWriter = new BufferedWriter(fileWriterPath);
										bufferedWriter.write(fileContent);
										bufferedWriter.close();

										errorMsg = "<Unknown Transaction File> A transaction file with refernce ("
												+ orgnlMsgId + ") is unknown and moved to "
												+ pendingTransactionFilesDir;
										System.out.println(errorMsg);
										ActivityLogger.logActivity(errorMsg);
									}

								}

							} catch (Exception exp) {
								errorMsg = "<File Content and Directory Error>" + exp.getClass().getSimpleName() + "->"
										+ exp.getCause() + "->" + exp.getMessage();
								System.out.println(errorMsg);
								ActivityLogger.logActivity(errorMsg);
							}
						}
					}
				}
			}
		}
	}
}
