package af.aib.paymentResponse.model;

import java.sql.Date;

/**
 * CustomPaymentFile model defines a complete row in database which represent a
 * complete (matched) payment files
 * 
 * @author hizwat
 *
 */
public class CustomPaymentFile {

	private String paymentRef;
	private String batchFileName;
	private int noOfTxn;
	private String ackMergedFileName;
	private int noOfScTxn;
	private int noOfPdTxn;
	private String insDate;
	private boolean isBatchFileSent;
	private boolean isAckFileSent;
	private String lastUpdateDate;
	private boolean isCompleted;

	public CustomPaymentFile() {
		super();
	}

	public CustomPaymentFile(String paymentRef, String fileName, int noOfTxn, String insDate, boolean isFileSent) {
		super();
		this.paymentRef = paymentRef;
		if (fileName.contains("FileLevel")) {
			this.batchFileName = fileName;
			this.noOfTxn = noOfTxn;
			this.isBatchFileSent = isFileSent;
		}

		else {
			this.ackMergedFileName = fileName;
			this.noOfScTxn = noOfTxn;
			this.isAckFileSent = isFileSent;
		}
		this.insDate = insDate;

	}

	public CustomPaymentFile(String paymentRef, String fileName, int noOfTxn, int noOfPdTxn) {
		super();
		this.paymentRef = paymentRef;
		this.ackMergedFileName = fileName;
		this.noOfScTxn = noOfTxn;
		this.noOfPdTxn = noOfPdTxn;

	}

	public CustomPaymentFile(String paymentRef, String batchFileName, int noOfTxn, String ackMergedFileName,
			int noOfScTxn, int noOfPdTxn, String insDate, boolean isBatchFileSent, boolean isAckFileSent,
			String lastUpdateDate, boolean isCompleted) {
		super();
		this.paymentRef = paymentRef;
		this.batchFileName = batchFileName;
		this.noOfTxn = noOfTxn;
		this.ackMergedFileName = ackMergedFileName;
		this.noOfScTxn = noOfScTxn;
		this.noOfPdTxn = noOfPdTxn;
		this.insDate = insDate;
		this.isBatchFileSent = isBatchFileSent;
		this.isAckFileSent = isAckFileSent;
		this.lastUpdateDate = lastUpdateDate;
		this.isCompleted = isCompleted;
	}

	public String getPaymentRef() {
		return paymentRef;
	}

	public void setPaymentRef(String paymentRef) {
		this.paymentRef = paymentRef;
	}

	public String getBatchFileName() {
		return batchFileName;
	}

	public void setBatchFileName(String batchFileName) {
		this.batchFileName = batchFileName;
	}

	public int getNoOfTxn() {
		return noOfTxn;
	}

	public void setNoOfTxn(int i) {
		this.noOfTxn = i;
	}

	public String getAckMergedFileName() {
		return ackMergedFileName;
	}

	public void setAckMergedFileName(String ackMergedFileName) {
		this.ackMergedFileName = ackMergedFileName;
	}

	public int getNoOfScTxn() {
		return noOfScTxn;
	}

	public void setNoOfScTxn(int i) {
		this.noOfScTxn = i;
	}

	public int getNoOfPdTxn() {
		return noOfPdTxn;
	}

	public void setNoOfPdTxn(int i) {
		this.noOfPdTxn = i;
	}

	public String getInsDate() {
		return insDate;
	}

	public void setInsDate(String string) {
		this.insDate = string;
	}

	public boolean isBatchFileSent() {
		return isBatchFileSent;
	}

	public void setBatchFileSent(boolean isBatchFileSent) {
		this.isBatchFileSent = isBatchFileSent;
	}

	public boolean isAckFileSent() {
		return isAckFileSent;
	}

	public void setAckFileSent(boolean isAckFileSent) {
		this.isAckFileSent = isAckFileSent;
	}

	public String getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(String string) {
		this.lastUpdateDate = string;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	@Override
	public String toString() {
		return "CustomPaymentFile [paymentRef=" + paymentRef + ", batchFileName=" + batchFileName + ", noOfTxn="
				+ noOfTxn + ", ackMergedFileName=" + ackMergedFileName + ", noOfScTxn=" + noOfScTxn + ", noOfPdTxn="
				+ noOfPdTxn + ", insDate=" + insDate + ", isBatchFileSent=" + isBatchFileSent + ", isAckFileSent="
				+ isAckFileSent + ", lastUpdateDate=" + lastUpdateDate + ", isCompleted=" + isCompleted + "]";
	}

	/**
	 * Method to be used to compare instance of the class Comparing two complete -
	 * matched payment file Used to detect the duplicate files before inserting to
	 * database
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CustomPaymentFile that = (CustomPaymentFile) o;
		if (batchFileName == null || that.batchFileName == null || ackMergedFileName == null
				|| that.ackMergedFileName == null)
			return false;
		return paymentRef.equals(that.paymentRef) && batchFileName.equals(that.batchFileName)
				&& noOfTxn == (that.noOfTxn) && ackMergedFileName.equals(that.ackMergedFileName)
				&& noOfScTxn == that.noOfScTxn && noOfPdTxn == that.noOfPdTxn && isBatchFileSent == that.isBatchFileSent
				&& isAckFileSent == that.isAckFileSent;
	}

}
