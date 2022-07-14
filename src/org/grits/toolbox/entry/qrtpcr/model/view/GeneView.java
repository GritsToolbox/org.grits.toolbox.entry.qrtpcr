package org.grits.toolbox.entry.qrtpcr.model.view;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.grits.toolbox.entry.qrtpcr.model.Gene;

@XmlRootElement(name="gene")
public class GeneView extends Gene {
	
	public GeneView (Gene gene) {
		this.setDescription(gene.getDescription());
		this.setGeneIdentifier(gene.getGeneIdentifier());
		this.setGeneSymbol(gene.getGeneSymbol());
		this.setGeneIds(gene.getGeneIds());
		this.setNotes(gene.getNotes());
		this.setForwardPrimer(gene.getForwardPrimer());
		this.setReversePrimer(gene.getReversePrimer());
		this.setGroup(gene.getGroup());
		this.setIsCommon(gene.getIsCommon());
		this.setIsControl(gene.getIsControl());
		this.setRefSeq(gene.getRefSeq());
		this.setSecondaryRefSeq(gene.getSecondaryRefSeq());
		this.setLocations(gene.getLocations());
		this.setAliases(gene.getAliases());
		this.setFullName(gene.getFullName());
	}

	public GeneView() {
	}

	boolean isPlaced = false;
	boolean isUpdated = false;
	List<String> updatedFields;
	
	@XmlTransient
	public boolean isUpdated() {
		return isUpdated;
	}
	
	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}

	@XmlTransient
	public boolean isPlaced() {
		return isPlaced;
	}

	public void setPlaced(boolean isPlaced) {
		this.isPlaced = isPlaced;
	}
	
	public void setUpdatedFields(List<String> updatedFields) {
		this.updatedFields = updatedFields;
	}
	
	public List<String> getUpdatedFields() {
		return updatedFields;
	}
	
	public void addUpdatedField (String field) {
		if (this.updatedFields == null)
			this.updatedFields = new ArrayList<String>();
		this.updatedFields.add(field);
	}
	
	public void removeUpdatedField (String field) {
		if (this.updatedFields != null)
			this.updatedFields.remove(field);
	}
	
}
