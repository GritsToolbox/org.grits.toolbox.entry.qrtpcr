package org.grits.toolbox.entry.qrtpcr.model.merge;

import java.util.List;

import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.TableData;

public class MergeTableData extends TableData {
	
	Gene gene;
	List<MergeData> dataList;
	
	public MergeTableData(Gene g, List<MergeData> list) {
		this.gene = g;
		this.dataList = list;
	}
	
	public Gene getGene() {
		return gene;
	}
	
	public void setGene(Gene gene) {
		this.gene = gene;
	}
	
	public List<MergeData> getDataList() {
		return dataList;
	}
	
	public void setDataList(List<MergeData> dataList) {
		this.dataList = dataList;
	}

}
