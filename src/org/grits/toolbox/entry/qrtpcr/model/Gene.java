package org.grits.toolbox.entry.qrtpcr.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.grits.toolbox.entry.qrtpcr.model.xml.GeneDataMapAdapter;
import org.grits.toolbox.entry.qrtpcr.model.xml.IntegerDoubleMapAdapter;


@XmlRootElement(name="gene")
public class Gene extends TableData implements Comparable<Gene>{
	Integer runId = -1; // which one of the runs is being used for the calculations. initially -1 (no data). each time a new data is added, it is incremented. 
	List<Integer> geneIds;  // geneId (ncbi)
	String geneIdentifier;
	String fullName;
	String geneSymbol;
	String forwardPrimer;
	String reversePrimer;
	List<String> refSeq;
	List<String> secondaryRefSeq;
	List<String> locations;
	List<String> aliases;
	String description;
	String group;
	String notes;
	
	Map<Integer, List<GeneData>> dataMap; 
	
	Integer numOfReplicates;
	
	Boolean isControl = false;
	Boolean isCommon = false; // if the same gene appears in multiple plates for some reason, need to handle reruns carefully in this case
	Boolean shouldRerun = false;
	Boolean reRun = false;
	
	Map<Integer, Double> normValueMap;  // runId -> normalized value
	
	@XmlAttribute
	public Integer getRunId() {
		return runId;
	}
	public void setRunId(Integer runId) {
		this.runId = runId;
	}
	@XmlAttribute @XmlID
	public String getGeneIdentifier() {
		return geneIdentifier;
	}
	public void setGeneIdentifier(String geneIdentifier) {
		this.geneIdentifier = geneIdentifier;
	}
	@XmlAttribute
	public String getGeneSymbol() {
		return geneSymbol;
	}
	public void setGeneSymbol(String geneSymbol) {
		this.geneSymbol = geneSymbol;
	}
	@XmlElement
	public String getForwardPrimer() {
		return forwardPrimer;
	}
	public void setForwardPrimer(String forwardPrimer) {
		this.forwardPrimer = forwardPrimer;
	}
	@XmlElement
	public String getReversePrimer() {
		return reversePrimer;
	}
	public void setReversePrimer(String reversePrimer) {
		this.reversePrimer = reversePrimer;
	}
	@XmlElement
	public List<String> getRefSeq() {
		return refSeq;
	}
	public void setRefSeq(List<String> refSeq) {
		this.refSeq = refSeq;
	}
	@XmlElement
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@XmlAttribute
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	@XmlJavaTypeAdapter(GeneDataMapAdapter.class)
	public Map<Integer, List<GeneData>> getDataMap() {
		return dataMap;
	}
	public void setDataMap(Map<Integer, List<GeneData>> data) {
		this.dataMap = data;
	}
	@XmlAttribute
	public Boolean getShouldRerun() {
		return shouldRerun;
	}
	public void setShouldRerun(Boolean shouldRerun) {
		this.shouldRerun = shouldRerun;
	}
	
	@XmlAttribute
	public Boolean getReRun() {
		return reRun;
	}
	public void setReRun(Boolean isReRun) {
		this.reRun = isReRun;
	}
	
	public List<Integer> getGeneIds() {
		return geneIds;
	}
	public void setGeneIds(List<Integer> geneIds) {
		this.geneIds = geneIds;
	}
	
	public void addGeneId (Integer id) {
		if (this.geneIds == null)
			this.geneIds = new ArrayList<>();
		this.geneIds.add(id);
	}
	
	@XmlJavaTypeAdapter(IntegerDoubleMapAdapter.class)
	public Map<Integer, Double> getNormValueMap() {
		return normValueMap;
	}
	public void setNormValueMap(Map<Integer, Double> normValueMap) {
		this.normValueMap = normValueMap;
	}
	
	public void addRefSeq (String refSeq) {
		if (this.refSeq == null) 
			this.refSeq = new ArrayList<String>();
		this.refSeq.add(refSeq);
	}
	public void addGeneDataList(List<GeneData> geneData) {
		if (this.dataMap == null)
			this.dataMap = new HashMap<>();
		this.dataMap.put(++this.runId, geneData);
	}
	public Boolean getIsControl() {
		return isControl;
	}
	public void setIsControl(Boolean isControl) {
		this.isControl = isControl;
	}
	
	public Boolean getIsCommon() {
		return isCommon;
	}
	public void setIsCommon(Boolean isCommon) {
		this.isCommon = isCommon;
	}
	public Integer getNumOfReplicates() {
		return numOfReplicates;
	}
	public void setNumOfReplicates(Integer numOfReplicates) {
		this.numOfReplicates = numOfReplicates;
	}
	
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public double getStandardDeviation (int runId, double lowerThreshold) {
		return getStandardDeviation(this.dataMap.get(runId), lowerThreshold);
	}
	
	public double getStandardDeviation (int runId, double lowerThreshold, boolean original) {
		return getStandardDeviation(this.dataMap.get(runId), lowerThreshold, original);
	}
	
	public double getStandardDeviation (double lowerThreshold) {
		return getStandardDeviation(this.dataMap.get(this.runId), lowerThreshold);
	}
	
	public static double getStandardDeviation (List<GeneData> data, double lowerThreshold) {
		return getStandardDeviation(data, lowerThreshold, false);
	}
	
	public static double getStandardDeviation (List<GeneData> data, double lowerThreshold, boolean original) {
		double average = 0;
		int total = 0;
		for (GeneData geneData : data) {
			if (!geneData.isEliminated()) {
				Double ct = null;
				if (original) ct = geneData.getOriginalCt();
				else ct = geneData.getCt();
				if (ct == null || ct.doubleValue() >=lowerThreshold) {
					average += lowerThreshold;
				}
				else  {
					average += ct;
				}
			total++;
			}
		}
		average = average / total;
		double[] deviations = new double[total];
		// find the deviations
		int i=0;
		for (GeneData geneData : data) {
			if (!geneData.isEliminated()) {
				Double ct = null;
				if (original) ct = geneData.getOriginalCt();
				else ct = geneData.getCt();
				if (ct == null || ct.doubleValue() >= lowerThreshold) 
					deviations[i++]  = Math.pow (lowerThreshold - average, 2);
				else if (ct != null)
					deviations[i++] = Math.pow(ct - average, 2);
			}
		}
		double variance=0;
		for (int j = 0; j < deviations.length; j++) {
			variance += deviations[j];
		}
		if (total > 1)
			variance = variance/(total-1);
		return Math.sqrt(variance);	
	}
	
	public void checkAndMarkForRerun (Integer runId, double lowerThreshold, Double stDevCutoff) {
		if (getStandardDeviation(runId, lowerThreshold) >= stDevCutoff) {
			boolean[] valueAboveThreshold = new boolean[numOfReplicates];
			for (int i=0; i < numOfReplicates; i++) {
				valueAboveThreshold[i] = true;
			}
			int i=0;
			for (GeneData geneData : this.dataMap.get(runId)) {
				if (!geneData.isEliminated()) {
					if (geneData.getCt() != null && geneData.getCt().doubleValue() < lowerThreshold) {
						if (i >= numOfReplicates)
							throw new RuntimeException("'Number of Replicates' do not match with the actual replicates of data. Please check your plate layout!");
						valueAboveThreshold[i++] = false;
					}
				}
			}
			int totalAbove = 0;
			for (i=0; i < numOfReplicates; i++) {
				if (valueAboveThreshold[i]) 
					totalAbove++;
			}
			
			if (totalAbove == (numOfReplicates -1))
				this.shouldRerun = true;
		}
	}
	
	/**
	 * 
	 * @param runId
	 * @param lowerThreshold
	 * @return
	 */
	public Double getAdjustedAverage (int runId, Double lowerThreshold, Double normValue) {
		return getAdjustedAverage(runId, false, lowerThreshold, normValue);
	}
	
	/**
	 * 
	 * @param runId
	 * @param original
	 * @param stdevCutOff
	 * @return
	 */
	public Double getAdjustedAverage (int runId, boolean original, Double lowerThreshold, Double normValue) {
		Double average = 0.0;
		int i=0;
		for (GeneData geneData : this.dataMap.get(runId)) {
			if (!geneData.isEliminated()) {
				Double adj = geneData.getAdjustedValue(runId, original, lowerThreshold, normValue);
				if (adj != null) {
					average += adj;
					i++;
				}
			}
		}
		
		average /= i;
		if (average == 0.0) 
			return null;
		return average;
	}
	
	/**
	 * 
	 * @param runIda
	 * @param stdevCutOff
	 * @return
	 */
	public Double getStDevForAdjusted (int runIda, Double lowerThreshold, Double normValue) {
		return getStDevForAdjusted(runId, false, lowerThreshold, normValue);
	}
	
	/**
	 * 
	 * @param runId
	 * @param original
	 * @param stdevCutOff
	 * @return
	 */
	public Double getStDevForAdjusted (int runId, boolean original, Double lowerThreshold, Double normValue) {
		int total=0;
		Double average = 0.0;
		for (GeneData geneData : this.dataMap.get(runId)) {
			if (!geneData.isEliminated()) {
				Double adj = geneData.getAdjustedValue(runId, original, lowerThreshold, normValue);
				if (adj != null) {
					average += adj;
					total++;
				}
			}
		}
		average /= total;
		
		if (average == 0.0) {
			return null;
		}
		
		double[] deviations = new double[total];
		// find the deviations
		int i=0;
		for (GeneData geneData : this.dataMap.get(runId)) {
			if (!geneData.isEliminated()) {
				Double adj = geneData.getAdjustedValue(runId, original, lowerThreshold, normValue);
				if (adj != null) {
					deviations[i++] = Math.pow(adj - average, 2);
				}
			}
		}
		double variance=0;
		for (int j = 0; j < deviations.length; j++) {
			variance += deviations[j];
		}
		if (total > 1)
			variance = variance/(total-1);
		return Math.sqrt(variance);	
	}
	
	@Override
	public String toString() {
		if (geneIdentifier != null)
			return geneIdentifier;
		return super.toString();
	}
	
	public void setNormValue (Integer runId, Double normValue) {
		if (this.normValueMap == null)
			this.normValueMap = new HashMap<>();
		this.normValueMap.put (runId, normValue);
	}
	
	public Double getNormValue(Integer runId) {
		if (this.normValueMap != null)
			return this.normValueMap.get(runId);
		else
			return null;
	}
	
	/**
	 * 
	 * @param runId
	 * @param lowerThreshold
	 * @return
	 */
	public double getScaler(Integer runId, Double lowerThreshold, Double normValue) {
		return Math.pow(2, -1 * lowerThreshold) / normValue - 0.000001;
	}
	
	/**
	 * 
	 * @return refSeq numbers as a comma delimited string
	 */
	public String getRefSeqString() {
		if (this.refSeq == null)
			return "";
		StringBuffer refSeqString = new StringBuffer();
		for (String refSeqNo : refSeq) {
			if (refSeqNo == null || refSeqNo.trim().length() == 0)
				continue;
			refSeqString.append(refSeqNo);
			refSeqString.append(",");
		}
		if (refSeqString.length() > 0) {
			int lastComma = refSeqString.lastIndexOf(",");
			if (lastComma != -1) // should not happen
				refSeqString = new StringBuffer (refSeqString.substring(0, lastComma));
		}
		
		return refSeqString.toString();
	}
	
	public List<String> getSecondaryRefSeq() {
		return secondaryRefSeq;
	}
	
	public void setSecondaryRefSeq(List<String> secondaryRefSeq) {
		this.secondaryRefSeq = secondaryRefSeq;
	}
	
	/**
	 * 
	 * @return secondary refSeq numbers as a comma delimited string
	 */
	public String getSecondaryRefSeqString() {
		if (this.secondaryRefSeq == null)
			return "";
		StringBuffer refSeqString = new StringBuffer();
		for (String refSeqNo : secondaryRefSeq) {
			if (refSeqNo == null || refSeqNo.trim().length() == 0)
				continue;
			refSeqString.append(refSeqNo);
			refSeqString.append(",");
		}
		if (refSeqString.length() > 0) {
			int lastComma = refSeqString.lastIndexOf(",");
			if (lastComma != -1) // should not happen
				refSeqString = new StringBuffer (refSeqString.substring(0, lastComma));
		}
		
		return refSeqString.toString();
	}
	
	/**
	 * convert comma delimited refSeq numbers into the list
	 * @param refSeqString
	 */
	public void setRefSeq (String refSeqString) {
		this.refSeq = new ArrayList<String>();
		String splitChar = ",";
		if (refSeqString.contains("/"))
			splitChar = "/";
		String[] refSeqNumbers = refSeqString.split(splitChar);
		for (String refSeqNo : refSeqNumbers) {
			refSeq.add(refSeqNo);
		}
	}
	
	public void setGeneIds(String geneIdString) {
		this.geneIds = new ArrayList<Integer>();
		String splitChar = ",";
		if (geneIdString.contains("/"))
			splitChar = "/";
		String[] geneIdNumbers = geneIdString.split(splitChar);
		for (String geneId : geneIdNumbers) {
			try {
				geneIds.add(Integer.parseInt(geneId.trim()));
			} catch (NumberFormatException e) {
				Logger.getLogger(getClass()).warn("Gene Id is not a valid integer");
			}
		}
		
	}
	public String getGeneIdString() {
		if (this.geneIds == null)
			return "";
		StringBuffer geneIdString = new StringBuffer();
		for (Integer geneId : geneIds) {
			if (geneId == null)
				continue;
			geneIdString.append(geneId+"");
			geneIdString.append(",");
		}
		if (geneIdString.length() > 0) {
			int lastComma = geneIdString.lastIndexOf(",");
			if (lastComma != -1) // should not happen
				geneIdString = new StringBuffer (geneIdString.substring(0, lastComma));
		}
		
		return geneIdString.toString();
	}
	public List<String> getAliases() {
		return aliases;
	}
	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}
	public void addAlias(String value) {
		if (this.aliases == null) 
			this.aliases = new ArrayList<>();
		if (!this.aliases.contains(value))
			this.aliases.add(value);	
	}
	public void setAliasString(String aliasString) {
		this.aliases = new ArrayList<String>();
		String splitChar = ",";
		if (aliasString.contains("/"))
			splitChar = "/";
		String[] aliasList = aliasString.split(splitChar);
		for (String alias : aliasList) {
			aliases.add(alias);
		}
	}
	
	@XmlTransient
	public String getAliasString() {
		if (this.aliases == null)
			return "";
		StringBuffer aliasString = new StringBuffer();
		for (String alias : this.aliases) {
			if (alias == null || alias.trim().length() == 0)
				continue;
			aliasString.append(alias);
			aliasString.append(",");
		}
		if (aliasString.length() > 0) {
			int lastComma = aliasString.lastIndexOf(",");
			if (lastComma != -1) // should not happen
				aliasString = new StringBuffer (aliasString.substring(0, lastComma));
		}
		
		return aliasString.toString();
	}
	
	public List<String> getLocations() {
		return locations;
	}
	
	public void setLocations(List<String> locations) {
		this.locations = locations;
	}
	
	/**
	 * 
	 * @return locations as a comma delimited string
	 */
	@XmlTransient
	public String getLocationString() {
		if (this.locations == null)
			return "";
		StringBuffer locationString = new StringBuffer();
		for (String location : locations) {
			if (location == null || location.trim().length() == 0)
				continue;
			locationString.append(location);
			locationString.append(",");
		}
		if (locationString.length() > 0) {
			int lastComma = locationString.lastIndexOf(",");
			if (lastComma != -1) // should not happen
				locationString = new StringBuffer (locationString.substring(0, lastComma));
		}
		
		return locationString.toString();
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public void setLocationString(String text) {
		this.locations = new ArrayList<String>();
		String splitChar = ",";
		if (text.contains("/"))
			splitChar = "/";
		String[] locationList = text.split(splitChar);
		for (String location : locationList) {
			locations.add(location);
		}
	}
	public void setSecondaryRefSeq(String refSeqString) {
		this.secondaryRefSeq = new ArrayList<String>();
		String splitChar = ",";
		if (refSeqString.contains("/"))
			splitChar = "/";
		String[] refSeqNumbers = refSeqString.split(splitChar);
		for (String refSeqNo : refSeqNumbers) {
			secondaryRefSeq.add(refSeqNo);
		}
		
	}
	
	@Override
	public int compareTo(Gene o) {
		int c=0;
		if (this.dataMap != null && this.dataMap.size() > 0 &&
				o.dataMap != null && o.dataMap.size() > 0) {
			c = this.dataMap.get(0).get(0).compareTo(o.getDataMap().get(0).get(0));
		}
		
		return c;
	}
}
