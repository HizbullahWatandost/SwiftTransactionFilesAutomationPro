package af.aib.paymentResponse.etl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.ResponseFile;
import af.aib.paymentResponse.util.AppCommons;
import af.aib.paymentResponse.util.Banner;

/**
 * This class is used to extract files from source directory (swift) to the
 * destination directory (cams server)
 * 
 * @author Hizbullah Watandost
 *
 */
public class FileExtraction {

	private static AppConfig config = new AppConfig();

	// Error handling
	private static String errorMsg = "";

	// Logging message
	private static String loggMsg = "";

	/**
	 * This method is used to extract the Response file from swift based on
	 * organization id or folder name i.e. 86154
	 * 
	 * @param org
	 * @throws InterruptedException 
	 */
	public static void extractResponseFileFromSwift(String org) throws InterruptedException {

		if (config.configSetup()) {

			String dir = AppConfig.getResponseFileSrcPath() + "received\\" + org + "\\";
			File srcDir = new File(dir);

			if (srcDir.exists()) {

				// Get list of the files inside the directory
				File[] listOfFiles = srcDir.listFiles();
				FileWriter fileWriterPath = null;
				BufferedWriter bufferedWriter = null;

				// If the directory is not empty , then...
				if (listOfFiles != null && listOfFiles.length > 0) {

					AppCommons.createFolderForTodayResponseFiles();
					String destinationWritingPath = AppCommons.getTodaysResponseFilesFolder(org);

					for (File file : listOfFiles) {

						// TODO - move the files from swift (src dir) to destination path (app path)
						// Moving only xml files

						if (AppCommons.isXMLFile(file.getName())) {

							try {

								// Reading the file and removing the schema tag if it is UNDP file
								String fileContent = AppCommons.removeSchemaTagFromUNDPFiles(file.toString());
								fileWriterPath = new FileWriter(destinationWritingPath + "\\" + file.getName());
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
					
				} else {
					
					loggMsg = "<Empty Directory> No file found in " + dir + " directory.";
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);
				}
			}
		}
	}

	/**
	 * This method returns the list of all files properties i.e. OrgnlMsgId,
	 * OrgnlNbOfTx of the file
	 * 
	 * @param org
	 * @return list of files
	 */
	public static ArrayList<ResponseFile> getListOfAllFilesProperties(String org) {

		ArrayList<ResponseFile> output = new ArrayList<ResponseFile>();

		if (config.configSetup() && Banner.appConfigCheck()) {

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
								output.add(responseFile);

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

		return output;
	}

	/**
	 * This method is used to extract transaction files from swift to specific
	 * orgnlMsgId folders
	 * 
	 * @param org
	 */
	public static void extractTransactionFilesFromSwift(String org) {

		if (config.configSetup()) {

			String dir = AppConfig.getResponseFileSrcPath() + "success\\" + org + "\\";
			File srcDir = new File(dir);

			if (srcDir.exists()) {

				// Get list of the transaction files inside the success source directory
				File[] listOfFiles = srcDir.listFiles();

				// Get list of Response files properties
				ArrayList<ResponseFile> fileListProperties = FileExtraction.getListOfAllFilesProperties(org);

				// For each response file
				for (ResponseFile responseFile : fileListProperties) {

					// Create a folder based on transaction Id
					String destinationWritingPath = AppCommons.getTransactionFilesFolder(org,
							responseFile.getOrgnlMsgId());

					// If the directory is not empty , then...
					if (listOfFiles != null && listOfFiles.length > 0) {

						for (File file : listOfFiles) {

							FileWriter fileWriterPath = null;
							BufferedWriter bufferedWriter = null;

							// Copy only XML files
							if (AppCommons.isXMLFile(file.getName())) {

								try {

									// Reading the file and removing the schema tag if it is UNDP file
									String fileContent = AppCommons.removeSchemaTagFromUNDPFiles(file.toString());
									String orgnlMsgId = AppCommons.getTransactionOrgnlMsgId(file.toString());

									// If the OrgnlMsgId of the transaction file matches the response file then copy
									// it
									// in side the folder with that transaction code
									if (orgnlMsgId.equals(responseFile.getOrgnlMsgId())) {

										fileWriterPath = new FileWriter(destinationWritingPath + "\\" + file.getName());
										bufferedWriter = new BufferedWriter(fileWriterPath);
										bufferedWriter.write(fileContent);
										bufferedWriter.close();

										errorMsg = "<Transaction File Moved> A transaction file with refernce ("
												+ orgnlMsgId + ") moved to" + destinationWritingPath;
										System.out.println(errorMsg);
										ActivityLogger.logActivity(errorMsg);

									}

								} catch (Exception exp) {
									errorMsg = "<File Content and Directory Error>" + exp.getClass().getSimpleName()
											+ "->" + exp.getCause() + "->" + exp.getMessage();
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
		}
	}

	/**
	 * This method is used to extract transaction files from the swift based on
	 * Organization code as input
	 * 
	 * @param org
	 * @throws IOException
	 */
	// second approach: supportive
	@Deprecated
	public static void extractTransactionFilesFromSwift2(String org) {

		if (config.configSetup()) {

			String dir = AppConfig.getResponseFileSrcPath() + "success\\" + org + "\\";
			File srcDir = new File(dir);

			if (srcDir.exists()) {

				// Get list of the files inside the directory
				File[] listOfFiles = srcDir.listFiles();
				FileWriter fileWriterPath = null;
				BufferedWriter bufferedWriter = null;

				ArrayList<ResponseFile> fileListProperties = FileExtraction.getListOfAllFilesProperties(org);

				// If the directory is not empty , then...
				if (listOfFiles != null && listOfFiles.length > 0) {

					for (File file : listOfFiles) {

						for (ResponseFile responseFile : fileListProperties) {

							String destinationWritingPath = AppCommons.getTransactionFilesFolder(org,
									responseFile.getOrgnlMsgId());

							if (AppCommons.isXMLFile(file.getName())) {

								try {

									// Reading the file and removing the schema tag if it is UNDP file
									String fileContent = AppCommons.removeSchemaTagFromUNDPFiles(file.toString());
									String orgnlMsgId = AppCommons.getTransactionOrgnlMsgId(file.toString());

									if (orgnlMsgId.equals(responseFile.getOrgnlMsgId())) {

										fileWriterPath = new FileWriter(destinationWritingPath + "\\" + file.getName());
										bufferedWriter = new BufferedWriter(fileWriterPath);
										bufferedWriter.write(fileContent);
										System.out.println(file.getName() + ": --> " + fileContent);
										bufferedWriter.close();

										errorMsg = "<Transaction File Moved> A transaction file with refernce ("
												+ orgnlMsgId + ") moved to" + destinationWritingPath;
										System.out.println(errorMsg);
										ActivityLogger.logActivity(errorMsg);

									}

								} catch (Exception exp) {
									errorMsg = "<File Content and Directory Error>" + exp.getClass().getSimpleName()
											+ "->" + exp.getCause() + "->" + exp.getMessage();
									System.out.println(errorMsg);
									ActivityLogger.logActivity(errorMsg);
								}
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
	}

	/**
	 * This method is used to extract transaction files from swift to specific
	 * orgnlMsgId folders
	 * 
	 * @param org
	 */
	// Using Java 7 standard copy method : supportive
	@Deprecated
	public static void extractTransactionFilesFromSwift3(String org) {

		if (config.configSetup()) {

			String dir = AppConfig.getResponseFileSrcPath() + "success\\" + org + "\\";
			File srcDir = new File(dir);

			if (srcDir.exists()) {

				// Get list of the files inside the directory
				File[] listOfFiles = srcDir.listFiles();

				ArrayList<ResponseFile> fileListProperties = FileExtraction.getListOfAllFilesProperties(org);

				// If the directory is not empty , then...
				if (listOfFiles != null && listOfFiles.length > 0) {

					for (File file : listOfFiles) {

						for (ResponseFile responseFile : fileListProperties) {

							String destinationWritingPath = AppCommons.getTransactionFilesFolder(org,
									responseFile.getOrgnlMsgId());
							srcDir = new File(dir + "\\" + file.getName());
							File destDir = new File(destinationWritingPath + "\\" + file.getName());

							if (AppCommons.isXMLFile(file.getName())) {

								try {

									String orgnlMsgId = AppCommons.getTransactionOrgnlMsgId(file.toString());

									if (orgnlMsgId.equals(responseFile.getOrgnlMsgId())) {

										Files.copy(srcDir.toPath(), destDir.toPath(),
												StandardCopyOption.REPLACE_EXISTING);

										errorMsg = "<Transaction File Moved> A transaction file with refernce ("
												+ orgnlMsgId + ") moved to" + destinationWritingPath;
										System.out.println(errorMsg);
										ActivityLogger.logActivity(errorMsg);

									}

								} catch (Exception exp) {
									errorMsg = "<File Content and Directory Error>" + exp.getClass().getSimpleName()
											+ "->" + exp.getCause() + "->" + exp.getMessage();
									System.out.println(errorMsg);
									ActivityLogger.logActivity(errorMsg);
								}
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
	}
}
