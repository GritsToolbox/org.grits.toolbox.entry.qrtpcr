package org.grits.toolbox.entry.qrtpcr.model.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.grits.toolbox.entry.qrtpcr.model.Gene;
import org.grits.toolbox.entry.qrtpcr.model.GeneData;

public class GeneDataMapEntry {

	@XmlElement Integer runId;
	@XmlElement List<GeneData> data;
	@XmlTransient public Gene gene;
	
	@SuppressWarnings("unused")
	private GeneDataMapEntry() {} //Required by JAXB

	public GeneDataMapEntry(Integer key, List<GeneData> value)
	{
	    this.runId   = key;
	    this.data = value;
	}
}
