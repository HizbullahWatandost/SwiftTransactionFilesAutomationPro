package af.aib.paymentResponse.model;

import java.util.ArrayList;

public class RejectedFilesCollection {
	
	private ArrayList<ArrayList<String>> rejectedFiles;

	public RejectedFilesCollection() {
		super();
	}
	
	public RejectedFilesCollection(ArrayList<ArrayList<String>> rejectedFiles) {
		super();
		this.rejectedFiles = rejectedFiles;
	}

	public ArrayList<ArrayList<String>> getRejectedFiles() {
		return rejectedFiles;
	}

	public void setRejectedFiles(ArrayList<ArrayList<String>> rejectedFiles) {
		this.rejectedFiles = rejectedFiles;
	}

	@Override
	public String toString() {
		return "RejectedFilesCollection [rejectedFiles=" + rejectedFiles + ", getRejectedFiles()=" + getRejectedFiles()
				+ ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString()
				+ "]";
	}
	
}
