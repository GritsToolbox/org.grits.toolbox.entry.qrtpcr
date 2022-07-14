package org.grits.toolbox.entry.qrtpcr.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.entry.qrtpcr.util.GeneUtils;

@XmlRootElement
public class GeneList {

	String parentListId;
	String listName;
	String organism;
	Date dateCreated;
	String description;
	List<Gene> genes;
	
	String filename;
	
	@XmlAttribute
	public String getParentListId() {
		return parentListId;
	}
	public void setParentListId(String parentListId) {
		this.parentListId = parentListId;
	}
	public List<Gene> getGenes() {
		return genes;
	}
	public void setGenes(List<Gene> genes) {
		this.genes = genes;
	}
	
	@XmlAttribute
	public String getListName() {
		return listName;
	}
	public void setListName(String listName) {
		this.listName = listName;
	}
	
	@XmlAttribute
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlAttribute
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getOrganism() {
		return organism;
	}
	public void setOrganism(String organism) {
		this.organism = organism;
	}
	
	public void addGene(Gene gene) {
		if (this.getGenes() == null) {
			this.genes = new ArrayList<Gene>();
		}
		this.genes.add(gene);
	}
	
	public void removeGene(Gene gene) {
		if (this.genes != null)
			this.genes.remove(gene);
	}
	public GeneList copy() {
		GeneList copyList = new GeneList();
		copyList.setListName(listName);
		copyList.setDescription(description);
		copyList.setFilename(filename);
		List<Gene> copyGenes = new ArrayList<>();
		for (Gene gene : this.genes) {
			copyGenes.add(GeneUtils.makeACopy(gene));
		}
		copyList.setGenes(copyGenes);
		return copyList;
	}
}
