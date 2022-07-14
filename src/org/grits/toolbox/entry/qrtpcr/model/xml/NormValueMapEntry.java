package org.grits.toolbox.entry.qrtpcr.model.xml;

import javax.xml.bind.annotation.XmlAttribute;

public class NormValueMapEntry {

	@XmlAttribute Integer runId;
	@XmlAttribute Double normValue;
	
	@SuppressWarnings("unused")
	private NormValueMapEntry() {} //Required by JAXB

	public NormValueMapEntry(Integer runId, Double n)
	{
	    this.runId   = runId;
	    this.normValue = n;
	}
	
}
