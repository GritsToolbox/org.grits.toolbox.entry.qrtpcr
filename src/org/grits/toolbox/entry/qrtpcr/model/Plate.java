package org.grits.toolbox.entry.qrtpcr.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.grits.toolbox.entry.qrtpcr.model.xml.PlateLayoutMapAdapter;

@XmlRootElement(name="plate")
public class Plate {
	String plateId;
	Map<Well, Gene> plateMap;
	
	@XmlAttribute
	public String getPlateId() {
		return plateId;
	}
	
	public void setPlateId(String plateId) {
		this.plateId = plateId;
	}
	
	@XmlJavaTypeAdapter(PlateLayoutMapAdapter.class)
	public Map<Well, Gene> getPlateMap() {
		return plateMap;
	}
	
	public void setPlateMap(Map<Well, Gene> plateMap) {
		this.plateMap = plateMap;
	}
}
