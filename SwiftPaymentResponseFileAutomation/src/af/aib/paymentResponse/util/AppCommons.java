package af.aib.paymentResponse.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.ResponseFile;
import af.aib.paymentResponse.model.TransactionFile;

/**
 * This class contains all the common methods used within the application
 * 
 * @author Hizbullah Watandost
 *
 */
public class AppCommons {

	private static AppConfig config = new AppConfig();

	// Error handling
	private static String errorMsg = "";

	// Logging message
	private static String loggMsg = "";

	/**
	 * This method returns the current get and time which is used as sub folder name
	 * to track the activity easily
	 * 
	 * @return: current date and time.
	 */
	public static String getCurrentDateTime() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
		LocalDateTime now = LocalDateTime.now();
		return (dtf.format(now));
	}

	/**
	 * This method read the complete content of a file from the path
	 * 
	 * @param filePath: The input parameter of the method will be the path of the
	 *                  file
	 * @return: Returns the the complete content of the file after reading it
	 */
	public static String readFileAllContent(String filePath) {

		String content = "";

		try {

			// Reading all the content of the file
			File file = new File(filePath);
			content = new String(Files.readAllBytes(Paths.get(filePath)));

		} catch (IOException exp) {

			errorMsg = "<Reading File Error>" + exp.getClass().getSimpleName() + "->" + exp.getCause() + "->"
					+ exp.getMessage();
			System.out.println(errorMsg);
			ActivityLogger.logActivity(errorMsg);
		}

		return content;
	}

	/**
	 * This method is used to check whether the file is XML file or not
	 * 
	 * @param fileName
	 * @return true if the file is xml else false
	 */
	public static boolean isXMLFile(String fileName) {

		String fileExtension = "";

		int j = fileName.lastIndexOf('.');

		if (j > 0) {

			fileExtension = "." + fileName.substring(j + 1);
		}

		if (fileExtension.equals(".xml"))
			return true;
		else
			return false;
	}

	/**
	 * This method returns the current date
	 * 
	 * @return: current date.
	 */
	public static String getCurrentDate() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime now = LocalDateTime.now();
		return (dtf.format(now));
	}

	/**
	 * This method creates folder for today response files i.e. received It is also
	 * creates two sub folders for undp and unicef inside parent folder (received)
	 */
	public static void createFolderForTodayResponseFiles() {

		if (config.configSetup()) {

			// Creating directory for todays response files
			String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "received";

			File fileWriterDir = new File(directory);

			if (!fileWriterDir.exists()) {

				if (fileWriterDir.mkdirs()) {

					loggMsg = "<Creating Response File Directory> A directory " + directory + " created sccessfully";
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);

					// Creating sub folders for UNDP and UNICEF response files
					ArrayList<String> subFolders = new ArrayList<>();

					subFolders.add("86154"); // UNDP response file
					subFolders.add("86570"); // UNICEF response file

					for (String subFolder : subFolders) {

						String dir = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "received" + "\\"
								+ subFolder;
						File subFileWriterDir = new File(dir);

						if (!subFileWriterDir.exists()) {

							if (subFileWriterDir.mkdirs()) {

								loggMsg = "<Creating Response File Sub Folder > A Folder " + directory + "\\"
										+ subFolder + " created sccessfully";
								System.out.println(loggMsg);
								ActivityLogger.logActivity(loggMsg);

							} else {

								loggMsg = "<Creating Response File Sub Folder Error> Failed to create folder "
										+ directory;
								System.out.println(loggMsg);
								ActivityLogger.logActivity(loggMsg);
							}

						} else {

							loggMsg = "<The Sub Folder Already Exist> The sub folder already exists!";
							System.out.println(loggMsg);
							ActivityLogger.logActivity(loggMsg);

						}
					}

				} else {

					loggMsg = "<Creating Response File Directory Error> Failed to create directory " + directory;
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);
				}

			} else {

				loggMsg = "<The Directory Already Exist> The directory already exist, in case you want to rerun the application please delete the todays created directory and rerun it!";
				System.out.println(loggMsg);
				ActivityLogger.logActivity(loggMsg);
			}
		}
	}

	/**
	 * This method is used to create a folder for duplicate response files
	 * 
	 * @param the organization name i.e. for which we want to move the duplicate
	 *            files
	 */
	public static void createFolderForDuplicateResponseFiles(String org) {

		if (config.configSetup()) {

			// Creating folder for duplicate response files
			String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "received" + "\\" + org + "\\"
					+ "duplicates";

			File fileWriterDir = new File(directory);

			if (!fileWriterDir.exists()) {

				if (fileWriterDir.mkdirs()) {

					loggMsg = "<Creating Folder For Duplicate Response Files> A folder " + directory
							+ " created sccessfully";
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);

				} else {

					loggMsg = "<Creating Duplicate Response File Folder Error> Failed to create folder " + directory;
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);
				}

			} else {

				loggMsg = "<The Duplicate Folder Already Exist> The duplicate folder already exists!";
				System.out.println(loggMsg);
				ActivityLogger.logActivity(loggMsg);
			}
		}
	}

	/**
	 * This method returns the specific response file destination folder
	 * 
	 * @param organization folder: the folder in which we want to store the response
	 *                     files
	 * @return the destination folder
	 */
	public static String getTodaysResponseFilesFolder(String org) {

		String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "received" + "\\" + org;

		File dir = new File(directory);

		if (!dir.exists()) {

			loggMsg = "<Today Response File Directory Not Found> Can not find the directory " + directory;
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
			directory = "";
		}

		return directory;
	}

	/**
	 * This method returns the specific transaction file destination folder
	 * 
	 * @param organization folder: the folder in which we want to store the response
	 *                     files
	 * @return the destination folder
	 */
	public static String getTodaysTransactionFilesFolder(String org) {

		String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "success" + "\\" + org;

		File dir = new File(directory);

		if (!dir.exists()) {

			loggMsg = "<Today Transaction File Directory Not Found> Can not find the directory " + directory;
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
			directory = "";
		}

		return directory;
	}

	/**
	 * This method is used to create folder for rejected transaction files
	 * 
	 * @param org
	 */
	public static void createFolderForRejectedTransactionFiles(String org) {

		if (config.configSetup()) {

			// Creating folder for duplicate response files
			String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "success" + "\\" + org + "\\"
					+ "rejected";

			File fileWriterDir = new File(directory);

			if (!fileWriterDir.exists()) {

				if (fileWriterDir.mkdirs()) {

					loggMsg = "<Creating Folder For Rejected Transaction Files> A folder " + directory
							+ " created sccessfully";
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);

				} else {

					loggMsg = "<Creating Rejected Transaction File Folder Error> Failed to create folder " + directory;
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);
				}

			} else {

				loggMsg = "<The Rejected Transaction Folder Already Exist> The duplicate folder already exists!";
				System.out.println(loggMsg);
				ActivityLogger.logActivity(loggMsg);
			}
		}
	}

	/**
	 * This method is used to get the transactions rejected folder based on
	 * organization folder code
	 * 
	 * @param org
	 * @return
	 */
	public static String getTodaysTransactionFilesRejectedFolder(String org) {

		String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "success" + "\\" + org + "\\"
				+ "rejected";

		File dir = new File(directory);

		if (!dir.exists()) {

			loggMsg = "<Today Response File Directory Not Found> Can not find the directory " + directory;
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
			directory = "";
		}

		return directory;
	}

	/**
	 * This method is used to create folder for merged transaction files
	 * 
	 * @param org
	 */
	public static void createFolderForMergedTransactionFiles(String org) {

		if (config.configSetup()) {

			// Creating folder for duplicate response files
			String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "success" + "\\" + org + "\\"
					+ "merged";

			File fileWriterDir = new File(directory);

			if (!fileWriterDir.exists()) {

				if (fileWriterDir.mkdirs()) {

					loggMsg = "<Creating Folder For Merged Files> A folder " + directory + " created sccessfully";
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);

				} else {

					loggMsg = "<Creating Merged File Folder Error> Failed to create folder " + directory;
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);
				}

			} else {

				loggMsg = "<The Merged Folder Already Exist> The merged folder already exists!";
				System.out.println(loggMsg);
				ActivityLogger.logActivity(loggMsg);
			}
		}
	}

	/**
	 * This method is used to get the today's merged transaction files folder
	 * 
	 * @param org
	 * @return
	 */
	public static String getTodaysTransactionFilesMergedFolder(String org) {

		String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "success" + "\\" + org + "\\"
				+ "merged";

		File dir = new File(directory);

		if (!dir.exists()) {

			loggMsg = "<Today Response Merged File Directory Not Found> Can not find the directory " + directory;
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
			directory = "";
		}

		return directory;
	}

	/**
	 * This method is used to return the organization specific duplicate folder
	 * 
	 * @param org: the organization for which we want to move the duplicate response
	 *             filess
	 * @return the duplicate directory
	 */
	public static String getDuplicateResponseFileDirectory(String org) {

		String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "received" + "\\" + org + "\\"
				+ "duplicates";

		File dir = new File(directory);

		if (!dir.exists()) {

			loggMsg = "<Duplicate Response File Directory Not Found> Can not find the directory " + directory;
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
			directory = "";
		}

		return directory;
	}

	/**
	 * This method is used to move a file from one path to another path
	 * 
	 * @param src:  the source path from which we want to move the file
	 * @param dest: the destination path in which we move the file
	 */
	public static void moveFile(String src, String dest) {

		Path result = null;

		try {

			result = Files.move(Paths.get(src), Paths.get(dest));

		} catch (IOException e) {

			loggMsg = "Exception while moving the file: " + e.getMessage();
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
		}

		if (result != null) {

			loggMsg = "The file has been moved from " + src + " to " + dest + " successfully!";
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);

		} else {

			loggMsg = "The file movement from " + src + " to " + dest + " failed!";
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
		}
	}

	/**
	 * This method removes the schema tag from UNDP files only based on the client
	 * system compatibility and request
	 * 
	 * @param the file path
	 * @return the transformed file content (if it is UNDP file it removes else it
	 *         ignores)
	 */
	public static String removeSchemaTagFromUNDPFiles(String filePath) { // the input to the method is filepath

		String fileContent = AppCommons.readFileAllContent(filePath);

		// If it is UNDP file which contains UNDP BICCODE
		if (fileContent.contains("<BkPtyId>GUS00352</BkPtyId>")) {

			fileContent = fileContent.replace("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ", "");
			loggMsg = "The shcema tag is removed from UNDP file -- " + filePath;
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
		}

		return fileContent;
	}

	/**
	 * This metod is used to get the file properties i.e. OrgnlMsgId and
	 * OrgnlNbOfTxs
	 * 
	 * @param filePath
	 * @return Response file model class
	 */
	public static ResponseFile getFileProperties(String filePath) {

		ResponseFile responseFile = new ResponseFile();
		String fileContent = AppCommons.readFileAllContent(filePath);

		String orgnlMsgId = "";
		int orgnlNbOfTxs = 0;

		if (fileContent.contains("<OrgnlMsgId>")) {
			orgnlMsgId = fileContent.substring(fileContent.indexOf("<OrgnlMsgId>") + "<OrgnlMsgId>".length(),
					fileContent.indexOf("</OrgnlMsgId>"));
		}

		if (fileContent.contains("<OrgnlNbOfTxs>")) {
			orgnlNbOfTxs = Integer
					.parseInt(fileContent.substring(fileContent.indexOf("<OrgnlNbOfTxs>") + "<OrgnlNbOfTxs>".length(),
							fileContent.indexOf("</OrgnlNbOfTxs>")));
		}
		
		responseFile.setFileName(Paths.get(filePath).getFileName().toString());
		responseFile.setOrgnlMsgId(orgnlMsgId);
		responseFile.setOrgnlNbOfTxs(orgnlNbOfTxs);

		return responseFile;
	}

	/**
	 * This method is used to create transaction files folder and sort (move all the
	 * transaction with same transaction code inside that folder) based on
	 * organization and file OrgnlMsgId
	 * 
	 * @param org      the organization folder code, the file OrgnlMsgId
	 * @param fileName
	 */
	public static void createTransactionFilesFolder(String org, String orgnlMsgId) {

		if (config.configSetup()) {

			// Creating folder for sorting and storing the transaction files
			String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "success" + "\\" + org + "\\"
					+ "temp" + "\\" + orgnlMsgId;

			File fileWriterDir = new File(directory);

			if (!fileWriterDir.exists()) {

				if (fileWriterDir.mkdirs()) {

					loggMsg = "<Creating Folder For Storing Transaction Files> A folder " + directory
							+ " created sccessfully";
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);

				} else {

					loggMsg = "<Creating Transactions File Folder Error> Failed to create folder " + directory;
					System.out.println(loggMsg);
					ActivityLogger.logActivity(loggMsg);
				}

			} else {

				loggMsg = "<The Transaction Files Folder Already Exists> The transactions file folder already exists!";
				System.out.println(loggMsg);
				ActivityLogger.logActivity(loggMsg);
			}
		}
	}

	/**
	 * This method returns the sorted transaction files folder
	 * 
	 * @param org
	 * @param orgnlMsgId
	 * @return
	 */
	public static String getTransactionFilesFolder(String org, String orgnlMsgId) {

		String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "success" + "\\" + org + "\\" + "temp"
				+ "\\" + orgnlMsgId;

		File dir = new File(directory);

		if (!dir.exists()) {

			loggMsg = "<Today Response File Directory Not Found> Can not find the directory " + directory;
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
			directory = "";
		}

		return directory;
	}

	/**
	 * This method returns the file transaction orgnlMsgId
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getTransactionOrgnlMsgId(String filePath) {

		String fileContent = AppCommons.readFileAllContent(filePath);
		String orgnlMsgId = "";

		if (fileContent.contains("<OrgnlMsgId>")) {

			orgnlMsgId = fileContent.substring(fileContent.indexOf("<OrgnlMsgId>") + "<OrgnlMsgId>".length(),
					fileContent.indexOf("</OrgnlMsgId>"));

		} else {

			loggMsg = "<Invalid Transaction File> The file doesn't have OrgnlMsgId";
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
		}
		return orgnlMsgId;
	}
	
	/**
	 * This method is used to get transaction or ACK files properties based on file path
	 * @param filePath
	 * @return
	 */
	public static TransactionFile getTransactionFileProperties(String filePath) {
		
		TransactionFile transactionFile = new TransactionFile();
		String fileContent = AppCommons.readFileAllContent(filePath);

		String fileName = "";
		String orgnlMsgId = "";
		int orgnlNbOfTxs = 0;

		fileName = Paths.get(filePath).getFileName().toString();
		

		if(fileContent.contains("<OrgnlMsgId>")) {
			orgnlMsgId = fileContent.substring(fileContent.indexOf("<OrgnlMsgId>") + "<OrgnlMsgId>".length(),
					fileContent.indexOf("</OrgnlMsgId>"));
		}
		
		transactionFile.setOrgnlMsgId(orgnlMsgId);
		
		// UNDP file
		if (fileContent.contains("<TxInfAndSts>")) {
			orgnlNbOfTxs = fileContent.split("<TxInfAndSts>").length - 1;
		}
		// UNICEF file
		if (fileContent.contains("<OrgnlPmtInfAndSts>")) {
			orgnlNbOfTxs = fileContent.split("<OrgnlPmtInfAndSts>").length - 1;
		}
		
		transactionFile.setOrgnlNbOfTxs(orgnlNbOfTxs);
		
		transactionFile.setFileName(fileName);
		transactionFile.setOrgnlMsgId(orgnlMsgId);
		transactionFile.setOrgnlNbOfTxs(orgnlNbOfTxs);

		return transactionFile;
	}

}
