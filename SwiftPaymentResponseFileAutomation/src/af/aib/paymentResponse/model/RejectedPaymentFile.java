package af.aib.paymentResponse.model;

public class RejectedPaymentFile {

	private String paymentRef;
	private String fileName;
	private int noOfTxn;
	private String insDate;
	private boolean isFileSend;
	
	public RejectedPaymentFile() {
		super();
	}

	public RejectedPaymentFile(String paymentRef, String fileName, int noOfTxn) {
		super();
		this.paymentRef = paymentRef;
		this.fileName = fileName;
		this.noOfTxn = noOfTxn;
	}

	public RejectedPaymentFile(String paymentRef, String fileName, int noOfTxn, String insDate, boolean isFileSend) {
		super();
		this.paymentRef = paymentRef;
		this.fileName = fileName;
		this.noOfTxn = noOfTxn;
		this.insDate = insDate;
		this.isFileSend = isFileSend;
	}

	public String getPaymentRef() {
		return paymentRef;
	}

	public void setPaymentRef(String paymentRef) {
		this.paymentRef = paymentRef;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getNoOfTxn() {
		return noOfTxn;
	}

	public void setNoOfTxn(int noOfTxn) {
		this.noOfTxn = noOfTxn;
	}

	public String getInsDate() {
		return insDate;
	}

	public void setInsDate(String insDate) {
		this.insDate = insDate;
	}

	public boolean isFileSend() {
		return isFileSend;
	}

	public void setFileSend(boolean isFileSend) {
		this.isFileSend = isFileSend;
	}

	@Override
	public String toString() {
		return "RejectedPaymentFile [paymentRef=" + paymentRef + ", fileName=" + fileName + ", noOfTxn=" + noOfTxn
				+ ", insDate=" + insDate + ", isFileSend=" + isFileSend + "]";
	}	
}
