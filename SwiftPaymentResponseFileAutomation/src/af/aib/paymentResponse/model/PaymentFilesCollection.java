package af.aib.paymentResponse.model;

import java.util.ArrayList;

public class PaymentFilesCollection {
	
	private ArrayList<ArrayList<String>> paymentsCollection;

	public PaymentFilesCollection() {
		super();
	}

	
	public PaymentFilesCollection(ArrayList<ArrayList<String>> paymentsCollection) {
		super();
		this.paymentsCollection = paymentsCollection;
	}

	public ArrayList<ArrayList<String>> getPaymentsCollection() {
		return paymentsCollection;
	}

	public void setPaymentsCollection(ArrayList<ArrayList<String>> paymentsCollection) {
		this.paymentsCollection = paymentsCollection;
	}

	@Override
	public String toString() {
		return "PaymentFilesCollection [paymentsCollection=" + paymentsCollection + ", getPaymentsCollection()="
				+ getPaymentsCollection() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}
	
	

}
