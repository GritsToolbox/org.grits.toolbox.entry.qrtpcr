package org.grits.toolbox.entry.qrtpcr.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="gene-data")
public class GeneData extends TableData implements Comparable<GeneData> {
	String plateId;
	Well position;
	Double ct;
	Double ctMean;
	Double ctDev;
	
	Gene gene;
	
	Boolean eliminated = false;
	
	List<CtHistory> previousValues;
	
	public GeneData(GeneData geneData) {
		this.plateId = geneData.plateId;
		this.position = geneData.position;
		this.ct = geneData.ct;
		this.ctMean = geneData.ctMean;
		this.ctDev = geneData.ctDev;
		this.gene = geneData.gene;
	}
	
	public GeneData() {
	}

	@XmlAttribute
	public Boolean isEliminated() {
		return eliminated;
	}
	
	public void setEliminated(Boolean eliminated) {
		this.eliminated = eliminated;
	}
	
	public List<CtHistory> getPreviousValues() {
		return previousValues;
	}
	public void setPreviousValues(List<CtHistory> previousValues) {
		this.previousValues = previousValues;
	}
	
	@XmlAttribute
	public String getPlateId() {
		return plateId;
	}
	public void setPlateId(String plateId2) {
		this.plateId = plateId2;
	}
	
	@XmlElement
	public Well getPosition() {
		return position;
	}
	public void setPosition(Well position) {
		this.position = position;
	}
	
	@XmlAttribute
	public Double getCt() {
		return ct;
	}
	public void setCt(Double ct) {
		this.ct = ct;
	}
	
	@XmlAttribute
	public Double getCtMean() {
		return ctMean;
	}
	public void setCtMean(Double ctMean) {
		this.ctMean = ctMean;
	}
	
	@XmlAttribute
	public Double getCtDev() {
		return ctDev;
	}
	public void setCtDev(Double ctDev) {
		this.ctDev = ctDev;
	}
	
	public void addPreviousValue (Double ct, String reasonCode) {
		if (previousValues == null)
			previousValues = new ArrayList<CtHistory>();
		CtHistory newHistory = new CtHistory();
		newHistory.setCt(ct);
		newHistory.setReasonCode(reasonCode);
		previousValues.add(newHistory);
	}
	
	public Double getOriginalCt() {
		if (previousValues != null && previousValues.size() > 0)
			return previousValues.get(0).getCt();
		else { // no previous value, return the current ct which is the original
			return ct;
		}
	}
	
	@XmlIDREF
	@XmlAttribute
	public Gene getGene() {
		return gene;
	}
	public void setGene(Gene gene) {
		this.gene = gene;
	}
	
	public Double get2PowCt() {
		return this.get2PowCt(false);
	}
	
	public Double getAdjustedValue (int runId, Double lowerThreshold, Double normValue) {
		return this.getAdjustedValue(runId, false, lowerThreshold, normValue);
	}
	
	public Double getAdjustedValue (int runId, boolean original, Double lowerThreshold, Double normValue) {
		Double d = this.get2PowCt(original);
		//Double d = this.get2PowCt();
		if (d != null) {
			return (d / normValue) - gene.getScaler(runId, lowerThreshold, normValue);
		}
		return null;
	}
	
	public Double get2PowCt(boolean original) {
		if (original && this.getOriginalCt() != null)
			return Math.pow(2,  -1* getOriginalCt());	
		else if (this.ct != null)
			return Math.pow(2,  -1* ct);
		else 
			return null;
	}

	@Override
	public int compareTo(GeneData o) {
		int c = 0;
		c = this.getPosition().getX().compareTo(o.getPosition().getX());
		if (c == 0)
			c = this.getPosition().getY().compareTo(o.getPosition().getY());
		return c;
	}
}
