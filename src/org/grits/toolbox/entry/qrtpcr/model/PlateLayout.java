package org.grits.toolbox.entry.qrtpcr.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.grits.toolbox.entry.qrtpcr.model.xml.ControlGeneLocationsMapAdapter;

@XmlRootElement(name="platelayout")
public class PlateLayout {
	String name;
	String description;
	
	String filename;

	Integer numberOfReplicates = 3;
	GeneList inputList;
	Map<Gene, Well> controlGeneLocations;
	List<Plate> plates;
	Size size; // default is 8 by 12
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	@XmlAttribute
	public String getFilename() {
		return filename;
	}
	
	@XmlAttribute
	public Integer getNumberOfReplicates() {
		return numberOfReplicates;
	}
	public void setNumberOfReplicates(Integer numberOfReplicates) {
		this.numberOfReplicates = numberOfReplicates;
	}
	
	public GeneList getInputList() {
		return inputList;
	}
	public void setInputList(GeneList inputList) {
		this.inputList = inputList;
	}
	
	@XmlJavaTypeAdapter(ControlGeneLocationsMapAdapter.class)
	public Map<Gene, Well> getControlGeneLocations() {
		return controlGeneLocations;
	}
	public void setControlGeneLocations(Map<Gene, Well> controlGeneLocations) {
		this.controlGeneLocations = controlGeneLocations;
	}
	
	public List<Plate> getPlates() {
		return plates;
	}
	public void setPlates(List<Plate> plates) {
		this.plates = plates;
	}
	
	public Size getSize() {
		return size;
	}
	public void setSize(Size size) {
		this.size = size;
	}
	
	public void addPlate (Plate plate) {
		if (this.plates == null)
			plates = new ArrayList<Plate>();
		this.plates.add(plate);
	}
}
