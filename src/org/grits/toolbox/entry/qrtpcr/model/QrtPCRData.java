package org.grits.toolbox.entry.qrtpcr.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="qrtPCR-Data")
public class QrtPCRData {
	
	String name;
	Date dateCreated;
	Integer numberOfReplicates = 3;
	
	List<String> dyes;
	// other stuff
	// ...
	
	List<Gene> genes;
	
	Double normValue=null;

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@XmlAttribute
	public Integer getNumberOfReplicates() {
		return numberOfReplicates;
	}

	public void setNumberOfReplicates(Integer numberOfReplicates) {
		this.numberOfReplicates = numberOfReplicates;
	}

	@XmlElement
	public List<String> getDyes() {
		return dyes;
	}

	public void setDyes(List<String> dyes) {
		this.dyes = dyes;
	}

	@XmlElementWrapper(name="gene")
	public List<Gene> getGenes() {
		return genes;
	}

	public void setGenes(List<Gene> genes) {
		this.genes = genes;
	}

	public void addDye(String dye) {
		if (this.dyes == null) 
			this.dyes = new ArrayList<String>();
		this.dyes.add(dye);
	}
	
	/**
	 * this version sets the normValue for the given runId but does not modify the genes' normalized values
	 * @param runId
	 */
	public void calculateNormValue (int runId) {
		this.normValue = findNormValue(runId, false);
	}
	
	/**
	 * calculate the normalized value and set each gene's normalized value
	 */
	public void calculateNormValue () {
		// find the control gene
		
		this.normValue = findNormValue(null, false);
		
		// set the norm value for the genes
		for (Gene gene: this.genes) {
			gene.setNormValue(gene.getRunId(), normValue);
		}
	}
	
	/**
	 * This only calculates the norm value and returns the value without setting it to the member variable 
	 * and modifying all genes' values
	 * @param runId: pass null if you would like to use the gene's current runId
	 * @return
	 */
	public double findNormValue(Integer runId, boolean original) {
		// find the control gene
		double average=0;
		int i=0;
		for (Gene gene: this.genes) {
			if (gene.getIsControl()) {
				if (runId == null) {
					runId = gene.getRunId();
				}
				for (GeneData data :gene.getDataMap().get(runId)) {
					if (!data.isEliminated()) {
						if (original) {
							if (data.getOriginalCt() != null) {
								average += data.getOriginalCt();
								i++;
							}
						} else {
							if (data.getCt() != null) {
								average += data.getCt();
								i++;
							}
						}
					}
				}
				break;
			}
		}
		average = average/i;
		double norm = Math.pow(2, -1 * average);	
				
		return norm;
	}
	
	public double getNormValue () {
		return this.normValue;
	}
	
	public double getScaler (Double lowerThreshold, boolean original) {
		if (original)
			return Math.pow(2, -1 * lowerThreshold) / findNormValue(0, original) - 0.000001;
		return Math.pow(2, -1 * lowerThreshold) / this.normValue - 0.000001;
	}
}
