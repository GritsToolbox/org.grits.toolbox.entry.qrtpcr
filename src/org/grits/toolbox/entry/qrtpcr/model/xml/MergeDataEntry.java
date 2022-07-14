package org.grits.toolbox.entry.qrtpcr.model.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.merge.MergeData;

public class MergeDataEntry {
	@XmlElement Gene gene;
	@XmlElementWrapper(name="mergeDataList") List<MergeData> mergeData;
	
	@SuppressWarnings("unused")
	private MergeDataEntry() {} //Required by JAXB

	public MergeDataEntry(Gene gene, List<MergeData> value)
	{
	    this.gene = gene;
	    this.mergeData = value;
	}

}
