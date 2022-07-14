package org.grits.toolbox.entry.qrtpcr.model;

public class ChartData {
	
	String geneIdentifier;
	String geneSymbol;
	
	Double value;
	Double error;
	
	public String getGeneIdentifier() {
		return geneIdentifier;
	}
	public void setGeneIdentifier(String geneIdentifier) {
		this.geneIdentifier = geneIdentifier;
	}
	public String getGeneSymbol() {
		return geneSymbol;
	}
	public void setGeneSymbol(String geneSymbol) {
		this.geneSymbol = geneSymbol;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double v) {
		this.value = v;
	}
	public Double getError() {
		return error;
	}
	public void setError(Double e) {
		this.error = e;
	}
}
