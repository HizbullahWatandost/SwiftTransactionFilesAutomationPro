package af.aib.paymentResponse.robot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.db.DBServiceImpl;
import af.aib.paymentResponse.log.ActivityLogger;
import af.aib.paymentResponse.model.CustomPaymentFile;
import af.aib.paymentResponse.model.RejectedPaymentFile;
import af.aib.paymentResponse.model.ResponseFile;
import af.aib.paymentResponse.model.TransactionFile;
import af.aib.paymentResponse.util.AppCommons;
import af.aib.paymentResponse.util.CombinerAutoBanner;

/**
 * This class just work like a robot which checks which payments file and ACK
 * files should be sent to Swift and which not.
 * 
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

	/**
	 * This method is used to return no of payments file based on organization id
	 * 
	 * @param org
	 * @return
	 * @throws InterruptedException
	 */
	public static HashMap<String, Integer> checkNoOfPaymentsInFile(String org) {

		// It stores no of payments in key:value pair
		HashMap<String, Integer> noOfPaymentsInFile = new HashMap<String, Integer>();

		if (config.configSetup() && CombinerAutoBanner.appConfigCheck()) {

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
								noOfPaymentsInFile.put(
										responseFile.getFileName() + " | " + responseFile.getOrgnlMsgId(),
										responseFile.getOrgnlNbOfTxs());

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

	/**
	 * This method is used to return no of transactions files properties based on
	 * organization
	 * 
	 * @param org
	 * @return
	 * @throws InterruptedException
	 */
	public static HashMap<String, Integer> checkNoOfTxnInAckFile(String org) {

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
								noOfTxnInAckFile.put(
										transactionFile.getFileName() + " | " + transactionFile.getOrgnlMsgId(),
										transactionFile.getOrgnlNbOfTxs());

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

	/**
	 * this method is used to return number of matched files pair (payment and
	 * transaction - ACK) files, the files which orgnlMsgId matches
	 * 
	 * @param org
	 * @return
	 * @throws InterruptedException
	 */
	public static ArrayList<ArrayList<String>> matchPaymentFileAndAckFilesProperties(String org) {

		ArrayList<ArrayList<String>> matchedFilesProps = new ArrayList<ArrayList<String>>();

		HashMap<String, Integer> paymentFiles = checkNoOfPaymentsInFile(org);
		HashMap<String, Integer> ackFiles = checkNoOfTxnInAckFile(org);

		paymentFiles.forEach((pKey, pValue) -> {

			pKeyFileOrgnlMsgId = "";

			if (pKey.contains("|")) {

				pKeyFileOrgnlMsgId = pKey.substring(pKey.indexOf("|") + ("|").length() + 1);
			}

			ackFiles.forEach((tKey, tValue) -> {

				fileProps = new ArrayList<String>();
				tKeyFileOrgnlMsgId = "";

				tKeyFileOrgnlMsgId = tKey.substring(tKey.indexOf("|") + ("|").length() + 1);

				if (pKeyFileOrgnlMsgId.equals(tKeyFileOrgnlMsgId)) {

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

	/**
	 * This method is used to return the rejected payment files. the files which
	 * their OrgnlMsgId do not match to any transaction files
	 * 
	 * @param org
	 * @return
	 * @throws InterruptedException
	 */
	public static HashMap<String, Integer> getRejectedAndWarningPaymentFilesProperties(String org) {

		HashMap<String, Integer> allPaymentFiles = checkNoOfPaymentsInFile(org);

		for (int i = 0; i < matchPaymentFileAndAckFilesProperties(org).size(); i++) {
			allPaymentFiles.remove(matchPaymentFileAndAckFilesProperties(org).get(i).get(0));
		}

		return allPaymentFiles;
	}

	/**
	 * This method is used to get the rejected transaction files. The files which
	 * their OrgnlMsgId do not match to any payment level files.
	 * 
	 * @param org
	 * @return
	 * @throws InterruptedException
	 */
	public static HashMap<String, Integer> getRejectedAndWarningTxnFilesProperties(String org) {
		// It stores no of payments in key:value pair
		HashMap<String, Integer> rejectedTxnFilesProps = new HashMap<String, Integer>();

		if (config.configSetup()) {

			String dir = AppCommons.getTodayPendingFolder(org);

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
								rejectedTxnFilesProps.put(
										transactionFile.getFileName() + " | " + transactionFile.getOrgnlMsgId(),
										transactionFile.getOrgnlNbOfTxs());

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

		return rejectedTxnFilesProps;
	}

	/**
	 * This method is used to store the matched payment file pair (FileLevel + ACK)
	 * to database
	 * 
	 * @param paymentFiles
	 */
	public static void storePaymentFiles(ArrayList<ArrayList<CustomPaymentFile>> paymentFiles) {
		CustomPaymentFile cpf;
		// Looping through the payment files collection
		for (ArrayList<CustomPaymentFile> paymentFileCollection : paymentFiles)
			// Looping through each payment file in collection
			for (CustomPaymentFile paymentFile : paymentFileCollection) {
				// Get the payment file from db
				cpf = DBServiceImpl.getPaymentFile(paymentFile.getPaymentRef());
				// If the payment exist in database
				if (cpf != null) {
					// If the new file is not equal to the file stored in database
					if (!cpf.equals(paymentFile) & !cpf.isCompleted()) {

						// If the new payment file no of transaction is greater or equal to the old file
						// (stored) in database and its no of transaction is not equal to its no of
						// received ACKs files.
						if (paymentFile.getNoOfTxn() >= cpf.getNoOfTxn()
								&& paymentFile.getNoOfTxn() != paymentFile.getNoOfScTxn()) {
							// If the payment file already has ACK files stored in database, then append the
							// new ACK file
							if (cpf.getAckMergedFileName() != null) {
								paymentFile.setAckMergedFileName(
										cpf.getAckMergedFileName() + "," + paymentFile.getAckMergedFileName());
								paymentFile.setNoOfScTxn(cpf.getNoOfScTxn() + paymentFile.getNoOfScTxn());
								paymentFile.setNoOfPdTxn(paymentFile.getNoOfTxn() - paymentFile.getNoOfScTxn());
								paymentFile.setInsDate(cpf.getInsDate());
								paymentFile.setCompleted(
										paymentFile.getNoOfTxn() == paymentFile.getNoOfScTxn() ? true : false);
								DBServiceImpl.updatePaymentFile(cpf.getPaymentRef(), paymentFile);

								// If the payment file is new, or it doesn't have any ACK file in database
							} else {
								paymentFile.setInsDate(cpf.getInsDate());
								paymentFile.setCompleted(
										paymentFile.getNoOfTxn() == paymentFile.getNoOfScTxn() ? true : false);
								DBServiceImpl.updatePaymentFile(cpf.getPaymentRef(), paymentFile);

							}
							// If the payment file no of transaction is equal to no of received transaction
							// ACKs.
						} else if (paymentFile.getNoOfTxn() == paymentFile.getNoOfScTxn()) {
							paymentFile.setInsDate(cpf.getInsDate());
							paymentFile.setCompleted(true);
							DBServiceImpl.updatePaymentFile(cpf.getPaymentRef(), paymentFile);
						}

						// If the file pair is completed, all its ACKs are received (no of txn = no of
						// sc txn)
					} else if (cpf.isCompleted()) {
						loggMsg = "<Complete Payment File> file is completed and alread sent to Swift";
						System.out.println(loggMsg);
						ActivityLogger.logActivity(loggMsg);
					} else {
						loggMsg = "<Duplicate Payment File> the payment file is already stored in database";
						System.out.println(loggMsg);
						ActivityLogger.logActivity(loggMsg);
					}

					// If the payment file is new and there is no payment reference with the new one
					// then store it in database
				} else {
					DBServiceImpl.savePaymentFile(paymentFile);
				}

			}
	}

	/**
	 * This method is to store the rejected or warning payment file in database
	 * 
	 * @param rejectedFileCollection
	 */
	public static void checkRejectedPaymentFiles(ArrayList<ArrayList<RejectedPaymentFile>> rejectedFileCollection) {

		CustomPaymentFile cpf;

		// looping through the rejected payment file collection
		for (ArrayList<RejectedPaymentFile> rejectedFiles : rejectedFileCollection) {
			// Looping through each rejected payment file
			for (RejectedPaymentFile rejectedFile : rejectedFiles) {
				// get the rejected file with same reference from table
				cpf = DBServiceImpl.getPaymentFile(rejectedFile.getPaymentRef());
				// If it exist
				if (cpf != null) {
					// If the new rejected file is not completed
					if (!cpf.isCompleted()) {
						CustomPaymentFile ncpf = cpf;
						// If the rejected file is FileLevel and it exist in database
						if (rejectedFile.getFileName().equals(cpf.getBatchFileName())) {
							// If the new FileLevel with same name contains more no.of.txn compare to the
							// old one
							if (rejectedFile.getNoOfTxn() > cpf.getNoOfTxn()) {
								ncpf.setBatchFileName(rejectedFile.getFileName());
								ncpf.setNoOfTxn(rejectedFile.getNoOfTxn());
								ncpf.setNoOfPdTxn(rejectedFile.getNoOfTxn() - cpf.getNoOfScTxn());
								ncpf.setBatchFileSent(rejectedFile.isFileSend());
								ncpf.setLastUpdateDate(rejectedFile.getInsDate());
								ncpf.setCompleted(rejectedFile.getNoOfTxn() == cpf.getNoOfScTxn() ? true : false);
								DBServiceImpl.updatePaymentFile(rejectedFile.getPaymentRef(), ncpf);

							}
						}
						// If the new rejected file is ACK file and it exist in database
						else if (rejectedFile.getFileName().equals(cpf.getAckMergedFileName())) {
							// if the new ACK file with same name has more no.of.sc.txn compare to the old
							// one
							if (rejectedFile.getNoOfTxn() > cpf.getNoOfScTxn()) {
								ncpf.setAckMergedFileName(rejectedFile.getFileName());
								ncpf.setNoOfScTxn(rejectedFile.getNoOfTxn());
								ncpf.setNoOfPdTxn(cpf.getNoOfTxn() - ncpf.getNoOfScTxn());
								ncpf.setLastUpdateDate(rejectedFile.getInsDate());
								ncpf.setAckFileSent(rejectedFile.isFileSend());
								ncpf.setCompleted(rejectedFile.getNoOfTxn() == cpf.getNoOfTxn() ? true : false);
								DBServiceImpl.updatePaymentFile(rejectedFile.getPaymentRef(), ncpf);
							}
							// If the new file is not stored in database and it is a FileLevel
						} else if (!rejectedFile.getFileName().equals(cpf.getBatchFileName())
								&& rejectedFile.getFileName().contains("FileLevel")) {
							ncpf.setBatchFileName(rejectedFile.getFileName());
							ncpf.setLastUpdateDate(rejectedFile.getInsDate());
							ncpf.setNoOfTxn(rejectedFile.getNoOfTxn());
							ncpf.setNoOfPdTxn(rejectedFile.getNoOfTxn() - ncpf.getNoOfScTxn());
							ncpf.setCompleted(rejectedFile.getNoOfTxn() == cpf.getNoOfScTxn() ? true : false);
							DBServiceImpl.updatePaymentFile(rejectedFile.getPaymentRef(), ncpf);

							// If the new file is not stored in database and it is ACK file
						} else if (!rejectedFile.getFileName().equals(cpf.getAckMergedFileName())
								&& !rejectedFile.getFileName().contains("FileLevel")) {
							// If there is no ACK file in database
							if (cpf.getAckMergedFileName() == null) {
								ncpf.setAckMergedFileName(rejectedFile.getFileName());
								ncpf.setNoOfScTxn(rejectedFile.getNoOfTxn());
								ncpf.setNoOfPdTxn(cpf.getNoOfTxn() - rejectedFile.getNoOfTxn());

								// If there is already an ACK file with that payment reference in database
							} else {
								// If the new ACK file contains a complete no of SC txn.
								if (ncpf.getNoOfTxn() == rejectedFile.getNoOfTxn()) {
									ncpf.setAckMergedFileName(rejectedFile.getFileName());
									ncpf.setNoOfScTxn(rejectedFile.getNoOfTxn());

									// If the new ACK file contains incomplete no of SC txn.
								} else {
									ncpf.setAckMergedFileName(
											cpf.getAckMergedFileName() + "," + rejectedFile.getFileName());
									ncpf.setNoOfScTxn(cpf.getNoOfScTxn() + rejectedFile.getNoOfTxn());
								}

								// If there is already FileLevel stored in database with the new rejected
								// payment ACK reference
								if (cpf.getBatchFileName() != null) {
									// update no of pending accordingly
									ncpf.setNoOfPdTxn(cpf.getNoOfPdTxn() - rejectedFile.getNoOfTxn());

									// If there is no FileLevel with new rejected file reference in database
								} else {
									// Setting no of pending to -1
									ncpf.setNoOfPdTxn(-1);
								}

							}

							ncpf.setAckFileSent(rejectedFile.isFileSend());
							ncpf.setLastUpdateDate(rejectedFile.getInsDate());
							ncpf.setCompleted(rejectedFile.getNoOfTxn() == cpf.getNoOfScTxn() ? true : false);

							DBServiceImpl.updatePaymentFile(rejectedFile.getPaymentRef(), ncpf);
						}
					}

					// If the new rejected file is not already stored in database and there is no
					// reference match to its reference then store it in database
				} else {
					DBServiceImpl.savePaymentFile(
							new CustomPaymentFile(rejectedFile.getPaymentRef(), rejectedFile.getFileName(),
									rejectedFile.getNoOfTxn(), rejectedFile.getInsDate(), rejectedFile.isFileSend()));
				}

			}
		}
	}

	/**
	 * This method is used to change the status of the each file to true in database
	 * after sending it to Swift
	 * 
	 * @param folder
	 * @param org
	 */
	public static void verifyingFileSendingToSwift(String folder, String org) {
		String srcPath = null;
		CustomPaymentFile cpf = null;
		ResponseFile responseFile = null;

		if ("received".equals(folder)) {
			srcPath = AppCommons.getTodaysResponseFilesFolder(org);
		} else if ("success".equals(folder)) {
			srcPath = AppCommons.getTodaysTransactionFilesMergedFolder(org);
		} else {

			errorMsg = "<Invalid Folder Name> unknown folder has been given '" + folder
					+ "', the folder name should be either received or success";
			System.out.println(errorMsg);
			ActivityLogger.logActivity(errorMsg);
		}

		File srcDir = new File(srcPath);

		File[] files = srcDir.listFiles();
		if (files != null) {

			for (File file : files) {

				responseFile = AppCommons.getFileProperties(file.toString());
				cpf = DBServiceImpl.getPaymentFile(responseFile.getOrgnlMsgId());

				if (cpf != null) {

					if (AppCommons.isXMLFile(file.getName())) {
						if (file.getName().contains("FileLevel")) {

							if (cpf.getBatchFileName().equals(file.getName())) {

								cpf.setBatchFileSent(true);
								DBServiceImpl.updatePaymentFile(cpf.getPaymentRef(), cpf);
								loggMsg = "<Sending FileLevel File to Swift> the payment file '" + file.getName()
										+ "' is has been sent to Swift successfully!";
								System.out.println(loggMsg);
								ActivityLogger.logActivity(loggMsg);
							} else {
								errorMsg = "<Unknown Payment File> the payment file '" + file.getName()
										+ "' is unknown!";
								System.out.println(errorMsg);
								ActivityLogger.logActivity(errorMsg);
							}
						} else {
							if (cpf.getAckMergedFileName().contains(file.getName())) {

								cpf.setAckFileSent(true);
								DBServiceImpl.updatePaymentFile(cpf.getPaymentRef(), cpf);
								loggMsg = "<Sending ACK File to Swift> the ACK file '" + file.getName()
										+ "' is has been sent to Swift successfully!";
								System.out.println(loggMsg);
								ActivityLogger.logActivity(loggMsg);
							} else {
								errorMsg = "<Unknown ACK File> the ACK file '" + file.getName() + "' is unknown!";
								System.out.println(errorMsg);
								ActivityLogger.logActivity(errorMsg);
							}
						}
					} else {
						errorMsg = "<Invalid File> the file '" + file.getName()
								+ "' is invalid! It must be an xml file";
						System.out.println(errorMsg);
						ActivityLogger.logActivity(errorMsg);
					}

				} else {
					errorMsg = "<File Not Exist in Database> no files '" + file.getName() + "' found in database";
					System.out.println(errorMsg);
					ActivityLogger.logActivity(errorMsg);
				}

			}
		} else {
			errorMsg = "<Empty Directory> no files found in directory!" + srcPath;
			System.out.println(errorMsg);
			ActivityLogger.logActivity(errorMsg);
		}
	}
}
