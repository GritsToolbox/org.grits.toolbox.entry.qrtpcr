package org.grits.toolbox.entry.qrtpcr.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.grits.toolbox.entry.qrtpcr.model.xml.IntegerQrtPCRTableMapAdapter;
import org.grits.toolbox.entry.qrtpcr.preference.QrtPCRPreferencePage;

@XmlRootElement(name = "qrtPCR")
public class QrtPCRRun {
	
	public static final String CURRENT_VERSION = "1.0";

	String geneListFile;
	Integer runId = -1;
	Map<Integer, QrtPCRTable> runIdTableMap;
	Double threshold = QrtPCRPreferencePage.THRESHOLD_PREFERENCE;
	Double stDevCutOff = QrtPCRPreferencePage.STDEVCUTOFF_PREFERENCE;
	
	@XmlAttribute
	public Double getThreshold() {
		return threshold;
	}
	
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}
	
	@XmlAttribute
	public Double getStDevCutOff() {
		return stDevCutOff;
	}
	
	public void setStDevCutOff(Double stDevCutOff) {
		this.stDevCutOff = stDevCutOff;
	}

	@XmlJavaTypeAdapter(IntegerQrtPCRTableMapAdapter.class)
	public Map<Integer, QrtPCRTable> getRunIdTableMap() {
		return runIdTableMap;
	}

	public void setRunIdTableMap(Map<Integer, QrtPCRTable> runIdTableMap) {
		this.runIdTableMap = runIdTableMap;
	}
	
	@XmlAttribute
	public String getGeneListFile() {
		return geneListFile;
	}

	public void setGeneListFile(String geneListFile) {
		this.geneListFile = geneListFile;
	}
	
	@XmlAttribute
	public Integer getRunId() {
		return runId;
	}
	
	public void setRunId(Integer r) {
		this.runId = r;
	}

	public void addRun (QrtPCRTable table) {
		if (runIdTableMap == null)
			runIdTableMap = new HashMap<>();
		runIdTableMap.put(++runId, table);
	}
	
	public QrtPCRTable getRun (int run) {
		if (runIdTableMap != null) 
			return runIdTableMap.get(run);
		return null;
	}
	
	public QrtPCRTable getFirstRun () {
		if (runIdTableMap != null) 
			return runIdTableMap.get(0);
		return null;
	}
	
	public QrtPCRTable getLastRun() {
		if (runIdTableMap != null) 
			return runIdTableMap.get(this.runId);
		return null;
	}
	
	
	public static List<Gene> generateMasterTable (QrtPCRTable qrtPCRTable) {
		List<Gene> geneList = new ArrayList<>();
		for (Iterator<String> iterator = qrtPCRTable.getPlateDataMap().keySet().iterator(); iterator.hasNext();) {
			String plateId= (String) iterator.next();
			QrtPCRData data = qrtPCRTable.getData(plateId);
			geneList.addAll(data.getGenes());
		}
		
		return geneList;
		
	}
}
