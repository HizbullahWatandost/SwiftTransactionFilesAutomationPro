package af.aib.paymentResponse.model;

// The model class for Response file
public class ResponseFile {
	
	private String fileName;
	// Response file OrgnlMsgId attribute 
	private String orgnlMsgId;
	// Response file OrgnlNbOfTxs attribute
	private int orgnlNbOfTxs;
	
	// Default constructor
	public ResponseFile() {
		super();
	}

	// Parameterized constructor
	public ResponseFile(String fileName, String orgnlMsgId, int orgnlNbOfTxs) {
		super();
		this.fileName = fileName;
		this.orgnlMsgId = orgnlMsgId;
		this.orgnlNbOfTxs = orgnlNbOfTxs;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getOrgnlMsgId() {
		return orgnlMsgId;
	}

	public void setOrgnlMsgId(String orgnlMsgId) {
		this.orgnlMsgId = orgnlMsgId;
	}

	public int getOrgnlNbOfTxs() {
		return orgnlNbOfTxs;
	}

	public void setOrgnlNbOfTxs(int orgnlNbOfTxs) {
		this.orgnlNbOfTxs = orgnlNbOfTxs;
	}

	@Override
	public String toString() {
		return "ResponseFile [fileName=" + fileName + ", orgnlMsgId=" + orgnlMsgId + ", orgnlNbOfTxs=" + orgnlNbOfTxs
				+ "]";
	}

	
}
