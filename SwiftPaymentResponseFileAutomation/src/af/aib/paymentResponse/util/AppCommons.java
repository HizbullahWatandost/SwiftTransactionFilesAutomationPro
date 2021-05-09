package af.aib.paymentResponse.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.db.DBServiceImpl;
import af.aib.paymentResponse.db.MSDBConnection;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.CustomPaymentFile;
import af.aib.paymentResponse.model.RejectedPaymentFile;
import af.aib.paymentResponse.model.ResponseFile;
import af.aib.paymentResponse.model.TransactionFile;
import af.aib.paymentResponse.robot.AIBSwiftPaymentFileRobot;

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

	private static MSDBConnection msdbConnection = new MSDBConnection();

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
					subFolders.add("86702"); // UNOPS customer 1
					subFolders.add("86754"); // UNOPS customer 2

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

			loggMsg = "<Today Response File Temp Directory Not Found> Can not find the directory " + directory;
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
	@Deprecated
	public static void createFolderForRejectedTransactionFiles(String org) {

		if (config.configSetup()) {

			// Creating folder for duplicate response files
			String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + org + "_pending";
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
	@Deprecated
	public static String getTodaysTransactionFilesRejectedFolder(String org) {

		String directory = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + org + "_pending";

		File dir = new File(directory);

		if (!dir.exists()) {

			loggMsg = "<Today Response File Rejected/Warning Directory Not Found> Can not find the directory "
					+ directory;
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
	public static void moveFile(boolean log, String src, String dest) {

		Path result = null;

		try {

			result = Files.move(Paths.get(src), Paths.get(dest));

		} catch (IOException e) {

			loggMsg = "Exception while moving the file: " + e.getMessage();
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
		}

		if (result != null) {

			if (log) {
				loggMsg = "The file has been moved from " + src + " to " + dest + " successfully!";
				System.out.println(loggMsg);
				ActivityLogger.logActivity(loggMsg);
			}

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

			loggMsg = "<Today Response File Temp Directory Not Found> Can not find the directory " + directory;
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
	 * This method is used to get transaction or ACK files properties based on file
	 * path
	 * 
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

		if (fileContent.contains("<OrgnlMsgId>")) {
			orgnlMsgId = fileContent.substring(fileContent.indexOf("<OrgnlMsgId>") + "<OrgnlMsgId>".length(),
					fileContent.indexOf("</OrgnlMsgId>"));
		}

		transactionFile.setOrgnlMsgId(orgnlMsgId);

		// UNDP file
		if (fileContent.contains("<TxInfAndSts>")) {
			orgnlNbOfTxs = fileContent.split("<TxInfAndSts>").length - 1;
		}
		// UNICEF & UNOPS file
		if (fileContent.contains("<OrgnlPmtInfAndSts>")) {
			orgnlNbOfTxs = fileContent.split("<OrgnlPmtInfAndSts>").length - 1;
		}

		transactionFile.setOrgnlNbOfTxs(orgnlNbOfTxs);

		transactionFile.setFileName(fileName);
		transactionFile.setOrgnlMsgId(orgnlMsgId);
		transactionFile.setOrgnlNbOfTxs(orgnlNbOfTxs);

		return transactionFile;
	}

	/**
	 * Deleting the file after processing them
	 * 
	 * @param folder: received or success
	 * @param org:    undp or unicef
	 */
	public static void deleteFilesOfDirectory(String folder, String org) {

		if (config.configSetup()) {

			String dir = AppConfig.getResponseFileSrcPath() + folder + "\\" + org + "\\";
			File srcDir = new File(dir);

			if (srcDir.exists()) {
				// Get list of the files inside the directory
				File[] listOfFiles = srcDir.listFiles();
				if (listOfFiles != null) {
					for (File f : listOfFiles) {
						if (f.isDirectory()) {
							deleteFilesOfDirectory(folder, org);
						} else {
							f.delete();
						}
					}
				}
			}
		}
	}

	/**
	 * Moving the file to uploaded folder after processing them
	 * 
	 * @param folder: received or success
	 * @param org:    undp or unicef or unops
	 */
	public static void moveFilesToUploadedFolder(String folder, String org) {

		if (config.configSetup()) {

			String sDir = AppConfig.getResponseFileSrcPath() + folder + "\\" + org + "\\";
			File srcDir = new File(sDir);

			String dDir = AppConfig.getResponseFileSrcPath() + folder + "\\" + org + "\\" + "uploaded" + "\\";
			File destDir = new File(dDir);

			if (srcDir.exists()) {

				if (!destDir.exists()) {

					destDir.mkdirs();
				}

				// Get list of the files inside the directory
				File[] listOfFiles = srcDir.listFiles();
				if (listOfFiles != null && listOfFiles.length > 0) {

					for (File file : listOfFiles) {

						if (AppCommons.isXMLFile(file.getName())) {

							try {
								// moving the file to uploaded folder
								AppCommons.moveFile(false, sDir + file.getName(), dDir + file.getName());

							} catch (Exception exp) {
								errorMsg = "<File Movement Error>" + exp.getClass().getSimpleName() + "->"
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

	/**
	 * Getting the rejected and warning files and moving them to the related pending
	 * folder
	 * 
	 * @param folder
	 * @param org
	 */
	public static void rejectedOrWarningFileAdvanceSearch(String folder, String org) {

		if (config.configSetup()) {

			// Path of today pending folder
			String pendingFilesPath = getTodayPendingFolder(org);

			// If the method is called on received folder
			if (folder.equals("received")) {

				// Get today (processed) files from received folder
				String fileLevelSrc = getTodaysResponseFilesFolder(org);
				File fileLevelSrcDir = new File(fileLevelSrc);

				// Finding rejected or warning payment files
				HashMap<String, Integer> rejectedAndWarningPaymentFiles = AIBSwiftPaymentFileRobot
						.getRejectedAndWarningPaymentFilesProperties(org);

				for (Map.Entry<String, Integer> rejectedFile : rejectedAndWarningPaymentFiles.entrySet()) {

					int xmlIndex = rejectedFile.getKey().lastIndexOf(".xml") + (".xml").length();
					String fileName = rejectedFile.getKey().substring(0, xmlIndex);

					// Moving the file to today's pending folder to trace the pendings (used for
					// safety purpose)
					if (fileLevelSrcDir.exists()) {

						if (AppCommons.isXMLFile(fileName)) {

							try {
								// moving the file back to the source path
								AppCommons.moveFile(false, fileLevelSrc + "\\" + fileName,
										pendingFilesPath + "\\" + fileName);

							} catch (Exception exp) {

								errorMsg = "<File Movement Error>" + exp.getClass().getSimpleName() + "->"
										+ exp.getCause() + "->" + exp.getMessage();
								System.out.println(errorMsg);
								ActivityLogger.logActivity(errorMsg);
							}
						}
					}
				}

			}

			// If the method is called on success files
			if (folder.equals("success")) {
				// copying the rejected and warning ack file
				String sDir = getTodayPendingFolder(org);
				File srcDir = new File(sDir);

				if (srcDir.exists()) {
					// Get list of the files inside the directory
					File[] listOfFiles = srcDir.listFiles();
					if (listOfFiles != null) {
						for (File file : listOfFiles) {
							if (AppCommons.isXMLFile(file.getName())) {
								// If it is ack files then,
								if (!file.getName().contains("FileLevel.xml")) {

									try {

										// moving the file back to the source path
										AppCommons.moveFile(false, file.toString(),
												pendingFilesPath + "\\" + file.getName());
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
	 * Double checking the rejected and warning files inside pending folder and in
	 * case of match based on database, send them to Swift same as other complete
	 * and matched file to avoid business process delay and customer complaints.
	 * Only the matched file will be send to avoid delay in ACK file. The new or
	 * unmatched file will remain in pending status in pending folder and will be
	 * rechecked in next software process.
	 * 
	 * @param org
	 */
	public static void rectifyingWarningAndRejectedFiles(String org) {

		if (config.configSetup()) {

			String srcPath = getTodayPendingFolder(org);
			String destPath;
			File srcDir = new File(srcPath);

			if (srcDir.exists()) {
				File[] filesList = srcDir.listFiles();
				ResponseFile responseFile;
				CustomPaymentFile cpf;
				for (File file : filesList) {

					// check its file level whether exist or not
					responseFile = getFileProperties(file.toString());
					cpf = DBServiceImpl.getPaymentFile(responseFile.getOrgnlMsgId());
					// If there is a record with its reference in database
					if (cpf != null && cpf.getBatchFileName() != null) {
						// If it is stored in database in case of FileLevel or its FileLevel exist in
						// database in case it is ACK file
						if (cpf.getBatchFileName().equals(file.getName())
								|| (cpf.getAckMergedFileName().contains(file.getName())
										&& cpf.getBatchFileName() != null)) {

							if (AppCommons.isXMLFile(file.getName())) {

								try {
									// If it is FileLeve then move it back to the source path
									if (file.getName().contains("FileLevel")) {
										destPath = getTodaysResponseFilesFolder(org);
										File dDir = new File(destPath);

										if (!dDir.exists()) {
											if (dDir.mkdirs()) {
												loggMsg = "<Creating Folder For File Level> A folder " + destPath
														+ " created sccessfully";
												System.out.println(loggMsg);
												ActivityLogger.logActivity(loggMsg);
											} else {
												loggMsg = "<Creating Folder For File Level> Failed to create folder "
														+ destPath;
												System.out.println(loggMsg);
												ActivityLogger.logActivity(loggMsg);
											}
										}

										AppCommons.moveFile(false, srcPath + "\\" + file.getName(),
												destPath + "\\" + file.getName());
									}
									// If it is ACK file
									else {
										destPath = getTodaysTransactionFilesMergedFolder(org);
										File dDir = new File(destPath);
										if (!dDir.exists()) {
											if (dDir.mkdirs()) {
												loggMsg = "<Creating Folder For Merged ACK Files> A folder " + destPath
														+ " created sccessfully";
												System.out.println(loggMsg);
												ActivityLogger.logActivity(loggMsg);

											} else {
												loggMsg = "<Creating Folder For Merged ACK Files> Failed to create folder "
														+ destPath;
												System.out.println(loggMsg);
												ActivityLogger.logActivity(loggMsg);
											}

										}
										AppCommons.moveFile(false, srcPath + "\\" + file.getName(),
												destPath + "\\" + file.getName());
									}

								} catch (Exception exp) {

									errorMsg = "<File Movement Error>" + exp.getClass().getSimpleName() + "->"
											+ exp.getCause() + "->" + exp.getMessage();
									System.out.println(errorMsg);
									ActivityLogger.logActivity(errorMsg);
								}
							}
						}

					}

				}
			} else {
				errorMsg = "<Pending Paht not Found> There is no pending folder with " + srcPath + " name";
				System.out.println(errorMsg);
				ActivityLogger.logActivity(errorMsg);
			}
		}

	}

	/**
	 * Moving the rejected or warning files (the files which is new and their
	 * FileLevel doesn't exist). These files will be checked in next trail alongside
	 * with other new files tomorrow
	 * 
	 * @param org
	 */
	public static void moveRejectedOrWarningFilesBackToSwift(String org) {

		// Copying the file level rejected/warning/pending files back to the source
		// folder to be processed next day
		String srcPath = getTodayPendingFolder(org);
		File pendingFilesDir = new File(srcPath);
		String destPath;
		if (pendingFilesDir.exists()) { // Get list of the files inside the directory
			File[] listOfFiles = pendingFilesDir.listFiles();

			if (listOfFiles != null) {
				for (File file : listOfFiles) {
					if (AppCommons.isXMLFile(file.getName())) {
						if (file.getName().contains("FileLevel")) {
							destPath = AppConfig.getResponseFileSrcPath() + "received\\" + org + "\\";
						} else {
							destPath = AppConfig.getResponseFileSrcPath() + "success\\" + org + "\\";
						}
						try {

							String fileContent = readFileAllContent(file.toString());
							FileWriter fileWriterPath = new FileWriter(destPath + "\\" + file.getName());
							BufferedWriter bufferedWriter = new BufferedWriter(fileWriterPath);
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
				loggMsg = "<No Pending or Rejected File> there is no pending or rejected file.";
				System.out.println(errorMsg);
				ActivityLogger.logActivity(errorMsg);
			}
		}
	}

	/**
	 * this method creates a folder for today's pending payment and transaction
	 * files
	 * 
	 * @param org
	 */
	public static void createTodayPendingFolder(String org) {

		String destPath = "";

		// UNDP
		if (org.equals("86154")) {

			destPath = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "undp" + "_pending";

			// UNICEF
		} else if (org.equals("86570")) {

			destPath = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "unicef" + "_pending";

			// UNOPS
		} else if (org.equals("86702") || org.equals("86754")) {

			destPath = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "unops_" + org + "_pending";
		} else {

			loggMsg = "<Unknown Organization Detected> The organization name " + org + " is unknown to this software!";
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
		}

		File destDir = new File(destPath);

		if (!destDir.exists()) {

			if (destDir.mkdirs()) {

				loggMsg = "<Creating Folder For Pending Payment/Transaction Files> A folder " + destPath
						+ " created sccessfully";
				System.out.println(loggMsg);
				ActivityLogger.logActivity(loggMsg);

			} else {

				loggMsg = "<Creating Rejected Payment/Transaction File Folder Error> Failed to create folder "
						+ destPath;
				System.out.println(loggMsg);
				ActivityLogger.logActivity(loggMsg);
			}

		} else {

			loggMsg = "<The Rejected Payment/Transaction Folder Already Exist> The duplicate folder already exists!";
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
		}
	}

	/**
	 * this method is used to get the today's pending payment and transaction files
	 * folder
	 * 
	 * @param org
	 * @return
	 */
	public static String getTodayPendingFolder(String org) {

		String destPath = "";

		if (org.equals("86154")) {

			destPath = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "undp" + "_pending";

		} else if (org.equals("86570")) {

			destPath = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "unicef" + "_pending";

		} else if (org.equals("86702") || org.equals("86754")) {

			destPath = AppConfig.getAppRootPath() + getCurrentDate() + "\\" + "unops_" + org + "_pending";
		} else {

			loggMsg = "<Unknown Organization Detected> The organization name " + org + " is unknown to this software!";
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
		}

		File destDir = new File(destPath);

		if (!destDir.exists()) {

			loggMsg = "\n<Today " + org + "_pending folder Not Found> Can not find the directory " + destPath;
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);
			destPath = "";
		}

		return destPath;
	}

	/**
	 * This method is used to display the process percentage
	 * 
	 * @throws InterruptedException
	 */
	public static void processFiles() throws InterruptedException {

		if (config.configSetup()) {

			loggMsg = "\nPlease wait, I am checking and processing the files ...";
			System.out.println(loggMsg);
			ActivityLogger.logActivity(loggMsg);

			loggMsg = ".";
			for (int i = 0; i <= 100; i++) {

				if (i == 0 || i == 25 || i == 50 || i == 75 || i == 100)
					loggMsg = i + "%";
				else
					loggMsg = ".";
				System.out.print(loggMsg);
				Thread.sleep(200);
			}

			System.out.println("\n>>> Hey, I am done with processing the files");
			loggMsg += "\n>>> Hey, I am done with processing the files";
			ActivityLogger.logActivity(loggMsg);
		}
	}

	/**
	 * This method is used to collect the matched file (FileLevel + ACK file) in one
	 * collection
	 * 
	 * @param filesCollection
	 * @return
	 */
	public static ArrayList<CustomPaymentFile> collectMatchedFiles(ArrayList<ArrayList<String>> filesCollection) {

		ArrayList<CustomPaymentFile> paymentFiles = new ArrayList<>();
		CustomPaymentFile customPaymentFile;
		String fName = "";

		for (int i = 0; i < filesCollection.size(); i++) {
			customPaymentFile = new CustomPaymentFile();
			customPaymentFile.setPaymentRef(
					filesCollection.get(i).get(0).substring(filesCollection.get(i).get(0).indexOf("|") + 2));
			customPaymentFile.setBatchFileName(
					filesCollection.get(i).get(0).substring(0, filesCollection.get(i).get(0).indexOf("|") - 1));
			customPaymentFile.setNoOfTxn(Integer.parseInt(filesCollection.get(i).get(1)));
			customPaymentFile.setAckMergedFileName(
					filesCollection.get(i).get(2).substring(0, filesCollection.get(i).get(2).indexOf("|") - 1));
			customPaymentFile.setNoOfScTxn(Integer.parseInt(filesCollection.get(i).get(3)));
			customPaymentFile.setNoOfPdTxn(
					Integer.parseInt(filesCollection.get(i).get(1)) - Integer.parseInt(filesCollection.get(i).get(3)));
			customPaymentFile
					.setInsDate(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));

			fName = filesCollection.get(i).get(0).substring(0, filesCollection.get(i).get(0).indexOf("|") - 1);
			int noOfPdTxn = Integer.parseInt(filesCollection.get(i).get(1))
					- Integer.parseInt(filesCollection.get(i).get(3));
			customPaymentFile.setBatchFileSent(false);
			fName = filesCollection.get(i).get(2).substring(0, filesCollection.get(i).get(2).indexOf("|") - 1);
			customPaymentFile.setAckFileSent(false);
			customPaymentFile
					.setLastUpdateDate(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));
			paymentFiles.add(customPaymentFile);
		}

		return paymentFiles;
	}

	/**
	 * This method is used to collect the rejected files in one collection
	 * 
	 * @param filesCollection
	 * @return
	 */
	public static ArrayList<RejectedPaymentFile> collectRejectedFiles(HashMap<String, Integer> filesCollection) {

		ArrayList<RejectedPaymentFile> rejectedFiles = new ArrayList<RejectedPaymentFile>();
		RejectedPaymentFile rejectedPaymentFile;

		for (Map.Entry<String, Integer> rejectedFile : filesCollection.entrySet()) {
			rejectedPaymentFile = new RejectedPaymentFile();
			rejectedPaymentFile.setPaymentRef(rejectedFile.getKey().substring(rejectedFile.getKey().indexOf("|") + 2));
			rejectedPaymentFile.setFileName(rejectedFile.getKey().substring(0, rejectedFile.getKey().indexOf("|") - 1));
			rejectedPaymentFile.setNoOfTxn(rejectedFile.getValue());
			rejectedPaymentFile
					.setInsDate(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));
			rejectedPaymentFile.setFileSend(false);

			rejectedFiles.add(rejectedPaymentFile);
		}

		return rejectedFiles;
	}
}
