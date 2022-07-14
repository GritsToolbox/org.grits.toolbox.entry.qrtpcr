package org.grits.toolbox.entry.qrtpcr.model;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.grits.toolbox.entry.qrtpcr.model.xml.MapAdapter;
import org.grits.toolbox.entry.qrtpcr.model.xml.StringStringMapAdapter;


@XmlRootElement(name = "qrtPCR-Table")
public class QrtPCRTable {
	
	public static final String defaultControlGene = "HRPL4";
	
	Map <String, QrtPCRData> plateDataMap;   // plateLayout (unique identifier) -> qrtPCRdata 
	Map <String, String> instrumentFileMap;
	
	String plateLayoutFile;

	private Boolean original = true;
	
	@XmlAttribute
	public Boolean getOriginal() {
		return original;
	}

	public void setOriginal(Boolean original) {
		this.original = original;
	}

	@XmlJavaTypeAdapter(MapAdapter.class)
	public Map<String, QrtPCRData> getPlateDataMap() {
		return plateDataMap;
	}

	public void setPlateDataMap(Map<String, QrtPCRData> plateDataMap) {
		this.plateDataMap = plateDataMap;
	}
	
	public void addData (String plateId, QrtPCRData data) {
		if (plateDataMap == null) {
			plateDataMap = new TreeMap<>();
		}
		if (plateDataMap.containsKey(plateId)) {
			plateDataMap.remove(plateId);
		}
		plateDataMap.put(plateId, data);
	}

	public QrtPCRData getData(String plateId) {
		if (plateDataMap != null)
			return plateDataMap.get(plateId);
		return null;
	}

	@XmlJavaTypeAdapter(StringStringMapAdapter.class)
	public Map<String, String> getInstrumentFileMap() {
		return instrumentFileMap;
	}

	public void setInstrumentFileMap(Map<String, String> instrumentFileMap) {
		this.instrumentFileMap = instrumentFileMap;
	}
	
	public void addInstrumentFile (String plateId, String instrumentFileName) {
		if (this.instrumentFileMap == null)
			this.instrumentFileMap = new TreeMap<String, String> ();
		if (instrumentFileMap.containsKey(plateId)) {
			instrumentFileMap.remove(plateId);
		}
		this.instrumentFileMap.put(plateId, instrumentFileName);
	}
	
	@XmlAttribute
	public String getPlateLayoutFile() {
		return plateLayoutFile;
	}

	public void setPlateLayoutFile(String plateLayoutFile) {
		this.plateLayoutFile = plateLayoutFile;
	}

	public void remove(String plateId) {
		if (plateDataMap != null)
			plateDataMap.remove(plateId);
	}
}
