package org.grits.toolbox.entry.qrtpcr.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;
import org.grits.toolbox.entry.qrtpcr.model.GeneList;

public class GeneUtils {

	/**
	 * try to find values among the list of GeneData values that satisfy the stdev requirement. The number of values to be used to calculate the
	 * new stdev is one less than the numOfReplicates. If numOfReplicates is 3, we will try to use 2 of the values to see if they have a stdev
	 * of less than stDevCutOff. If we can find such 2 numbers, we will replace the 3rd number with the average of these 2 numbers.
	 * The method will return the new list of GeneData values containing the new one whose value is replaced with the average of the other values. 
	 * 
	 * 
	 * @param dataList updated inside the method to the new values
	 * @param numOfReplicates
	 * @param stDevCutOff
	 * 
	 * @return the new list that contains the value that is replaced with the average of the remaining values
	 *         null if no such alternatives can be find
	 */
	public static List<GeneData> findBestValuesAndReplace (List<GeneData> dataList, int numOfReplicates, Double lowerThreshold, Double stDevCutOff) {
		int index = -1;
		
		if (dataList.size() < numOfReplicates) 
			return null;
		
		
		Map<List<GeneData>, Double> stDevMap = new HashMap<>();
		// try all combinations
		for (int i=0; i < numOfReplicates; i++) {
			List<GeneData> newDataList = new ArrayList<>();
			for (int j=0; j < numOfReplicates-1;  j++) {
				int k = (i+j) % numOfReplicates;
				newDataList.add(dataList.get(k));
			}
			// add the average of the values as the last data before checking for stdev
			double average = findAverage (newDataList, lowerThreshold);
			GeneData averageData = new GeneData();
			averageData.setCt(average);
			List<GeneData> listForStDev = new ArrayList<>();
			listForStDev.addAll(newDataList);
			listForStDev.add(averageData);
			double stDev = Gene.getStandardDeviation(listForStDev, lowerThreshold);
			if (stDev < stDevCutOff)
				stDevMap.put(newDataList, stDev);
		}
		
		//find the combination with the lowest stdev
		if (stDevMap.isEmpty())
			return null;
		
		double lowest = stDevCutOff;
		List<GeneData> lowestCombination=null;
		for (Iterator<List<GeneData>> iterator = stDevMap.keySet().iterator(); iterator.hasNext();) {
			List<GeneData> geneDataList = (List<GeneData>) iterator.next();
			Double stDev = stDevMap.get(geneDataList);
			if (stDev < lowest) {
				lowest = stDev;
				lowestCombination = geneDataList;
			}
			else if (stDev == lowest) {
				// compare the values in lowestCombination and geneDataList, pick the one with smaller values
				double valueTotal1=0.0;
				double valueTotal2=0.0;
				for (int i=0; i < geneDataList.size(); i++) {
					Double ct = geneDataList.get(i).getCt();
					if (ct == null || ct.doubleValue() >= lowerThreshold)
						ct = (double) lowerThreshold;
					valueTotal1 += ct;
				}
				if (lowestCombination != null) {
					for (int i=0; i < lowestCombination.size(); i++) {
						Double ct = lowestCombination.get(i).getCt();
						if (ct == null || ct.doubleValue() >= lowerThreshold)
							ct = (double) lowerThreshold;
						valueTotal2 += ct;
					}
				}
				if (valueTotal2 > 0 && valueTotal1 < valueTotal2) {
					lowestCombination = geneDataList;
				}
			}
		}
		
		if (lowestCombination == null) // cannot find values satisfying the criteria
			return null;
		
		// compare the values in lowestCombination and modify dataList accordingly
		// set the index to the index of the value that is being replaced
		int i = 0;
		double average = 0;
		int total = 0;
		for (GeneData geneData : dataList) {
			boolean keep = false;
			for (GeneData geneData2 : lowestCombination) {
				if (geneData.equals(geneData2)) {
					total++;
					Double ct = geneData2.getCt();
					if (ct == null || ct.doubleValue() >= lowerThreshold)
						ct = (double) lowerThreshold;
					average += ct;
					keep = true;
					break;
				}
			}
			if (!keep) {
				index = i;
				
			}
			i++;
		}
		// modify the value with the average of lowestCombination
		List<GeneData> modifiedList = new ArrayList<>();
		i=0;
		for (GeneData geneData : dataList) {
			if (i == index) {
				GeneData newGeneData = new GeneData();
				newGeneData.setCt(average/total);
			/*	if (geneData.getCt() != null)
					newGeneData.addPreviousValue(geneData.getCt(), CtHistory.Reason.AVERAGE.getReason());*/
				newGeneData.setPlateId(geneData.getPlateId());
				newGeneData.setPosition(geneData.getPosition());
				modifiedList.add(i, newGeneData);
			}
			else 
				modifiedList.add(i, geneData);
			i++;
		}
		
		return modifiedList;
		
	}

	private static double findAverage(List<GeneData> newDataList, Double lowerThreshold) {
		int total = 0;
		double average = 0.0;
		for (GeneData geneData : newDataList) {
			Double ct = geneData.getCt();
			if (ct == null || ct.doubleValue() >= lowerThreshold)
				ct = (double) lowerThreshold;
			average += ct;
			total++;
		}
		if (total > 0) 
			return average/total;
		return 0;
	}
	
	
	public static void cleanUpGenesForMasterGeneList (GeneList geneList) {
		if (geneList != null) {
			List<Gene> genes = geneList.getGenes();
			if (genes != null) {
				for (Gene gene : genes) {
					gene.setRunId(null);
					gene.setReRun(null);
					gene.setShouldRerun(null);
				}
			}
		}
	}
	
	public static Gene makeACopy (Gene gene) {
		Gene newGene = new Gene();
		newGene.setAliases(gene.getAliases());
		newGene.setDataMap(gene.getDataMap());
		newGene.setDescription(gene.getDescription());
		newGene.setForwardPrimer(gene.getForwardPrimer());
		newGene.setFullName(gene.getFullName());
		newGene.setGeneIdentifier(gene.getGeneIdentifier());
		newGene.setGeneIds(gene.getGeneIds());
		newGene.setGeneSymbol(gene.getGeneSymbol());
		newGene.setGroup(gene.getGroup());
		newGene.setIsCommon(gene.getIsCommon());
		newGene.setIsControl(gene.getIsControl());
		newGene.setLocations(gene.getLocations());
		newGene.setNotes(gene.getNotes());
		newGene.setNumOfReplicates(gene.getNumOfReplicates());
		newGene.setRefSeq(gene.getRefSeq());
		newGene.setSecondaryRefSeq(gene.getSecondaryRefSeq());
		newGene.setReRun(gene.getReRun());
		newGene.setReversePrimer(gene.getReversePrimer());
		newGene.setRunId(gene.getRunId());
		newGene.setShouldRerun(gene.getShouldRerun());
		newGene.setNormValueMap(gene.getNormValueMap());
		
		return newGene;
	}
}
