package af.aib.paymentResponse.etl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.ResponseFile;
import af.aib.paymentResponse.util.AppCommons;

/**
 * This class used to transform the form to expected format and merge the
 * transaction files based on transaction code
 * 
 * @author Hizbullah Watandost
 *
 */
public class FileTransformationAndMerging {

	private static AppConfig config = new AppConfig();

	// Error handling
	private static String errorMsg = "";

	// Logging message
	private static String loggMsg = "";

	/**
	 * This method is used to remove the last closing tags from the transaction
	 * files in order to append other transactions to the first file.
	 * 
	 * @param fileContent
	 * @return transformed file content
	 */
	public static String removeLastClosingTags(String fileContent) {

		// modifying the content of the first file - removing the last closing tags

		// UNDP file
		if (fileContent.contains("</pain.002.001.02>")) {

			fileContent = fileContent.substring(0, fileContent.lastIndexOf("</pain.002.001.02>"));

			// UNICEF file
		} else if (fileContent.contains("</CstmrPmtStsRpt>")) {

			fileContent = fileContent.substring(0, fileContent.lastIndexOf("</CstmrPmtStsRpt>"));

		}
		return fileContent;
	}

	/**
	 * This method is used to add the last closing tags to the file content after other transactions are
	 * appended to the transaction file.
	 * 
	 * @param fileContent
	 * @return
	 */

	public static String addingLastClosingTags(String fileContent) {
		// if it is UNDP file then append the last UNDP payment file closing tags
		if (fileContent.contains("<pain.002.001.02>")) {

			if (!fileContent.contains("</pain.002.001.02>")) {
				fileContent = fileContent + " </pain.002.001.02>\n" + "</Document>";
			}

			// if it is UNICEF file then append the last UNICEF payment file closing tagn
		} else if (fileContent.contains("<CstmrPmtStsRpt>")) {

			if (!fileContent.contains("</CstmrPmtStsRpt>")) {

				fileContent = fileContent + " </CstmrPmtStsRpt>\n" + "</Document>";
			}
		}
		return fileContent;
	}

	/**
	 * This method is used to modify and copy the first transaction files from each
	 * transactions releted folder to source base directory
	 * 
	 * @param org
	 */
	public static void modifyAndCopyFirstTransacitonFileToSrcPath(String org) {

		// List the properties of payment file
		ArrayList<ResponseFile> fileListProperties = FileExtraction.getListOfAllFilesProperties(org);

		// For each payment file
		for (ResponseFile responseFile : fileListProperties) {

			// Get the transactions folder related to that payment file based on transaction
			// code
			String transactionFilesSrcPath = AppCommons.getTransactionFilesFolder(org, responseFile.getOrgnlMsgId());
			File srcPath = new File(transactionFilesSrcPath);

			// check if that directory exist
			if (srcPath.exists()) {

				// Get list of the files inside the directory
				File[] listOfFiles = srcPath.listFiles();

				FileWriter fileWriterPath = null;
				BufferedWriter bufferedWriter = null;

				if (listOfFiles != null && listOfFiles.length > 0) {

					// Get the first file name
					String firstFileName = listOfFiles[0].getName();
					String fileContent = "";

					try {

						// Get the destination path or source base path to copy the first transaction
						// file
						String destinationFilePath = AppCommons.getTodaysTransactionFilesFolder(org);
						fileWriterPath = new FileWriter(destinationFilePath + "\\" + firstFileName);
						bufferedWriter = new BufferedWriter(fileWriterPath);
						fileContent = AppCommons.readFileAllContent(listOfFiles[0].toString());

						// If there is more than file with same transaction code then remove the last
						// closing tags of the file before while copying to the destination path
						if (listOfFiles.length > 1) {

							fileContent = removeLastClosingTags(fileContent);
						}

						bufferedWriter.write(fileContent);
						bufferedWriter.close();

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

	/**
	 * This method is used to get the transaction portion of the file it ignores
	 * other oppening and closing tags.
	 * 
	 * @param fileContent
	 * @return transactions from the file content
	 */
	public static String getTransactionPortionFromFile(String fileContent) {

		// if it is UNDP file then get the payment portion
		if (fileContent.contains("<pain.002.001.02>") && fileContent.contains("<TxInfAndSts>")) {

			fileContent = fileContent.substring(fileContent.indexOf("<TxInfAndSts>"),
					fileContent.lastIndexOf("</pain.002.001.02>"));

			// if it is UNICEF file then get the payment portion
		} else if (fileContent.contains("<CstmrPmtStsRpt>") && fileContent.contains("<OrgnlPmtInfAndSts>")) {

			fileContent = fileContent.substring(fileContent.indexOf("<OrgnlPmtInfAndSts>"),
					fileContent.lastIndexOf("</CstmrPmtStsRpt>"));

		}

		return fileContent;
	}

	/**
	 * This method is used to merge the transaction files (append other transactions
	 * to the first file) and move it to the merged folder inside source directory.
	 * 
	 * @param org
	 */
	public static void mergingTransactionsFilesAndMovingToMergedPath(String org) {

		ArrayList<ResponseFile> fileListProperties = FileExtraction.getListOfAllFilesProperties(org);

		for (ResponseFile responseFile : fileListProperties) {

			// reading the transactions with that specific transaction id
			String transactionFilesSrcPath = AppCommons.getTransactionFilesFolder(org, responseFile.getOrgnlMsgId());
			File srcPath = new File(transactionFilesSrcPath);

			// copying the first files of each unique transaction id to source path
			// directory
			if (srcPath.exists()) {

				// Get list of the files inside the directory with that transaction code
				File[] listOfFiles = srcPath.listFiles();

				FileWriter fileWriterPath = null;
				BufferedWriter bufferedWriter = null;

				if (listOfFiles != null && listOfFiles.length > 0) {

					// Creating folder for merged transaction files
					AppCommons.createFolderForMergedTransactionFiles(org);

					String firstFileName = listOfFiles[0].getName();
					String fileContent = "";

					// Get the first file from the directory
					String destinationFilePath = AppCommons.getTodaysTransactionFilesFolder(org);

					try {

						fileWriterPath = new FileWriter(destinationFilePath + "\\" + firstFileName, true); // true ->
																											// open the
																											// file in
																											// append
																											// mode //
																											// mode
						bufferedWriter = new BufferedWriter(fileWriterPath);

						for (int i = 0; i < listOfFiles.length; i++) {

							if (i > 0) {

								// Reading content of each file
								fileContent = AppCommons.readFileAllContent(listOfFiles[i].toString());
								fileContent = getTransactionPortionFromFile(fileContent);

								// Appending other files content to the first file
								bufferedWriter.write(fileContent);
							}
						}

						bufferedWriter.close();

						// moving the files to merged path after merging
						String firstFileSrcPath = AppCommons.getTodaysTransactionFilesFolder(org) + "\\"
								+ firstFileName;
						String firstFileDestPath = AppCommons.getTodaysTransactionFilesMergedFolder(org) + "\\"
								+ firstFileName;
						AppCommons.moveFile(firstFileSrcPath, firstFileDestPath);

					} catch (IOException exp) {

						errorMsg = "<File Content and Directory Error>" + exp.getClass().getSimpleName() + "->"
								+ exp.getCause() + "->" + exp.getMessage();
						System.out.println(errorMsg);
						ActivityLogger.logActivity(errorMsg);
					}

				}
			}
		}

	}

	/**
	 * This method adds the closing tags to the file after the merge is done
	 * 
	 * @param org
	 */
	public static void addingClosingTagsToTheTransactionFiles(String org) {

		if (config.configSetup()) {

			// The transaction files merged path
			String dir = AppCommons.getTodaysTransactionFilesMergedFolder(org) + "\\";
			File srcDir = new File(dir);

			if (srcDir.exists()) {

				// Get list of the files inside the directory
				File[] listOfFiles = srcDir.listFiles();

				FileWriter fileWriterPath = null;
				BufferedWriter bufferedWriter = null;
				String fileContent = "";

				if (listOfFiles != null && listOfFiles.length > 0) {

					for (File file : listOfFiles) {

						if (AppCommons.isXMLFile(file.getName())) {

							try {

								// Read content of each file
								fileContent = AppCommons.readFileAllContent(file.toString());
								// Add the closing tags to the file content
								fileContent = addingLastClosingTags(fileContent);
								fileWriterPath = new FileWriter(dir + "\\" + file.getName());
								bufferedWriter = new BufferedWriter(fileWriterPath);
								bufferedWriter.write(fileContent);
								bufferedWriter.close();

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
