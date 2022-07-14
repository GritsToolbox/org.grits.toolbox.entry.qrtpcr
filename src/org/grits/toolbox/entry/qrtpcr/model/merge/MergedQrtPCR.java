package org.grits.toolbox.entry.qrtpcr.model.merge;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.xml.MergeDataMapAdapter;
import org.grits.toolbox.entry.qrtpcr.model.xml.StringStringMapAdapter;


@XmlRootElement(name="qrtPCR-merge")
public class MergedQrtPCR {
	public static final String CURRENT_VERSION = "1.0";
	
	String name;
	String description;
	Map<String, String> aliasList;
	
	String controlAlias;
	Double stDevCutOff = 0.5;

	Map<Gene, List<MergeData>> mergeDataMap;

	@XmlJavaTypeAdapter(MergeDataMapAdapter.class)
	public Map<Gene, List<MergeData>> getQrtPCRGeneListMap() {
		return mergeDataMap;
	}

	public void setQrtPCRGeneListMap(Map<Gene, List<MergeData>> qrtPCRGeneListMap) {
		this.mergeDataMap = qrtPCRGeneListMap;
	}

	@XmlJavaTypeAdapter(StringStringMapAdapter.class)
	public Map<String, String> getAliasList() {
		return aliasList;
	}

	public void setAliasList(Map<String, String> aliasList) {
		this.aliasList = aliasList;
	}

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

	public String getControlAlias() {
		return controlAlias;
	}

	public void setControlAlias(String controlAlias) {
		this.controlAlias = controlAlias;
	}
	
	public void setStDevCutOff(Double stDevCutOff) {
		this.stDevCutOff = stDevCutOff;
	}
	
	@XmlAttribute
	public Double getStDevCutOff() {
		return stDevCutOff;
	}
}
