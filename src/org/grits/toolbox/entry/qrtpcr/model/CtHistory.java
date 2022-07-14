package org.grits.toolbox.entry.qrtpcr.model;

public class CtHistory {	
	
	public enum Reason {
		CURRENT ("Current"), NULL("Value Absent"), ABOVETHRESHOLD("Above Threshold"), USERCHANGE("User Change"), AVERAGE("Analysis Change"), RERUN("Rerun");
		String reason;
		
		private Reason(String value) {
			this.reason = value;
		}
		
		public String getReason() {
			return this.reason;
		}
	};
	
	Double ct;
	String reasonCode;
	
	public Double getCt() {
		return ct;
	}
	
	public void setCt(Double ct) {
		this.ct = ct;
	}
	
	public String getReasonCode() {
		return reasonCode;
	}
	
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
}
